package microservice.base_source.data_access.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "SALE_PRODUCT")
@NoArgsConstructor
@AllArgsConstructor
public class SaleProduct {

	@Column(name = "sale_event_id")
    private Long saleEventId;

	@Column(name = "product_detail_id")
	private Long productDetailId;

	@Column(name = "max_buy_per_buyer")
	private String maxBuyPerBuyer;

	@Column(name = "discount_value")
	private String discountValue;

	@Column(name = "quantity")
	private Long quantity;

	@Column(name = "current_quantity")
	private Long currentQuantity;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

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
