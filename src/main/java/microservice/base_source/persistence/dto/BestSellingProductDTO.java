package microservice.base_source.persistence.dto;

public interface BestSellingProductDTO {
    Long getProductGeneralId();
    String getName();
    Long getCategoryId();
    String getImg();
    Long getTotalQuantity();
}
