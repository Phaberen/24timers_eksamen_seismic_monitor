package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.AlertResponse;
import dk.ek.seismicmonitor.dto.ChangeAlertStatusRequest;
import dk.ek.seismicmonitor.dto.SensorReadingResponse;
import dk.ek.seismicmonitor.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    @GetMapping("/active")
    public List<AlertResponse> getActiveAlerts() {
        return alertService.getActiveAlerts();
    }

    @PatchMapping("/{id}/status")
    public AlertResponse changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody ChangeAlertStatusRequest request) {
        return AlertResponse.from(alertService.changeStatus(id, request.status()));
    }

    @GetMapping("/{id}/readings")
    public List<SensorReadingResponse> getReadings(@PathVariable Long id) {
        return alertService.getReadingsForAlert(id);
    }
}
