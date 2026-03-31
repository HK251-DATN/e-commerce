package microservice.base_source.presentation.response.searchProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Getter
@Setter
public class ProductSearchResponse {
	private Long   	      productGeneralId;
    private Long   	      categoryId;
    private String 	      providerId;
    private String 	      name;
    private String 	      description;
    private String 	      img;

    List<DetailResponse> listDetails;

    public static List<ProductSearchResponse> toResponse(List<DetailGeneralDTO> list) {

        // Group theo productGeneralId
        Map<Long, List<DetailGeneralDTO>> grouped =
                list.stream().collect(Collectors.groupingBy(DetailGeneralDTO::getProductGeneralId));

        // Chuyển đổi từng nhóm thành ProductSearchResponse
        return grouped.entrySet().stream().map(entry -> {
            Long productGeneralId = entry.getKey();             // get key of each group
            List<DetailGeneralDTO> details = entry.getValue();  // get list detail of each group

            DetailGeneralDTO first = details.get(0);     // Lấy một phần tử để lấy thông tin chung

            ProductSearchResponse response = new ProductSearchResponse();
            response.setProductGeneralId(productGeneralId);
            response.setCategoryId(first.getCategoryId());
            response.setProviderId(first.getProviderId());
            response.setName(first.getName());
            response.setDescription(first.getDescription());
            response.setImg(first.getImg());

            List<DetailResponse> detailResponses = details.stream().map(detail -> {
                DetailResponse detailResponse = new DetailResponse();
                detailResponse.setBatchId(detail.getBatchId());
                detailResponse.setQuantity(detail.getQuantity());
                detailResponse.setOriginPrice(detail.getOriginPrice());
                detailResponse.setDisVal(detail.getDisVal());
                detailResponse.setAvgRate(detail.getAvgRate());
                detailResponse.setNumRate(detail.getNumRate());
                detailResponse.setSaleEventId(detail.getSaleEventId());
                detailResponse.setCreatedAt(detail.getCreatedAt());

                // Tính salePrice
                BigDecimal salePrice = calculateSalePrice(detail.getOriginPrice(), detail.getDisVal());
                detailResponse.setSalePrice(salePrice);

                return detailResponse;
            }).collect(Collectors.toList());

            response.setListDetails(detailResponses);
            return response;
        }).collect(Collectors.toList());
        
    }

    public static BigDecimal calculateSalePrice(BigDecimal originPrice, BigDecimal disVal) {
        if (disVal == null || disVal.compareTo(BigDecimal.ZERO) == 0) {
            return originPrice;
        }
        else {
            return originPrice.multiply(disVal).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }
}
