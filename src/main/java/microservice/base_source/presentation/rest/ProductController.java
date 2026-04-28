package microservice.base_source.presentation.rest;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.service.ProductInfoService;
import microservice.base_source.presentation.response.global.ApiResponse;
import microservice.base_source.presentation.response.product.ProductDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductInfoService productInfoService;

    /**
     * GET /api/products/{batchDetailId}
     *
     * Returns full product info: ProductGeneral fields, BatchDetail fields
     * (including detailContent), active sale pricing, and proof images
     * fetched from the product-storage service.
     */
    @GetMapping("/{batchDetailId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @PathVariable String batchDetailId) {
        try {
            ProductDetailResponse response = productInfoService.getByBatchDetailId(batchDetailId);
            return ResponseEntity.ok(ApiResponse.SUCCESS(
                    HttpStatus.OK.toString(),
                    "Product detail retrieved successfully",
                    response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(
                            HttpStatus.NOT_FOUND.toString(),
                            e.getMessage(),
                            null
                    ));
        }
    }
}
