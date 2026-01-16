package microservice.base_source.presentation.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.entity.Order.OrderType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	@NotBlank
	private String buyerId;

	@NotNull
	private String note;

	@NotBlank
	private OrderType type; // DEFAULT, PREORDER, CART

	@NotEmpty
	List<OrderItemRequest> orderItems;

	public Order toOrderEntity() {
		Order order = new Order();
		order.setBuyerId(this.buyerId);
		order.setStatus(OrderStatus.PENDING);
		order.setNote(this.note);
		order.setType(this.type);
		
		return order;
	}

	public List<OrderItem> toEntity() {
		// Convert OrderItemRequest to OrderItem entities
		List<OrderItem> items = this.orderItems.stream()
			.map(OrderItemRequest::toEntity)
			.toList();
		return items;
	}
}
