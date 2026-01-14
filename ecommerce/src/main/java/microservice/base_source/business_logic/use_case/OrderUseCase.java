package microservice.base_source.business_logic.use_case;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.data_access.entity.Order;
import microservice.base_source.data_access.entity.Order.OrderStatus;

public interface OrderUseCase {
	/**
	 * Search order by: 
	 * 	- buyer_id or total price 
	 * 	- note or status 
	 * 	- createdAt 
	 * Sort case:
	 * 	- buyer_id asc/desc
	 * 	- total_price asc/desc
	 * 	- createdAt asc/desc
	 * 	- status
	 * Filter case:
	 * 	- status
	 *  - total_price range
	 *  - createdAt range
	 * @param buyerId 
	 * @param searchString 
	 * @param status
	 * @param minPrice 
	 * @param maxPrice 
	 * @param minTime 
	 * @param maxTime
	 * @param sortByStatus	ASC/DESC
	 * @param sortByPrice	ASC/DESC
	 * @param sortByTime	ASC/DESC
	 * @param page
	 * @param size
	 * @return danh sách order có phân trang
	 */
	List<Order> search(
		String buyerId, 
		String searchString,
		OrderStatus status,
		BigDecimal minPrice, 
		BigDecimal maxPrice,
		LocalDateTime minTime,
		LocalDateTime maxTime,
		String sortByStatus,
		String sortByPrice,
		String sortByTime,
		int page, int size);

	/**
	 * Lấy danh sách order
	 * @param page
	 * @param size
	 * @return List order
	 */
	List<Order> getAll(String buyerId, int page, int size);
	Order get(Long id);
	Order create(Order order);
	Order update(Long id, Order order);
	void delete(Long id);
}
