package microservice.base_source.domain.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.SaleEvent;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.SaleEventUseCase;
import microservice.base_source.persistence.repository.SaleEventRepository;

@Service
public class SaleEventService implements SaleEventUseCase {
	@Autowired
	private SaleEventRepository saleEventRepository;

	@Override
	public List<SaleEvent> searchEvents(String searchString, String activeYn, String enableYn, LocalTime beginTime,
			LocalTime endTime, LocalDateTime beginDate, LocalDateTime endDate, int page, int size) {
		return saleEventRepository.search(searchString, activeYn, enableYn, beginTime, endTime, beginDate, endDate, page, size);
	}

	@Override
	public List<SaleEvent> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<SaleEvent> saleEventPage = saleEventRepository.findAll(pageable);
		return saleEventPage.getContent();
	}

	@Override
	public SaleEvent get(Long saleEventid) {
		return saleEventRepository.findById(saleEventid).orElseThrow(() -> new NotFoundException("SaleEvent not found"));
	}

	@Override
	public SaleEvent create(SaleEvent saleEvent) {
		return saleEventRepository.save(saleEvent);
	}

	@Override
	public SaleEvent update(Long saleEventid, SaleEvent saleEvent) {
		SaleEvent existingSaleEvent = saleEventRepository.findById(saleEventid)
				.orElseThrow(() -> new NotFoundException("SaleEvent not found"));

		if (saleEvent.getName() != null) existingSaleEvent.setName(saleEvent.getName());
		if (saleEvent.getDescription() != null) existingSaleEvent.setDescription(saleEvent.getDescription());
		if (saleEvent.getImg() != null) existingSaleEvent.setImg(saleEvent.getImg());
		if (saleEvent.getDisplayPriority() != null) existingSaleEvent.setDisplayPriority(saleEvent.getDisplayPriority());
		if (saleEvent.getActiveYn() != null) existingSaleEvent.setActiveYn(saleEvent.getActiveYn());
		if (saleEvent.getEnabledYn() != null) existingSaleEvent.setEnabledYn(saleEvent.getEnabledYn());
		if (saleEvent.getBeginTime() != null) existingSaleEvent.setBeginTime(saleEvent.getBeginTime());
		if (saleEvent.getEndTime() != null) existingSaleEvent.setEndTime(saleEvent.getEndTime());
		if (saleEvent.getBeginDate() != null) existingSaleEvent.setBeginDate(saleEvent.getBeginDate());
		if (saleEvent.getEndDate() != null) existingSaleEvent.setEndDate(saleEvent.getEndDate());
		
		return saleEventRepository.save(existingSaleEvent);
	}

	@Override
	public List<SaleEvent> getActiveEvents(int page, int size) {
		return saleEventRepository.findActiveEvents(page, size);
	}

	@Override
	public SaleEvent cancel(Long saleEventId) {
		SaleEvent existing = saleEventRepository.findById(saleEventId)
				.orElseThrow(() -> new NotFoundException("SaleEvent not found"));
		existing.setActiveYn("N");
		existing.setEnabledYn("N");
		return saleEventRepository.save(existing);
	}

	@Override
	public SaleEvent enable(Long saleEventId) {
		SaleEvent existing = saleEventRepository.findById(saleEventId)
				.orElseThrow(() -> new NotFoundException("SaleEvent not found"));
		existing.setActiveYn("Y");
		existing.setEnabledYn("Y");
		return saleEventRepository.save(existing);
	}

	@Override
	public void delete(Long saleEventid) {
		saleEventRepository.findById(saleEventid)
			.ifPresentOrElse(
				saleEventRepository::delete,
				() -> {}
			);
	}

	@Override
	public SaleEvent uploadBanner(Long saleEventId, String bannerUrl) {
		SaleEvent existing = saleEventRepository.findById(saleEventId)
				.orElseThrow(() -> new NotFoundException("SaleEvent not found"));
		existing.setImg(bannerUrl);
		return saleEventRepository.save(existing);
	}

}
