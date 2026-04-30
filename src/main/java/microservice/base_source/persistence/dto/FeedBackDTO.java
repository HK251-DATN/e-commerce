package microservice.base_source.persistence.dto;

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
    String getContent();
    String getImg();
    String getDetail();
    LocalDateTime getCreatedAt();
}