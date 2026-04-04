package microservice.base_source.presentation.request;

import lombok.Data;
import microservice.base_source.domain.entity.CartItem;

@Data
public class CartItemRequest {
	private Long cartId;

	private String batchDetailId;

	private Long quantity;

	private Boolean isSelected;

	public CartItem toEntity() {
		CartItem cartItem = new CartItem();
		cartItem.setCartId(this.cartId);
		cartItem.setBatchDetailId(this.batchDetailId);
		cartItem.setQuantity(this.quantity);
		cartItem.setIsSelected(this.isSelected);
		return cartItem;
	}
}
