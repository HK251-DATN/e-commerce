package microservice.base_source.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
	@Autowired
	private BuyerRepository buyerRepository;
    @Autowired
    private ProductGeneralRepository productGeneralRepository;


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
	public Order createFromCart(String buyerId, Order.PaymentMethod paymentMethod, String note) {
		// 1. Find user's cart
		Cart cart = cartRepository.findByBuyerId(buyerId)
				.orElseThrow(() -> new NotFoundException("Cart not found for user"));

		// 2. Validate cart has an address selected
		if (cart.getAddressId() == null) {
			throw new BadRequestException("Please select a delivery address for your cart");
		}

		// 3. Validate address exists and belongs to user
		Address address = addressRepository.findById(cart.getAddressId())
				.orElseThrow(() -> new NotFoundException("Address not found"));

		if (!address.getBuyerId().equals(buyerId)) {
			throw new UnauthorizedException("Address does not belong to the user");
		}

		// 4. Get selected cart items
		List<CartItem> selectedItems = cartItemRepository
				.findByCartIdAndIsSelected(cart.getCartId(), true);

		if (selectedItems.isEmpty()) {
			throw new BadRequestException("No items selected in cart");
		}

		// 5. Validate stock, sale limits, and calculate total
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

		// 5b. Re-validate that the cart's coupon still matches buyer's user groups.
		// Buyer's groups may have changed since the coupon was applied to the cart.
		if (cart.getCouponId() != null) {
			Coupon coupon = couponRepository.findById(cart.getCouponId())
					.orElseThrow(() -> new BadRequestException("Applied coupon no longer exists"));

			Buyer buyer = buyerRepository.findById(buyerId).orElse(null);
			List<String> buyerGroups = (buyer == null || buyer.getListUserGroup() == null)
					? List.of()
					: buyer.getListUserGroup();
			List<String> couponGroups = coupon.getListUserGroup();

			boolean matches = couponGroups != null
					&& !couponGroups.isEmpty()
					&& couponGroups.stream().anyMatch(buyerGroups::contains);
			if (!matches) {
				throw new BadRequestException("Coupon is not available for your user group");
			}
		}

		// 6. Create order - copy all information from cart
		Order order = new Order();
		order.setBuyerId(buyerId);
		order.setAddressId(cart.getAddressId());
        if (cart.getShippingFee() != null) {
            order.setShippingFee(cart.getShippingFee());
        } else {
            order.setShippingFee(0L);
        }
		order.setPaymentMethod(paymentMethod);
		order.setNote(note);
		order.setStatus(OrderStatus.PENDING);

		// Copy coupon and pricing information from cart
		order.setCouponId(cart.getCouponId());
		order.setPriceBeforeDiscount(cart.getPriceBeforeDiscount());
		order.setDiscountAmount(cart.getDiscountAmount());
		order.setPriceAfterDiscount(cart.getPriceAfterDiscount());
		order.setTotalPrice(cart.getTotalPrice());

		// For COD, set status to PAID immediately
		if (paymentMethod == Order.PaymentMethod.COD) {
			order.setStatus(OrderStatus.PAID);
		}

		Order savedOrder = orderRepository.save(order);

		// For VNPAY, generate QR code URL for payment
		if (savedOrder.getPaymentMethod() != null && savedOrder.getPaymentMethod().equals(Order.PaymentMethod.VNPAY)) {
			String transactionQrUrl = qrPaymentService.createOrderTransactionQrUrl(
					savedOrder.getOrderId().toString(), savedOrder.getTotalPrice());
			savedOrder.setTransactionQrUrl(transactionQrUrl);
			orderRepository.save(savedOrder);
		}

		// 7. Save order items
		for (OrderItem item : orderItems) {
			item.setOrderId(savedOrder.getOrderId());
			orderItemRepository.save(item);
		}

		// 8. Decrement stock: batch quantity and sale curQty
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

		// 9. Clear selected cart items and reset cart coupon
		cartItemRepository.deleteByCartIdAndIsSelected(cart.getCartId(), true);

		// Reset cart coupon and pricing after order creation
		cart.setCouponId(null);
		cart.setPriceBeforeDiscount(null);
		cart.setDiscountAmount(null);
		cart.setPriceAfterDiscount(null);
		cart.setTotalPrice(0L);
		cartRepository.save(cart);

		// 10. Publish order created event
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        // Resolve product names and images via BatchDetail -> ProductGeneral
        Map<Long, String> productNameByOrderItemId = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderItem::getOrderItemId,
                        item -> {
                            String batchDetailId = item.getBatchDetailId();
                            if (batchDetailId == null) return "";
                            return batchDetailRepository.findById(batchDetailId)
                                    .map(bd -> productGeneralRepository.findById(bd.getProductGeneralId())
                                            .map(ProductGeneral::getName)
                                            .orElse(""))
                                    .orElse("");
                        }
                ));

        Map<Long, String> productImgByOrderItemId = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderItem::getOrderItemId,
                        item -> {
                            String batchDetailId = item.getBatchDetailId();
                            if (batchDetailId == null) return "";
                            return batchDetailRepository.findById(batchDetailId)
                                    .map(bd -> productGeneralRepository.findById(bd.getProductGeneralId())
                                            .map(ProductGeneral::getImg)
                                            .orElse(""))
                                    .orElse("");
                        }
                ));

        Address address = order.getAddressId() != null
                ? addressRepository.findById(order.getAddressId()).orElse(null)
                : null;

        return OrderDetailResponse.fromEntityWithProductNamesAndAddress(order, orderItems, productNameByOrderItemId, productImgByOrderItemId, address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if ((order.getStatus() != OrderStatus.PENDING) && (order.getStatus() != OrderStatus.PAID)) {
            throw new BadRequestException("Order is not in PENDING OR PAID state");
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
