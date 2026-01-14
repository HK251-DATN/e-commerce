package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.ProductDetail;

public interface ProductDetailUseCase {
	List<ProductDetail> getAll(Integer page, Integer size);
	ProductDetail get(Long id);
	ProductDetail create(ProductDetail productDetail);
	ProductDetail update(Long id, ProductDetail productDetail);
	void delete(Long id);
}
