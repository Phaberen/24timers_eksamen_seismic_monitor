package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.SensorDataRequest;
import dk.ek.seismicmonitor.dto.SensorLocationDto;
import dk.ek.seismicmonitor.model.AlertStatus;
import dk.ek.seismicmonitor.repository.EarthquakeAlertRepository;
import dk.ek.seismicmonitor.repository.SensorReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlertCreationTest {

    @Autowired SensorDataService service;
    @Autowired SensorReadingRepository readingRepository;
    @Autowired EarthquakeAlertRepository alertRepository;

    private SensorDataRequest reading(String readingId, String sensorId,
                                      double lat, double lon, double magnitude) {
        return new SensorDataRequest(
                readingId,
                sensorId,
                new SensorLocationDto(lat, lon),
                47.31,
                magnitude,
                LocalDateTime.parse("2026-05-20T10:15:30"));
    }

    @Test
    void exactlyThreeValidReadings_createsUnderReviewAlertWithReadingsLinked() {
        service.ingest(List.of(
                reading("READ-1", "SEN-1", 55.6, 12.5, 3.0),
                reading("READ-2", "SEN-2", 55.7, 12.6, 3.4),
                reading("READ-3", "SEN-3", 55.5, 12.7, 4.0)));

        assertThat(alertRepository.count()).isEqualTo(1);
        assertThat(alertRepository.findAll().getFirst().getStatus())
                .isEqualTo(AlertStatus.UNDER_REVIEW);
        assertThat(readingRepository.findAll())
                .hasSize(3)
                .allSatisfy(r -> assertThat(r.getAlert()).isNotNull());
    }

    @Test
    void fewerThanThreeReadings_storesReadingsButNoAlert() {
        service.ingest(List.of(
                reading("READ-1", "SEN-1", 55.6, 12.5, 3.0),
                reading("READ-2", "SEN-2", 55.7, 12.6, 3.4)));

        assertThat(readingRepository.count()).isEqualTo(2);
        assertThat(alertRepository.count()).isZero();
    }

    @Test
    void threeReadingsWithOneInvalid_storesAllButNoAlert() {
        service.ingest(List.of(
                reading("READ-1", "SEN-1", 55.6, 12.5, 3.0),
                reading("READ-2", "SEN-2", 55.7, 12.6, 3.4),
                reading("READ-3", "SEN-3", 55.5, 12.7, 0.0)));

        assertThat(readingRepository.count()).isEqualTo(3);
        assertThat(alertRepository.count()).isZero();
    }

    @Test
    void estimatorFailure_storesReadingsButNoAlert() {
        service.ingest(List.of(
                reading("READ-1", "SEN-1", 55.6, 12.5, 3.0),
                reading("READ-2", "SEN-2", 55.6, 12.5, 3.4),
                reading("READ-3", "SEN-3", 55.6, 12.5, 4.0)));

        assertThat(readingRepository.count()).isEqualTo(3);
        assertThat(alertRepository.count()).isZero();
    }
}
