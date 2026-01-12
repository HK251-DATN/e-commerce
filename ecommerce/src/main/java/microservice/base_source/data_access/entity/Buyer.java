package microservice.base_source.data_access.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
// import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

// import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "BUYER")
public class Buyer {
	@Id
	@Column(name = "buyer_id", nullable = false)
	private String buyerId;

	@Column(name = "name")
	private String name;

	@Column(name = "phone")
	private String phone;

	@Column(name = "alias_nm")
	private String aliasNm;

	@Column(name = "avatar")
	private String avatar;

	@Column(name = "sex")
	private String sex;

	@Column(name = "back_ground")
	private String backGround;

	@Column(name = "email")
	private String email;
	
	@Column(name = "active_yn")
	private String activeYn; // Y or N
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "list_address", columnDefinition = "jsonb")
	private List<Address> listAddress;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "detail", columnDefinition = "jsonb")
	private Object detail;
	
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
