package microservice.base_source.presentation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.service.CartItemService;
import microservice.base_source.presentation.request.CartItemRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/cart-item")
@RequiredArgsConstructor
public class CartItemController {
	@Autowired
	private final CartItemService cartItemService;

    @PostMapping
    public ApiResponse<CartItem> create(@Valid @RequestBody CartItemRequest req) {
        CartItem created = cartItemService.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create CartItem success", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<CartItem> getById(@PathVariable Long id) {
        CartItem p = cartItemService.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get CartItem success", p);
    }

    @PutMapping("/{id}")
    public ApiResponse<CartItem> update(@Valid @RequestBody CartItemRequest req, @PathVariable Long id) {
        CartItem toUpdate = req.toEntity();
        CartItem updated = cartItemService.update(id, toUpdate);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cartItemService.delete(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
    }
}
