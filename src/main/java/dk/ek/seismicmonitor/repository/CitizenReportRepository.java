package dk.ek.seismicmonitor.repository;

import dk.ek.seismicmonitor.model.CitizenReport;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// REPOSITORY LAYER — database access for CitizenReport.
public interface CitizenReportRepository extends JpaRepository<CitizenReport, Long> {

    // "Has this user already reported on this alert?" — used to enforce one report per user per alert.
    boolean existsByAlertAndReportedBy(EarthquakeAlert alert, String reportedBy);

    // How many reports an alert has. "count..." returns a number instead of the rows.
    long countByAlert(EarthquakeAlert alert);

    // All reports for an alert, newest first (admin view).
    List<CitizenReport> findByAlertOrderByCreatedAtDesc(EarthquakeAlert alert);
}