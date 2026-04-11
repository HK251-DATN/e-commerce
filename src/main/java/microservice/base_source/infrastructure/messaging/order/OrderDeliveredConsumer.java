package microservice.base_source.infrastructure.messaging.order;

import lombok.AllArgsConstructor;
import microservice.base_source.domain.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderDeliveredConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderDeliveredConsumer.class);
    private final OrderService orderService;

    @KafkaListener(topics = "order-delivered-events", groupId = "ecommerce-group")
    public void consume(OrderDeliveredEvent event) {
        log.info("Consumed message -> {}", event);
        try {
            orderService.updateOrderStatus(event.getOrderId(), microservice.base_source.domain.entity.Order.OrderStatus.DELIVERED);
            log.info("Order {} status updated to DELIVERED", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order {} status to DELIVERED: {}", event.getOrderId(), e.getMessage());
        }
    }
}
