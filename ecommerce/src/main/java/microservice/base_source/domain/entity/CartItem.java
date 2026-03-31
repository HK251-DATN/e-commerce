package microservice.base_source.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CART_ITEM")
public class CartItem {

	@Id
	@GeneratedValue
	@Column(name = "cart_item_id", nullable = false)
    private UUID cartItemId;

	@Column(name = "cart_id", nullable = false)
	private UUID cartId;

	@Column(name = "batch_detail_id", nullable = false)
	private String batchDetailId;

	@Column(name = "quantity")
	private Integer quantity;

	@Column(name = "is_selected")
	private Integer isSelected;

	@Column(name = "created_at")
	private LocalDateTime createdAt; // addedAt

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

	@PreRemove
	public void preRemove() {
		deletedAt = LocalDateTime.now();
	}
}
