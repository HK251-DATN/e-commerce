package microservice.base_source.data_access.repository;

import microservice.base_source.data_access.entity.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query("SELECT c FROM Category c WHERE " +
			"c.isSubCategory = 'Y' AND " +
			"(LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :searchName, '%')) OR " +
			"LOWER(c.description) LIKE LOWER(CONCAT('%', :searchName, '%')))")
	Page<Category> search(@Param("searchName") String searchName, Pageable pageable);
}
