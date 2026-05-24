package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.exception.type.BadRequestException;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.persistence.repository.CouponRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    private static final Set<String> VALID_SORT_BY  = Set.of("createdAt", "expiredAt", "discountValue");
    private static final Set<String> VALID_SORT_DIR = Set.of("ASC", "DESC");

    public List<Coupon> adminSearch(
            String couponCode,
            String discountType,
            String publicYn,
            Boolean isActive,
            String sortBy,
            String sortDir,
            int page,
            int size) {

        if (sortBy != null && !VALID_SORT_BY.contains(sortBy)) {
            throw new BadRequestException("Invalid sortBy value. Allowed: " + VALID_SORT_BY);
        }
        if (sortDir != null && !VALID_SORT_DIR.contains(sortDir.toUpperCase())) {
            throw new BadRequestException("Invalid sortDir value. Allowed: ASC, DESC");
        }

        String resolvedSortBy  = sortBy  != null ? sortBy               : "createdAt";
        String resolvedSortDir = sortDir != null ? sortDir.toUpperCase() : "DESC";
        int offset = (page - 1) * size;

        Integer discountTypeOrdinal = null;
        if (discountType != null) {
            discountTypeOrdinal = Coupon.DiscountType.valueOf(discountType.toUpperCase()).ordinal();
        }

        return couponRepository.adminSearch(
                couponCode,
                discountTypeOrdinal,
                publicYn,
                isActive,
                resolvedSortBy,
                resolvedSortDir,
                size,
                offset);
    }

    public List<Coupon> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return couponRepository.findByPublicYnAndCurrentQuantityGreaterThanAndExpiredAtAfter(
                "Y", 0L, LocalDateTime.now(), pageable);
    }

    public Coupon getById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found with id: " + id));
    }

    public Coupon create(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public void deleteById(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new NotFoundException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

    public Coupon update(Long id, Coupon updatedCoupon) {
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found with id: " + id));

        existing.setCouponCode(updatedCoupon.getCouponCode());
        existing.setTotalQuantity(updatedCoupon.getTotalQuantity());
        existing.setCurrentQuantity(updatedCoupon.getCurrentQuantity());
        existing.setDiscountType(updatedCoupon.getDiscountType());
        existing.setDiscountValue(updatedCoupon.getDiscountValue());
        existing.setMaxDiscountAmount(updatedCoupon.getMaxDiscountAmount());
        existing.setMinOrderValue(updatedCoupon.getMinOrderValue());
        existing.setExpiredAt(updatedCoupon.getExpiredAt());
        existing.setPublicYn(updatedCoupon.getPublicYn());

        return couponRepository.save(existing);
    }
}