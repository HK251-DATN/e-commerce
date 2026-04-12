package microservice.base_source.presentation.response.coupon;

import lombok.Data;
import microservice.base_source.domain.entity.Coupon;

@Data
public class CartCouponResponse {
	private Coupon coupon;
	private Long saleAmount;
	private Long amountToReachDiscount;
}
