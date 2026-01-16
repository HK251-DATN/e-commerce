package microservice.base_source.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Repository
public interface ProductGeneralRepository extends JpaRepository<ProductGeneral, Long> {
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
				PD.PRODUCT_DETAIL_ID AS productDetailId,
				PD.CATEGORY_ID AS categoryId,
				PD.PROVIDER_ID AS providerId,
				PD.NAME        AS name,
				PD.DESCRIPTION AS description,
                PD.IMG         AS img,

                BD.BATCH_ID   AS BatchId
                BD.QUANTITY   AS Quantity
                BD.PRICE      AS OriginPrice
                COALESCE(BD.SALE_PRICE, 0) AS salePrice
                BD.AVG_RATE   AS AvgRate
                BD.NUM_RATE   AS NumRate
                BD.CREATED_AT AS CreatedAt
			FROM PRODUCT_DETAIL PD
			JOIN BATCH_DETAIL   BD
				ON PD.PRODUCT_GENERAL_ID = BD.PRODUCT_GENERAL_ID
			WHERE
				(:categoryId = 0 OR BD.CATEGORY_ID = :categoryId)
				AND (:productGeneralId = 0 OR BD.PRODUCT_GENERAL_ID = :productGeneralId)
				AND (
					:searchString = '' OR
					LOWER(PG.NAME)        LIKE LOWER(CONCAT('%', :searchString, '%')) OR
					LOWER(PG.DESCRIPTION) LIKE LOWER(CONCAT('%', :searchString, '%'))
				)
				AND (:minPrice   = 0 OR BD.PRICE    >= :minPrice)
				AND (:maxPrice   = 0 OR BD.PRICE    <= :maxPrice)
				AND (:minRating  = 0 OR BD.AVG_RATE >= :minRating)
				AND (:maxRating  = 0 OR BD.AVG_RATE <= :maxRating)
                AND (:minNumRate = 0 OR BD.NUM_RATE >= :minNumRate)
				AND (:maxNumRate = 0 OR BD.NUM_RATE <= :maxNumRate)
				AND (
					:searchTags IS NULL OR
					cardinality(:searchTags) = 0 OR
					PG.TAGS && CAST(:searchTags AS text[])
				)
			ORDER BY
				CASE WHEN :createdSortOption = 'ASC'  THEN BD.CREATED_AT END ASC,
				CASE WHEN :createdSortOption = 'DESC' THEN BD.CREATED_AT END DESC,
				CASE WHEN :ratingSortOption  = 'ASC'  THEN BD.AVG_RATE   END ASC,
				CASE WHEN :ratingSortOption  = 'DESC' THEN BD.AVG_RATE   END DESC
                CASE WHEN :numRateSortOption = 'ASC'  THEN BD.NUM_RATE   END ASC,
				CASE WHEN :numRateSortOption = 'DESC' THEN BD.NUM_RATE   END DESC
			LIMIT :size OFFSET (:page - 1) * :size
			""", nativeQuery = true)
	List<DetailGeneralDTO> aggregatedSearch(
		@Param("categoryId") Long categoryId,
		@Param("productGeneralId") Long productGeneralId,
		@Param("searchString") String searchString,
		@Param("minPrice")  BigDecimal minPrice,
		@Param("maxPrice")  BigDecimal maxPrice,
		@Param("minRating") BigDecimal minRating,
		@Param("maxRating") BigDecimal maxRating,
        @Param("minNumRate") int minNumRate,
		@Param("maxNumRate") int maxNumRate,
		@Param("searchTags") String[] searchTags,
		@Param("createdSortOption") String createdSortOption,
		@Param("ratingSortOption")  String ratingSortOption,
        @Param("numRateSortOption") String numRateSortOption,
		@Param("page") Integer page,
		@Param("size") Integer size
	);
}
