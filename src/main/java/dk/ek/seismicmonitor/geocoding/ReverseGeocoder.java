package dk.ek.seismicmonitor.geocoding;

import java.util.Optional;

public interface ReverseGeocoder {

    Optional<String> lookupArea(double latitude, double longitude);
}
