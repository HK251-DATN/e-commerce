package microservice.base_source.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import microservice.base_source.business_logic.service.ProductDetailService;
import microservice.base_source.data_access.entity.ProductDetail;
import microservice.base_source.presentation.request.ProductDetailRequest;

@RestController
@RequestMapping("/api/product-details")
@RequiredArgsConstructor
public class ProductDetailController {
    @Autowired
	private final ProductDetailService productDetailService;

    @PostMapping
    public ResponseEntity<ProductDetail> create(@Valid @RequestBody ProductDetailRequest req) {
        ProductDetail created = productDetailService.create(req.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetail> getById(@PathVariable Long id) {
        ProductDetail d = productDetailService.get(id);
        return ResponseEntity.ok(d);
    }

    @GetMapping
    public ResponseEntity<List<ProductDetail>> getAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        if (productDetailService.getAll(page, size).isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productDetailService.getAll(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetail> update(@PathVariable Long id, @Valid @RequestBody ProductDetailRequest req) {
        ProductDetail toUpdate = req.toEntity();
        ProductDetail updated = productDetailService.update(id, toUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
