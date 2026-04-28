package microservice.base_source.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.persistence.dto.DetailGeneralDTO;
import microservice.base_source.persistence.repository.BatchDetailRepository;
import microservice.base_source.persistence.repository.ProductGeneralRepository;
import microservice.base_source.presentation.response.product.ProductDetailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInfoService {

    private final ProductGeneralRepository productGeneralRepository;
    private final BatchDetailRepository batchDetailRepository;
    private final RestTemplate restTemplate;

    @Value("${services.product-storage.url}")
    private String productStorageUrl;

    public ProductDetailResponse getByBatchDetailId(String batchDetailId) {
        DetailGeneralDTO dto = productGeneralRepository.findByBatchDetailId(batchDetailId)
                .orElseThrow(() -> new NotFoundException("Product not found for batch: " + batchDetailId));

        ProductGeneral pg = productGeneralRepository.findById(dto.getProductGeneralId())
                .orElseThrow(() -> new NotFoundException("ProductGeneral not found: " + dto.getProductGeneralId()));

        BatchDetail bd = batchDetailRepository.findById(batchDetailId)
                .orElseThrow(() -> new NotFoundException("BatchDetail not found: " + batchDetailId));

        List<String> proofImages = fetchProofImages(batchDetailId);

        return ProductDetailResponse.from(dto, pg, bd, proofImages);
    }

    private List<String> fetchProofImages(String batchDetailId) {
        try {
            String url = productStorageUrl + "/api/product-batch/" + batchDetailId + "/proof-images";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("detail")) {
                return List.of();
            }

            JsonNode detail = response.get("detail");
            List<String> images = new ArrayList<>();
            if (detail.isArray()) {
                detail.forEach(node -> images.add(node.asText()));
            }
            return images;
        } catch (Exception e) {
            log.warn("Failed to fetch proof images for batch {}: {}", batchDetailId, e.getMessage());
            return List.of();
        }
    }
}
