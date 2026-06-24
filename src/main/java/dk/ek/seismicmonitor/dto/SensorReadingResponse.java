package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.SensorReading;

import java.time.LocalDateTime;

// DTO — the OUTGOING shape of a reading (what the API sends back as JSON).
// We use a separate response type instead of returning the entity, so the JSON we expose is
// decoupled from the database class (e.g. we flatten sensor -> sensorId, hide internal links).
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
    // Mapper: build a response DTO from a reading entity. Kept in one place so the conversion
    // isn't copy-pasted around the services.
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