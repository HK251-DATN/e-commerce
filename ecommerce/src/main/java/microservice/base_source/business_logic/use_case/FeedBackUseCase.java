package microservice.base_source.business_logic.use_case;

import java.util.List;

import microservice.base_source.data_access.entity.Feedback;

public interface FeedBackUseCase {
	/**
	 * Get list feedback by batchId
	 * @param page
	 * @param size
	 * @return List feedback
	 */
	List<Feedback> getByBatchId(Integer page, Integer size);

	/**
	 * Lấy danh sách feedback
	 * @param page
	 * @param size
	 * @return List feedback
	 */
	List<Feedback> getAll(Integer page, Integer size);
	Feedback get(Long id);
	Feedback create(Feedback feedback);
	Feedback update(Long id, Feedback feedback);
	void delete(Long id);
}
