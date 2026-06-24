package dk.ek.seismicmonitor.repository;

import dk.ek.seismicmonitor.model.EarthquakeAlert;
import dk.ek.seismicmonitor.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// REPOSITORY LAYER — database access for SensorReading. Gets the standard CRUD methods from
// JpaRepository; the two methods below are extra finders named so Spring Data writes the query.
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    // "Is there already a reading with this readingId?" — used to skip resends during ingest.
    boolean existsByReadingId(String readingId);

    // All readings that belong to a given alert, newest first. Powers the admin "what led to this
    // alert?" view. "OrderByRecordedAtDesc" in the name becomes an ORDER BY in the query.
    List<SensorReading> findByAlertOrderByRecordedAtDesc(EarthquakeAlert alert);
}