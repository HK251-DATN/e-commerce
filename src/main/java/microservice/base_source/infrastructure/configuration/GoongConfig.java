package microservice.base_source.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GoongConfig {

    @Value("${goong.api-key}")
    private String apiKey;

    @Bean
    public String goongApiKey() {
        return apiKey;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}