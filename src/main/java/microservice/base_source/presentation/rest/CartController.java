package microservice.base_source.presentation.rest;

import java.util.List;

// import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import microservice.base_source.presentation.response.coupon.CartCouponResponse;
import microservice.base_source.infrastructure.security.AuthenticatedUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.service.CartService;
import microservice.base_source.presentation.request.CartRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
	@Autowired
	private final CartService cartService;

    @PostMapping
    public ApiResponse<Cart> create(@Valid @RequestBody CartRequest req,
        @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        String buyerId = principal.getId().toString();
        req.setBuyerId(buyerId);

        Cart created = cartService.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create Cart success", created);
    }
    
    @GetMapping
    public ApiResponse<Cart> getCartById(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        String userId = principal.getId().toString();
        Cart p = cartService.getByBuyerId(userId);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get Cart success", p);
    }

    @GetMapping("/coupons")
    public ApiResponse<List<?>> getCartById(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        String userId = principal.getId().toString();
        Cart userCart = cartService.getByBuyerId(userId);
        List<CartCouponResponse> listCouponResponse = cartService.getAllCoupon(userCart.getCartId(), page, size);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get Cart success", listCouponResponse);
    }

    @GetMapping("/admin/{userId}")
    public ApiResponse<Cart> adminGetByUserId(@PathVariable String userId) {
        Cart p = cartService.getByBuyerId(userId);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get Cart success", p);
    }

    // @PutMapping("/{id}")
    // public ApiResponse<Cart> update(@Valid @RequestBody CartRequest req, @PathVariable Long id) {
    //     Cart toUpdate = req.toEntity();
    //     Cart updated = cartService.update(id, toUpdate);
    //     return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
    // }

    // @DeleteMapping("/{id}")
    // public ApiResponse<Void> delete(@PathVariable Long id) {
    //     cartService.delete(id);
    //     return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
    // }
}
