package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.OrderItem;

public interface OrderItemUseCase {
	/**
	 * Get list order
	 * @param page
	 * @param size
	 * @return List order
	 */
	List<OrderItem> getAll(Long buyerId, int page, int size);
	OrderItem get(Long id);
	OrderItem create(OrderItem orderItem);
	OrderItem update(Long id, OrderItem order);
	void delete(Long id);
}
