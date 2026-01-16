package microservice.base_source.business_logic.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import microservice.base_source.data_access.dto.FeedBackDTO;
import microservice.base_source.data_access.entity.FeedBack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import microservice.base_source.business_logic.use_case.FeedBackUseCase;
import microservice.base_source.data_access.repository.FeedBackRepository;
import microservice.base_source.presentation.exception.type.NotFoundException;

import org.springframework.beans.BeanUtils;

@Service
public class FeedBackService implements FeedBackUseCase {
	@Autowired
	private FeedBackRepository feedBackRepository;

	@Override
	public List<FeedBack> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<FeedBack> feedbackPage = feedBackRepository.findAll(pageable);
		return feedbackPage.getContent();
	}

	@Override
	public FeedBack get(Long id) {
		return feedBackRepository.findById(id).orElseThrow(() -> new NotFoundException("Feedback not found"));
	}

	@Override
	public List<FeedBackDTO> getByBatchId(Long batchId, Integer page, Integer size) {
		return feedBackRepository.getByBatchId(batchId, page, size);
	}

	@Override
	public FeedBack create(FeedBack feedback) {
		return feedBackRepository.save(feedback);
	}

	@Override
	public FeedBack update(Long id, FeedBack feedback) {
		FeedBack existingfeedback = feedBackRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Feedback not found"));
		// Update all fields 
		BeanUtils.copyProperties(feedback, existingfeedback, "id");
		return feedBackRepository.save(existingfeedback);
	}

	@Override
	public void delete(Long id) {
		feedBackRepository.findById(id)
			.ifPresentOrElse(
				feedBackRepository::delete,
				() -> {}	
			);
	}
}