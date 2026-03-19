package microservice.base_source.infrastructure.messaging.user;

public record ProductGeneralCreatedEvent(
                Long prodGenId,
                String prodName,
                String imgUrl,
                String description,
                Long categoryId
        ) {

}
