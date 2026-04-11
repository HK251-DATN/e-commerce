package microservice.base_source.infrastructure.messaging.order;

import lombok.AllArgsConstructor;
import microservice.base_source.domain.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderDeliveringConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderDeliveringConsumer.class);
    private final OrderService orderService;

    @KafkaListener(topics = "order-delivering-events", groupId = "ecommerce-group")
    public void consume(OrderDeliveringEvent event) {
        log.info("Consumed message -> {}", event);
        try {
            orderService.updateOrderStatus(event.getOrderId(), microservice.base_source.domain.entity.Order.OrderStatus.DELIVERING);
            log.info("Order {} status updated to DELIVERING", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order {} status to DELIVERING: {}", event.getOrderId(), e.getMessage());
        }
    }
}
