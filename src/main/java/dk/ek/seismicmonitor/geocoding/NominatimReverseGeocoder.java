package dk.ek.seismicmonitor.geocoding;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

// GEOCODING LAYER — the real ReverseGeocoder, calling OpenStreetMap's free Nominatim service.
// @Profile("!test") means this bean is used everywhere EXCEPT tests, where a fake offline version
// is swapped in instead — which also proves the provider is swappable.
@Component
@Profile("!test")
public class NominatimReverseGeocoder implements ReverseGeocoder {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";

    private static final int MAX_AREA_LENGTH = 255; // keep the name short enough for the DB column

    private final RestClient restClient; // Spring's HTTP client for calling other web services

    public NominatimReverseGeocoder() {
        // Build the HTTP client once. Nominatim's usage policy asks for a descriptive User-Agent.
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", "SeismicMonitor/1.0 (exam project)")
                .build();
    }

    @Override
    public Optional<String> lookupArea(double latitude, double longitude) {
        try {
            // Call GET /reverse?format=json&lat=..&lon=.. and let Spring parse the JSON answer
            // into our NominatimResponse record below.
            NominatimResponse response = restClient.get()
                    .uri(uri -> uri.path("/reverse")
                            .queryParam("format", "json")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .build())
                    .retrieve()
                    .body(NominatimResponse.class);
            return Optional.ofNullable(response)
                    .map(NominatimResponse::toArea)
                    .map(NominatimReverseGeocoder::capLength);
        } catch (Exception lookupFailed) {
            // Best-effort: if anything goes wrong (network down, bad response), just return empty.
            // The alert is still created, only without an area name.
            return Optional.empty();
        }
    }

    // Trim an over-long name so it can't break the insert into the VARCHAR(255) "area" column.
    private static String capLength(String area) {
        return area.length() > MAX_AREA_LENGTH ? area.substring(0, MAX_AREA_LENGTH) : area;
    }

    // Mirrors the part of Nominatim's JSON we care about. @JsonProperty maps the JSON field
    // "display_name" onto our differently-named field.
    private record NominatimResponse(
            @JsonProperty("display_name") String displayName,
            Address address) {

        // Prefer a specific place from the address; otherwise fall back to the long display name.
        String toArea() {
            if (address != null && address.bestArea() != null) {
                return address.bestArea();
            }
            return displayName;
        }
    }

    // The "address" sub-object. We pick the most specific name available, smallest area first.
    private record Address(String city, String town, String village, String state, String country) {
        String bestArea() {
            if (city != null) return city;
            if (town != null) return town;
            if (village != null) return village;
            if (state != null) return state;
            return country;
        }
    }
}
