package dk.ek.seismicmonitor.dto;

import java.time.LocalDateTime;

public record SensorDataRequest(
        String readingId,
        String sensorId,
        SensorLocationDto sensorLocation,
        Double estimatedDistanceToEpicenterKm,
        Double estimatedMagnitude,
        LocalDateTime recordedAt
) {
    public boolean isValid() {
        return sensorLocation != null
                && sensorLocation.latitude() != null
                && sensorLocation.longitude() != null
                && estimatedDistanceToEpicenterKm != null && estimatedDistanceToEpicenterKm > 0
                && estimatedMagnitude != null && estimatedMagnitude > 0
                && recordedAt != null;
    }
}
