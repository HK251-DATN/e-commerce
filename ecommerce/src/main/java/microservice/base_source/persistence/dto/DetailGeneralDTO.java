package microservice.base_source.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DetailGeneralDTO {
	// general info
	Long   		  getProductGeneralId();
	Long   		  getCategoryId();
	String 		  getProviderId();
	String 		  getName();
	String 		  getDescription();
	String 		  getImg();

	// batch detail info
	Long 		  getBatchId();
	Integer 	  getQuantity();  // status AVAILABLE, OUT_OF_STOCK when quantity = 0
	BigDecimal 	  getOriginPrice();
	BigDecimal 	  getSalePrice(); // when sale event, default: 0
	BigDecimal 	  getAvgRate();   // default: 0 
	int 		  getNumRate();	  // default: 0
	LocalDateTime getCreatedAt(); // sort ASC/DESC
}
