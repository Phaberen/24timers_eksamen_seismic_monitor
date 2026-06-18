package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.AlertStatus;
import jakarta.validation.constraints.NotNull;

// Body of PATCH /api/alerts/{id}/status, e.g. { "status": "ACTIVE" }.
// Jackson maps the JSON string to the AlertStatus enum; an unknown value is
// rejected as a 400 before the request ever reaches the service.
public record ChangeAlertStatusRequest(
        @NotNull AlertStatus status
) { }
