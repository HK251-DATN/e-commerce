package microservice.base_source.presentation.response.searchProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DetailResponse {
	private String 		  batchId;
	private Integer 	  quantity;  // status AVAILABLE, OUT_OF_STOCK when quantity = 0
	private BigDecimal 	  originPrice;
	private BigDecimal 	  salePrice;
	private BigDecimal 	  disVal;    // discount percent, null if not in sale
	private BigDecimal 	  avgRate;   // default: 0 
	private int 		  numRate;	  // default: 0
	private Long		  saleEventId;
	private LocalDateTime createdAt; // sort ASC/DESC
}
