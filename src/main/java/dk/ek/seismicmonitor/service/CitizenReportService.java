package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.CitizenReportResponse;
import dk.ek.seismicmonitor.exception.DuplicateReportException;
import dk.ek.seismicmonitor.model.CitizenReport;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import dk.ek.seismicmonitor.repository.CitizenReportRepository;
import dk.ek.seismicmonitor.repository.EarthquakeAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// SERVICE LAYER — handles citizen reports ("I felt this quake") for a given alert.
@Service
public class CitizenReportService {

    private final EarthquakeAlertRepository alertRepository;
    private final CitizenReportRepository reportRepository;

    public CitizenReportService(EarthquakeAlertRepository alertRepository,
                                CitizenReportRepository reportRepository) {
        this.alertRepository = alertRepository;
        this.reportRepository = reportRepository;
    }

    // Files a new report. Finds the alert (404 if missing), blocks a second report from the same
    // user on the same alert (409), then saves and returns it as a DTO.
    @Transactional
    public CitizenReportResponse createReport(Long alertId, String reportedBy, int intensity) {
        EarthquakeAlert alert = alertRepository.getByIdOrThrow(alertId);
        if (reportRepository.existsByAlertAndReportedBy(alert, reportedBy)) {
            throw new DuplicateReportException(alertId, reportedBy);
        }
        CitizenReport report = reportRepository.save(new CitizenReport(alert, reportedBy, intensity));
        return CitizenReportResponse.from(report);
    }

    // How many reports an alert has (used for the count shown on each alert card).
    @Transactional(readOnly = true)
    public long countReports(Long alertId) {
        return reportRepository.countByAlert(alertRepository.getByIdOrThrow(alertId));
    }

    // The full list of reports for an alert (admin view).
    @Transactional(readOnly = true)
    public List<CitizenReportResponse> getReports(Long alertId) {
        return reportRepository.findByAlertOrderByCreatedAtDesc(alertRepository.getByIdOrThrow(alertId))
                .stream()
                .map(CitizenReportResponse::from)
                .toList();
    }
}