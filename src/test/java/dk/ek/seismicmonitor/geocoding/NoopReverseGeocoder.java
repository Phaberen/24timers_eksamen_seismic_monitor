package dk.ek.seismicmonitor.geocoding;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Test-profile stand-in for the real geocoder: returns no area and makes no
// network call, keeping unit tests fast and offline.
@Component
@Profile("test")
public class NoopReverseGeocoder implements ReverseGeocoder {

    @Override
    public Optional<String> lookupArea(double latitude, double longitude) {
        return Optional.empty();
    }
}