package dk.ek.seismicmonitor.repository;
import dk.ek.seismicmonitor.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// REPOSITORY LAYER — database access for Sensor.
// By extending JpaRepository<Sensor, Long> we get save/findById/findAll/delete for free, with no
// implementation: Spring Data generates the class at runtime. <Sensor, Long> = entity type + key type.
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    // A custom finder. Spring Data reads the method NAME ("findBySensorId") and writes the query for
    // us: "find the Sensor whose sensorId matches". Optional = it might not exist yet.
    Optional<Sensor> findBySensorId(String sensorId);
}
