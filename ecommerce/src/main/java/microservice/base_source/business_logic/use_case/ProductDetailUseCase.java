package microservice.base_source.business_logic.use_case;

import java.math.BigDecimal;
import java.util.List;

import microservice.base_source.data_access.dto.DetailGeneralDTO;
import microservice.base_source.data_access.entity.ProductDetail;

public interface ProductDetailUseCase {

	/**
	 * Search products detail with pagination and optional filters.
	 * @param categoryId
	 * @param searchString
	 * @param page
	 * @param size
	 * @return List of ProductDetail container ProductGeneral data
	 */
	List<DetailGeneralDTO> search(Long categoryId, String searchString, Integer page, Integer size);

	/**
	 * Search products detail with advanced options: filter, sort and pagination
	 * @param categoryId filter
	 * @param productGeneralId filter
	 * @param searchString search detail description: 'ACTIVE', DELETED. OUT_OF_STOCK
	 * @param status filter
	 * @param minPrice BigDecimal filter 
	 * @param maxPrice BigDecimal filter 
	 * @param minRating BigDecimal filter 
	 * @param maxRating BigDecimal filter 
	 * @param tags string[] filter 
	 * @param createdTime sort: ASC/DESC 
	 * @param page
	 * @param size
	 * @return List DetailGeneralDTO container ProductGeneral data
	 */
	List<DetailGeneralDTO> aggregatedSearch(
		Long categoryId,
		Long productGeneralId,
		String searchString,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		BigDecimal minRating,
		BigDecimal maxRating,
		String[] tags,
		String createdSortOption, // ASC/DESC
		String ratingSortOption, // ASC/DESC
		Integer page,
		Integer size
	);

	/**
	 * Count search products detail with advanced options: filter, sort and pagination
	 * @param categoryId filter
	 * @param productGeneralId filter
	 * @param searchString search detail description: 'ACTIVE', DELETED. OUT_OF_STOCK
	 * @param status filter
	 * @param minPrice BigDecimal filter 
	 * @param maxPrice BigDecimal filter 
	 * @param minRating BigDecimal filter 
	 * @param maxRating BigDecimal filter 
	 * @param tags string[] filter 
	 * @param createdTime sort: ASC/DESC 
	 * @param page
	 * @param size
	 * @return total count of matching records
	 */
	Long countAggregatedSearch(Long categoryId, Long productGeneralId, String searchString, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating, BigDecimal maxRating, String[] tags
	);

	List<ProductDetail> getAll(Integer page, Integer size);
	ProductDetail get(Long id);
	ProductDetail create(ProductDetail productDetail);
	ProductDetail update(Long id, ProductDetail productDetail);
	void delete(Long id);
}
