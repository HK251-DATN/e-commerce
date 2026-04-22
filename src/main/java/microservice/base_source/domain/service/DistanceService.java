package microservice.base_source.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class DistanceService {

    private final RestTemplate restTemplate;

    @Value("${goong.api-key}")
    private String apiKey;

    private static final String GOONG_DISTANCE_MATRIX_URL = "https://rsapi.goong.io/DistanceMatrix";

    public double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        try {
            String origins = lat1 + "," + lng1;
            String destinations = lat2 + "," + lng2;

            String url = UriComponentsBuilder.fromHttpUrl(GOONG_DISTANCE_MATRIX_URL)
                    .queryParam("origins", origins)
                    .queryParam("destinations", destinations)
                    .queryParam("vehicle", "bike")
                    .queryParam("api_key", apiKey)
                    .build(false)
                    .toUriString();

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("rows") || response.get("rows").isEmpty()) {
                throw new RuntimeException("Distance Matrix API returned no results");
            }

            JsonNode element = response.get("rows").get(0).get("elements").get(0);
            String status = element.get("status").asText();

            if (!"OK".equals(status)) {
                throw new RuntimeException("Distance Matrix returned status: " + status);
            }

            // Get distance in meters and convert to kilometers
            long distanceInMeters = element.get("distance").get("value").asLong();
            return distanceInMeters / 1000.0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate distance", e);
        }
    }

    public Long calculateShippingFee(double distanceKm) {
        if (distanceKm <= 2)    return 0L;
        if (distanceKm <= 5)    return 10_000L;
        if (distanceKm <= 15)   return 20_000L;
        if (distanceKm <= 30)   return 30_000L;
        return 50_000L;
    }
}
