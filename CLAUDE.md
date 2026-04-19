# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ecommerce microservice built with **Clean Architecture** pattern (hexagonal architecture). Handles order processing, cart management, product catalog (read-side), sales events, and feedback. Consumes inventory events from product-storage service and publishes order events to Kafka.

**Key distinction**: Unlike other services in the platform (identity, back-office, product-storage) which use traditional layered architecture, this service strictly follows Clean Architecture principles to isolate business logic from framework concerns.

## Technology Stack

- **Spring Boot**: 3.5.6
- **Java**: 21
- **Database**: PostgreSQL (`ecommerce_db` or `ecommerce_db_2`)
- **Messaging**: Kafka (consumer & producer)
- **Security**: JWT authentication via custom filter
- **Storage**: Cloudflare R2 for product images and user avatars
- **Payment**: Sepay QR payment with Google Sheets polling
- **External APIs**: Google Sheets API v4 for payment transaction polling
- **Build Tool**: Maven Wrapper (`./mvnw`)

## Quick Start

**Development setup:**
```bash
# Copy environment template
cp .env.example .env
# Edit .env with your credentials (DB, Kafka, R2)

# Build
./mvnw clean install

# Run tests
./mvnw test

# Run application (requires PostgreSQL and Kafka running)
./mvnw spring-boot:run

# Skip tests during build
./mvnw clean install -DskipTests
```

**Database seeding:**
The service includes an automatic DataSeeder that populates the database with Vietnamese fresh food sample data on first run:
- 5 buyers with addresses
- 18 product categories (hierarchical)
- 15 fresh food products (fruits, vegetables, meat, seafood, dairy)
- 15 batch details with pricing
- 3 active sale events with discounts
- Shopping carts with items
- Product reviews in Vietnamese

The seeder only runs when the database is empty (checks `buyer` table). Logs show detailed seeding progress.

**Docker setup:**
```bash
# Run via Docker Compose (includes DB setup)
docker-compose up --build
```

**Default port**: 9301

## Clean Architecture Structure

The codebase follows Clean Architecture with clear separation of concerns:

```
microservice.base_source/
├── domain/                    # Core business logic (pure Java, no frameworks)
│   ├── entity/               # Domain entities (NOT JPA entities)
│   │                         # Pure business objects with behavior
│   ├── use_case/             # Use case interfaces (ports)
│   │                         # Define what the application does
│   ├── service/              # Domain services (business logic implementations)
│   │                         # Orchestrate entities and use cases
│   └── exception/            # Domain exceptions
│       └── type/             # Exception types
│
├── persistence/              # Data access layer (adapter)
│   ├── repository/           # Spring Data JPA repositories
│   │                         # Extend JpaRepository<Entity, ID>
│   └── dto/                  # Database DTOs (not used extensively)
│
├── infrastructure/           # Framework and external concerns (adapter)
│   ├── configuration/        # Spring configuration classes
│   ├── messaging/            # Kafka consumers and producers
│   │   ├── batchdetail/      # Consume batch-detail-events
│   │   ├── buyer/            # Buyer event handling
│   │   ├── category/         # Category event handling
│   │   ├── order/            # Order event handling
│   │   ├── orderitem/        # Produce order-item-events
│   │   └── productgeneral/   # Product general event handling
│   └── security/             # JWT authentication, filters, security config
│
└── presentation/             # API layer (adapter)
    ├── rest/                 # REST controllers (@RestController)
    ├── request/              # Request DTOs
    ├── response/             # Response DTOs
    └── mapping/              # Mappers between layers
```

### Architecture Principles

1. **Domain Layer Independence**: Domain entities and use cases have NO Spring/JPA annotations. They are pure Java objects representing business concepts.

2. **Dependency Rule**: Dependencies point inward. Infrastructure and Presentation depend on Domain, never the reverse.

3. **Entities vs DTOs**: 
   - Domain entities in `domain/entity/` represent business concepts with behavior
   - Despite being in a package called "entity", these ARE annotated with JPA (`@Entity`) - this is a deviation from pure Clean Architecture but pragmatic for Spring Boot
   - Presentation layer uses separate request/response DTOs

4. **Use Cases as Interfaces**: `domain/use_case/` defines interfaces for business operations. `domain/service/` implements them.

5. **Ports and Adapters**: Repositories are ports (interfaces). JPA implementations are adapters.

## Core Domain Entities

**Primary entities** (all in `domain/entity/`):

- **Order**: Customer orders with status tracking (PENDING, CONFIRMED, SHIPPED, etc.)
- **OrderItem**: Line items in orders, linked to ProductDetail
- **Cart / CartItem**: Shopping cart functionality per buyer
- **Buyer**: Customer information synced from identity service
- **ProductGeneral**: Product catalog (brand, name, category) - read model
- **ProductDetail**: Sellable product units with pricing - read model
- **BatchDetail**: Inventory batches received from product-storage service
- **SaleEvent**: Time-bound sales campaigns
- **SaleProduct**: Products participating in sale events with discounts
- **Category**: Category hierarchy (3 levels synced from back-office)
- **FeedBack**: Product reviews and ratings
- **Address**: Customer shipping addresses
- **ProcessedTransaction**: Tracks processed payment transactions to prevent duplicate processing

**Entity conventions**:
- All use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment IDs
- Timestamps managed via `@PrePersist` and `@PreUpdate` lifecycle callbacks
- Use Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)

## Event-Driven Integration

### Kafka Consumers (Inbound Events)

Located in `infrastructure/messaging/`:

1. **BatchDetailCreatedConsumer** (`batch-detail-events`)
   - Receives inventory updates from product-storage service
   - Creates local ProductDetail records for sale
   - Pattern: Event → Domain Service → Repository

2. **CategoryCreatedConsumer** (`category-events`)
   - Syncs category hierarchy from back-office
   - Maintains local denormalized category cache

3. **ProductGeneralCreatedConsumer** (`product-general-events`)
   - Syncs product catalog from back-office
   - Updates local product general information

4. **BuyerCreatedConsumer** (`buyer-events`)
   - Syncs user information from identity service
   - Creates/updates buyer profiles

**Consumer pattern**:
```java
@Component
@KafkaListener(topics = "topic-name")
public class EventConsumer {
    private final DomainService service;
    
    public void consume(Event event) {
        try {
            service.processEvent(event.toEntity());
        } catch (Exception e) {
            log.error("Failed to process event", e);
            // No automatic retry - handle idempotency in service layer
        }
    }
}
```

### Kafka Producers (Outbound Events)

1. **OrderItemProducer** (`order-item-events`)
   - Publishes order item creation to product-storage service
   - Used for inventory reservation/deduction
   - Returns `CompletableFuture` for async handling

**Producer pattern**:
```java
@Service
public class EventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publish(Event event) {
        kafkaTemplate.send("topic-name", event.id().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) log.error("Publish failed", ex);
                else log.info("Published with offset: {}", result.getRecordMetadata().offset());
            });
    }
}
```

## Payment Integration

### Sepay QR Payment with Google Sheets Polling

The service integrates with **Sepay** (Vietnamese payment gateway) for QR code-based bank transfers. Payment confirmations are tracked via Google Sheets API polling.

**Payment flow**:
1. Order created with status PENDING
2. QR code URL generated via `QrPaymentService` with order code format `DH00012345` (zero-padded to 8 digits)
3. Customer scans QR and completes bank transfer with order code in description
4. Sepay logs transaction to Google Sheets
5. `PaymentPollingService` polls Google Sheets every 10 seconds (configurable)
6. Service extracts order code from transaction description, updates order status to PAID
7. `ProcessedTransaction` entity prevents duplicate processing

### QrPaymentService

Generates Sepay QR code URLs for payment:

```java
public String createOrderTransactionQrUrl(String orderId, Long totalPrice);
```

**Parameters**:
- `orderId`: Order ID (will be formatted as `DH00012345`)
- `totalPrice`: Total amount in VND (long integer, no decimals)

**Returns**: URL for Sepay QR code image with embedded payment info

**Example**: `https://qr.sepay.vn/img?acc=SEPTAK21191&bank=OCB&amount=100000&des=DH00012345`

### PaymentPollingService

Polls Google Sheets for new transactions via scheduled task:

**Key methods**:
- `pollPayments()`: Scheduled method (runs every `payment.polling.interval` milliseconds)
- `processRow(List<Object> row)`: Processes individual transaction row
- `extractOrderCode(String description)`: Extracts order code from transaction description using regex

**Implementation notes**:
- Uses `lastProcessedRow` counter to avoid re-reading entire sheet
- Only processes new rows since last poll
- Validates transaction hasn't been processed before checking `ProcessedTransaction` table
- Updates order status to PAID and saves transaction ID
- Logs warnings for missing order codes or unfound orders

**Transaction deduplication**: Uses `ProcessedTransaction.transactionId` as unique constraint

### Google Sheets Configuration

**GoogleSheetsConfig** (`infrastructure/configuration/`):
- Initializes Google Sheets API v4 client
- Uses service account credentials from `google-credentials.json`
- Scoped to `SPREADSHEETS_READONLY`
- Application name: "Capstone Payment"

**Required credentials file**: `src/main/resources/google-credentials.json` (service account JSON)

**Configuration** (in `.env` or `application.yaml`):
```bash
# Google Sheets
GOOGLE_SHEETS_SPREADSHEET_ID=your_spreadsheet_id
GOOGLE_SHEETS_CREDENTIALS_PATH=google-credentials.json

# Sepay
SEPAY_ACCOUNT=your_account_number
SEPAY_BANK=bank_code  # e.g., OCB, VCB, MB

# Payment polling interval (milliseconds)
PAYMENT_POLLING_INTERVAL=10000  # 10 seconds
```

**Google Sheets format**: Sheet named "sepay" with columns:
- [0] Timestamp
- [1] Amount
- [2-4] Other fields
- [5] Description (contains order code)
- [6-7] Other fields
- [8] Transaction ID (unique identifier)

### Payment Dependencies

**pom.xml** includes:
- `google-http-client-jackson2`: HTTP client for Google APIs
- `google-auth-library-oauth2-http`: OAuth2 authentication
- `google-api-services-sheets`: Google Sheets API v4

## Security and Authentication

**JWT-based authentication** with Spring Security:

- **JwtAuthenticationFilter**: Intercepts requests, validates JWT tokens
- **JwtTokenValidator**: Extracts and validates token claims
- **SecurityConfig**: Configures filter chain, permits `/api/auth/**` endpoints
- **AuthenticatedUser**: Custom principal with user claims (id, email, role)

**Protected endpoints**: Most `/api/**` routes require valid JWT (except auth endpoints)

**Token validation**: Tokens issued by identity-service, validated using shared secret/public key

## REST API Structure

Controllers in `presentation/rest/`:

- **OrderController**: Create orders, order history, order details, status updates
- **CartItemController**: Add to cart, update quantity, remove items, view cart
- **ProductSearchController**: Search products, filter by category, pagination
- **ProductGeneralController**: Product catalog CRUD (admin)
- **ProductDetailController**: Product detail CRUD (admin)
- **SaleEventController**: Manage sales campaigns
- **SaleProductController**: Assign products to sales
- **CategoryController**: Category hierarchy management
- **FeedBackController**: Product reviews and ratings
- **AddressController**: Customer shipping addresses
- **BatchDetailController**: Inventory batch management (admin)

**API conventions**:
- Base path: `/api/**`
- Use `ApiResponse<T>` wrapper for consistent response format
- Request validation via `@Valid` and Bean Validation annotations
- Return HTTP status codes: 200 (OK), 201 (Created), 404 (Not Found), etc.

## Use Cases and Services

**Use case interfaces** (`domain/use_case/`): Define business operations as contracts

**Domain services** (`domain/service/`): Implement use cases with business logic

**Key services**:
- **OrderService**: Order lifecycle management, cart-to-order conversion, status transitions
- **CartItemService**: Cart operations, quantity updates, cart summary
- **ProductGeneralService**: Product catalog management
- **SaleEventService**: Sales campaign logic, active sale validation
- **SearchService**: Product search with filtering and pagination
- **PaymentPollingService**: Scheduled polling of Google Sheets for payment confirmations
- **QrPaymentService**: Generates Sepay QR code URLs for bank transfer payments

**Service pattern**:
```java
@Service
@RequiredArgsConstructor  // Constructor injection
public class DomainService {
    private final Repository repository;
    private final EventProducer producer;
    
    public Entity createEntity(Entity entity) {
        // Business logic here
        Entity saved = repository.save(entity);
        producer.publishEvent(new Event(saved));
        return saved;
    }
}
```

## Database Schema

Schema managed via JPA with `hibernate.ddl-auto: update`

**Key tables**:
- `ORDERS`: order_id (PK), buyer_id, total_price, status, timestamps
- `ORDER_ITEMS`: order_item_id (PK), order_id (FK), product_detail_id (FK), quantity, unit_price
- `CARTS`: cart_id (PK), buyer_id (unique)
- `CART_ITEMS`: cart_item_id (PK), cart_id (FK), product_detail_id (FK), quantity
- `PRODUCT_GENERAL`: product_general_id (PK), name, brand, category_id (FK)
- `PRODUCT_DETAIL`: product_detail_id (PK), product_general_id (FK), batch_detail_id (FK), unit_price
- `BATCH_DETAIL`: batch_detail_id (PK), product_general_id (FK), quantity_remain
- `SALE_EVENTS`: sale_event_id (PK), name, start_time, end_time, is_active
- `SALE_PRODUCTS`: sale_product_id (PK), sale_event_id (FK), product_detail_id (FK), discount_percent
- `PROCESSED_TRANSACTIONS`: id (PK), transaction_id (unique), processed_at

**Schema documentation**: See `db_schema/README.md` for backup/restore commands

## Configuration

**Environment variables** (`.env` file):
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Kafka
KAFKA_HOST=localhost
KAFKA_PORT=9092

# Cloudflare R2
R2_ACCOUNT_ID=your_account_id
R2_ACCESS_KEY=your_access_key
R2_SECRET_KEY=your_secret_key
```

**Application configuration** (`src/main/resources/application.yaml`):
- Database: `jdbc:postgresql://${DB_HOST}:${DB_PORT}/ecommerce_db`
- Server port: 9300 (changed from 9301)
- Kafka: Configured via `app.kafka-url`
- R2 buckets: `back-office-user-avts`, `product-general-img`
- Google Sheets:
  - `google.sheets.credentials-path`: Path to service account JSON (default: `google-credentials.json`)
  - `google.sheets.spreadsheet-id`: Google Sheets spreadsheet ID
- Sepay:
  - `sepay.account`: Sepay account number
  - `sepay.bank`: Bank code (e.g., OCB, VCB, MB)
- Payment polling:
  - `payment.polling.interval`: Milliseconds between polling cycles (default: 10000 = 10 seconds)

**Important**: Add `google-credentials.json` to `.gitignore` - this file contains service account credentials and should NOT be committed

## Development Workflow

**Typical development flow**:

1. **Add new feature**:
   - Define domain entity in `domain/entity/`
   - Create use case interface in `domain/use_case/`
   - Implement in `domain/service/`
   - Add repository in `persistence/repository/`
   - Create REST controller in `presentation/rest/`
   - Add request/response DTOs in `presentation/`

2. **Add Kafka integration**:
   - Create event POJO in `infrastructure/messaging/{topic}/`
   - Implement consumer with `@KafkaListener`
   - Implement producer with `KafkaTemplate`
   - Wire to domain service

3. **Testing**:
   - Unit test domain services (no Spring context)
   - Integration test controllers with `@SpringBootTest`
   - Test Kafka with embedded broker (if needed)

**Running individual tests**:
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=OrderServiceTest

# Run with coverage
./mvnw clean verify
```

## Common Patterns

**Exception handling**:
- Throw custom exceptions from domain layer (e.g., `OrderNotFoundException`)
- Handle in `@ControllerAdvice` global exception handler
- Return appropriate HTTP status and error messages

**Pagination**:
- Use `Pageable` parameter in repositories
- Return `Page<T>` from services
- Convert to `PageResponse<T>` in controllers

**Mapping between layers**:
- Use mappers in `presentation/mapping/`
- Convert Request → Entity, Entity → Response
- Keep DTOs flat and simple

**Transaction management**:
- Annotate service methods with `@Transactional`
- Default propagation: REQUIRED
- Consider rollback on checked exceptions if needed

## Integration with Other Services

**Upstream dependencies** (consumes from):
- **product-storage service**: Inventory updates via `batch-detail-events`
- **back-office service**: Product catalog via `product-general-events`, categories via `category-events`
- **identity service**: User profiles via `buyer-events`

**Downstream dependencies** (publishes to):
- **product-storage service**: Order items via `order-item-events` for inventory deduction

**Cross-service communication**:
- Asynchronous via Kafka events (preferred)
- No direct HTTP calls between services
- Each service owns its data (database-per-service pattern)

## Postman Collection

API documentation and testing: `Ecommerce API v1.0.5.postman_collection.json`

Import into Postman for pre-configured requests.

## Troubleshooting

**Build issues**:
- Ensure Java 21 is installed: `java -version`
- Clear Maven cache: `./mvnw clean`
- Check for lombok issues: Ensure annotation processing is enabled in IDE

**Runtime issues**:
- **Database connection failed**: Verify PostgreSQL is running and credentials in `.env` are correct
- **Kafka consumer not receiving**: Check Kafka is running, topic exists, consumer group ID
- **JWT authentication failed**: Ensure token is valid and identity-service shared secret matches

**Event processing issues**:
- Check Kafka UI (localhost:9280) for message details
- Verify event serialization format matches producer
- Look for exceptions in consumer logs (events are NOT auto-retried)

**Port conflicts**:
- Default port 9300 can be changed via `SERVER_PORT` env var
- Check if other services are using the same port

**Payment polling issues**:
- **Google Sheets API failed**: Verify `google-credentials.json` exists in resources and service account has read access
- **Payments not detected**: Check spreadsheet ID, sheet name "sepay", and polling interval
- **Order not found**: Verify transaction description contains order code format `DH00012345`

## Key Architectural Decisions

**Why Clean Architecture?**
- Business logic in `domain/` is testable without Spring
- Easy to swap infrastructure (e.g., change from JPA to MyBatis)
- Clear separation of concerns makes codebase maintainable

**Why JPA annotations on domain entities?**
- Pragmatic compromise for Spring Boot integration
- Avoids extra mapping layer between domain entities and JPA entities
- Trade-off: Domain entities are not 100% framework-independent

**Why event-driven with Kafka?**
- Loose coupling between microservices
- Asynchronous processing improves performance
- Event log provides audit trail and enables eventual consistency

**Why separate ProductGeneral and ProductDetail?**
- ProductGeneral: Read model for catalog browsing (denormalized)
- ProductDetail: Sellable units with pricing and inventory
- Aligns with CQRS pattern (Command-Query Responsibility Segregation)
