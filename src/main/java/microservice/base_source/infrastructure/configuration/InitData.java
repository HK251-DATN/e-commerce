package microservice.base_source.infrastructure.configuration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Binds {@code src/main/resources/init_data.json} for {@link DataSeeder}.
 *
 * Buyer/Category/ProductGeneral/BatchDetail rows are not seeded here - they arrive
 * asynchronously via Kafka from identity-service's / back-office-service's own
 * DataSeeders (BuyerCreateConsumer / CategoryCreatedConsumer /
 * ProductGeneralCreatedConsumer). Addresses reference a Buyer by email (resolved
 * against the local, Kafka-populated table at seed time) rather than by id, since
 * identity-service assigns Buyer ids via auto-increment.
 */
@Getter
@Setter
public class InitData {

    @JsonProperty("Addresses")
    private List<AddressSeed> addresses;

    @JsonProperty("Coupons")
    private List<CouponSeed> coupons;

    @Getter
    @Setter
    public static class AddressSeed {
        private String buyerEmail;
        private String receiverName;
        private String receiverPNum;
        private String province;
        private String district;
        private String commune;
        private String detail;
        private boolean defaultAddress;
        private Double lat;
        private Double lng;
    }

    @Getter
    @Setter
    public static class CouponSeed {
        private String couponCode;
        private Long totalQuantity;
        private Long currentQuantity;
        private String discountType;
        private Long discountValue;
        private Long maxDiscountAmount;
        private Long minOrderValue;
        private Long expiresInHours;
    }
}
