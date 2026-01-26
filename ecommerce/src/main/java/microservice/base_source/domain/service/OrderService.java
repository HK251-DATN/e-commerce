package microservice.base_source.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.OrderUseCase;
import microservice.base_source.persistence.repository.OrderRepository;

@Service
public class OrderService implements OrderUseCase {
	@Autowired
	private OrderRepository orderRepository;

	@PersistenceContext
    EntityManager entityManager;

	@Override
	public List<Order> search(String buyerId, String searchString, OrderStatus status, BigDecimal minPrice,
			BigDecimal maxPrice, LocalDateTime minTime, LocalDateTime maxTime, String sortByStatus, String sortByPrice,
			String sortByTime, int page, int size) {
		return orderRepository.search(buyerId, searchString, status, minPrice, maxPrice, minTime, maxTime,
				sortByStatus, sortByPrice, sortByTime, page, size);
	}

	@Override
	@Transactional(
		rollbackFor = Exception.class,
		propagation = Propagation.REQUIRED
	)
	public Order create(Order order, List<OrderItem> orderItems) {
		// check product in stocks

		// insert order
		Order insertedOrder = orderRepository.save(order);

		// insert order item
		orderItems.forEach(orderItem -> {
			orderItem.setOrderId(insertedOrder.getOrderId());
		});
		saveBatch(orderItems);

		// update quantity batch detail & status product detail
		return insertedOrder;
	}

	@Override
	public List<Order> getAll(String buyerId, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
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

	@Transactional(propagation = Propagation.MANDATORY)
	public void saveBatch(List<OrderItem> items) {
		int batchSize = 50;

		for (int i = 0; i < items.size(); i++) {
			entityManager.persist(items.get(i));

			if (i > 0 && i % batchSize == 0) {
				entityManager.flush();
				entityManager.clear();
			}
		}
	}
}
