package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.SaleProduct;

public interface SaleProductUseCase {

	/**
	 * Lấy danh sách SaleProduct
	 * @param page
	 * @param size
	 * @return List SaleProduct
	 */
	List<SaleProduct> getAll(Integer page, Integer size);
	SaleProduct get(Long saleEventid, Long batchId);
	SaleProduct create(SaleProduct saleProduct);
	SaleProduct update(Long saleEventid, Long batchId, SaleProduct saleProduct);
	void delete(Long saleEventid, Long batchId);
}
