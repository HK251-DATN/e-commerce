package microservice.base_source.domain.use_case;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.persistence.dto.OrderDeliveryDTO;
import microservice.base_source.persistence.dto.OrderSummaryDTO;
import microservice.base_source.presentation.response.order.OrderDetailResponse;
import microservice.base_source.presentation.response.order.OrderPaymentStatusResponse;

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
     * Create order from cart (simplified flow)
     * Uses cart's selected address, shipping fee, and coupon
     * @param buyerId - authenticated user ID
     * @param paymentMethod - COD or VNPAY
     * @param note - optional order note
     * @return created order
     */
    Order createFromCart(String buyerId, Order.PaymentMethod paymentMethod, String note);

    /**
     * Get all orders (with optional buyer filter)
     * @return List order
     */
    List<Order> getAll(int page, int size);

    /**
     * Get orders by buyer ID
     */
    List<Order> getByBuyerId(String buyerId, int page, int size);

    /**
     * Get order with order items
     */
    OrderDetailResponse getOrderDetail(Long orderId);

    /**
     * Get single order by ID
     */
    Order get(Long id);

    /**
     * Delete order
     */
    void delete(Long id);

    /**
     * Confirm order
     */
    Order confirmOrder(Long orderId);

    /**
     * Update order status
     */
    void updateOrderStatus(Long orderId, OrderStatus status);

    List<OrderSummaryDTO> getOrderSummaryList(String status);

    List<OrderDeliveryDTO> getDeliveryInfo();

    OrderDeliveryDTO getDeliveryInfoByOrderId(Long orderId);

    /**
     * Get payment status for order (lightweight for polling)
     */
    OrderPaymentStatusResponse getPaymentStatus(Long orderId);
}