package microservice.base_source.presentation.request;

import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.data_access.entity.Order.OrderStatus;
import microservice.base_source.data_access.entity.Order.OrderType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	@NotBlank
	private String buyerId;

	@NotBlank
	@Enumerated(EnumType.STRING)
	private OrderStatus status; // PENDING, PAID, CONFIRMED, PACKAGED, SHIPPED, COMPLETED, CANCELLED

	@NotNull
	private String note;

	@NotBlank
	private OrderType type; // DEFAULT, PREORDER, CART

	@NotEmpty
	List<OrderItemRequest> orderItems;
}
