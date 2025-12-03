package microservice.base_source.data_access.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.ProductDetail;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
	@Query(
		value = """
				SELECT 
					pd.*, pg.*
				FROM PRODUCT_DETAIL pd
				JOIN PRODUCT_GENERAL pg 
					ON pd.product_general_id = pg.product_general_id
				WHERE 
					(:categoryId = 0 OR pg.category_id = :categoryId)
					AND (
							:searchString = '' OR
							LOWER(pd.description) LIKE LOWER(CONCAT('%', :searchString, '%')) OR
							LOWER(pg.product_name) LIKE LOWER(CONCAT('%', :searchString, '%')) OR
							LOWER(pg.description) LIKE LOWER(CONCAT('%', :searchString, '%'))
						)
				LIMIT :size
				OFFSET ((:page - 1) * :size)
				""",
		nativeQuery = true
	)
	List<Object[]> search(Long categoryId, String searchString, Integer page, Integer size);
}
