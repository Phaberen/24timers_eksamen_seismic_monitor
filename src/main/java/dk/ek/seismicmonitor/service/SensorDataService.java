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

import java.util.ArrayList;
import java.util.List;

// SERVICE LAYER — the first step of the ingest pipeline.
// Its job: save every reading that arrived on POST /api/sensor-data, then pass the valid
// ones to AlertService to decide if they add up to an earthquake.
// Flow: SensorDataController -> SensorDataService.ingest() -> AlertService.
//
// @Service tells Spring to create one of these and hand it out wherever it's needed
// (here, to SensorDataController).
@Service
public class SensorDataService {

    // The things this service needs to do its job. "final" means they're set once and never null.
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final AlertService alertService;

    // Spring passes these in when it builds the service (constructor injection).
    public SensorDataService(SensorRepository sensorRepository,
                             SensorReadingRepository readingRepository,
                             AlertService alertService) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.alertService = alertService;
    }

    // Handles one request from the simulator (it sends 1–3 measurements at a time).
    // @Transactional = all the database saves below happen as one unit: they all succeed
    // together, or if something fails they all get undone.
    @Transactional
    public void ingest(List<SensorDataRequest> measurements) {
        if (measurements == null) return; // nothing sent -> nothing to do

        // Only measurements from THIS request can be combined into an alert. We collect the
        // valid ones here and pass them on once, after the loop.
        List<SensorReading> validReadings = new ArrayList<>();

        for (SensorDataRequest m : measurements) {
            // The simulator can resend the same readingId. If we already have it, skip it,
            // so we don't store the same reading (or raise the same alert) twice.
            if (m.readingId() != null && readingRepository.existsByReadingId(m.readingId())) {
                continue;
            }
            // Every reading belongs to a sensor. Look it up, or create it the first time we see it.
            Sensor sensor = findOrCreateSensor(m);

            // Turn the incoming JSON (DTO) into a database object (entity). The helpers below
            // turn missing numbers into 0.0 so we can still save a messy reading.
            SensorReading reading = new SensorReading(
                    m.readingId(),
                    sensor,
                    latOf(m.sensorLocation()),
                    lonOf(m.sensorLocation()),
                    orZero(m.estimatedDistanceToEpicenterKm()),
                    orZero(m.estimatedMagnitude()),
                    m.recordedAt()
            );
            readingRepository.save(reading); // save EVERY reading, even invalid ones (assignment rule)

            // Is it a usable reading? (location present, distance > 0, magnitude > 0, time set)
            // Only valid readings can take part in forming an alert.
            if (m.isValid()) {
                validReadings.add(reading);
            }
        }

        // Hand the valid readings to the alert layer. It only creates an alert when there are
        // exactly 3 valid ones; otherwise nothing happens and the readings just stay saved.
        alertService.createAlertIfWarranted(validReadings);
    }

    // Admin view: return all stored readings, converted to response objects (DTOs).
    // readOnly = true just tells Spring we're only reading, not changing anything.
    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAllReadings() {
        return readingRepository.findAll().stream()
                .map(SensorReadingResponse::from) // entity -> DTO
                .toList();
    }

    // Find the sensor by its id, or create and save it the first time we see it.
    private Sensor findOrCreateSensor(SensorDataRequest m) {
        return sensorRepository.findBySensorId(m.sensorId())
                .orElseGet(() -> sensorRepository.save(
                        new Sensor(m.sensorId(),
                                latOf(m.sensorLocation()),
                                lonOf(m.sensorLocation()))));
    }

    // The JSON may leave fields out, so these turn a missing value into 0.0 instead of crashing.
    // A 0.0 simply fails the isValid() check later.

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