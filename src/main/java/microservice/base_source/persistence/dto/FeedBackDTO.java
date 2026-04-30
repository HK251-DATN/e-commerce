package microservice.base_source.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FeedBackDTO {
    // buyer short info
    String getBuyerId();
    String getFname();
    String getLname();
    // String getAvatar();
    // String getAliasNm();

    //batch detail when hover or click
    String getBatchDetailId();

    // feedback content
    BigDecimal getRating();
    String getContent();
    String getImg();
    String getDetail();
    LocalDateTime getCreatedAt();
}