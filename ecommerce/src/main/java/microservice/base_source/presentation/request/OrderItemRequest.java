package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
	@NotBlank
	private Long orderId;

	@NotBlank
	private String batchId;
	
	@NotBlank
	private Long productGeneralId;
	
	@NotBlank
	private Long productDetailId;
	
	@NotBlank
	private String quantity;
	
	@NotBlank
	private String originalPrice;
	
	@NotBlank
	private String salePrice;

	@NotBlank
	private String type; // DEFAULT, PREORDER, CART
}