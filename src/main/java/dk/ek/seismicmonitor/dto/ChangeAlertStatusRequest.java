package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeAlertStatusRequest(
        @NotNull AlertStatus status
) { }
