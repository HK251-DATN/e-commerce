package microservice.base_source.domain.use_case;

import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.persistence.dto.BestSellingProductDTO;

public interface OrderItemUseCase {
	/**
	 * Get list order
	 * @return List order
	 */
	List<OrderItem> getAll(Long buyerId, int page, int size);
	OrderItem get(Long id);
	OrderItem create(OrderItem orderItem);
	OrderItem update(Long id, OrderItem order);
	void delete(Long id);

	List<BestSellingProductDTO> getBestSellingProducts(
			Long categoryId,
			LocalDateTime startDate,
			LocalDateTime endDate,
			int limit);
}
