package microservice.base_source.data_access.entity;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ORDER_ITEM")
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_item_id")
    private Long orderItemId;
	
	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "batch_id")
	private String batchId;

	@Column(name = "product_general_id")
	private Long productGeneralId;

	@Column(name = "product_detail_id")
	private Long productDetailId;

	@Column(name = "quantity")
	private String quantity;

	@Column(name = "original_price")
	private String originalPrice;

	@Column(name = "sale_price")
	private String salePrice;

	@Column(name = "temp_yn")
	private String tempYn;

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
