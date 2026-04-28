package microservice.base_source.presentation.rest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import microservice.base_source.domain.entity.SaleEvent;
import microservice.base_source.domain.service.SaleSliderService;
import microservice.base_source.domain.use_case.SaleEventUseCase;
import microservice.base_source.presentation.request.SaleEventRequest;
import microservice.base_source.presentation.response.global.ApiResponse;
import microservice.base_source.presentation.response.saleevent.SaleEventWithProductsResponse;

@RestController
@RequestMapping("/api/sale-events")
@RequiredArgsConstructor
public class SaleEventController {

    @Autowired
    private SaleEventUseCase saleEventUseCase;

    @Autowired
    private SaleSliderService saleSliderService;

    @PostMapping
    public ApiResponse<SaleEvent> create(@Valid @RequestBody SaleEventRequest req) {
        SaleEvent created = saleEventUseCase.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<SaleEvent> getById(@PathVariable Long id) {
        SaleEvent opt = saleEventUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get sale event success", opt);
    }

    @GetMapping
    public ApiResponse<List<SaleEvent>> getAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<SaleEvent> events = saleEventUseCase.getAll(page, size);
        if (events.isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No sale events found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all sale events success", events);
    }

    @GetMapping("/search")
    public ApiResponse<List<SaleEvent>> search(
            @RequestParam(defaultValue = "") String searchString,
            @RequestParam(defaultValue = "") String activeYn,
            @RequestParam(defaultValue = "") String enableYn,
            @RequestParam(required = false) LocalTime beginTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(required = false) LocalDateTime beginDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<SaleEvent> results = saleEventUseCase.searchEvents(
                searchString, activeYn, enableYn, beginTime, endTime, beginDate, endDate, page, size);
        if (results.isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No sale events found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search sale events success", results);
    }

    // Public endpoint — no auth required (permitted in SecurityConfig via anyRequest().permitAll())
    @GetMapping("/public/active")
    public ApiResponse<List<SaleEventWithProductsResponse>> getActiveSaleEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<SaleEventWithProductsResponse> responses =
                saleSliderService.getActiveSaleEventsWithProducts(page, size);
        if (responses.isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No active sale events", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get active sale events success", responses);
    }

    @PutMapping("/{id}")
    public ApiResponse<SaleEvent> update(@PathVariable Long id, @Valid @RequestBody SaleEventRequest req) {
        SaleEvent updated = saleEventUseCase.update(id, req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<SaleEvent> cancel(@PathVariable Long id) {
        SaleEvent cancelled = saleEventUseCase.cancel(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Sale event cancelled", cancelled);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        saleEventUseCase.delete(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
    }
}
