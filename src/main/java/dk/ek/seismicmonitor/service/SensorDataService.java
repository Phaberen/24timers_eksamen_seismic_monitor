package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.SensorDataRequest;
import dk.ek.seismicmonitor.dto.SensorLocationDto;
import dk.ek.seismicmonitor.dto.SensorReadingResponse;
import dk.ek.seismicmonitor.model.Sensor;
import dk.ek.seismicmonitor.model.SensorReading;
import dk.ek.seismicmonitor.repository.SensorReadingRepository;
import dk.ek.seismicmonitor.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SensorDataService {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;

    public SensorDataService(SensorRepository sensorRepository,
                             SensorReadingRepository readingRepository) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
    }

    // Persists every reading in the request (a request carries 1-3 measurements).
    // Readings are always stored, even when no alert can be created (Delopgave 2).
    @Transactional
    public void ingest(List<SensorDataRequest> measurements) {
        if (measurements == null) return;
        for (SensorDataRequest m : measurements) {
            // Skip resends: readingId is a unique business key.
            if (m.readingId() != null && readingRepository.existsByReadingId(m.readingId())) {
                continue;
            }
            Sensor sensor = findOrCreateSensor(m);
            SensorReading reading = new SensorReading(
                    m.readingId(),
                    sensor,
                    latOf(m.sensorLocation()),
                    lonOf(m.sensorLocation()),
                    orZero(m.estimatedDistanceToEpicenterKm()),
                    orZero(m.estimatedMagnitude()),
                    m.recordedAt()
            );
            readingRepository.save(reading);
        }
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAllReadings() {
        return readingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private Sensor findOrCreateSensor(SensorDataRequest m) {
        return sensorRepository.findBySensorId(m.sensorId())
                .orElseGet(() -> sensorRepository.save(
                        new Sensor(m.sensorId(),
                                latOf(m.sensorLocation()),
                                lonOf(m.sensorLocation()))));
    }


    private SensorReadingResponse toResponse(SensorReading r) {
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

    private static double latOf(SensorLocationDto loc) {
        return loc == null || loc.latitude() == null ? 0.0 : loc.latitude();
    }

    private static double lonOf(SensorLocationDto loc) {
        return loc == null || loc.longitude() == null ? 0.0 : loc.longitude();
    }

    private static double orZero(Double value) {
        return value == null ? 0.0 : value;
    }
}
