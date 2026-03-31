package microservice.base_source.domain.service;

import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.persistence.repository.CartItemRepository;

@Service
public class CartItemService {
	@Autowired
	private CartItemRepository cartItemRepository;

	public CartItem get(UUID id) {
		return cartItemRepository.findById(id).orElseThrow(() -> new NotFoundException("CartItem not found"));
	}

	public CartItem create(CartItem cartItem) {
		return cartItemRepository.save(cartItem);
	}

	public CartItem update(UUID id, CartItem cartItem) {
		CartItem existingCategory = cartItemRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("CartItem not found"));
		// Update all fields 
		BeanUtils.copyProperties(cartItem, existingCategory, "cartItemId", "cartId", "batchDetailId");
		return cartItemRepository.save(existingCategory);
	}

	public void delete(UUID id) {
		cartItemRepository.findById(id)
			.ifPresentOrElse(
				cartItemRepository::delete,
				() -> {}	
			);
	}
}
