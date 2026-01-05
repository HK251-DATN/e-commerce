package microservice.base_source.data_access.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.ProductGeneral;

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
                        LOWER(pg.product_name) LIKE LOWER(CONCAT('%', :searchString, '%')) OR
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

	
}
