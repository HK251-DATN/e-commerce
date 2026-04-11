package microservice.base_source.presentation.request;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.ProductDetail;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRequest {
	@NotNull
	private Long productGeneralId;

	@NotNull
	private String batchId;

	private Map<String, Object> detail;

	private String img;

    private String status; // ACTIVE, DELETED

    public ProductDetail toEntity() {
        ProductDetail d = new ProductDetail();
        d.setProductGeneralId(this.getProductGeneralId());
		d.setBatchId(this.getBatchId());
		d.setDetail(this.getDetail());
		d.setImg(this.getImg());
		d.setStatus(this.getStatus());
        return d;
    }
}
