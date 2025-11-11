package microservice.base_source.presentation.controller.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import microservice.base_source.presentation.request.UserRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	@PostMapping
	public String test(@Valid UserRequest userRequest) {
		return userRequest.toString();
	}
}
