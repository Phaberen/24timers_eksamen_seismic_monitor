package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.AlertResponse;
import dk.ek.seismicmonitor.dto.ChangeAlertStatusRequest;
import dk.ek.seismicmonitor.dto.SensorReadingResponse;
import dk.ek.seismicmonitor.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CONTROLLER LAYER — the alert endpoints. Base URL "/api/alerts".
// Who may call each one is decided in SecurityConfig, not here.
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // GET /api/alerts — all alerts (admin).
    @GetMapping
    public List<AlertResponse> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    // GET /api/alerts/active — only active alerts (user + admin).
    @GetMapping("/active")
    public List<AlertResponse> getActiveAlerts() {
        return alertService.getActiveAlerts();
    }

    // PATCH /api/alerts/{id}/status — change one alert's status (admin).
    // @PathVariable pulls the id out of the URL; @Valid @RequestBody reads + checks the JSON body.
    // PATCH (not PUT) because we're updating one field, not replacing the whole alert.
    @PatchMapping("/{id}/status")
    public AlertResponse changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody ChangeAlertStatusRequest request) {
        return AlertResponse.from(alertService.changeStatus(id, request.status()));
    }

    // GET /api/alerts/{id}/readings — the readings that led to this alert (admin).
    @GetMapping("/{id}/readings")
    public List<SensorReadingResponse> getReadings(@PathVariable Long id) {
        return alertService.getReadingsForAlert(id);
    }
}
