package dk.ek.seismicmonitor.exception;

public class DuplicateReportException extends RuntimeException {

    public DuplicateReportException(Long alertId, String reportedBy) {
        super("User " + reportedBy + " has already reported alert " + alertId);
    }
}
