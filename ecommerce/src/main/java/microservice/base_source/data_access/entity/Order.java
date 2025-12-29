package microservice.base_source.data_access.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
@Table(name = "ORDER")
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id", nullable = false)
    private Long orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status;

	@Column(name = "order_note")
	private String orderNote;

	@Column(name = "order_type")
	private String orderType;

	@Column(name = "total_price")
	private Long   totalPrice;

	@Column(name = "buyer_id")
	private String buyerId;

	@Column(name = "confirm_by_emp")
	private String confirmByEmp;

	@Column(name = "package_by_emp")
	private String packageByEmp;

	@Column(name = "ship_by_emp")
	private String shipByEmp;

	@Column(name = "ship_to_addr")
	private String shipToAddr;

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

	public enum OrderStatus {
		PENDING,
		PAID,
		CONFIRMED,
		PACKAGED,
		SHIPPED,
		COMPLETED,
		CANCELLED
	}
}
