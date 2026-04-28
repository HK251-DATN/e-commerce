package microservice.base_source.presentation.response.saleevent;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SaleProductBriefResponse {
    private String batchId;
    private Long productGeneralId;
    private String name;
    private String description;
    private String img;
    private BigDecimal originPrice;
    private Integer salePrice;
    private Integer disVal;
    private Long maxQty;
    private Long curQty;
    private Long maxBuy;
}
