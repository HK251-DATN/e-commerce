package microservice.base_source.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import microservice.base_source.domain.entity.SaleProduct;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.SaleProductUseCase;
import microservice.base_source.persistence.repository.SaleProductRepository;

public class SaleProductService implements SaleProductUseCase {
	@Autowired
	private SaleProductRepository saleProductRepository;

	@Override
	public List<SaleProduct> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<SaleProduct> saleProductList = saleProductRepository.findAll(pageable);
		return saleProductList.getContent();
	}

	@Override
	public SaleProduct get(Long saleEventid, Long batchId) {
		return saleProductRepository.findById(batchId)
				.orElseThrow(() -> new NotFoundException("SaleProduct not found"));
	}

	@Override
	public SaleProduct create(SaleProduct saleProduct) {
		return saleProductRepository.save(saleProduct);
	}

	@Override
	public SaleProduct update(Long saleEventid, Long batchId, SaleProduct saleProduct) {
		SaleProduct existingSaleProduct = saleProductRepository.findById(batchId)
				.orElseThrow(() -> new NotFoundException("SaleProduct not found"));
		// Update maxQty, maxBuy, disVal
		existingSaleProduct.setMaxQty(saleProduct.getMaxQty());
		existingSaleProduct.setMaxBuy(saleProduct.getMaxBuy());
		existingSaleProduct.setDisVal(saleProduct.getDisVal());
		return saleProductRepository.save(existingSaleProduct);
	}

	@Override
	public void delete(Long saleEventid, Long batchId) {
		saleProductRepository.findById(batchId)
			.ifPresentOrElse(
				saleProductRepository::delete,
				() -> {}
			);
	}
	
}
