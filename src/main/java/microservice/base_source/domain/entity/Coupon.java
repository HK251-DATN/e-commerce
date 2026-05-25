package microservice.base_source.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.persistence.converter.StringListConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COUPON")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_code", unique = true)
    private String couponCode;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "current_quantity")
    private Long currentQuantity;

    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value")
    private Long discountValue;

    @Column(name = "maxDiscount_amount")
    private Long maxDiscountAmount;

    @Column(name = "minOrder_value")
    private Long minOrderValue;

    @Column(name = "public_yn")
    private String publicYn; // Y or N

    @Convert(converter = StringListConverter.class)
    @Column(name = "list_user_group")
    private List<String> listUserGroup = new ArrayList<>();

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (listUserGroup == null || listUserGroup.isEmpty()) {
            listUserGroup = new ArrayList<>(List.of("default_buyer"));
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
}
