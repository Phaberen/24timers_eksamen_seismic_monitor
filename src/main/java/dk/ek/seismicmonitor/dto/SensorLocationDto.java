package dk.ek.seismicmonitor.dto;

// DTO — the nested "sensorLocation" object inside an incoming measurement.
// Double (not double) so a missing latitude/longitude arrives as null rather than crashing.
public record SensorLocationDto(Double latitude, Double longitude) { }
