package microservice.base_source.domain.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.Category;
import microservice.base_source.domain.exception.type.CategoryNotFoundException;
import microservice.base_source.domain.use_case.CategoryUseCase;
import microservice.base_source.persistence.repository.CategoryRepository;

import org.springframework.beans.BeanUtils;

@Service
public class CategoryService implements CategoryUseCase {
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<Category> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Category> categoryPage = categoryRepository.findAll(pageable);
		return categoryPage.getContent();
	}

	@Override
	public Category get(Long id) {
		return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found"));
	}

	@Override
	public Category create(Category category) {
		return categoryRepository.save(category);
	}

	@Override
	public Category update(Long id, Category category) {
		Category existingCategory = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException("Category not found"));
		// Update all fields 
		BeanUtils.copyProperties(category, existingCategory, "id");
		return categoryRepository.save(existingCategory);
	}

	@Override
	public void delete(Long id) {
		categoryRepository.findById(id)
			.ifPresentOrElse(
				categoryRepository::delete,
				() -> {}	
			);
	}

	@Override
	public List<Category> search(String searchName, Integer page, Integer size) {
		return categoryRepository.search(searchName, page, size);
	}
	
}
