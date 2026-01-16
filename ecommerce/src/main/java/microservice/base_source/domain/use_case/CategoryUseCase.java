package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.Category;

public interface CategoryUseCase {
	/**
	 * Tìm kiếm category theo category_name or description nếu is_sub_category true
	 * @return List category
	 */
	List<Category> search(String searchName, Integer page, Integer size);

	/**
	 * Lấy danh sách category
	 * @return List category
	 */
	List<Category> getAll(Integer page, Integer size);
	Category get(Long id);
	Category create(Category category);
	Category update(Long id, Category category);
	void delete(Long id);
}
