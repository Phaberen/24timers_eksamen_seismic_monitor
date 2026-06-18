package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.SensorReading;

import java.time.LocalDateTime;

public record SensorReadingResponse(
        Long id,
        String readingId,
        String sensorId,
        double latitude,
        double longitude,
        double estimatedDistanceToEpicenterKm,
        double estimatedMagnitude,
        LocalDateTime recordedAt
) {
    public static SensorReadingResponse from(SensorReading r) {
        return new SensorReadingResponse(
                r.getId(),
                r.getReadingId(),
                r.getSensor().getSensorId(),
                r.getLatitude(),
                r.getLongitude(),
                r.getEstimatedDistanceToEpicenterKm(),
                r.getEstimatedMagnitude(),
                r.getRecordedAt());
    }
}