package microservice.base_source.persistence.repository;

import org.springframework.data.domain.Pageable;
import microservice.base_source.domain.entity.Coupon;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	List<Coupon> findByPublicYn(String publicYn, Pageable pageable);
	Optional<Coupon> findByCouponCode(String couponCode);
}
