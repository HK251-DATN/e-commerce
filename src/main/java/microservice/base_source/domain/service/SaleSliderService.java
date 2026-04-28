package microservice.base_source.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.SaleEvent;
import microservice.base_source.domain.use_case.SaleEventUseCase;
import microservice.base_source.persistence.dto.SaleProductDetailDTO;
import microservice.base_source.persistence.repository.SaleProductRepository;
import microservice.base_source.presentation.response.saleevent.SaleEventWithProductsResponse;
import microservice.base_source.presentation.response.saleevent.SaleProductBriefResponse;

@Service
@RequiredArgsConstructor
public class SaleSliderService {

    private final SaleEventUseCase saleEventUseCase;
    private final SaleProductRepository saleProductRepository;

    public List<SaleEventWithProductsResponse> getActiveSaleEventsWithProducts(int page, int size) {
        List<SaleEvent> activeEvents = saleEventUseCase.getActiveEvents(page, size);
        return activeEvents.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SaleEventWithProductsResponse toResponse(SaleEvent event) {
        List<SaleProductDetailDTO> products = saleProductRepository.findProductsByEventId(event.getSaleEventId());

        List<SaleProductBriefResponse> productResponses = products.stream()
                .map(p -> {
                    SaleProductBriefResponse r = new SaleProductBriefResponse();
                    r.setBatchId(p.getBatchId());
                    r.setProductGeneralId(p.getProductGeneralId());
                    r.setName(p.getName());
                    r.setDescription(p.getDescription());
                    r.setImg(p.getImg());
                    r.setOriginPrice(p.getOriginPrice());
                    r.setSalePrice(p.getSalePrice());
                    r.setDisVal(p.getDisVal());
                    r.setMaxQty(p.getMaxQty());
                    r.setCurQty(p.getCurQty());
                    r.setMaxBuy(p.getMaxBuy());
                    return r;
                })
                .collect(Collectors.toList());

        SaleEventWithProductsResponse resp = new SaleEventWithProductsResponse();
        resp.setSaleEventId(event.getSaleEventId());
        resp.setName(event.getName());
        resp.setDescription(event.getDescription());
        resp.setImg(event.getImg());
        resp.setDisplayPriority(event.getDisplayPriority());
        resp.setBeginDate(event.getBeginDate());
        resp.setEndDate(event.getEndDate());
        resp.setProducts(productResponses);
        return resp;
    }
}
