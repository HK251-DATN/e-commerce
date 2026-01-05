package microservice.base_source.data_access.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCT_DETAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetail {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long productDetailId;

    // foreign key to ProductGeneral
	@Column(name = "product_general_id", nullable = false)
    private Long productGeneralId;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // content detail info: HTML, CSS
    
    @Column(name = "status")
    private String status; // ACTIVE, DELETED, OUT_OF_STOCK
    
    @Column(name = "quantity_available")
    private Integer quantityAvailable;
    
    @Column(name = "price", precision = 10, scale = 2) // index
    private BigDecimal price;

    @Column(name = "rating", precision = 10, scale = 2) // index
    private BigDecimal rating; // job caculate rating
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // index
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
