package dk.ek.seismicmonitor.geocoding;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

// Resolves an area via OpenStreetMap's Nominatim reverse-geocoding API.
// Disabled under the "test" profile so unit tests make no network calls.
@Component
@Profile("!test")
public class NominatimReverseGeocoder implements ReverseGeocoder {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";

    private final RestClient restClient;

    public NominatimReverseGeocoder() {
        // Nominatim's usage policy requires a descriptive User-Agent.
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", "SeismicMonitor/1.0 (exam project)")
                .build();
    }

    @Override
    public Optional<String> lookupArea(double latitude, double longitude) {
        try {
            NominatimResponse response = restClient.get()
                    .uri(uri -> uri.path("/reverse")
                            .queryParam("format", "json")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .build())
                    .retrieve()
                    .body(NominatimResponse.class);
            return Optional.ofNullable(response).map(NominatimResponse::toArea);
        } catch (Exception lookupFailed) {
            // Best-effort: a geocoding failure must never block alert creation.
            return Optional.empty();
        }
    }

    // The slice of Nominatim's JSON we care about; unknown fields are ignored.
    private record NominatimResponse(
            @JsonProperty("display_name") String displayName,
            Address address) {

        String toArea() {
            if (address != null && address.bestArea() != null) {
                return address.bestArea();
            }
            return displayName;
        }
    }

    private record Address(String city, String town, String village, String state, String country) {
        // Most specific available area, falling back to broader ones.
        String bestArea() {
            if (city != null) return city;
            if (town != null) return town;
            if (village != null) return village;
            if (state != null) return state;
            return country;
        }
    }
}