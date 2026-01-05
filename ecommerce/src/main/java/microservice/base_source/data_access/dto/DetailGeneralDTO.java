package microservice.base_source.data_access.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DetailGeneralDTO {
	// detail info
	Long getProductDetailId();
	String getDescription();
	String getStatus();
	Integer getQuantityAvailable();
	BigDecimal getPrice();
	BigDecimal getRating();
	LocalDateTime getCreatedAt();
	LocalDateTime getUpdatedAt();

	// general info
	Long getProductGeneralId();
	Long getCategoryId();
	String getProductName();
	String getGeneralDescription();
	String getGeneralStatus();
	String getPhotoUrls();
}
