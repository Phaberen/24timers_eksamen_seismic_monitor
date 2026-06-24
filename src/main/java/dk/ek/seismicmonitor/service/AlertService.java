package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.AlertResponse;
import dk.ek.seismicmonitor.dto.SensorReadingResponse;
import dk.ek.seismicmonitor.estimation.EpicenterEstimator;
import dk.ek.seismicmonitor.estimation.Location;
import dk.ek.seismicmonitor.estimation.LocationWithDistance;
import dk.ek.seismicmonitor.estimation.MagnitudeEstimator;
import dk.ek.seismicmonitor.estimation.MagnitudeSample;
import dk.ek.seismicmonitor.geocoding.ReverseGeocoder;
import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import dk.ek.seismicmonitor.model.SensorReading;
import dk.ek.seismicmonitor.repository.EarthquakeAlertRepository;
import dk.ek.seismicmonitor.repository.SensorReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// SERVICE LAYER — the second step of the ingest pipeline: turn valid readings into an alert.
// It also handles reading/listing alerts and changing their status.
// Note it depends on INTERFACES (EpicenterEstimator, MagnitudeEstimator, ReverseGeocoder), not
// concrete classes — so the actual algorithms/provider can be swapped without touching this code.
@Service
public class AlertService {

    private static final int REQUIRED_VALID_MEASUREMENTS = 3; // exactly 3 readings make an alert

    private final EpicenterEstimator epicenterEstimator;
    private final MagnitudeEstimator magnitudeEstimator;
    private final ReverseGeocoder reverseGeocoder;
    private final EarthquakeAlertRepository alertRepository;
    private final SensorReadingRepository readingRepository;

    // Spring injects one matching bean for each interface here.
    public AlertService(EpicenterEstimator epicenterEstimator,
                        MagnitudeEstimator magnitudeEstimator,
                        ReverseGeocoder reverseGeocoder,
                        EarthquakeAlertRepository alertRepository,
                        SensorReadingRepository readingRepository) {
        this.epicenterEstimator = epicenterEstimator;
        this.magnitudeEstimator = magnitudeEstimator;
        this.reverseGeocoder = reverseGeocoder;
        this.alertRepository = alertRepository;
        this.readingRepository = readingRepository;
    }

    // Creates an alert only if the readings warrant one. Returns Optional: "maybe an alert, maybe
    // nothing" — clearer and safer than returning null.
    @Transactional
    public Optional<EarthquakeAlert> createAlertIfWarranted(List<SensorReading> validReadings) {
        // Rule: need exactly 3 valid readings. Fewer (or more) -> no alert, but the readings stay saved.
        if (validReadings.size() != REQUIRED_VALID_MEASUREMENTS) {
            return Optional.empty();
        }

        // Work out the epicenter. The math can fail on bad geometry (3 readings that don't pin down a
        // point); if it throws we just make no alert — the readings are already saved either way.
        Location epicenter;
        try {
            epicenter = epicenterEstimator.estimate(toLocationWithDistance(validReadings));
        } catch (IllegalArgumentException unstableGeometry) {
            return Optional.empty();
        }

        // Estimate strength, then try to name the area. Geocoding is best-effort: if it fails we
        // fall back to null (no area) rather than blocking the alert.
        double magnitude = magnitudeEstimator.estimate(toMagnitudeSamples(validReadings));
        String area = reverseGeocoder.lookupArea(epicenter.latitude(), epicenter.longitude())
                .orElse(null);

        // Save the new alert (starts as UNDER_REVIEW), then link the 3 readings to it.
        EarthquakeAlert alert = alertRepository.save(
                new EarthquakeAlert(epicenter.latitude(), epicenter.longitude(), magnitude, area));

        validReadings.forEach(reading -> reading.assignToAlert(alert));
        readingRepository.saveAll(validReadings);

        return Optional.of(alert);
    }

    // Admin action: move an alert to a new status. Load it (404 if missing), then let the alert
    // object check the move is legal (it throws if not). No explicit save needed — see below.
    @Transactional
    public EarthquakeAlert changeStatus(Long alertId, AlertStatus target) {
        EarthquakeAlert alert = alertRepository.getByIdOrThrow(alertId);
        alert.changeStatusTo(target);
        // Inside a transaction, JPA notices the loaded object changed and saves it automatically
        // when the method ends, so we don't have to call save() ourselves.
        return alert;
    }

    // Public list: only ACTIVE alerts, mapped to DTOs.
    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.ACTIVE)
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    // Admin list: every alert in any status.
    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    // Admin view: the readings that produced a given alert.
    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getReadingsForAlert(Long alertId) {
        EarthquakeAlert alert = alertRepository.getByIdOrThrow(alertId);
        return readingRepository.findByAlertOrderByRecordedAtDesc(alert)
                .stream()
                .map(SensorReadingResponse::from)
                .toList();
    }

    // The estimators speak in their own simple types, not in our entities. These two helpers convert
    // readings into those types, which also keeps the estimators independent of our database classes.
    private List<LocationWithDistance> toLocationWithDistance(List<SensorReading> readings) {
        return readings.stream()
                .map(r -> new LocationWithDistance(
                        new Location(r.getLatitude(), r.getLongitude()),
                        r.getEstimatedDistanceToEpicenterKm()))
                .toList();
    }

    private List<MagnitudeSample> toMagnitudeSamples(List<SensorReading> readings) {
        return readings.stream()
                .map(r -> new MagnitudeSample(
                        r.getEstimatedMagnitude(),
                        r.getEstimatedDistanceToEpicenterKm()))
                .toList();
    }
}