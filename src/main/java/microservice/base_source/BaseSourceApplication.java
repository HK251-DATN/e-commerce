package microservice.base_source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BaseSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaseSourceApplication.class, args);
	}

}
