package microservice.base_source.presentation.request;

import lombok.Data;
import microservice.base_source.domain.entity.Cart;

@Data
public class CartRequest {
	private String buyerId;

	public Cart toEntity() {
		Cart cart = new Cart();
		cart.setBuyerId(this.buyerId);
		return cart;
	}
}
