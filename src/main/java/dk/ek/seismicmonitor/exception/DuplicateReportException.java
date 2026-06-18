package dk.ek.seismicmonitor.exception;

// Thrown when a user submits a second report for the same alert. Mapped to 409.
public class DuplicateReportException extends RuntimeException {

    public DuplicateReportException(Long alertId, String reportedBy) {
        super("User " + reportedBy + " has already reported alert " + alertId);
    }
}