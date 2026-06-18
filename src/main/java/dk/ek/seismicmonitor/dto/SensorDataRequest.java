package dk.ek.seismicmonitor.dto;

import java.time.LocalDateTime;

public record SensorDataRequest(
        String readingId,
        String sensorId,
        SensorLocationDto sensorLocation,
        Double estimatedDistanceToEpicenterKm,
        Double estimatedMagnitude,
        LocalDateTime recordedAt
) { }