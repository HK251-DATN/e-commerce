package microservice.base_source.presentation.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestSellingProductResponse {
    private Long productGeneralId;
    private String name;
    private Long categoryId;
    private String img;
    private Long totalQuantity;
}
