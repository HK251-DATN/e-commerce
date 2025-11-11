package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
	@NotNull(message = "name is required")
	private String name;	
}
