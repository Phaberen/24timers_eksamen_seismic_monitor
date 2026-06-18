package dk.ek.seismicmonitor.repository;

import dk.ek.seismicmonitor.exception.AlertNotFoundException;
import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EarthquakeAlertRepository extends JpaRepository<EarthquakeAlert, Long> {

    List<EarthquakeAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<EarthquakeAlert> findAllByOrderByCreatedAtDesc();

    // One home for the "load by id or 404" rule shared by the services.
    default EarthquakeAlert getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new AlertNotFoundException(id));
    }
}
