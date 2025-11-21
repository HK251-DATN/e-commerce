package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.data_access.entity.ProductGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductGeneralRequest {
	@NotBlank
    @Size(max = 250)
    private String productName;

    private String description;

    private String status;

    /**
     * Nếu lưu dưới dạng JSON string (mảng url), giữ type String.
     * Nếu dùng List<String> trong entity, đổi tương ứng.
     */
    private String photoUrls;

    public ProductGeneral toEntity() {
        ProductGeneral p = new ProductGeneral();
        p.setProductName(this.productName);
        p.setDescription(this.description);
        p.setStatus(this.status);
        p.setPhotoUrls(this.photoUrls);
        return p;
    }
}
