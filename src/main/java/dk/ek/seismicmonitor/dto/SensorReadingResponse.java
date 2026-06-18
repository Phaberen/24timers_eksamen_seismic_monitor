package dk.ek.seismicmonitor.dto;

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
) { }