package microservice.base_source.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query(
		value = """
            SELECT *
            FROM CATEGORY c
            WHERE c.is_sub_category = 'Y'
				AND (
					:searchString = '' OR
					LOWER(c.category_name) LIKE LOWER(CONCAT('%', :searchString, '%'))
					OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchString, '%'))
				)
            LIMIT :size 
			OFFSET ((:page - 1) * :size)
            """,
		nativeQuery = true
	)
	List<Category> search(
		@Param("searchString") String searchString, 
		@Param("page") Integer page, 
		@Param("size") Integer size
	);
}
