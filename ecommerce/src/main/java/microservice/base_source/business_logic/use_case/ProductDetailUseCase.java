package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.ProductDetail;

public interface ProductDetailUseCase {

	/**
	 * Search products with pagination and optional filters.
	 * @param categoryId
	 * @param searchString
	 * @param page
	 * @param size
	 * @return List of Object arrays ProductDetail container ProductGeneral data
	 */
	List<Object[]> search(Long categoryId, String searchString, Integer page, Integer size);
	List<ProductDetail> getAll(Integer page, Integer size);
	ProductDetail get(Long id);
	ProductDetail create(ProductDetail productDetail);
	ProductDetail update(Long id, ProductDetail productDetail);
	void delete(Long id);
}
