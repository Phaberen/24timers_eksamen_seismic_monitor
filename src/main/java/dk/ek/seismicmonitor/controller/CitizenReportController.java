package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.CitizenReportResponse;
import dk.ek.seismicmonitor.dto.CreateCitizenReportRequest;
import dk.ek.seismicmonitor.service.CitizenReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts/{alertId}/reports")
public class CitizenReportController {

    private final CitizenReportService reportService;

    public CitizenReportController(CitizenReportService reportService) {
        this.reportService = reportService;
    }

    // The reporter is the authenticated user, not a body field, so one user can
    // only ever file one report per alert.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitizenReportResponse create(@PathVariable Long alertId,
                                        @Valid @RequestBody CreateCitizenReportRequest request,
                                        Authentication authentication) {
        return reportService.createReport(alertId, authentication.getName(), request.intensity());
    }

    @GetMapping("/count")
    public long count(@PathVariable Long alertId) {
        return reportService.countReports(alertId);
    }

    @GetMapping
    public List<CitizenReportResponse> list(@PathVariable Long alertId) {
        return reportService.getReports(alertId);
    }
}