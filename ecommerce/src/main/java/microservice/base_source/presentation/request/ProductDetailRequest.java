package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.data_access.entity.ProductDetail;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRequest {
	@NotBlank
	private Long productGeneralId;

	@NotBlank
	private String batchId;

	private Object detail;

	private String img;

    private String status; // ACTIVE, DELETED

    public ProductDetail toEntity() {
        ProductDetail d = new ProductDetail();
        d.setProductGeneralId(this.productGeneralId);
		d.setBatchId(this.batchId);
		d.setDetail(this.detail);
		d.setImg(this.img);
		d.setStatus(this.status);
        return d;
    }
}
