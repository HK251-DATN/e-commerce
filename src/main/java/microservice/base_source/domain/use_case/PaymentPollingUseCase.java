package microservice.base_source.domain.use_case;

import java.util.List;

public interface PaymentPollingUseCase {
    void pollPayments();
    
    void processRow(List<Object> row);
    
    String extractOrderCode(String description);
}
