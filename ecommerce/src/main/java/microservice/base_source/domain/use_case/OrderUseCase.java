package microservice.base_source.domain.use_case;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;

public interface OrderUseCase {
	/** 
	 * <p> Search: 
	 * <p> 	- buyer_id or total price 
	 * <p> 	- note or status 
	 * <p> 	- createdAt 
	 * <p> Sort:
	 * <p> 	- buyer_id asc/desc
	 * <p> 	- total_price asc/desc
	 * <p> 	- createdAt asc/desc
	 * <p> 	- status
	 * <p> Filter:
	 * <p> 	- status
	 * <p>  - total_price range
	 * <p>  - createdAt range
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
	 * <p> Condition create order
	 * <p> 	- coupon: valid
	 * <p> 	- product: in stock
	 * <p> 	- buyer: exist
	 * <p> 	- create order temp employee confirm
	 * @return
	 */
	Order create(Order order, List<OrderItem> orderItems);

	/**
	 * Lấy danh sách order
	 * @return List order
	 */
	List<Order> getAll(String buyerId, int page, int size);
	Order get(Long id);
	// Order update(Long id, Order order);
	void delete(Long id);
}
