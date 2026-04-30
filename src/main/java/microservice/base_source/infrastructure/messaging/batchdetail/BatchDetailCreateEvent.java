package microservice.base_source.infrastructure.messaging.batchdetail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import microservice.base_source.domain.entity.BatchDetail;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BatchDetailCreateEvent {

    private String batchDetailId;

    private Long productGeneralId;

    private Long quantity;

    private Long price;

    private Long avgRate;

    private Long numRate;

    private String detailContent;

    private Long subBatchId;

    private String verificationType;

    private String certificateType;

    private Long providerId;

    private String logoUrl;

    public BatchDetail toBatchDetailEntity() {
        BatchDetail batchDetail = new BatchDetail();

        batchDetail.setBatchDetailId(String.valueOf(batchDetailId));
        batchDetail.setProductGeneralId(productGeneralId);
        batchDetail.setQuantity(quantity.intValue());
        batchDetail.setPrice(BigDecimal.valueOf(price));
        batchDetail.setAvgRate(BigDecimal.valueOf(avgRate));
        batchDetail.setNumRate(numRate.intValue());
        batchDetail.setDetailContent(String.valueOf(detailContent));
        batchDetail.setSubBatchId(subBatchId);
        batchDetail.setVerificationType(verificationType);
        batchDetail.setCertificateType(certificateType);
        batchDetail.setProviderId(providerId);
        batchDetail.setLogoUrl(logoUrl);

        return batchDetail;
    }
}
