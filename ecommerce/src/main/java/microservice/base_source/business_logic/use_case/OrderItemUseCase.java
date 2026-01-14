package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.Order;

public interface OrderItemUseCase {
	/**
	 * Get list order
	 * @param page
	 * @param size
	 * @return List order
	 */
	List<Order> getAll(Long buyerId, int page, int size);
	Order get(Long id);
	Order create(Order order);
	Order update(Long id, Order order);
	void delete(Long id);
}
