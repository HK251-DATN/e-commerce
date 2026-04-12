package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.persistence.repository.CouponRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> getAll(int page, int size) {
        return couponRepository.findAll().stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .toList();
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

        return couponRepository.save(existing);
    }
}