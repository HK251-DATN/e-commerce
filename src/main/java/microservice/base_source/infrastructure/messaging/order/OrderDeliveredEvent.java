package microservice.base_source.infrastructure.messaging.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderDeliveredEvent {
    private Long orderId;
}
