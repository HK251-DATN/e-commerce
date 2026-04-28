package microservice.base_source.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BuyerSaleProductId.class)
@Table(name = "BUYER_SALE_PRODUCT")
public class BuyerSaleProduct {

    @Id
    @Column(name = "batch_detail_id", nullable = false)
    private String batchDetailId;

    @Id
    @Column(name = "buyer_id", nullable = false)
    private String buyerId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
