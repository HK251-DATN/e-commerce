package microservice.base_source.presentation.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionResponse {
	private LocalDateTime timestamp;
	private String type;
	private String message;
	private Map<String, Object> detail;
}
