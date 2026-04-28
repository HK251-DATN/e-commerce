package microservice.base_source.presentation.response.product;

import lombok.Builder;
import lombok.Data;
import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductDetailResponse {

    // ProductGeneral fields
    private Long productGeneralId;
    private Long categoryId;
    private String providerId;
    private String name;
    private String description;
    private String img;
    private String[] tags;
    private ProductGeneral.Unit unit;
    private Long unitQuantity;

    // BatchDetail fields
    private String batchId;
    private Integer quantity;
    private BigDecimal originPrice;
    private BigDecimal avgRate;
    private int numRate;
    private String detailContent;
    private LocalDateTime createdAt;

    // Sale info (null when not in an active sale)
    private BigDecimal salePrice;
    private BigDecimal disVal;
    private Long saleEventId;

    // Proof images from product-storage service
    private List<String> proofImages;

    public static ProductDetailResponse from(DetailGeneralDTO dto,
                                             ProductGeneral pg,
                                             BatchDetail bd,
                                             List<String> proofImages) {
        return ProductDetailResponse.builder()
                .productGeneralId(dto.getProductGeneralId())
                .categoryId(dto.getCategoryId())
                .providerId(dto.getProviderId())
                .name(dto.getName())
                .description(dto.getDescription())
                .img(dto.getImg())
                .tags(pg.getTags())
                .unit(pg.getUnit())
                .unitQuantity(pg.getUnitQuantity())
                .batchId(dto.getBatchId())
                .quantity(dto.getQuantity())
                .originPrice(dto.getOriginPrice())
                .avgRate(dto.getAvgRate())
                .numRate(dto.getNumRate())
                .detailContent(bd.getDetailContent())
                .createdAt(dto.getCreatedAt())
                .salePrice(dto.getSalePrice())
                .disVal(dto.getDisVal())
                .saleEventId(dto.getSaleEventId())
                .proofImages(proofImages)
                .build();
    }
}
