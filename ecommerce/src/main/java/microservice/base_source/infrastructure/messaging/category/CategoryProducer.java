package microservice.base_source.infrastructure.messaging.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCategoryCreated(CategoryCreatedEvent event) {
        kafkaTemplate.send("category-events", event.categoryId().toString(), event).whenComplete((result, ex) -> {
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
