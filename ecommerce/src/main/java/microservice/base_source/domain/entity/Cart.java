package microservice.base_source.domain.entity;

import java.time.LocalDateTime;
// import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
// import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
// import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CART")
public class Cart {

	@Id
	@GeneratedValue
	@Column(name = "cart_id", nullable = false)
	private UUID cartId;

	@Column(name = "buyer_id", nullable = false)
	private String buyerId;

	// @Column(name = "active_yn")
	// private String activeYn; // Y or N

	@Column(name = "total_price")
	private Double totalPrice;

	// @OneToMany(mappedBy = "cart_item", fetch = FetchType.EAGER)
	// private List<CartItem> listCartItem;

	@Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delete_at")
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
