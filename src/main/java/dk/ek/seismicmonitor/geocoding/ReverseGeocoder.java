package dk.ek.seismicmonitor.geocoding;

import java.util.Optional;

public interface ReverseGeocoder {

    // Resolves a human-readable area (city, region or country) for the given
    // coordinates. Returns empty when no area could be determined.
    Optional<String> lookupArea(double latitude, double longitude);
}
