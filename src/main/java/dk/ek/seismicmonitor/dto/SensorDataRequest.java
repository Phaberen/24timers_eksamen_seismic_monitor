package dk.ek.seismicmonitor.dto;

import java.time.LocalDateTime;

// DTO (Data Transfer Object) — the shape of ONE incoming measurement in the JSON request body.
// A "record" is a short way to declare an immutable data holder: Java writes the constructor,
// getters (called readingId(), sensorId(), ...), equals, and toString for us.
// All fields are object types (Double, not double) so a missing JSON value becomes null instead
// of crashing — which lets isValid() below decide what to do about it.
public record SensorDataRequest(
        String readingId,
        String sensorId,
        SensorLocationDto sensorLocation,
        Double estimatedDistanceToEpicenterKm,
        Double estimatedMagnitude,
        LocalDateTime recordedAt
) {
    // The business rule for "is this measurement usable?": location present, distance and
    // magnitude both above zero, and a timestamp present. Only valid ones can form an alert.
    public boolean isValid() {
        return sensorLocation != null
                && sensorLocation.latitude() != null
                && sensorLocation.longitude() != null
                && estimatedDistanceToEpicenterKm != null && estimatedDistanceToEpicenterKm > 0
                && estimatedMagnitude != null && estimatedMagnitude > 0
                && recordedAt != null;
    }
}
