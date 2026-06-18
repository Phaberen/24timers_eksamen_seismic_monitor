package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.CitizenReportResponse;
import dk.ek.seismicmonitor.dto.CreateCitizenReportRequest;
import dk.ek.seismicmonitor.service.CitizenReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts/{alertId}/reports")
public class CitizenReportController {

    private final CitizenReportService reportService;

    public CitizenReportController(CitizenReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitizenReportResponse create(@PathVariable Long alertId,
                                        @Valid @RequestBody CreateCitizenReportRequest request) {
        return reportService.createReport(alertId, request.intensity());
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