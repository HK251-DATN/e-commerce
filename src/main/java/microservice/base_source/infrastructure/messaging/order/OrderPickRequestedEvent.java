package microservice.base_source.infrastructure.messaging.order;

import java.util.List;

public record OrderPickRequestedEvent(
        Long orderId,
        List<OrderItemInfo> orderItems,
        Long buyerId
) {
    public record OrderItemInfo(String batchDetailId, Long quantity) {}
}
