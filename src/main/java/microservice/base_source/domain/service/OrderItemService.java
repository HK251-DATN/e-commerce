package microservice.base_source.domain.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.OrderItemUseCase;
import microservice.base_source.persistence.dto.BestSellingProductDTO;
import microservice.base_source.persistence.repository.OrderItemRepository;

@Service
public class OrderItemService implements OrderItemUseCase {

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Override
	public List<OrderItem> getAll(Long buyerId, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<OrderItem> orderItemList = orderItemRepository.findAll(pageable);
		return orderItemList.getContent();
	}

	@Override
	public OrderItem get(Long id) {
		return orderItemRepository.findById(id).orElseThrow(() -> new NotFoundException("OrderItem not found"));
	}

	@Override
	public OrderItem create(OrderItem orderItem) {
		return orderItemRepository.save(orderItem);
	}

	@Override
	public OrderItem update(Long id, OrderItem orderItem) {
		return null;
	}

	@Override
	public void delete(Long id) {
		orderItemRepository.findById(id)
			.ifPresentOrElse(
				orderItemRepository::delete,
				() -> {}
			);
	}

	@Override
	public List<BestSellingProductDTO> getBestSellingProducts(
			Long categoryId,
			LocalDateTime startDate,
			LocalDateTime endDate,
			int limit) {
		return orderItemRepository.findBestSellingProducts(categoryId, startDate, endDate, limit);
	}
}
