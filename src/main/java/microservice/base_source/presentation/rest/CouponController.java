package microservice.base_source.presentation.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Coupon;
import microservice.base_source.domain.service.CouponService;
import microservice.base_source.presentation.request.CouponRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {
    @Autowired
	private final CouponService CouponService;

    @PostMapping
    public ApiResponse<Coupon> create(@Valid @RequestBody CouponRequest req) {
        Coupon created = CouponService.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create coupon detail success", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<Coupon> getById(@PathVariable Long id) {
        Coupon pd = CouponService.getById(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get coupon detail success", pd);
    }

    @GetMapping
    public ApiResponse<List<Coupon>> getAll(
		@RequestParam(defaultValue = "1") Integer page, 
		@RequestParam(defaultValue = "20") Integer size) {
        if (CouponService.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No coupon details found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all coupon details success", CouponService.getAll(page, size));
    }

    /**
     * GET {ecommerce}/api/coupon/admin
     *
     * Search and filter coupons for admin management.
     *
     * Query parameters:
     *   couponCode   (String,  optional) - partial match (case-insensitive)
     *   discountType (String,  optional) - enum name: PERCENTAGE | FIXED_AMOUNT
     *   publicYn     (String,  optional) - visibility flag: Y | N
     *   isActive     (Boolean, optional) - true = has remaining quantity and not expired; false = exhausted or expired
     *   sortBy       (String,  optional) - createdAt | expiredAt | discountValue  (default: createdAt)
     *   sortDir      (String,  optional) - ASC | DESC  (default: DESC)
     *   page         (Integer, optional) - 1-based page number  (default: 1)
     *   size         (Integer, optional) - page size            (default: 20)
     *
     * Responses:
     *   200 OK          - list of matching coupons
     *   204 No Content  - no coupons matched the filters
     *   400 Bad Request - invalid sortBy or sortDir value, or unrecognised discountType
     */
    @GetMapping("/admin")
    public ApiResponse<List<Coupon>> adminGetAll(
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) String publicYn,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(defaultValue = "1")  Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<Coupon> results = CouponService.adminSearch(
                couponCode, discountType, publicYn, isActive, sortBy, sortDir, page, size);
        if (results.isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No coupons found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all coupons success", results);
    }

    @PutMapping("/{id}")
    public ApiResponse<Coupon> update(@PathVariable Long id, @Valid @RequestBody CouponRequest req) {
        Coupon toUpdate = req.toEntity();
        Coupon updated = CouponService.update(id, toUpdate);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        CouponService.deleteById(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
    }
}
