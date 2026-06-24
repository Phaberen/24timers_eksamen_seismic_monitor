package dk.ek.seismicmonitor.geocoding;

import java.util.Optional;

// GEOCODING LAYER — the contract for turning coordinates into a place name.
// An interface so the map-data provider can be swapped without changing AlertService (graded).
public interface ReverseGeocoder {

    // Given a point, return its area name, or an empty Optional if it can't be worked out.
    Optional<String> lookupArea(double latitude, double longitude);
}
