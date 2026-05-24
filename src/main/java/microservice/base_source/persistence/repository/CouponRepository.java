package microservice.base_source.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import microservice.base_source.domain.entity.Coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	List<Coupon> findByPublicYn(String publicYn, Pageable pageable);
	List<Coupon> findByPublicYnAndCurrentQuantityGreaterThanAndExpiredAtAfter(
			String publicYn, Long currentQuantity, LocalDateTime now, Pageable pageable);
	Optional<Coupon> findByCouponCode(String couponCode);

	@Query(value = """
		SELECT * FROM COUPON c
		WHERE (CAST(:couponCode AS text)   IS NULL OR c.coupon_code   ILIKE CONCAT('%', CAST(:couponCode AS text), '%'))
		  AND (CAST(:discountType AS integer) IS NULL OR c.discount_type = CAST(:discountType AS integer))
		  AND (CAST(:publicYn AS text)     IS NULL OR c.public_yn     = CAST(:publicYn AS text))
		  AND (CAST(:isActive AS boolean)  IS NULL
		       OR (CAST(:isActive AS boolean) = TRUE  AND c.current_quantity > 0 AND c.expired_at > NOW())
		       OR (CAST(:isActive AS boolean) = FALSE AND (c.current_quantity <= 0 OR c.expired_at <= NOW())))
		ORDER BY
		  CASE WHEN :sortBy = 'createdAt'     AND :sortDir = 'ASC'  THEN c.created_at     END ASC,
		  CASE WHEN :sortBy = 'createdAt'     AND :sortDir = 'DESC' THEN c.created_at     END DESC,
		  CASE WHEN :sortBy = 'expiredAt'     AND :sortDir = 'ASC'  THEN c.expired_at     END ASC,
		  CASE WHEN :sortBy = 'expiredAt'     AND :sortDir = 'DESC' THEN c.expired_at     END DESC,
		  CASE WHEN :sortBy = 'discountValue' AND :sortDir = 'ASC'  THEN c.discount_value END ASC,
		  CASE WHEN :sortBy = 'discountValue' AND :sortDir = 'DESC' THEN c.discount_value END DESC,
		  c.created_at DESC
		LIMIT :size OFFSET :offset
		""", nativeQuery = true)
	List<Coupon> adminSearch(
		@Param("couponCode")   String couponCode,
		@Param("discountType") Integer discountType,
		@Param("publicYn")     String publicYn,
		@Param("isActive")     Boolean isActive,
		@Param("sortBy")       String sortBy,
		@Param("sortDir")      String sortDir,
		@Param("size")         int size,
		@Param("offset")       int offset
	);
}
