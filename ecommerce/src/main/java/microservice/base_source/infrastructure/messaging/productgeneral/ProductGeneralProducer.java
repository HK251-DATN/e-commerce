package microservice.base_source.infrastructure.messaging.productgeneral;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductGeneralProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductGeneralCreated(ProductGeneralCreatedEvent event) {
        kafkaTemplate.send("product-general-events", event.prodGenId().toString(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event", ex);
            } else {
                log.info("Event sent with offset {}",
                    result.getRecordMetadata().offset()
                );
            }
        });
    }
}
