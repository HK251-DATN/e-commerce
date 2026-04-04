package microservice.base_source.persistence.dto;

import java.time.LocalDateTime;
import java.util.Map;

public interface FeedBackDTO {
	// buyer short info
	String getBuyerId();
	String getName();
	String getAvatar();
	String getAliasNm();

	//product detail when hover or click
	Long   getProductDetailId();

	// feedback content
	String getContent();
	String getImg();
	Map<String, Object> getDetail();
	LocalDateTime getCreatedAt();
}
