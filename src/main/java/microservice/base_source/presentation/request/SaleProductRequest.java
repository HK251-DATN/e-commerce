package microservice.base_source.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.SaleProduct;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleProductRequest {
    @NotNull
    private Long saleEventId;

    @NotBlank
    private String batchId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer disVal;

    @NotNull
    @Min(1)
    private Long maxQty;

    private Long maxBuy;

    public SaleProduct toEntity() {
        SaleProduct sp = new SaleProduct();
        sp.setSaleEventId(this.saleEventId);
        sp.setBatchId(this.batchId);
        sp.setDisVal(this.disVal);
        sp.setMaxQty(this.maxQty);
        sp.setCurQty(this.maxQty); // initialize available qty = max qty
        sp.setMaxBuy(this.maxBuy);
        return sp;
    }
}
