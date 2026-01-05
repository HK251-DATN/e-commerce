package microservice.base_source.presentation.response.searchProduct;

import lombok.Data;

@Data
public class ProductSearchResponse {
	private ProductDetailResponse productDetail;
    private ProductGeneralResponse productGeneral;

    public ProductSearchResponse(
            ProductDetailResponse productDetail,
            ProductGeneralResponse productGeneral
    ) {
        this.productDetail = productDetail;
        this.productGeneral = productGeneral;
    }
}
