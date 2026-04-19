package microservice.base_source.persistence.repository;

import microservice.base_source.domain.entity.ProcessedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedTransactionRepository extends JpaRepository<ProcessedTransaction, Long> {
    
    boolean existsByTransactionId(String transactionId);
}
