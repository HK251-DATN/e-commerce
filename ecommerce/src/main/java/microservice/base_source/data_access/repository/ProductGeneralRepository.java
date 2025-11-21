package microservice.base_source.data_access.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.data_access.entity.ProductGeneral;

@Repository
public interface ProductGeneralRepository extends JpaRepository<ProductGeneral, Long> {
	@Query("SELECT p FROM ProductGeneral p WHERE " +
			"(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
			"(:searchString IS NULL OR :searchString = '' OR " +
			"LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchString, '%')) OR " +
			"LOWER(p.description) LIKE LOWER(CONCAT('%', :searchString, '%')))")
	Page<ProductGeneral> search(@Param("categoryId") Long categoryId,
			@Param("searchString") String searchString,
			Pageable pageable);
}
