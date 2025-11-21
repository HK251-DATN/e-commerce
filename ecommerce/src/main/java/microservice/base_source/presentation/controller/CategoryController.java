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
import microservice.base_source.business_logic.service.CategoryService;
import microservice.base_source.data_access.entity.Category;
import microservice.base_source.presentation.request.CategoryRequest;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    @Autowired
	private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest req) {
        Category created = categoryService.create(req.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        Category opt = categoryService.get(id);
        return ResponseEntity.ok(opt);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        if (categoryService.getAll(page, size).isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categoryService.getAll(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        Category toUpdate = req.toEntity();
        toUpdate.setCategoryId(id);
        Category updated = categoryService.update(id, toUpdate);
		return ResponseEntity.ok(updated);
	}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
		categoryService.delete(id);
		return ResponseEntity.ok().build();
	}
}
