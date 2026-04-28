package microservice.base_source.persistence.dto;

import java.math.BigDecimal;

public interface SaleProductDetailDTO {
    String getBatchId();
    Long getProductGeneralId();
    String getName();
    String getDescription();
    String getImg();
    BigDecimal getOriginPrice();
    Integer getSalePrice();
    Integer getDisVal();
    Long getMaxQty();
    Long getCurQty();
    Long getMaxBuy();
}
