package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.Category;

public interface CategoryUseCase {
	/**
	 * Tìm kiếm category theo categoryName nếu isSubCategory true
	 * @param searchName tìm kiếm categoryNamw
	 * @param page
	 * @param size
	 * @return danh sách category có phân trang
	 */
	List<Category> search(String searchName, Integer page, Integer size);

	/**
	 * Lấy danh sách category
	 * @param page
	 * @param size
	 * @return
	 */
	List<Category> getAll(Integer page, Integer size);
	Category get(Long id);
	Category create(Category category);
	Category update(Long id, Category category);
	void delete(Long id);
}
