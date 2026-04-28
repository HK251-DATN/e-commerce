package microservice.base_source.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Repository
public interface BatchDetailRepository extends JpaRepository<BatchDetail, String> {

    @Query(value = """
            SELECT
                PG.PRODUCT_GENERAL_ID  AS "productGeneralId",
                PG.CATEGORY_ID         AS "categoryId",
                PG.PROVIDER_ID         AS "providerId",
                PG.NAME                AS "name",
                PG.DESCRIPTION         AS "description",
                PG.IMG                 AS "img",
                BD.BATCH_DETAIL_ID     AS "batchId",
                BD.QUANTITY            AS "quantity",
                BD.PRICE               AS "originPrice",
                NULL                   AS "salePrice",
                NULL                   AS "disVal",
                BD.AVG_RATE            AS "avgRate",
                BD.NUM_RATE            AS "numRate",
                NULL                   AS "saleEventId",
                BD.CREATED_AT          AS "createdAt"
            FROM BATCH_DETAIL BD
            JOIN PRODUCT_GENERAL PG ON PG.PRODUCT_GENERAL_ID = BD.PRODUCT_GENERAL_ID
            WHERE BD.QUANTITY > 0
              AND (CAST(:searchString AS TEXT) = '' OR
                   LOWER(PG.NAME)        LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%')) OR
                   LOWER(PG.DESCRIPTION) LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%')))
              AND NOT EXISTS (
                  SELECT 1
                  FROM SALE_PRODUCT SP
                  JOIN SALE_EVENT SE ON SE.SALE_EVENT_ID = SP.SALE_EVENT_ID
                  WHERE SP.BATCH_ID    = BD.BATCH_DETAIL_ID
                    AND SE.ACTIVE_YN  = 'Y'
                    AND SE.ENABLED_YN = 'Y'
                    AND (SE.BEGIN_DATE IS NULL OR SE.BEGIN_DATE <= NOW())
                    AND (SE.END_DATE   IS NULL OR SE.END_DATE   >= NOW())
                    AND SP.CUR_QTY > 0
              )
            ORDER BY BD.CREATED_AT DESC
            LIMIT :size OFFSET (:page - 1) * :size
            """, nativeQuery = true)
    List<DetailGeneralDTO> findAvailableForSaleEvent(
            @Param("searchString") String searchString,
            @Param("page") int page,
            @Param("size") int size
    );
}
