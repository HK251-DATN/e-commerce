package microservice.base_source.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CART")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cartId;

    @Column(name = "buyer_id")
    private String buyerId;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "shipping_fee")
    private Long shippingFee;

    @Column(name = "total_price")
    private Long totalPrice;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "price_before_discount")
    private Long priceBeforeDiscount;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @Column(name = "price_after_discount")
    private Long priceAfterDiscount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
