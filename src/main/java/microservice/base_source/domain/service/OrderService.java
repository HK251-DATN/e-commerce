package microservice.base_source.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import microservice.base_source.domain.entity.*;
import microservice.base_source.domain.entity.Coupon.DiscountType;
import microservice.base_source.domain.exception.type.BadRequestException;
import microservice.base_source.domain.exception.type.UnauthorizedException;
import microservice.base_source.infrastructure.messaging.order.OrderConfirmedEvent;
import microservice.base_source.infrastructure.messaging.order.OrderCreatedEvent;
import microservice.base_source.infrastructure.messaging.order.OrderPickRequestedEvent;
import microservice.base_source.infrastructure.messaging.order.OrderProducer;
import microservice.base_source.persistence.dto.OrderDeliveryDTO;
import microservice.base_source.persistence.dto.OrderSummaryDTO;
import microservice.base_source.persistence.repository.*;
import microservice.base_source.presentation.response.order.OrderDetailResponse;
import microservice.base_source.presentation.response.order.OrderPaymentStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.OrderUseCase;

@Service
public class OrderService implements OrderUseCase {
	@Autowired
	private OrderRepository orderRepository;

	@PersistenceContext
    EntityManager entityManager;

	@Autowired
	private OrderProducer orderProducer;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CartRepository cartRepository;
	@Autowired
	private CartItemRepository cartItemRepository;
	@Autowired
	private BatchDetailRepository batchDetailRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private SaleProductRepository saleProductRepository;
    @Autowired
    private QrPaymentService qrPaymentService;
	@Autowired
	private CouponRepository couponRepository;


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

//
//		// check product in stocks
//
//		// insert order
//		Order insertedOrder = orderRepository.save(order);
//
//		// insert order item
//		orderItems.forEach(orderItem -> {
//			orderItem.setOrderId(insertedOrder.getOrderId());
//		});
//		saveBatch(orderItems);
//
//		orderProducer.publishOrderCreated(new OrderCreatedEvent(insertedOrder.getOrderId()));
//
//		// update quantity batch detail & status product detail
//		return insertedOrder;

		return null;
	}

	@Override
	public List<Order> getAll(int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Order> orderPage = orderRepository.findAll(pageable);
		return orderPage.getContent();
	}

	@Override
	public Order get(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
	}
    
    @Override
    public List<Order> getByBuyerId(String buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderRepository.findByBuyerId(buyerId, pageable);
        return orderPage.getContent();
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

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public Order createFromCart(String buyerId, Long addressId, Order.PaymentMethod paymentMethod) {
		// 1. Validate address belongs to user
		Address address = addressRepository.findById(addressId)
				.orElseThrow(() -> new NotFoundException("Address not found"));

		if (!address.getBuyerId().equals(buyerId)) {
			throw new UnauthorizedException("Address does not belong to the user");
		}

		// 2. Find user's cart
		Cart cart = cartRepository.findByBuyerId(buyerId)
				.orElseThrow(() -> new NotFoundException("Cart not found for user"));

		// 3. Get selected cart items
		List<CartItem> selectedItems = cartItemRepository
				.findByCartIdAndIsSelected(cart.getCartId(), true);

		if (selectedItems.isEmpty()) {
			throw new BadRequestException("No items selected in cart");
		}

		// 4. Validate stock, sale limits, and calculate total
		BigDecimal totalPrice = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem cartItem : selectedItems) {
			BatchDetail batchDetail = batchDetailRepository
					.findById(cartItem.getBatchDetailId())
					.orElseThrow(() -> new NotFoundException(
							"Product not found: " + cartItem.getBatchDetailId()));

			// Check overall stock
			if (batchDetail.getQuantity() < cartItem.getQuantity()) {
				throw new BadRequestException(
						"Insufficient stock for product: " + batchDetail.getBatchDetailId() +
								". Available: " + batchDetail.getQuantity() +
								", Requested: " + cartItem.getQuantity());
			}

			// Check sale purchase limit (maxBuy) when this cart item is part of a sale
			BigDecimal effectivePrice = batchDetail.getPrice();
			if (cartItem.getSaleEventId() != null) {
				SaleProduct saleProduct = saleProductRepository
						.findOneByEventAndBatch(cartItem.getSaleEventId(), cartItem.getBatchDetailId())
						.orElse(null);

				if (saleProduct != null) {
					// Check remaining sale stock
					if (cartItem.getQuantity() > saleProduct.getCurQty()) {
						throw new BadRequestException(
								"Insufficient sale stock for product: " + cartItem.getBatchDetailId() +
										". Sale stock available: " + saleProduct.getCurQty() +
										", Requested: " + cartItem.getQuantity());
					}

					// Check per-buyer limit
					if (saleProduct.getMaxBuy() != null) {
						long pastOrderQty = orderItemRepository.sumCommittedSaleQuantity(
								buyerId, cartItem.getBatchDetailId(), cartItem.getSaleEventId());
						long allowed = saleProduct.getMaxBuy() - pastOrderQty;
						if (cartItem.getQuantity() > allowed) {
							throw new BadRequestException(
									"Exceeds sale purchase limit for product: " + cartItem.getBatchDetailId() +
											". Limit per buyer: " + saleProduct.getMaxBuy() +
											", Already ordered: " + pastOrderQty +
											", Requested now: " + cartItem.getQuantity());
						}
					}

					// Apply sale price when available
					if (saleProduct.getSalePrice() != null && saleProduct.getSalePrice() > 0) {
						effectivePrice = BigDecimal.valueOf(saleProduct.getSalePrice());
					}
				}
			}

			BigDecimal itemTotal = effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
			totalPrice = totalPrice.add(itemTotal);

			OrderItem orderItem = new OrderItem();
			orderItem.setBatchDetailId(cartItem.getBatchDetailId());
			orderItem.setSaleEventId(cartItem.getSaleEventId());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setOriginalPrice(batchDetail.getPrice());
			orderItem.setUnitPriceAtPurchase(effectivePrice);
			orderItems.add(orderItem);
		}

		// 5. Create order
		Order order = new Order();
		order.setBuyerId(buyerId);
		order.setAddressId(addressId);
		order.setPaymentMethod(paymentMethod);
		order.setStatus(OrderStatus.PENDING);
		order.setTotalPrice(totalPrice.longValue());

		Order savedOrder = orderRepository.save(order);

		if (savedOrder.getPaymentMethod() != null && savedOrder.getPaymentMethod().equals(Order.PaymentMethod.VNPAY)) {
			String transactionQrUrl = qrPaymentService.createOrderTransactionQrUrl(
					savedOrder.getOrderId().toString(), order.getTotalPrice());
			savedOrder.setTransactionQrUrl(transactionQrUrl);
			orderRepository.save(savedOrder);
		}

		// caculate apply coupon
		Long couponId = Long.parseLong(order.getCouponId());
		Optional<Coupon> optionalCoupon = couponRepository.findById(couponId);
		Coupon appliedCoupon = new Coupon();
		if (optionalCoupon.isPresent()) {
			appliedCoupon = optionalCoupon.get();
		}

		if (appliedCoupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
			order.setTotalPrice(order.getTotalPrice() - appliedCoupon.getDiscountValue());
		} else {
			order.setTotalPrice(order.getTotalPrice() * (1 - appliedCoupon.getDiscountValue()));
		}

		// 6. Save order items
		for (OrderItem item : orderItems) {
			item.setOrderId(savedOrder.getOrderId());
			orderItemRepository.save(item);
		}

		// 7. Decrement stock: batch quantity and sale curQty
		for (CartItem cartItem : selectedItems) {
			BatchDetail batchDetail = batchDetailRepository.findById(cartItem.getBatchDetailId()).get();
			batchDetail.setQuantity(batchDetail.getQuantity() - cartItem.getQuantity().intValue());
			batchDetailRepository.save(batchDetail);

			if (cartItem.getSaleEventId() != null) {
				saleProductRepository
						.findOneByEventAndBatch(cartItem.getSaleEventId(), cartItem.getBatchDetailId())
						.ifPresent(sp -> {
							sp.setCurQty(sp.getCurQty() - cartItem.getQuantity());
							saleProductRepository.save(sp);
						});
			}
		}

		// 8. Clear selected cart items
		cartItemRepository.deleteByCartIdAndIsSelected(cart.getCartId(), true);

		// 9. Publish order created event
		if (orderProducer != null) {
			orderProducer.publishOrderCreated(new OrderCreatedEvent(
					savedOrder.getOrderId(),
					savedOrder.getBuyerId(),
					savedOrder.getTotalPrice()));
		}

		return savedOrder;
	}
    
    @Override
    public OrderDetailResponse getOrderDetail(Long orderId) {
        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        // Get order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        // Build response
        return OrderDetailResponse.fromEntity(order, orderItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING state");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        List<OrderConfirmedEvent.OrderItemInfo> orderItemInfosForConfirmedEvent = orderItems.stream()
                .map(item -> new OrderConfirmedEvent.OrderItemInfo(item.getBatchDetailId(), item.getQuantity()))
                .toList();
        
        orderProducer.publishOrderConfirmed(new OrderConfirmedEvent(
                savedOrder.getOrderId(),
                savedOrder.getBuyerId(),
                savedOrder.getAddressId(),
                savedOrder.getNote(),
                savedOrder.getTotalPrice(),
                orderItemInfosForConfirmedEvent
        ));

        List<OrderPickRequestedEvent.OrderItemInfo> orderItemInfosForPickRequestedEvent = orderItems.stream()
                .map(item -> new OrderPickRequestedEvent.OrderItemInfo(item.getBatchDetailId(), item.getQuantity()))
                .toList();

        orderProducer.publishOrderPickRequested(new OrderPickRequestedEvent(
                savedOrder.getOrderId(),
                orderItemInfosForPickRequestedEvent,
                Long.valueOf(order.getBuyerId())
        ));

        return savedOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }
    
    @Override
    public List<OrderSummaryDTO> getOrderSummaryList (String status) {
        return orderRepository.getOrderSummaryInfo(status);
    }

    @Override
    public List<OrderDeliveryDTO> getDeliveryInfo() {
        return orderRepository.getDeliveryInfo();
    }

    @Override
    public OrderDeliveryDTO getDeliveryInfoByOrderId(Long orderId) {
        return orderRepository.getDeliveryInfoByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Delivery info not found for order: " + orderId));
    }

    @Override
    public OrderPaymentStatusResponse getPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return new OrderPaymentStatusResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getTransactionQrUrl(),
                order.getTransactionId(),
                order.getTotalPrice()
        );
    }
}
