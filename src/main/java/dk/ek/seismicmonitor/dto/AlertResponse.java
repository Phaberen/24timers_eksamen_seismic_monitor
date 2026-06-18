package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.model.EarthquakeAlert;

import java.time.LocalDateTime;

// Read model for an earthquake alert. Keeps the JPA entity out of the API so the
// JSON shape is decoupled from the database mapping.
public record AlertResponse(
        Long id,
        AlertStatus status,
        double epicenterLatitude,
        double epicenterLongitude,
        double magnitude,
        LocalDateTime createdAt
) {
    // Maps an entity to its response shape in one place, so every endpoint that
    // returns an alert produces identical JSON.
    public static AlertResponse from(EarthquakeAlert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getStatus(),
                alert.getEpicenterLatitude(),
                alert.getEpicenterLongitude(),
                alert.getMagnitude(),
                alert.getCreatedAt());
    }
}
