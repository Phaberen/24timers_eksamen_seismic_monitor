package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.AlertStatus;
import jakarta.validation.constraints.NotNull;

// DTO — the body for "change an alert's status": just the target status.
// @NotNull tells the validator the field is required; an empty body is rejected as a 400 Bad Request
// (this is checked because the controller marks the body @Valid).
public record ChangeAlertStatusRequest(
        @NotNull AlertStatus status
) { }
