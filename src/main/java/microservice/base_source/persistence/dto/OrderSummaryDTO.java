package microservice.base_source.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface OrderSummaryDTO {
    
    @JsonProperty("order_id")
    Long getOrderId();
    
    @JsonProperty("total_quantity")
    Integer getTotalQuantity();
    
    @JsonProperty("num_of_item")
    Integer getNumOfItem();
}
