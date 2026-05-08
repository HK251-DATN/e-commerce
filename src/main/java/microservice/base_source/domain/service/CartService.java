package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.entity.Address;
import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.persistence.dto.CartItemWithBatchDetailDTO;
import microservice.base_source.domain.exception.type.BadRequestException;
import microservice.base_source.domain.use_case.CartUseCase;
import microservice.base_source.domain.use_case.CartItemUseCase;
import microservice.base_source.domain.use_case.AddressUseCase;
import microservice.base_source.persistence.repository.CartRepository;
import microservice.base_source.persistence.repository.CouponRepository;
import microservice.base_source.persistence.repository.CartItemRepository;
import microservice.base_source.persistence.repository.BatchDetailRepository;
import microservice.base_source.presentation.response.cart.CartDetailResponse;
import microservice.base_source.presentation.response.cartitem.CartItemResponse;
import microservice.base_source.presentation.response.coupon.CartCouponResponse;
import microservice.base_source.presentation.response.order.ShipmentFeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService implements CartUseCase {

    private final CartRepository cartRepository;
    private final CouponRepository couponRepository;
    private final CartItemRepository cartItemRepository;
    private final BatchDetailRepository batchDetailRepository;
    private final CartItemUseCase cartItemUseCase;
    private final AddressUseCase addressUseCase;


    @Override
    @Transactional
    public Cart create(Cart cart) {
        // Set default address and shipping fee
        try {
            ShipmentFeeResponse shipmentFee = addressUseCase.calculateDefaultShipmentFee(cart.getBuyerId());
            cart.setAddressId(shipmentFee.getAddressId());
            cart.setShippingFee(shipmentFee.getShipmentFee());
        } catch (Exception e) {
            // If no default address found, set shipping fee to null
            // User will need to set an address later
            cart.setAddressId(null);
            cart.setShippingFee(null);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart getByBuyerId(String buyerId) {
        return cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for buyer: " + buyerId));
    }

    public CartDetailResponse getCartWithItems(String buyerId) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for buyer: " + buyerId));

        Long cartId = cart.getCartId();
        recalculateCartTotals(cartId);

        // Reload cart to pick up totals saved by recalculateCartTotals
        cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));

        validateAndUpdateCartCoupon(cart);

        List<CartItemResponse> items = cartItemUseCase.getAllWithBatchDetailByCartId(cart.getCartId());

        return CartDetailResponse.fromCartAndItems(cart, items);
    }

    public List<CartCouponResponse> getAllCoupon(Long cartId, int page, int size) {
        // get cart info
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));

        List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelected(cart.getCartId(), true);
        if (selectedItems.isEmpty()) {
			throw new BadRequestException("No items selected in cart");
		}

        // caculate total price of selected items
        BigDecimal tempTotalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : selectedItems) {
            BatchDetail batchDetail = batchDetailRepository
					.findById(cartItem.getBatchDetailId())
					.orElseThrow(() -> new NotFoundException(
							"Product not found: " + cartItem.getBatchDetailId()));

			// Check stock
			if (batchDetail.getQuantity() < cartItem.getQuantity()) {
				throw new BadRequestException(
						"Insufficient stock for product: " + batchDetail.getBatchDetailId() +
								". Available: " + batchDetail.getQuantity() +
								", Requested: " + cartItem.getQuantity());
			}

			// Calculate item total
			BigDecimal itemTotal = batchDetail.getPrice()
					.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
			tempTotalPrice = tempTotalPrice.add(itemTotal);
        }
        Long totalPrice = tempTotalPrice.longValue();

        // get all coupon
        Pageable pageable = PageRequest.of(page - 1, size);
		List<Coupon> coupons = couponRepository.findByPublicYn("Y", pageable);
        List<CartCouponResponse> listCartCouponResponses = new ArrayList<>();

        // check coupon: valid calculate saleAmount; not valid calculate amountToReachDiscount
        coupons.forEach(coupon -> {
            if (coupon.getMinOrderValue() > totalPrice) {
                CartCouponResponse cartCouponResponse = new CartCouponResponse();
                cartCouponResponse.setCoupon(coupon);
                cartCouponResponse.setSaleAmount(0L);
                cartCouponResponse.setAmountToReachDiscount(coupon.getMinOrderValue() - totalPrice);
                listCartCouponResponses.add(cartCouponResponse);
            }
            else {
                CartCouponResponse cartCouponResponse = new CartCouponResponse();
                cartCouponResponse.setCoupon(coupon);
                if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
                    Long discountAmount = totalPrice * coupon.getDiscountValue() / 100;
                    if (discountAmount > coupon.getMaxDiscountAmount()) {
                        discountAmount = coupon.getMaxDiscountAmount();
                    }
                    cartCouponResponse.setSaleAmount(discountAmount);
                }
                else {
                    cartCouponResponse.setSaleAmount(coupon.getDiscountValue());
                }
                cartCouponResponse.setAmountToReachDiscount(0L);
                listCartCouponResponses.add(cartCouponResponse);
            }
        });

        // return list coupon
        return listCartCouponResponses;
    }

    @Override
    @Transactional
    public Cart applyCoupon(String buyerId, String couponCode) {
        log.info("[applyCoupon] START - buyerId={}, couponCode={}", buyerId, couponCode);

        // Get cart
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> {
                    log.warn("[applyCoupon] Cart not found for buyerId={}", buyerId);
                    return new NotFoundException("Cart not found for buyer: " + buyerId);
                });
        log.info("[applyCoupon] Cart found - cartId={}", cart.getCartId());

        // Get selected items
        List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelected(cart.getCartId(), true);
        log.info("[applyCoupon] Selected items count={}", selectedItems.size());
        if (selectedItems.isEmpty()) {
            log.warn("[applyCoupon] No selected items in cart - cartId={}", cart.getCartId());
            throw new BadRequestException("No items selected in cart");
        }

        // Calculate total price of selected items
        BigDecimal tempTotalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : selectedItems) {
            log.info("[applyCoupon] Processing cartItemId={}, batchDetailId={}, qty={}",
                    cartItem.getCartItemId(), cartItem.getBatchDetailId(), cartItem.getQuantity());

            BatchDetail batchDetail = batchDetailRepository
                    .findById(cartItem.getBatchDetailId())
                    .orElseThrow(() -> {
                        log.warn("[applyCoupon] BatchDetail not found - id={}", cartItem.getBatchDetailId());
                        return new NotFoundException("Product not found: " + cartItem.getBatchDetailId());
                    });
            log.info("[applyCoupon] BatchDetail found - id={}, price={}, stockQty={}",
                    batchDetail.getBatchDetailId(), batchDetail.getPrice(), batchDetail.getQuantity());

            // Check stock
            if (batchDetail.getQuantity() < cartItem.getQuantity()) {
                log.warn("[applyCoupon] Insufficient stock - batchDetailId={}, available={}, requested={}",
                        batchDetail.getBatchDetailId(), batchDetail.getQuantity(), cartItem.getQuantity());
                throw new BadRequestException(
                        "Insufficient stock for product: " + batchDetail.getBatchDetailId() +
                                ". Available: " + batchDetail.getQuantity() +
                                ", Requested: " + cartItem.getQuantity());
            }

            BigDecimal itemTotal = batchDetail.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            tempTotalPrice = tempTotalPrice.add(itemTotal);
            log.info("[applyCoupon] Item total={}, running total={}", itemTotal, tempTotalPrice);
        }
        Long totalPrice = tempTotalPrice.longValue();
        log.info("[applyCoupon] Calculated cart total={}", totalPrice);

        // Get and validate coupon
        log.info("[applyCoupon] Looking up coupon - code={}", couponCode);
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> {
                    log.warn("[applyCoupon] Coupon not found - code={}", couponCode);
                    return new NotFoundException("Coupon not found: " + couponCode);
                });
        log.info("[applyCoupon] Coupon found - couponId={}, type={}, value={}, currentQty={}, minOrderValue={}, expiredAt={}",
                coupon.getCouponId(), coupon.getDiscountType(), coupon.getDiscountValue(),
                coupon.getCurrentQuantity(), coupon.getMinOrderValue(), coupon.getExpiredAt());

        // Validate coupon is not expired
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
            log.warn("[applyCoupon] Coupon expired - couponId={}, expiredAt={}", coupon.getCouponId(), coupon.getExpiredAt());
            throw new BadRequestException("Coupon has expired");
        }

        // Validate coupon has remaining quantity
        if (coupon.getCurrentQuantity() <= 0) {
            log.warn("[applyCoupon] Coupon exhausted - couponId={}, currentQty={}", coupon.getCouponId(), coupon.getCurrentQuantity());
            throw new BadRequestException("Coupon is no longer available");
        }

        // Validate minimum order value
        if (coupon.getMinOrderValue() != null && coupon.getMinOrderValue() > totalPrice) {
            log.warn("[applyCoupon] Cart total below minimum - required={}, actual={}", coupon.getMinOrderValue(), totalPrice);
            throw new BadRequestException(
                    "Order total does not meet minimum required: " + coupon.getMinOrderValue() +
                            ". Current total: " + totalPrice);
        }

        // Calculate discount amount
        Long discountAmount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discountAmount = totalPrice * coupon.getDiscountValue() / 100;
            if (coupon.getMaxDiscountAmount() != null && discountAmount > coupon.getMaxDiscountAmount()) {
                log.info("[applyCoupon] Discount capped at maxDiscountAmount={}", coupon.getMaxDiscountAmount());
                discountAmount = coupon.getMaxDiscountAmount();
            }
        } else {
            discountAmount = coupon.getDiscountValue();
        }
        log.info("[applyCoupon] Discount calculated - discountAmount={}", discountAmount);

        // Update cart with coupon information
        cart.setCouponId(coupon.getCouponId());
        cart.setPriceBeforeDiscount(totalPrice);
        cart.setDiscountAmount(discountAmount);
        cart.setPriceAfterDiscount(totalPrice - discountAmount);
        cart.setTotalPrice(totalPrice - discountAmount);
        log.info("[applyCoupon] Saving cart - cartId={}, beforeDiscount={}, discountAmount={}, totalPrice={}",
                cart.getCartId(), totalPrice, discountAmount, totalPrice - discountAmount);

        Cart saved = cartRepository.save(cart);
        log.info("[applyCoupon] SUCCESS - cartId={}", saved.getCartId());
        return saved;
    }

    @Override
    @Transactional
    public Cart removeCoupon(String buyerId) {
        // Get cart
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for buyer: " + buyerId));

        // Get selected items
        List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelected(cart.getCartId(), true);
        if (selectedItems.isEmpty()) {
            throw new BadRequestException("No items selected in cart");
        }

        // Calculate total price of selected items (without discount)
        BigDecimal tempTotalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : selectedItems) {
            BatchDetail batchDetail = batchDetailRepository
                    .findById(cartItem.getBatchDetailId())
                    .orElseThrow(() -> new NotFoundException(
                            "Product not found: " + cartItem.getBatchDetailId()));

            BigDecimal itemTotal = batchDetail.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            tempTotalPrice = tempTotalPrice.add(itemTotal);
        }
        Long totalPrice = tempTotalPrice.longValue();

        // Clear coupon information
        cart.setCouponId(null);
        cart.setPriceBeforeDiscount(null);
        cart.setDiscountAmount(null);
        cart.setPriceAfterDiscount(null);
        cart.setTotalPrice(totalPrice); // Restore original total price

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateAddress(String buyerId, Long addressId) {
        // Get cart
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for buyer: " + buyerId));

        // Verify address exists and belongs to buyer
        Address address = addressUseCase.read(addressId);
        if (!address.getBuyerId().equals(buyerId)) {
            throw new BadRequestException("Address does not belong to buyer");
        }

        // Calculate new shipping fee based on the new address
        ShipmentFeeResponse shipmentFee = addressUseCase.calculateShipmentFee(addressId);

        // Update cart address and shipping fee
        cart.setAddressId(addressId);
        cart.setShippingFee(shipmentFee.getShipmentFee());

        return cartRepository.save(cart);
    }

    /**
     * Recalculates and updates the cart's total price based on selected items.
     * If a coupon is applied, recalculates discount and final price.
     * This should be called whenever cart items are added, updated, or removed.
     */
    @Transactional
    public void recalculateCartTotals(Long cartId) {
        // Get cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));

        // Get selected items with sale price information
        List<CartItemWithBatchDetailDTO> selectedItems = cartItemRepository.findSelectedCartItemsWithBatchDetailByCartId(cartId);

        // Calculate total price of selected items
        BigDecimal tempTotalPrice = BigDecimal.ZERO;

        if (cart.getShippingFee() != null) {
            tempTotalPrice = tempTotalPrice.add(BigDecimal.valueOf(cart.getShippingFee()));
        }

        for (CartItemWithBatchDetailDTO item : selectedItems) {
            BigDecimal unitPrice = (item.getSalePrice() != null && item.getSalePrice() > 0)
                    ? BigDecimal.valueOf(item.getSalePrice())
                    : (item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            tempTotalPrice = tempTotalPrice.add(itemTotal);
        }
        Long totalPrice = tempTotalPrice.longValue();

        // Update cart totals
        if (cart.getCouponId() != null) {
            // Coupon is applied - recalculate with discount
            Coupon coupon = couponRepository.findById(cart.getCouponId()).orElse(null);

            // Validate coupon is still valid
            boolean couponValid = coupon != null
                    && (coupon.getExpiredAt() == null || coupon.getExpiredAt().isAfter(java.time.LocalDateTime.now()))
                    && coupon.getCurrentQuantity() > 0
                    && (coupon.getMinOrderValue() == null || coupon.getMinOrderValue() <= totalPrice);

            if (couponValid) {
                // Calculate discount amount
                Long discountAmount;
                if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
                    discountAmount = totalPrice * coupon.getDiscountValue() / 100;
                    if (coupon.getMaxDiscountAmount() != null && discountAmount > coupon.getMaxDiscountAmount()) {
                        discountAmount = coupon.getMaxDiscountAmount();
                    }
                } else {
                    discountAmount = coupon.getDiscountValue();
                }

                cart.setPriceBeforeDiscount(totalPrice);
                cart.setDiscountAmount(discountAmount);
                cart.setPriceAfterDiscount(totalPrice - discountAmount);
                cart.setTotalPrice(totalPrice - discountAmount);
            } else {
                // Coupon is no longer valid - remove it
                cart.setCouponId(null);
                cart.setPriceBeforeDiscount(null);
                cart.setDiscountAmount(null);
                cart.setPriceAfterDiscount(null);
                cart.setTotalPrice(totalPrice);
            }
        } else {
            // No coupon applied - just update total price
            cart.setTotalPrice(totalPrice);
        }

        cartRepository.save(cart);
    }

    /**
     * Validates the coupon applied to the cart and removes it if:
     * - Coupon has expired
     * - Coupon has run out of uses
     * - Coupon no longer exists
     *
     * Note: publicYn = "N" means private coupon (for employees, VIP, etc.), not disabled
     * Private coupons are still valid if applied
     *
     * If coupon is invalid, recalculates cart pricing without the discount
     */
    @Transactional
    private void validateAndUpdateCartCoupon(Cart cart) {
        // Skip if no coupon is applied
        if (cart.getCouponId() == null) {
            return;
        }

        // Get the applied coupon
        Coupon coupon = couponRepository.findById(cart.getCouponId()).orElse(null);

        boolean shouldRemoveCoupon = false;

        if (coupon == null) {
            shouldRemoveCoupon = true;
        } else {
            // Check if coupon has expired
            if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
                shouldRemoveCoupon = true;
            }
            // Check if coupon has run out of uses
            else if (coupon.getCurrentQuantity() <= 0) {
                shouldRemoveCoupon = true;
            }
        }

        // Remove coupon if invalid
        if (shouldRemoveCoupon) {
            // Get selected items to recalculate total price
            List<CartItem> selectedItems = cartItemRepository.findByCartIdAndIsSelected(cart.getCartId(), true);

            // Calculate total price without coupon
            BigDecimal tempTotalPrice = BigDecimal.ZERO;
            for (CartItem cartItem : selectedItems) {
                BatchDetail batchDetail = batchDetailRepository
                        .findById(cartItem.getBatchDetailId())
                        .orElse(null);

                if (batchDetail != null) {
                    BigDecimal itemTotal = batchDetail.getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                    tempTotalPrice = tempTotalPrice.add(itemTotal);
                }
            }
            Long totalPrice = tempTotalPrice.longValue();

            // Clear coupon information
            cart.setCouponId(null);
            cart.setPriceBeforeDiscount(null);
            cart.setDiscountAmount(null);
            cart.setPriceAfterDiscount(null);
            cart.setTotalPrice(totalPrice);

            // Save updated cart
            cartRepository.save(cart);
        }
    }
}