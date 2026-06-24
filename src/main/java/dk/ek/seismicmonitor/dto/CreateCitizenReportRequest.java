package dk.ek.seismicmonitor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// DTO — the body for filing a citizen report: how strongly the person felt the quake.
// @Min(1) @Max(10) restricts intensity to 1–10; anything outside is rejected as 400 Bad Request.
public record CreateCitizenReportRequest(
        @Min(1) @Max(10) int intensity
) { }