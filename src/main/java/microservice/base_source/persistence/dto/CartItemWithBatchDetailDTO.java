package microservice.base_source.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CartItemWithBatchDetailDTO {
    
    // CartItem fields
    Long getCartItemId();
    Long getCartId();
    String getBatchDetailId();
    Long getQuantity();
    Boolean getIsSelected();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    
    // BatchDetail fields
    String getProductName();
    BigDecimal getUnitPrice();
    Integer getBatchQuantity();

    // ProductGeneral fields
    String getProductImg();
    String getProductDescription();
    String getUnit();
    Long getUnitQuantity();

    // Sale info (null when product is not in an active sale)
    Long getSaleEventId();
    Integer getSalePrice();
    Integer getDisVal();
}