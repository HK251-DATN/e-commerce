package microservice.base_source.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.SaleProduct;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleProductUpdateRequest {

    @Min(0)
    @Max(100)
    private Integer disVal;

    @Min(1)
    private Long maxQty;

    private Long maxBuy;

    public SaleProduct toEntity() {
        SaleProduct sp = new SaleProduct();
        sp.setDisVal(this.disVal);
        sp.setMaxQty(this.maxQty);
        sp.setMaxBuy(this.maxBuy);
        return sp;
    }
}
