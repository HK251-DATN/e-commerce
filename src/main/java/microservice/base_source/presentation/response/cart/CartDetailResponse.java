package microservice.base_source.presentation.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.presentation.response.cartitem.CartItemResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDetailResponse {

    // Cart information
    private Long cartId;
    private String buyerId;
    private Long addressId;
    private Long shippingFee;
    private Long totalPrice;
    private Long couponId;
    private Long priceBeforeDiscount;
    private Long discountAmount;
    private Long priceAfterDiscount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Cart items
    private List<CartItemResponse> items;

    // Summary information
    private Integer totalItems;
    private Integer selectedItemsCount;

    public static CartDetailResponse fromCartAndItems(Cart cart, List<CartItemResponse> items) {
        int totalItems = items.size();
        int selectedCount = (int) items.stream().filter(CartItemResponse::getIsSelected).count();

        return CartDetailResponse.builder()
                .cartId(cart.getCartId())
                .buyerId(cart.getBuyerId())
                .addressId(cart.getAddressId())
                .shippingFee(cart.getShippingFee())
                .totalPrice(cart.getTotalPrice())
                .couponId(cart.getCouponId())
                .priceBeforeDiscount(cart.getPriceBeforeDiscount())
                .discountAmount(cart.getDiscountAmount())
                .priceAfterDiscount(cart.getPriceAfterDiscount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(items)
                .totalItems(totalItems)
                .selectedItemsCount(selectedCount)
                .build();
    }
}
