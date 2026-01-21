package microservice.base_source.presentation.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.use_case.OrderUseCase;
import microservice.base_source.presentation.request.OrderRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    @Autowired
	private OrderUseCase orderUseCase;

    @PostMapping
    public ApiResponse<Order> create(@Valid @RequestBody OrderRequest req) {
        // extract order request
        Order newOrder = req.toOrderEntity();
        List<OrderItem> listOrderItem = req.toListOrderItemEntity();

        // call api valid when have coupon (default couponId = 0)

        // check valid order internal
        orderUseCase.create(newOrder, listOrderItem);

        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success" , null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getById(@PathVariable Long id) {
        Order opt = orderUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get order success", opt);
    }

    @GetMapping
    public ApiResponse<List<Order>> getAll(
        @RequestParam(defaultValue = "") String buyerId, 
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (orderUseCase.getAll(buyerId, page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No orders found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all orders success", orderUseCase.getAll(buyerId, page, size));
    }

    @GetMapping("/search")
    public ApiResponse<List<Order>> search(
        @RequestParam(defaultValue = "") String buyerId, 
		@RequestParam(defaultValue = "") String searchString,
		@RequestParam(defaultValue = "") OrderStatus status,
		@RequestParam(defaultValue = "0") BigDecimal minPrice, 
		@RequestParam(defaultValue = "0") BigDecimal maxPrice,
		@RequestParam(defaultValue = "") LocalDateTime minTime,
		@RequestParam(defaultValue = "") LocalDateTime maxTime,
		@RequestParam(defaultValue = "") String sortByStatus,
		@RequestParam(defaultValue = "") String sortByPrice,
		@RequestParam(defaultValue = "") String sortByTime,
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (orderUseCase.search(buyerId, searchString, status, minPrice, maxPrice, minTime, maxTime, sortByStatus, sortByPrice, sortByTime, page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No orders found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search orders success", orderUseCase.search(buyerId, searchString, status, minPrice, maxPrice, minTime, maxTime, sortByStatus, sortByPrice, sortByTime, page, size));
    }

    // @PutMapping("/{id}")
    // public ApiResponse<Order> update(@PathVariable Long id, @Valid @RequestBody OrderRequest req) {
    //     Order toUpdate = req.toOrderEntity();
    //     Order updated = orderUseCase.update(id, toUpdate);
	// 	return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
	// }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
		orderUseCase.delete(id);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
	}
}
