package dk.ek.seismicmonitor.repository;

import dk.ek.seismicmonitor.exception.AlertNotFoundException;
import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// REPOSITORY LAYER — database access for EarthquakeAlert.
public interface EarthquakeAlertRepository extends JpaRepository<EarthquakeAlert, Long> {

    // Alerts in one status (e.g. ACTIVE), newest first — used for the public "active alerts" list.
    List<EarthquakeAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    // Every alert, newest first — the admin "all alerts" list.
    List<EarthquakeAlert> findAllByOrderByCreatedAtDesc();

    // A "default" method = a small helper with a body, written right here in the interface.
    // It's our single home for "load this alert or fail with a 404", so the services don't repeat
    // the same find-or-throw three times.
    default EarthquakeAlert getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new AlertNotFoundException(id));
    }
}
