package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.ProductGeneral;

public interface ProductGeneralUseCase {

	/**
	 * Tìm kiếm productGeneral bằng productName, description, categoryId
	 * @param categoryId categoryID của entity Category
	 * @param searchString dùng search cho cả productName và description
	 * @param page
	 * @param size
	 * @return danh sách productGeneral hỗ trợ pagination
	 */
	List<ProductGeneral> search(Long categoryId, String searchString, Integer page, Integer size);
	List<ProductGeneral> getAll(Integer page, Integer size);
	ProductGeneral get(Long id);
	ProductGeneral create(ProductGeneral productGeneral);
	ProductGeneral update(Long id, ProductGeneral productGeneral);
	void delete(Long id);
}
