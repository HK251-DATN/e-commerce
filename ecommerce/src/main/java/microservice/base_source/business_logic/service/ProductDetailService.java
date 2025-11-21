package microservice.base_source.business_logic.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.business_logic.use_case.ProductDetailUseCase;
import microservice.base_source.data_access.entity.ProductDetail;
import microservice.base_source.data_access.repository.ProductDetailRepository;
import microservice.base_source.presentation.exception.type.ProductNotFoundException;

@Service
public class ProductDetailService implements ProductDetailUseCase {
	@Autowired
	private ProductDetailRepository productDetailRepository;

	@Override
	public List<ProductDetail> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductDetail> productDetailPage = productDetailRepository.findAll(pageable);
		return productDetailPage.getContent();
	}

	@Override
	public ProductDetail get(Long id) {
		return productDetailRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("ProductDetail not found"));
	}

	@Override
	public ProductDetail create(ProductDetail productDetail) {
		return productDetailRepository.save(productDetail);
	}

	@Override
	public ProductDetail update(Long id, ProductDetail productDetail) {
		ProductDetail existingProductDetail = productDetailRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("ProductDetail not found"));

		return productDetailRepository.save(existingProductDetail);
	}

	@Override
	public void delete(Long id) {
		productDetailRepository.findById(id)
			.ifPresent(productDetailRepository::delete);
	}
	
}
