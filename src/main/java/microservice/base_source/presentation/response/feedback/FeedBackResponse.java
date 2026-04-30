package microservice.base_source.presentation.response.feedback;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import microservice.base_source.persistence.dto.FeedBackDTO;

@Data
public class FeedBackResponse {
	private static ObjectMapper mapper = new ObjectMapper();

	String buyerId;
	String fname;
	String lname;
	String batchDetailId;

	String content;
	String img;
	Map<String, Object> detail;
	LocalDateTime createdAt;

	public static FeedBackResponse dtoToResponse(FeedBackDTO dto) {
		FeedBackResponse response = new FeedBackResponse();
		response.setBuyerId(dto.getBuyerId());
		response.setFname(dto.getFname());
		response.setLname(dto.getLname());
		response.setBatchDetailId(dto.getBatchDetailId());
		response.setContent(dto.getContent());
		response.setImg(dto.getImg());
		response.setDetail(toMap(dto.getDetail()));
		response.setCreatedAt(dto.getCreatedAt());
		return response;
	}

	public static Map<String, Object> toMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format: " + json, e);
        }
    }
}
