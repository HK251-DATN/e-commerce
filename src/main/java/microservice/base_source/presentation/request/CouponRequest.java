package microservice.base_source.presentation.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.entity.Coupon.DiscountType;

@Data
public class CouponRequest {
	private String couponCode;
	private Long totalQuantity;
	private Long currentQuantity;
	private DiscountType discountType;
	private Long discountValue;
	private Long maxDiscountAmount;
	private Long minOrderValue;
	private LocalDateTime expiredAt;
	private String publicYn;
	private List<String> listUserGroup;

	public Coupon toEntity() {
		Coupon coupon = new Coupon();
		coupon.setCouponCode(this.couponCode);
		coupon.setTotalQuantity(this.totalQuantity);
		coupon.setCurrentQuantity(this.currentQuantity);
		coupon.setDiscountType(this.discountType);
		coupon.setDiscountValue(this.discountValue);
		coupon.setMaxDiscountAmount(this.maxDiscountAmount);
		coupon.setMinOrderValue(this.minOrderValue);
		coupon.setExpiredAt(this.expiredAt);
		coupon.setPublicYn(this.publicYn);
		coupon.setListUserGroup(this.listUserGroup);
		return coupon;
	}
}
