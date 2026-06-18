package dk.ek.seismicmonitor.repository;

import dk.ek.seismicmonitor.model.CitizenReport;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitizenReportRepository extends JpaRepository<CitizenReport, Long> {

    long countByAlert(EarthquakeAlert alert);

    List<CitizenReport> findByAlertOrderByCreatedAtDesc(EarthquakeAlert alert);
}