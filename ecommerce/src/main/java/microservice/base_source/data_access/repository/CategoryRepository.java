package microservice.base_source.data_access.repository;

import microservice.base_source.data_access.entity.Category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query(
		value = """
            SELECT *
            FROM CATEGORY c
            WHERE c.is_sub_category = 'Y'
				AND (
					LOWER(c.category_name) LIKE LOWER(CONCAT('%', :searchName, '%'))
					OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchName, '%'))
				)
            LIMIT :size 
			OFFSET ((:page - 1) * :size)
            """,
		nativeQuery = true
	)
	List<Category> search(
		@Param("searchName") String searchName, 
		@Param("page") Integer page, 
		@Param("size") Integer size
	);
}
