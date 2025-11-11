package microservice.base_source.presentation.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("restHealthCheckController")
@RequestMapping("/api/health")
public class HealthCheckController {
	
	@GetMapping
	public String sayHello() {
		return "Xin chào! Đây là phản hồi từ REST.";
	}
}
