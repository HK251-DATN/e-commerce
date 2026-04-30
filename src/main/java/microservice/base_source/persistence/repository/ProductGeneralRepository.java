package microservice.base_source.persistence.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Repository
public interface ProductGeneralRepository extends JpaRepository<ProductGeneral, Long> {
    
//    Logger log = LoggerFactory.getLogger(ProductGeneralRepository.class);
    
	@Query(
		value = """
            SELECT *
            FROM PRODUCT_GENERAL pg
            WHERE
                (:categoryId = 0 OR pg.category_id = :categoryId)
                AND (
                        :searchString = '' OR
                        LOWER(pg.name)        LIKE LOWER(CONCAT('%', :searchString, '%')) OR
                        LOWER(pg.description) LIKE LOWER(CONCAT('%', :searchString, '%'))
                    )
            LIMIT :size
            OFFSET ((:page - 1) * :size)
            """,
		nativeQuery = true
	)
	List<ProductGeneral> search(
		@Param("categoryId") Long categoryId,
		@Param("searchString") String searchString,
		@Param("page") Integer page,
		@Param("size") Integer size
	);
    
    @Query(value = """
            SELECT
                PG.PRODUCT_GENERAL_ID 	AS "productGeneralId",
                PG.CATEGORY_ID 			AS "categoryId",
                PG.PROVIDER_ID 			AS "providerId",
                PG.NAME        			AS "name",
                PG.DESCRIPTION 			AS "description",
                PG.IMG         			AS "img",
                PG.UNIT                 AS "unit",
                PG.UNIT_QUANTITY        AS "unitQuantity",
                BD.BATCH_DETAIL_ID		AS "batchId",
                BD.QUANTITY   			AS "quantity",
                BD.PRICE      			AS "originPrice",
                BD.AVG_RATE   			AS "avgRate",
                BD.NUM_RATE   			AS "numRate",
                BD.CREATED_AT 			AS "createdAt",
                BD.VERIFICATION_TYPE           AS "verificationType",
                BD.CERTIFICATE_TYPE            AS "certificateType",
                BD.PROVIDER_ID                 AS "providerId",
                BD.SUB_BATCH_ID                AS "subBatchId",
                BD.LOGO_URL                    AS "logoUrl",
                COALESCE(SP.DIS_VAL, null)        AS "disVal",
                SP.SALE_PRICE                  AS "salePrice",
                COALESCE(SP.SALE_EVENT_ID, null)  AS "saleEventId"
            FROM BATCH_DETAIL BD
            LEFT JOIN PRODUCT_GENERAL PG
                ON BD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
            LEFT JOIN (
                SELECT SP2.*
                FROM SALE_PRODUCT SP2
                JOIN SALE_EVENT SE ON SE.SALE_EVENT_ID = SP2.SALE_EVENT_ID
                WHERE SE.ACTIVE_YN  = 'Y'
                  AND SE.ENABLED_YN = 'Y'
                  AND (SE.BEGIN_DATE IS NULL OR SE.BEGIN_DATE <= NOW())
                  AND (SE.END_DATE   IS NULL OR SE.END_DATE   >= NOW())
                  AND SP2.CUR_QTY > 0
            ) SP ON SP.BATCH_ID = BD.BATCH_DETAIL_ID
            WHERE
                (
                    :categoryId = 0
                    OR PG.CATEGORY_ID = :categoryId
                    OR EXISTS (
                        SELECT 1
                        FROM CATEGORY C
                        WHERE C.CATEGORY_ID      = PG.CATEGORY_ID
                        AND C.IS_SUB_CATEGORY    = 'Y'
                        AND C.BELONG_TO_CATEGORY = :categoryId
                    )
                )
                AND (:productGeneralId = 0 OR BD.PRODUCT_GENERAL_ID = :productGeneralId)
                AND (
                    CAST(:searchString AS TEXT) = '' OR
                    LOWER(PG.NAME)        LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%')) OR
                    LOWER(PG.DESCRIPTION) LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%'))
                )
                AND (:minPrice   = 0 OR BD.PRICE    >= :minPrice)
                AND (:maxPrice   = 0 OR BD.PRICE    <= :maxPrice)
                AND (:minRating  = 0 OR BD.AVG_RATE >= :minRating)
                AND (:maxRating  = 0 OR BD.AVG_RATE <= :maxRating)
                AND (:minNumRate = 0 OR BD.NUM_RATE >= :minNumRate)
                AND (:maxNumRate = 0 OR BD.NUM_RATE <= :maxNumRate)
                AND (
                    cast(:searchTags as text) IS NULL OR
                    cast(:searchTags as text) = '' OR
                    PG.TAGS && string_to_array(cast(:searchTags as text), ',')::text[]
                )
            ORDER BY
                CASE WHEN :createdSortOption = 'ASC'  THEN BD.CREATED_AT END ASC,
                CASE WHEN :createdSortOption = 'DESC' THEN BD.CREATED_AT END DESC,
                CASE WHEN :ratingSortOption  = 'ASC'  THEN BD.AVG_RATE   END ASC,
                CASE WHEN :ratingSortOption  = 'DESC' THEN BD.AVG_RATE   END DESC,
                CASE WHEN :numRateSortOption = 'ASC'  THEN BD.NUM_RATE   END ASC,
                CASE WHEN :numRateSortOption = 'DESC' THEN BD.NUM_RATE   END DESC
            LIMIT :size OFFSET (:page - 1) * :size;
			""", nativeQuery = true)
    List<DetailGeneralDTO> aggregatedSearch (
            @Param("categoryId") Long categoryId,
            @Param("productGeneralId") Long productGeneralId,
            @Param("searchString") String searchString,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            @Param("minNumRate") int minNumRate,
            @Param("maxNumRate") int maxNumRate,
            @Param("searchTags") String searchTags,
            @Param("createdSortOption") String createdSortOption,
            @Param("ratingSortOption") String ratingSortOption,
            @Param("numRateSortOption") String numRateSortOption,
            @Param("page") Integer page,
            @Param("size") Integer size
    );
    
    @Query(value = """
            SELECT
                PG.PRODUCT_GENERAL_ID   AS "productGeneralId",
                PG.CATEGORY_ID          AS "categoryId",
                PG.PROVIDER_ID          AS "providerId",
                PG.NAME                 AS "name",
                PG.DESCRIPTION          AS "description",
                PG.IMG                  AS "img",
                BD.BATCH_DETAIL_ID      AS "batchId",
                BD.QUANTITY             AS "quantity",
                BD.PRICE                AS "originPrice",
                BD.AVG_RATE             AS "avgRate",
                BD.NUM_RATE             AS "numRate",
                BD.CREATED_AT           AS "createdAt",
                SP.DIS_VAL              AS "disVal",
                SP.SALE_PRICE           AS "salePrice",
                SP.SALE_EVENT_ID        AS "saleEventId",
                BD.VERIFICATION_TYPE    AS "verificationType",
                BD.CERTIFICATE_TYPE     AS "certificateType",
                BD.PROVIDER_ID          AS "providerId",
                BD.SUB_BATCH_ID         AS "subBatchId",
                BD.LOGO_URL             AS "logoUrl"
            FROM BATCH_DETAIL BD
            LEFT JOIN PRODUCT_GENERAL PG ON BD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
            LEFT JOIN (
                SELECT sp2.*
                FROM SALE_PRODUCT sp2
                JOIN SALE_EVENT se ON se.sale_event_id = sp2.sale_event_id
                WHERE se.active_yn   = 'Y'
                  AND se.enabled_yn  = 'Y'
                  AND (se.begin_date IS NULL OR se.begin_date <= NOW())
                  AND (se.end_date   IS NULL OR se.end_date   >= NOW())
                  AND sp2.cur_qty > 0
            ) SP ON SP.batch_id = BD.BATCH_DETAIL_ID
            WHERE BD.BATCH_DETAIL_ID = :batchDetailId
            LIMIT 1
            """, nativeQuery = true)
    Optional<DetailGeneralDTO> findByBatchDetailId(@Param("batchDetailId") String batchDetailId);

    
}
