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
import microservice.base_source.business_logic.service.ProductGeneralService;
import microservice.base_source.data_access.entity.ProductGeneral;
import microservice.base_source.presentation.request.ProductGeneralRequest;

@RestController
@RequestMapping("/api/product-generals")
@RequiredArgsConstructor
public class ProductGeneralController {
    @Autowired
	private final ProductGeneralService productGeneralService;

    @PostMapping
    public ResponseEntity<ProductGeneral> create(@Valid @RequestBody ProductGeneralRequest req) {
        ProductGeneral created = productGeneralService.create(req.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductGeneral> getById(@PathVariable Long id) {
        ProductGeneral p = productGeneralService.get(id);
        return ResponseEntity.ok(p);
    }

    @GetMapping
    public ResponseEntity<List<ProductGeneral>> getAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        if (productGeneralService.getAll(page, size).isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productGeneralService.getAll(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductGeneral> update(@PathVariable Long id, @Valid @RequestBody ProductGeneralRequest req) {
        ProductGeneral toUpdate = req.toEntity();
        ProductGeneral updated = productGeneralService.update(id, toUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productGeneralService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
