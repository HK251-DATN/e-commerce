package microservice.base_source.presentation.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.entity.Order.PaymentMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentStatusResponse {
    private Long orderId;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String transactionQrUrl;
    private String transactionId;
    private Long totalPrice;
}