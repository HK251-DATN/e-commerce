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
