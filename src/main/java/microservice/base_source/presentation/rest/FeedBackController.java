package microservice.base_source.presentation.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import microservice.base_source.domain.entity.FeedBack;
import microservice.base_source.domain.use_case.FeedBackUseCase;
import microservice.base_source.infrastructure.security.AuthenticatedUser;
import microservice.base_source.persistence.dto.FeedBackDTO;
import microservice.base_source.presentation.request.FeedBackRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/feed-backs")
@RequiredArgsConstructor
public class FeedBackController {
    @Autowired
	private FeedBackUseCase feedBackUseCase;

    @PostMapping
    public ApiResponse<FeedBack> create(
            @Valid @RequestBody FeedBackRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        // Prevent buyerId spoofing - use authenticated user's ID
        String buyerId = principal.getId().toString();
        req.setBuyerId(buyerId);

        FeedBack created = feedBackUseCase.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success" , created);
    }

    @GetMapping("/{id}")
    public ApiResponse<FeedBack> getById(@PathVariable Long id) {
        FeedBack opt = feedBackUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get feed back success", opt);
    }

    @GetMapping
    public ApiResponse<List<FeedBack>> getAll(
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (feedBackUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No feed backs found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all feed backs success", feedBackUseCase.getAll(page, size));
    }

    @GetMapping("/batch/{batchId}")
    public ApiResponse<List<FeedBackDTO>> searchByBatch(
        @PathVariable(name = "batchId") String batchId,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size) {
        if (feedBackUseCase.getByBatchId(batchId, page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No feed backs found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search feed backs success", feedBackUseCase.getByBatchId(batchId, page, size));
    }

    @GetMapping("/buyer/{buyerId}")
    public ApiResponse<List<FeedBackDTO>> getByBuyer(
        @PathVariable(name = "buyerId") String buyerId,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size) {
        List<FeedBackDTO> feedbacks = feedBackUseCase.getByBuyerId(buyerId, page, size);
        if (feedbacks.isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No feed backs found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get buyer feed backs success", feedbacks);
    }

    @GetMapping("/product/{productGeneralId}")
    public ApiResponse<List<FeedBackDTO>> getByProduct(
        @PathVariable(name = "productGeneralId") Long productGeneralId,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size) {
        List<FeedBackDTO> feedbacks = feedBackUseCase.getByProductGeneralId(productGeneralId, page, size);
        if (feedbacks.isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No feed backs found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get product feed backs success", feedbacks);
    }

    @PutMapping("/{id}")
    public ApiResponse<FeedBack> update(
            @PathVariable Long id,
            @Valid @RequestBody FeedBackRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        // Verify ownership
        FeedBack existing = feedBackUseCase.get(id);
        String authenticatedBuyerId = principal.getId().toString();

        if (!existing.getBuyerId().equals(authenticatedBuyerId)) {
            return ApiResponse.ERROR(HttpStatus.FORBIDDEN.toString(), "You can only update your own feedback", null);
        }

        // Prevent changing buyerId
        req.setBuyerId(authenticatedBuyerId);
        FeedBack toUpdate = req.toEntity();
        FeedBack updated = feedBackUseCase.update(id, toUpdate);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
	}

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        // Verify ownership
        FeedBack existing = feedBackUseCase.get(id);
        String authenticatedBuyerId = principal.getId().toString();

        if (!existing.getBuyerId().equals(authenticatedBuyerId)) {
            return ApiResponse.ERROR(HttpStatus.FORBIDDEN.toString(), "You can only delete your own feedback", null);
        }

		feedBackUseCase.delete(id);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
	}
}
