package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.data_access.entity.OrderItem;

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
	
	private Long productDetailId;
	
	@NotBlank
	private String quantity;
	
	@NotBlank
	private String originalPrice;
	
	@NotBlank
	private String salePrice;

	public OrderItem toEntity() {
		OrderItem item = new OrderItem();
		item.setOrderId(this.orderId);
		item.setBatchId(this.batchId);
		item.setProductGeneralId(this.productGeneralId);
		item.setProductDetailId(this.productDetailId);
		item.setQuantity(this.quantity);
		item.setOriginalPrice(this.originalPrice);
		item.setSalePrice(this.salePrice);
		return item;
	}
}