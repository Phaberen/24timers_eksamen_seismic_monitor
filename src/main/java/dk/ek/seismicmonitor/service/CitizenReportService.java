package dk.ek.seismicmonitor.service;

import dk.ek.seismicmonitor.dto.CitizenReportResponse;
import dk.ek.seismicmonitor.exception.AlertNotFoundException;
import dk.ek.seismicmonitor.exception.DuplicateReportException;
import dk.ek.seismicmonitor.model.CitizenReport;
import dk.ek.seismicmonitor.model.EarthquakeAlert;
import dk.ek.seismicmonitor.repository.CitizenReportRepository;
import dk.ek.seismicmonitor.repository.EarthquakeAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CitizenReportService {

    private final EarthquakeAlertRepository alertRepository;
    private final CitizenReportRepository reportRepository;

    public CitizenReportService(EarthquakeAlertRepository alertRepository,
                                CitizenReportRepository reportRepository) {
        this.alertRepository = alertRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public CitizenReportResponse createReport(Long alertId, String reportedBy, int intensity) {
        EarthquakeAlert alert = loadAlert(alertId);
        if (reportRepository.existsByAlertAndReportedBy(alert, reportedBy)) {
            throw new DuplicateReportException(alertId, reportedBy);
        }
        CitizenReport report = reportRepository.save(new CitizenReport(alert, reportedBy, intensity));
        return CitizenReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public long countReports(Long alertId) {
        return reportRepository.countByAlert(loadAlert(alertId));
    }

    @Transactional(readOnly = true)
    public List<CitizenReportResponse> getReports(Long alertId) {
        return reportRepository.findByAlertOrderByCreatedAtDesc(loadAlert(alertId))
                .stream()
                .map(CitizenReportResponse::from)
                .toList();
    }

    private EarthquakeAlert loadAlert(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
    }
}