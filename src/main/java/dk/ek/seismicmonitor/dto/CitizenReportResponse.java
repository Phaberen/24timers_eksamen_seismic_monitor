package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.CitizenReport;

import java.time.LocalDateTime;

public record CitizenReportResponse(
        Long id,
        Long alertId,
        int intensity,
        LocalDateTime createdAt
) {
    public static CitizenReportResponse from(CitizenReport report) {
        return new CitizenReportResponse(
                report.getId(),
                report.getAlert().getId(),
                report.getIntensity(),
                report.getCreatedAt());
    }
}