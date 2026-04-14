package microservice.base_source.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import microservice.base_source.domain.entity.Order.OrderStatus;
import java.time.LocalDateTime;

public interface OrderDeliveryDTO {
    @JsonProperty("order_id")
    Long getOrderId();
    
    @JsonProperty("buyer_id")
    String getBuyerId();
    
    @JsonProperty("status")
    OrderStatus getStatus();
    
    @JsonProperty("total_price")
    Long getTotalPrice();
    
    @JsonProperty("note")
    String getNote();
    
    @JsonProperty("created_at")
    LocalDateTime getCreatedAt();
    
    @JsonProperty("receiver_name")
    String getReceiverName();
    
    @JsonProperty("receiver_p_num")
    String getReceiverPNum();
    
    @JsonProperty("province")
    String getProvince();
    
    @JsonProperty("district")
    String getDistrict();
    
    @JsonProperty("commune")
    String getCommune();
    
    @JsonProperty("detail")
    String getDetail();
}
