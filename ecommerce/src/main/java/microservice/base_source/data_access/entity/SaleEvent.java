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
@Table(name = "SALE_EVENT")
@NoArgsConstructor
@AllArgsConstructor
public class SaleEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sale_event_id")
    private Long saleEventId;

	@Column(name = "sale_event_name")
	private String saleEventName;

	@Column(name = "description")
	private String description;

	@Column(name = "banner_img_url")
	private String bannerImgUrl;

	@Column(name = "display_priority")
	private Long displayPriority;

	@Column(name = "loop_option")
	private SaleEventStatus status; // refresh status every day in 4 a.m

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

	public enum LoopOption {
		DAY,
		WEEK,
		MONTH
	}

	public enum SaleEventStatus {
		ACTIVE,
		INACTIVE,
		DELETED,
		COMPLETED
	}
}
