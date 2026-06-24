package dk.ek.seismicmonitor.dto;

import dk.ek.seismicmonitor.model.CitizenReport;

import java.time.LocalDateTime;

// DTO — the outgoing shape of a citizen report (JSON returned by the API).
public record CitizenReportResponse(
        Long id,
        Long alertId,
        String reportedBy,
        int intensity,
        LocalDateTime createdAt
) {
    // Mapper: build a response DTO from a report entity (flattens alert -> alertId).
    public static CitizenReportResponse from(CitizenReport report) {
        return new CitizenReportResponse(
                report.getId(),
                report.getAlert().getId(),
                report.getReportedBy(),
                report.getIntensity(),
                report.getCreatedAt());
    }
}