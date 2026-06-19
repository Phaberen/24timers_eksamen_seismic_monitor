package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.model.EarthquakeAlert;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        AlertStatus status,
        double epicenterLatitude,
        double epicenterLongitude,
        double magnitude,
        String area,
        LocalDateTime createdAt
) {
    public static AlertResponse from(EarthquakeAlert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getStatus(),
                alert.getEpicenterLatitude(),
                alert.getEpicenterLongitude(),
                alert.getMagnitude(),
                alert.getArea(),
                alert.getCreatedAt());
    }
}
