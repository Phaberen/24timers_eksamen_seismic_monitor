package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.CitizenReportResponse;
import dk.ek.seismicmonitor.dto.CreateCitizenReportRequest;
import dk.ek.seismicmonitor.service.CitizenReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CONTROLLER LAYER — citizen reports, nested under a specific alert.
// The {alertId} in the base URL is available to every method here as a @PathVariable.
@RestController
@RequestMapping("/api/alerts/{alertId}/reports")
public class CitizenReportController {

    private final CitizenReportService reportService;

    public CitizenReportController(CitizenReportService reportService) {
        this.reportService = reportService;
    }

    // POST .../reports — file a report (user + admin). @ResponseStatus(CREATED) makes a success
    // return 201. "Authentication" is injected by Spring Security; getName() is the logged-in
    // username, which we use as the report's author (and to enforce one report per user).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitizenReportResponse create(@PathVariable Long alertId,
                                        @Valid @RequestBody CreateCitizenReportRequest request,
                                        Authentication authentication) {
        return reportService.createReport(alertId, authentication.getName(), request.intensity());
    }

    // GET .../reports/count — how many reports this alert has (user + admin).
    @GetMapping("/count")
    public long count(@PathVariable Long alertId) {
        return reportService.countReports(alertId);
    }

    // GET .../reports — the full list of reports (admin).
    @GetMapping
    public List<CitizenReportResponse> list(@PathVariable Long alertId) {
        return reportService.getReports(alertId);
    }
}