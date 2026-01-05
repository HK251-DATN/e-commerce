package microservice.base_source.presentation.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.data_access.entity.ProductDetail;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRequest {
	@Size(max = 1000)
    private String description;

    private String status;

    @Min(0)
    private Integer quantityAvailable;

    @NotNull
    private BigDecimal price;

    // id của productGeneral (nullable nếu cần)
    private Long productGeneralId;

    // id của storage tool (nullable)
    // private Long locatedInStorageTool;

    public ProductDetail toEntity() {
        ProductDetail d = new ProductDetail();
        d.setDescription(this.description);
        d.setStatus(this.status);
        d.setQuantityAvailable(this.quantityAvailable);
        d.setPrice(this.price);
        d.setProductGeneralId(this.productGeneralId);
        // d.setLocatedInStorageTool(this.locatedInStorageTool);
        return d;
    }
}
