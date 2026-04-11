# GEMINI.md - Ecommerce Service

## Project Overview

This is the e-commerce microservice, a core component of the platform responsible for customer-facing operations. It manages the product catalog (as a read-model), shopping carts, order processing, sales events, and user feedback.

A key characteristic of this service is its use of **Clean Architecture** (also known as Hexagonal Architecture). This sets it apart from other services in the system, which follow a more traditional layered approach. The architecture is designed to isolate the core business logic from framework-specific details, improving testability and maintainability.

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.6
- **Database:** PostgreSQL
- **Messaging:** Apache Kafka (for event-driven communication)
- **Security:** JWT-based authentication
- **Storage:** Cloudflare R2 (for product images)
- **Build Tool:** Maven (using the `./mvnw` wrapper)

## Core Architecture & Patterns

The service's structure is strictly divided into four main layers, following Clean Architecture principles:

1.  **`domain/`**: The heart of the application. Contains pure Java objects representing the business logic, including entities, use cases (interfaces), and domain services. This layer has no dependencies on Spring or any other framework.
2.  **`persistence/`**: The data access layer. Implements the repository interfaces defined in the domain layer using Spring Data JPA. It acts as an adapter between the domain and the database.
3.  **`infrastructure/`**: Handles all external concerns and technical details. This includes Spring Framework configuration, Kafka consumers and producers for messaging, and JWT security filters.
4.  **`presentation/`**: The API layer. Exposes the application's functionality via REST endpoints. It includes controllers, request/response DTOs, and mappers to convert between the API models and the domain entities.

### Event-Driven Integration

The service is highly event-driven, communicating with other microservices asynchronously via Kafka:

**Inbound Events (Consumers):**
-   **`batch-detail-events`**: Consumes inventory updates from the `product-storage-service`.
-   **`category-events`**: Syncs the product category hierarchy from the `back-office-service`.
-   **`product-general-events`**: Syncs the main product catalog information from the `back-office-service`.
-   **`buyer-events`**: Syncs user data from the `identity-service`.

**Outbound Events (Producers):**
-   **`order-item-events`**: Publishes events when an order is placed, allowing the `product-storage-service` to deduct inventory.

## Build & Run Commands

Ensure you have an `.env` file configured (by copying `.env.example`).

```bash
# Build the application and install dependencies
./mvnw clean install

# Skip tests for a faster build
./mvnw clean install -DskipTests

# Run the application (requires PostgreSQL and Kafka to be running)
./mvnw spring-boot:run

# Run the automated tests
./mvnw test
```

### Docker
The service can also be run using Docker Compose, which simplifies the setup of the required database and messaging services.

```bash
# Build and run the service in a Docker container
docker-compose up --build
```

## Configuration

-   **`src/main/resources/application.yaml`**: The main Spring Boot configuration file.
-   **`.env`**: Used for environment-specific variables like database credentials, Kafka host, and R2 API keys.
-   **Default Port**: `9301`

## Key Files & Directories

-   **`pom.xml`**: Defines all project dependencies and build settings.
-   **`CLAUDE.md`**: A very detailed, AI-generated guide to the project's architecture, patterns, and development workflow. It is an excellent resource for a deeper understanding.
-   **`Ecommerce API v1.0.5.postman_collection.json`**: A Postman collection for testing the service's REST APIs.
-   **`src/main/java/microservice/base_source/domain/`**: The most critical directory, containing the framework-independent business logic.
-   **`src/main/java/microservice/base_source/infrastructure/messaging/`**: Contains all Kafka consumer and producer implementations.
