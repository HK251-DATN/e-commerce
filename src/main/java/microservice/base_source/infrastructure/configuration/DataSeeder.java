package microservice.base_source.infrastructure.configuration;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.Address;
import microservice.base_source.domain.entity.Buyer;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.entity.Coupon.DiscountType;
import microservice.base_source.persistence.repository.AddressRepository;
import microservice.base_source.persistence.repository.BuyerRepository;
import microservice.base_source.persistence.repository.CouponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private static final String INIT_DATA_FILE = "init_data.json";

    // Addresses reference Buyers, which arrive asynchronously via Kafka from
    // identity-service's own DataSeeder (BuyerCreateConsumer, which also
    // auto-creates each Buyer's Cart) rather than being seeded locally - there's
    // no ordering guarantee between "this service has started" and "its Kafka
    // consumer has caught up". Bound the wait so a fresh `docker compose up`
    // doesn't need a manual restart to seed addresses correctly.
    private static final Duration BUYER_WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration BUYER_POLL_INTERVAL = Duration.ofSeconds(1);

    private final BuyerRepository buyerRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Addresses/Coupons are this service's own domain data, unlike
            // Buyer/Category/ProductGeneral/BatchDetail which arrive via Kafka -
            // use it (not buyerRepository) as the empty-check guard.
            if (addressRepository.count() > 0) {
                log.info("Database already contains data. Skipping seeding.");
                return;
            }

            log.info("Starting database seeding from {}...", INIT_DATA_FILE);

            InitData initData;
            try (InputStream is = new ClassPathResource(INIT_DATA_FILE).getInputStream()) {
                initData = objectMapper.readValue(is, InitData.class);
            }

            seedCoupons(initData.getCoupons());

            // Buyers/Categories/ProductGenerals/BatchDetails are not seeded here -
            // they arrive via the existing Kafka consumers from identity-service /
            // back-office-service / product_storage_service. Addresses need the
            // Buyer to already exist locally, so wait (bounded) for it to arrive.
            waitForBuyers(collectRequiredBuyerEmails(initData));
            seedAddresses(initData.getAddresses());

            log.info("Database seeding completed successfully!");
        };
    }

    private void seedCoupons(List<InitData.CouponSeed> coupons) {
        for (InitData.CouponSeed seed : coupons) {
            Coupon coupon = new Coupon();
            coupon.setCouponCode(seed.getCouponCode());
            coupon.setTotalQuantity(seed.getTotalQuantity());
            coupon.setCurrentQuantity(seed.getCurrentQuantity());
            coupon.setDiscountType(DiscountType.valueOf(seed.getDiscountType()));
            coupon.setDiscountValue(seed.getDiscountValue());
            coupon.setMaxDiscountAmount(seed.getMaxDiscountAmount());
            coupon.setMinOrderValue(seed.getMinOrderValue());
            coupon.setPublicYn("Y");
            coupon.setExpiredAt(LocalDateTime.now().plusHours(seed.getExpiresInHours()));
            couponRepository.save(coupon);
        }

        log.info("Seeded {} coupons", couponRepository.count());
    }

    private Set<String> collectRequiredBuyerEmails(InitData initData) {
        Set<String> emails = new HashSet<>();
        initData.getAddresses().forEach(seed -> emails.add(seed.getBuyerEmail()));
        return emails;
    }

    private void waitForBuyers(Set<String> requiredEmails) {
        Instant deadline = Instant.now().plus(BUYER_WAIT_TIMEOUT);
        Set<String> missing = requiredEmails;

        log.info("Waiting up to {}s for {} Buyers to arrive via Kafka from identity-service...",
                BUYER_WAIT_TIMEOUT.getSeconds(), missing.size());

        while (!missing.isEmpty() && Instant.now().isBefore(deadline)) {
            Set<String> stillMissing = new HashSet<>();
            for (String email : missing) {
                if (buyerRepository.findByEmail(email).isEmpty()) {
                    stillMissing.add(email);
                }
            }
            missing = stillMissing;

            if (!missing.isEmpty()) {
                try {
                    Thread.sleep(BUYER_POLL_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (missing.isEmpty()) {
            log.info("All required Buyers are available.");
        } else {
            log.warn("Timed out waiting for {} Buyers to arrive via Kafka: {}. "
                            + "Corresponding Address rows will be skipped.",
                    missing.size(), missing);
        }
    }

    private void seedAddresses(List<InitData.AddressSeed> addresses) {
        int skipped = 0;

        for (InitData.AddressSeed seed : addresses) {
            Buyer buyer = buyerRepository.findByEmail(seed.getBuyerEmail()).orElse(null);
            if (buyer == null) {
                log.warn("Address references Buyer '{}' which hasn't arrived via Kafka yet, skipping",
                        seed.getBuyerEmail());
                skipped++;
                continue;
            }

            Address address = new Address();
            address.setBuyerId(buyer.getBuyerId());
            address.setReceiverName(seed.getReceiverName());
            address.setReceiverPNum(seed.getReceiverPNum());
            address.setProvince(seed.getProvince());
            address.setDistrict(seed.getDistrict());
            address.setCommune(seed.getCommune());
            address.setDetail(seed.getDetail());
            address.setIsDefault(seed.isDefaultAddress());
            address.setLat(seed.getLat());
            address.setLng(seed.getLng());
            addressRepository.save(address);
        }

        log.info("Seeded {} addresses ({} skipped - Buyer not yet available)",
                addressRepository.count(), skipped);
    }
}
