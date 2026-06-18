package dk.ek.seismicmonitor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateCitizenReportRequest(
        @Min(1) @Max(10) int intensity
) { }