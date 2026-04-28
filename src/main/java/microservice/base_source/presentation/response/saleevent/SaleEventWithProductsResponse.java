package microservice.base_source.presentation.response.saleevent;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class SaleEventWithProductsResponse {
    private Long saleEventId;
    private String name;
    private String description;
    private String img;
    private Long displayPriority;
    private LocalDateTime beginDate;
    private LocalDateTime endDate;
    private List<SaleProductBriefResponse> products;
}
