package microservice.base_source.presentation.mapping;

import microservice.base_source.presentation.request.UserRequest;
import microservice.base_source.presentation.response.UserResponse;

public interface UserMapper {
	UserResponse toUserResponse();
	UserRequest toUserRequest();
}
