package microservice.base_source.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.SaleProduct;
import microservice.base_source.domain.entity.SaleProductId;
import microservice.base_source.persistence.dto.SaleProductDetailDTO;

@Repository
public interface SaleProductRepository extends JpaRepository<SaleProduct, SaleProductId> {

    @Query(value = """
                SELECT EXISTS (
                    SELECT 1
                    FROM SALE_PRODUCT
                    WHERE BATCH_ID = :batchId
                    AND SALE_EVENT_ID = :saleEventId
                )
            """, nativeQuery = true)
    boolean existsByEventAndBatch(@Param("saleEventId") Long saleEventId, @Param("batchId") String batchId);

    @Query(value = """
                SELECT *
                FROM SALE_PRODUCT
                WHERE BATCH_ID = :batchId
                    AND SALE_EVENT_ID = :saleEventId
                LIMIT 1
            """, nativeQuery = true)
    Optional<SaleProduct> findOneByEventAndBatch(@Param("saleEventId") Long saleEventId, @Param("batchId") String batchId);

    @Query(value = """
                SELECT *
                FROM SALE_PRODUCT
                WHERE BATCH_ID = :batchId
                LIMIT 1
            """, nativeQuery = true)
    Optional<SaleProduct> findOneByBatch(@Param("batchId") String batchId);

    /**
     * Finds the active SaleProduct entry for a batch — i.e. the sale event is currently running
     * and still has remaining stock (curQty > 0).
     */
    @Query(value = """
                SELECT sp.*
                FROM SALE_PRODUCT sp
                JOIN SALE_EVENT se ON se.sale_event_id = sp.sale_event_id
                WHERE sp.batch_id = :batchId
                AND se.active_yn  = 'Y'
                AND se.enabled_yn = 'Y'
                AND (se.begin_date IS NULL OR se.begin_date <= NOW())
                AND (se.end_date   IS NULL OR se.end_date   >= NOW())
                AND sp.cur_qty > 0
                LIMIT 1
            """, nativeQuery = true)
    Optional<SaleProduct> findActiveSaleProductByBatchId(@Param("batchId") String batchId);

    @Query(value = """
                SELECT
                    SP.BATCH_ID            AS "batchId",
                    BD.PRODUCT_GENERAL_ID  AS "productGeneralId",
                    PG.NAME                AS "name",
                    PG.DESCRIPTION         AS "description",
                    PG.IMG                 AS "img",
                    BD.PRICE               AS "originPrice",
                    SP.SALE_PRICE          AS "salePrice",
                    SP.DIS_VAL             AS "disVal",
                    SP.MAX_QTY             AS "maxQty",
                    SP.CUR_QTY             AS "curQty",
                    SP.MAX_BUY             AS "maxBuy"
                FROM SALE_PRODUCT SP
                JOIN BATCH_DETAIL BD ON SP.BATCH_ID = BD.BATCH_DETAIL_ID
                JOIN PRODUCT_GENERAL PG ON BD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
                WHERE SP.SALE_EVENT_ID = :saleEventId
                AND (SP.CUR_QTY IS NULL OR SP.CUR_QTY > 0)
            """, nativeQuery = true)
    List<SaleProductDetailDTO> findProductsByEventId(@Param("saleEventId") Long saleEventId);
}
