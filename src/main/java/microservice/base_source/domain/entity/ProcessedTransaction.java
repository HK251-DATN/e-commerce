package microservice.base_source.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// entity/ProcessedTransaction.java
@Entity
@Table(name = "processed_transactions")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, name = "transaction_id")
    private String transactionId; // Mã GD từ Sepay, tránh xử lý 2 lần
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @PrePersist
    void prePersist() { this.processedAt = LocalDateTime.now(); }
}
