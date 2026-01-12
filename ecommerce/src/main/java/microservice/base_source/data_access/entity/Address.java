package microservice.base_source.data_access.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
	private String addressId;
    private String description;
    private String status;
    private String type;
}
