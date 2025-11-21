package microservice.base_source.business_logic.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.business_logic.use_case.ProductGeneralUseCase;
import microservice.base_source.data_access.entity.ProductGeneral;
import microservice.base_source.data_access.repository.ProductGeneralRepository;
import microservice.base_source.presentation.exception.type.ProductNotFoundException;

@Service
public class ProductGeneralService implements ProductGeneralUseCase {
	@Autowired
	private ProductGeneralRepository productGeneralRepository;

	@Override
	public List<ProductGeneral> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductGeneral> productGeneralPage = productGeneralRepository.findAll(pageable);
		return productGeneralPage.getContent();
	}

	@Override
	public ProductGeneral get(Long id) {
		return productGeneralRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("ProductGeneral not found"));
	}

	@Override
	public ProductGeneral create(ProductGeneral productGeneral) {
		return productGeneralRepository.save(productGeneral);
	}

	@Override
	public ProductGeneral update(Long id, ProductGeneral productGeneral) {
		ProductGeneral existingProductGeneral = productGeneralRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("ProductGeneral not found"));
		
		return productGeneralRepository.save(existingProductGeneral);
	}

	@Override
	public void delete(Long id) {
		productGeneralRepository.findById(id)
			.ifPresent(productGeneralRepository::delete);
	}

	@Override
	public List<ProductGeneral> search(Long categoryId, String searchString, Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductGeneral> productGeneralPage = productGeneralRepository.search(categoryId, searchString, pageable);
		return productGeneralPage.getContent();
	}
}
