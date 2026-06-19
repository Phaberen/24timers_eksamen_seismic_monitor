package dk.ek.seismicmonitor.geocoding;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("test")
public class NoopReverseGeocoder implements ReverseGeocoder {

    @Override
    public Optional<String> lookupArea(double latitude, double longitude) {
        return Optional.empty();
    }
}
