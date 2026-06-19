package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.SensorDataRequest;
import dk.ek.seismicmonitor.dto.SensorLocationDto;
import dk.ek.seismicmonitor.exception.AlertNotFoundException;
import dk.ek.seismicmonitor.exception.DuplicateReportException;
import dk.ek.seismicmonitor.repository.EarthquakeAlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CitizenReportTest {

    @Autowired SensorDataService sensorDataService;
    @Autowired CitizenReportService reportService;
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

    private Long createAlert() {
        sensorDataService.ingest(List.of(
                reading("READ-1", "SEN-1", 55.6, 12.5, 3.0),
                reading("READ-2", "SEN-2", 55.7, 12.6, 3.4),
                reading("READ-3", "SEN-3", 55.5, 12.7, 4.0)));
        return alertRepository.findAll().getFirst().getId();
    }

    @Test
    void createReport_isCountedAndListed() {
        Long alertId = createAlert();

        reportService.createReport(alertId, "user", 5);

        assertThat(reportService.countReports(alertId)).isEqualTo(1);
        assertThat(reportService.getReports(alertId))
                .singleElement()
                .satisfies(r -> assertThat(r.intensity()).isEqualTo(5));
    }

    @Test
    void createReport_sameUserTwice_isRejected() {
        Long alertId = createAlert();
        reportService.createReport(alertId, "user", 5);

        assertThatThrownBy(() -> reportService.createReport(alertId, "user", 7))
                .isInstanceOf(DuplicateReportException.class);
    }

    @Test
    void createReport_unknownAlert_isRejected() {
        assertThatThrownBy(() -> reportService.createReport(999L, "user", 5))
                .isInstanceOf(AlertNotFoundException.class);
    }
}
