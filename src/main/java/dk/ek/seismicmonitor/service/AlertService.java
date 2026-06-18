package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.estimation.EpicenterEstimator;
import dk.ek.seismicmonitor.estimation.Location;
import dk.ek.seismicmonitor.estimation.LocationWithDistance;
import dk.ek.seismicmonitor.estimation.MagnitudeEstimator;
import dk.ek.seismicmonitor.estimation.MagnitudeSample;
import dk.ek.seismicmonitor.exception.AlertNotFoundException;
import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import dk.ek.seismicmonitor.model.SensorReading;
import dk.ek.seismicmonitor.repository.EarthquakeAlertRepository;
import dk.ek.seismicmonitor.repository.SensorReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private static final int REQUIRED_VALID_MEASUREMENTS = 3;

    private final EpicenterEstimator epicenterEstimator;
    private final MagnitudeEstimator magnitudeEstimator;
    private final EarthquakeAlertRepository alertRepository;
    private final SensorReadingRepository readingRepository;

    public AlertService(EpicenterEstimator epicenterEstimator,
                        MagnitudeEstimator magnitudeEstimator,
                        EarthquakeAlertRepository alertRepository,
                        SensorReadingRepository readingRepository) {
        this.epicenterEstimator = epicenterEstimator;
        this.magnitudeEstimator = magnitudeEstimator;
        this.alertRepository = alertRepository;
        this.readingRepository = readingRepository;
    }

    // Creates an alert when exactly three valid readings are present and the
    // epicenter can be estimated; otherwise returns empty and leaves readings as-is.
    @Transactional
    public Optional<EarthquakeAlert> createAlertIfWarranted(List<SensorReading> validReadings) {
        if (validReadings.size() != REQUIRED_VALID_MEASUREMENTS) {
            return Optional.empty();
        }

        Location epicenter;
        try {
            epicenter = epicenterEstimator.estimate(toLocationWithDistance(validReadings));
        } catch (IllegalArgumentException unstableGeometry) {
            return Optional.empty();
        }

        double magnitude = magnitudeEstimator.estimate(toMagnitudeSamples(validReadings));

        EarthquakeAlert alert = alertRepository.save(
                new EarthquakeAlert(epicenter.latitude(), epicenter.longitude(), magnitude));

        validReadings.forEach(reading -> reading.assignToAlert(alert));
        readingRepository.saveAll(validReadings);

        return Optional.of(alert);
    }

    // Admin action: move an alert to a new status. Loads the alert (404 if missing)
    // and delegates the rule check to the entity, which throws on an illegal jump.
    @Transactional
    public EarthquakeAlert changeStatus(Long alertId, AlertStatus target) {
        EarthquakeAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        alert.changeStatusTo(target);
        return alert; // managed entity: the status change is flushed on commit
    }

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