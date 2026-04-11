package microservice.base_source.infrastructure.messaging.order;

import java.util.List;

public record OrderConfirmedEvent (
        Long orderId,
        String buyerId,
        Long addressId,
        String note,
        Long totalPrice,
        List<OrderItemInfo> orderItems
) {
    public record OrderItemInfo(String batchDetailId, Long quantity) {}
}
