package microservice.base_source.infrastructure.messaging.productgeneral;

public record ProductGeneralCreatedEvent(
                Long prodGenId,
                String prodName,
                String imgUrl,
                String description,
                Long categoryId
        ) {

}
