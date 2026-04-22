package microservice.base_source.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.Coordinate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;

    @Value("${goong.api-key}")
    private String apiKey;

    private static final String GOONG_GEOCODING_URL = "https://rsapi.goong.io/geocode";

    public Coordinate getCoordinates(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GOONG_GEOCODING_URL)
                    .queryParam("address", address)
                    .queryParam("api_key", apiKey)
                    .build(false)
                    .toUriString();

            log.info("url: " + url);

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("results") || response.get("results").isEmpty()) {
                throw new RuntimeException("No coordinates found for: " + address);
            }

            log.info(response.asText());

            JsonNode location = response.get("results").get(0).get("geometry").get("location");
            double lat = location.get("lat").asDouble();
            double lng = location.get("lng").asDouble();

            return new Coordinate(lat, lng);

        } catch (Exception e) {
            throw new RuntimeException("Geocoding failed for address: " + address, e);
        }
    }
}
