package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.exception.type.BadRequestException;
import microservice.base_source.domain.use_case.CartUseCase;
import microservice.base_source.persistence.repository.CartRepository;
import microservice.base_source.persistence.repository.CouponRepository;
import microservice.base_source.persistence.repository.CartItemRepository;
import microservice.base_source.persistence.repository.BatchDetailRepository;
import microservice.base_source.presentation.response.coupon.CartCouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService implements CartUseCase {

    private final CartRepository cartRepository;
    private final CouponRepository couponRepository;
    private final CartItemRepository cartItemRepository;
    private final BatchDetailRepository batchDetailRepository;


    @Override
    @Transactional
    public Cart create(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart getByBuyerId(String buyerId) {
        return cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for buyer: " + buyerId));
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
		Page<Coupon> coupons = couponRepository.findAll(pageable);
        List<CartCouponResponse> listCartCouponResponses = new ArrayList<>();
        
        // check coupon: valid caculate saleAmount; not valid caculate amountToReachDiscount
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
}