package dk.ek.seismicmonitor.geocoding;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Profile("!test")
public class NominatimReverseGeocoder implements ReverseGeocoder {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";

    private static final int MAX_AREA_LENGTH = 255;

    private final RestClient restClient;

    public NominatimReverseGeocoder() {
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
            return Optional.ofNullable(response)
                    .map(NominatimResponse::toArea)
                    .map(NominatimReverseGeocoder::capLength);
        } catch (Exception lookupFailed) {
            return Optional.empty();
        }
    }

    private static String capLength(String area) {
        return area.length() > MAX_AREA_LENGTH ? area.substring(0, MAX_AREA_LENGTH) : area;
    }

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
        String bestArea() {
            if (city != null) return city;
            if (town != null) return town;
            if (village != null) return village;
            if (state != null) return state;
            return country;
        }
    }
}
