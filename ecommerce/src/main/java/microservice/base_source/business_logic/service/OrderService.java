package microservice.base_source.business_logic.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.business_logic.use_case.OrderUseCase;
import microservice.base_source.data_access.entity.Order;
import microservice.base_source.data_access.entity.Order.OrderStatus;
import microservice.base_source.data_access.entity.OrderItem;
import microservice.base_source.data_access.repository.OrderRepository;
import microservice.base_source.presentation.exception.type.NotFoundException;

@Service
public class OrderService implements OrderUseCase {
	@Autowired
	private OrderRepository orderRepository;

	@Override
	public List<Order> search(String buyerId, String searchString, OrderStatus status, BigDecimal minPrice,
			BigDecimal maxPrice, LocalDateTime minTime, LocalDateTime maxTime, String sortByStatus, String sortByPrice,
			String sortByTime, int page, int size) {
		return orderRepository.search(buyerId, searchString, status, minPrice, maxPrice, minTime, maxTime,
				sortByStatus, sortByPrice, sortByTime, page, size);
	}

	@Override
	public Order create(Order order, List<OrderItem> orderItems) {
		return orderRepository.save(order);
	}

	@Override
	public List<Order> getAll(String buyerId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Order> orderPage = orderRepository.findAll(pageable);
		return orderPage.getContent();
	}

	@Override
	public Order get(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
	}

	// @Override
	// public Order update(Long id, Order order) {
	// 	// not support update order
	// 	throw new UnsupportedOperationException("Unimplemented method 'update'");
	// }

	@Override
	public void delete(Long id) {
		orderRepository.findById(id)
			.ifPresentOrElse(
				orderRepository::delete,
				() -> {}	
			);
	}
	
}
