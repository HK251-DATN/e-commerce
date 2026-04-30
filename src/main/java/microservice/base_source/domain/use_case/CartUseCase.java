package microservice.base_source.domain.use_case;

import microservice.base_source.domain.entity.Cart;

public interface CartUseCase {
    Cart create(Cart cart);
    Cart getByBuyerId(String buyerId);
    Cart applyCoupon(String buyerId, String couponCode);
    Cart removeCoupon(String buyerId);
    Cart updateAddress(String buyerId, Long addressId);
    void recalculateCartTotals(Long cartId);
}