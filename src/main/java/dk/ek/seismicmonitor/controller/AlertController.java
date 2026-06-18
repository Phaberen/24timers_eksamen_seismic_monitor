package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.AlertResponse;
import dk.ek.seismicmonitor.dto.ChangeAlertStatusRequest;
import dk.ek.seismicmonitor.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // Admin action: change an alert's status. The service + entity enforce the
    // state machine; GlobalExceptionHandler turns an illegal jump into 409 and an
    // unknown id into 404, so this method only describes the happy path.
    @PatchMapping("/{id}/status")
    public AlertResponse changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody ChangeAlertStatusRequest request) {
        return AlertResponse.from(alertService.changeStatus(id, request.status()));
    }
}