package dk.ek.seismicmonitor.repository;

import dk.ek.seismicmonitor.model.EarthquakeAlert;
import dk.ek.seismicmonitor.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    boolean existsByReadingId(String readingId);

    List<SensorReading> findByAlertOrderByRecordedAtDesc(EarthquakeAlert alert);
}