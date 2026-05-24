package microservice.base_source.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.persistence.dto.BestSellingProductDTO;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    @Query(value = """
                SELECT pg.product_general_id  AS product_general_id,
                       pg.name                AS name,
                       pg.category_id         AS category_id,
                       pg.img                 AS img,
                       SUM(oi.quantity)        AS total_quantity
                FROM ORDER_ITEM oi
                JOIN ORDERS      o  ON oi.order_id         = o.order_id
                JOIN BATCH_DETAIL bd ON oi.batch_detail_id = bd.batch_detail_id
                JOIN PRODUCT_GENERAL pg ON bd.product_general_id = pg.product_general_id
                JOIN CATEGORY    c  ON pg.category_id      = c.category_id
                WHERE o.status <> 'CANCELLED'
                  AND (CAST(:startDate AS TIMESTAMP) IS NULL OR o.created_at >= CAST(:startDate AS TIMESTAMP))
                  AND (CAST(:endDate   AS TIMESTAMP) IS NULL OR o.created_at <= CAST(:endDate   AS TIMESTAMP))
                  AND (
                      CAST(:categoryId AS BIGINT) IS NULL
                      OR c.category_id = CAST(:categoryId AS BIGINT)
                      OR c.belong_to_category = CAST(:categoryId AS BIGINT)
                      OR EXISTS (
                          SELECT 1 FROM CATEGORY parent
                          WHERE parent.category_id = c.belong_to_category
                            AND parent.belong_to_category = CAST(:categoryId AS BIGINT)
                      )
                  )
                GROUP BY pg.product_general_id, pg.name, pg.category_id, pg.img
                ORDER BY total_quantity DESC
                LIMIT :limitCount
            """, nativeQuery = true)
    List<BestSellingProductDTO> findBestSellingProducts(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limitCount") int limitCount);

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
