package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.SensorDataRequest;
import dk.ek.seismicmonitor.dto.SensorLocationDto;
import dk.ek.seismicmonitor.dto.SensorReadingResponse;
import dk.ek.seismicmonitor.repository.SensorReadingRepository;
import dk.ek.seismicmonitor.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest loads only the JPA layer + repositories (fast, transactional, rolled back per test).
// @Import pulls the service into the slice; it's not a repository so it isn't picked up
// automatically.
// replace = NONE + profile "test" => use the configured H2 from application-test.properties (no
// MySQL/Docker needed).
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SensorDataService.class)
class SensorDataServiceTest {

    @Autowired
    SensorDataService service;
    @Autowired
    SensorRepository sensorRepository;
    @Autowired
    SensorReadingRepository readingRepository;

    // Builds a valid request; tests vary only the ids they care about.
    private SensorDataRequest reading(String readingId, String sensorId) {
        return new SensorDataRequest(
                readingId,
                sensorId,
                new SensorLocationDto(55.6, 12.5),
                47.31,
                3.4,
                LocalDateTime.parse("2026-05-20T10:15:30"));
    }

    // A full 3-measurement request stores all three readings.
    @Test
    void ingest_persistsAllReadingsInRequest() {
        service.ingest(List.of(
                reading("READ-1", "SEN-1"),
                reading("READ-2", "SEN-2"),
                reading("READ-3", "SEN-3")));

        assertThat(readingRepository.count()).isEqualTo(3);
    }

    // Fewer than 3 measurements are still stored (storage happens regardless of count).
    @Test
    void ingest_fewerThanThreeStillStored() {
        service.ingest(List.of(reading("READ-1", "SEN-1")));

        assertThat(readingRepository.count()).isEqualTo(1);
    }

    // Two readings from the same sensor reuse one Sensor row (find-or-create).
    @Test
    void ingest_reusesSensorForSameSensorId() {
        service.ingest(List.of(
                reading("READ-1", "SEN-1"),
                reading("READ-2", "SEN-1")));

        assertThat(sensorRepository.count()).isEqualTo(1);
        assertThat(readingRepository.count()).isEqualTo(2);
    }

    // A resent readingId is skipped (idempotency; protects the unique constraint).
    @Test
    void ingest_skipsDuplicateReadingId() {
        service.ingest(List.of(reading("READ-1", "SEN-1")));
        service.ingest(List.of(reading("READ-1", "SEN-1")));

        assertThat(readingRepository.count()).isEqualTo(1);
    }

    // getAllReadings() returns a flat DTO with sensorId pulled through the relationship.
    @Test
    void getAllReadings_returnsStoredReadingsAsDto() {
        service.ingest(List.of(reading("READ-1", "SEN-1")));

        List<SensorReadingResponse> all = service.getAllReadings();

        assertThat(all).singleElement().satisfies(r -> {
            assertThat(r.readingId()).isEqualTo("READ-1");
            assertThat(r.sensorId()).isEqualTo("SEN-1");
            assertThat(r.estimatedMagnitude()).isEqualTo(3.4);
        });
    }
}
