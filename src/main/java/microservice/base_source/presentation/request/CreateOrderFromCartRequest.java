package microservice.base_source.presentation.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Order;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderFromCartRequest {
    private Order.PaymentMethod paymentMethod;
    private String note; // Optional order note
}