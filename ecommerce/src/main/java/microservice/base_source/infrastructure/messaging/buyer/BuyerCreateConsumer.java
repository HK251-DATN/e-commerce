package microservice.base_source.infrastructure.messaging.buyer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.service.BatchDetailService;
import microservice.base_source.domain.use_case.BuyerUseCase;
import microservice.base_source.infrastructure.messaging.batchdetail.BatchDetailCreateEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class BuyerCreateConsumer {
    private final BuyerUseCase buyerUseCase;
    
    @KafkaListener(topics = "user-events")
    public void consume(BuyerCreateEvent event) {
        log.info("Received event: {}", event);
        
        try {
            buyerUseCase.create(event.toBuyerEntity());
            
            log.info("Create BUYER {} success", event.getBuyerId());
        } catch (Exception e) {
            log.error("Create BUYER {} fail due to: {}", event.getBuyerId(), e.getMessage());
        }
    }
}