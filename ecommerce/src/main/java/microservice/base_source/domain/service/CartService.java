package microservice.base_source.domain.service;

import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.persistence.repository.CartRepository;

@Service
public class CartService {
	@Autowired
	private CartRepository cartRepository;

	public Cart get(UUID id) {
		Cart cart = cartRepository.findById(id).orElseThrow(() -> new NotFoundException("Cart not found"));
		// caculate total price of cart
		// Double totalPrice = 0.0;
		// cart.getListCartItem().forEach(cartItem -> {
		// 	totalPrice += cartItem.get() * cartItem.getQuantity();
		// });
		return cart;
	}

	public Cart create(Cart cart) {
		return cartRepository.save(cart);
	}

	public Cart update(UUID id, Cart cart) {
		Cart existingCategory = cartRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Cart not found"));
		// Update all fields 
		BeanUtils.copyProperties(cart, existingCategory, "cartId");
		return cartRepository.save(existingCategory);
	}

	public void delete(UUID id) {
		cartRepository.findById(id)
			.ifPresentOrElse(
				cartRepository::delete,
				() -> {}	
			);
	}
}
