package microservice.base_source.business_logic.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.business_logic.use_case.ProductDetailUseCase;
import microservice.base_source.data_access.dto.DetailGeneralDTO;
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
			.ifPresentOrElse(
				productDetailRepository::delete,
				() -> {}
			);
	}

	@Override
	public List<DetailGeneralDTO> search(Long categoryId, String searchString, Integer page, Integer size) {
		return productDetailRepository.search(categoryId, searchString, page, size);
	}

	@Override
	public List<DetailGeneralDTO> aggregatedSearch(Long categoryId, Long productGeneralId, String searchString,
			BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, BigDecimal maxRating, String[] tags,
			String createdSortOption, String ratingSortOption, Integer page, Integer size) 
	{
		return productDetailRepository.aggregatedSearch(
			categoryId,
			productGeneralId,
			searchString,
			minPrice,
			maxPrice,
			minRating,
			maxRating,
			tags,
			createdSortOption,
			ratingSortOption,
			page,
			size
		);
	}

	@Override
	public Long countAggregatedSearch(Long categoryId, Long productGeneralId, String searchString,
			BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, BigDecimal maxRating, String[] tags) 
	{
		return productDetailRepository.countAggregatedSearch(
			categoryId,
			productGeneralId,
			searchString,
			minPrice,
			maxPrice,
			minRating,
			maxRating,
			tags
		);
	}
	
}
