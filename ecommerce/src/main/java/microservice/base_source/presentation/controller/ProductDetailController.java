// package microservice.base_source.presentation.controller;

// import java.math.BigDecimal;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import microservice.base_source.business_logic.use_case.ProductDetailUseCase;
// import microservice.base_source.data_access.dto.DetailGeneralDTO;
// import microservice.base_source.data_access.entity.ProductDetail;
// import microservice.base_source.presentation.mapping.ProductMapMapper;
// import microservice.base_source.presentation.request.ProductDetailRequest;
// import microservice.base_source.presentation.response.global.ApiResponse;
// import microservice.base_source.presentation.response.searchProduct.ProductSearchResponse;

// @RestController
// @RequestMapping("/api/product-details")
// @RequiredArgsConstructor
// public class ProductDetailController {
//     @Autowired
// 	private final ProductDetailUseCase productDetailUseCase;

//     @PostMapping
//     public ApiResponse<ProductDetail> create(@Valid @RequestBody ProductDetailRequest req) {
//         ProductDetail created = productDetailUseCase.create(req.toEntity());
//         return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create product detail success", created);
//     }

//     @GetMapping("/{id}")
//     public ApiResponse<ProductDetail> getById(@PathVariable Long id) {
//         ProductDetail pd = productDetailUseCase.get(id);
//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get product detail success", pd);
//     }

//     @GetMapping
//     public ApiResponse<List<ProductDetail>> getAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
//         if (productDetailUseCase.getAll(page, size).isEmpty()) {
//             return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No product details found", null);
//         }
//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all product details success", productDetailUseCase.getAll(page, size));
//     }

//     @GetMapping("/search")
//     public ApiResponse<List<ProductSearchResponse>> search(
//         @RequestParam(defaultValue = "0") Long categoryId,
//         @RequestParam(defaultValue = "") String searchString,
//         @RequestParam(defaultValue = "1") Integer page,
//         @RequestParam(defaultValue = "20") Integer size) {
        
//         List<DetailGeneralDTO> results = productDetailUseCase.search(categoryId, searchString, page, size);
//         if (results.isEmpty()) {
//             return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No matching product details found", null);
//         }

//         List<ProductSearchResponse> response = results.stream()
//             .map(ProductMapMapper::toFOSearchResult)
//             .toList();

//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search product details success", response);
//     }

//     @GetMapping("/aggregated-search")
//     public ApiResponse<List<ProductSearchResponse>> aggregatedSearch(
//         @RequestParam(defaultValue = "0") Long categoryId, 
//         @RequestParam(defaultValue = "0") Long productGeneralId, 
//         @RequestParam(defaultValue = "") String searchString,
// 		@RequestParam(defaultValue = "0") BigDecimal minPrice, 
//         @RequestParam(defaultValue = "0") BigDecimal maxPrice, 
//         @RequestParam(defaultValue = "0") BigDecimal minRating, 
//         @RequestParam(defaultValue = "0") BigDecimal maxRating, 
//         @RequestParam(required = false) String[] tags,
// 		@RequestParam(defaultValue = "") String createdSortOption, 
//         @RequestParam(defaultValue = "DESC") String ratingSortOption,
//         @RequestParam(defaultValue = "1") Integer page,
//         @RequestParam(defaultValue = "20") Integer size) 
//     {
//         System.out.println("Aggregated search called with params: " +
//             "categoryId=" + categoryId +
//             ", productGeneralId=" + productGeneralId +
//             ", searchString='" + searchString + '\'' +
//             ", minPrice=" + minPrice +
//             ", maxPrice=" + maxPrice +
//             ", minRating=" + minRating +
//             ", maxRating=" + maxRating +
//             ", tags=" + (tags != null ? String.join(",", tags) : "null") +
//             ", createdSortOption='" + createdSortOption + '\'' +
//             ", ratingSortOption='" + ratingSortOption + '\'' +
//             ", page=" + page +
//             ", size=" + size
//         );
//         List<DetailGeneralDTO> results = productDetailUseCase.aggregatedSearch(
//             categoryId,
//             productGeneralId,
//             searchString,
//             minPrice,
//             maxPrice,
//             minRating,
//             maxRating,
//             tags,
//             createdSortOption,
//             ratingSortOption,
//             page,
//             size
//         );
//         if (results.isEmpty()) {
//             return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No matching product details found", null);
//         }

//         List<ProductSearchResponse> response = results.stream()
//             .map(ProductMapMapper::toFOSearchResult)
//             .toList();
        
//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Aggregated search product details success", response);
//     }

//     @GetMapping("/count-aggregated-search")
//     public ApiResponse<Long> countAggregatedSearch(
//         @RequestParam(defaultValue = "0") Long categoryId, 
//         @RequestParam(defaultValue = "0") Long productGeneralId, 
//         @RequestParam(defaultValue = "") String searchString,
// 		@RequestParam(defaultValue = "0") BigDecimal minPrice, 
//         @RequestParam(defaultValue = "0") BigDecimal maxPrice, 
//         @RequestParam(defaultValue = "0") BigDecimal minRating, 
//         @RequestParam(defaultValue = "0") BigDecimal maxRating, 
//         @RequestParam(required = false) String[] tags) 
//     {
//         Long totalCount = productDetailUseCase.countAggregatedSearch(
//             categoryId,
//             productGeneralId,
//             searchString,
//             minPrice,
//             maxPrice,
//             minRating,
//             maxRating,
//             tags
//         );

//         // System.out.println("Total count from countAggregatedSearch: " + totalCount);

//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Count aggregated search product details success", totalCount);
//     }

//     @PutMapping("/{id}")
//     public ApiResponse<ProductDetail> update(@PathVariable Long id, @Valid @RequestBody ProductDetailRequest req) {
//         ProductDetail toUpdate = req.toEntity();
//         ProductDetail updated = productDetailUseCase.update(id, toUpdate);
//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
//     }

//     @DeleteMapping("/{id}")
//     public ApiResponse<Void> delete(@PathVariable Long id) {
//         productDetailUseCase.delete(id);
//         return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
//     }
// }
