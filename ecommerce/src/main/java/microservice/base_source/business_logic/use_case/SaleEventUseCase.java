package microservice.base_source.business_logic.use_case;

import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.data_access.entity.SaleEvent;

public interface SaleEventUseCase {

	/**
	 * Search SaleEvent
	 * @param page
	 * @param size
	 * @return List SaleEvent
	 */
	List<SaleEvent> searchEvents(String searchString, 
		String activeYn, String enableYn,
		LocalDateTime beginTime, LocalDateTime endTime,
		LocalDateTime beginDate, LocalDateTime endDate,
		int page, int size
	);

	/**
	 * Lấy danh sách SaleEvent
	 * @param page
	 * @param size
	 * @return List SaleEvent
	 */
	List<SaleEvent> getAll(Integer page, Integer size);
	SaleEvent get(Long saleEventid);
	SaleEvent create(SaleEvent saleEvent);
	SaleEvent update(Long saleEventid, SaleEvent saleEvent);
	void delete(Long saleEventid);
}
