package microservice.base_source.presentation.response.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentFeeResponse {
    private Long addressId;
    private String receiverName;
    private String fullAddress;
    private Double distanceKm;
    private Long shipmentFee;
}
