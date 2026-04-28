package microservice.base_source.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    /**
     * Sums the quantity already committed (across all non-cancelled orders) by a buyer
     * for a specific sale product, used to enforce per-buyer maxBuy limits.
     */
    @Query(value = """
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM ORDER_ITEM oi
                JOIN ORDERS o ON oi.order_id = o.order_id
                WHERE oi.batch_detail_id = :batchDetailId
                AND   oi.sale_event_id   = :saleEventId
                AND   o.buyer_id         = :buyerId
                AND   o.status          <> 'CANCELLED'
            """, nativeQuery = true)
    long sumCommittedSaleQuantity(
            @Param("buyerId") String buyerId,
            @Param("batchDetailId") String batchDetailId,
            @Param("saleEventId") Long saleEventId);
}
