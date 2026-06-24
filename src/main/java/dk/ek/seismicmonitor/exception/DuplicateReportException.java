package dk.ek.seismicmonitor.exception;

// EXCEPTION LAYER — thrown when a user tries to report the same alert a second time.
// The GlobalExceptionHandler turns it into a 409 Conflict.
public class DuplicateReportException extends RuntimeException {

    public DuplicateReportException(Long alertId, String reportedBy) {
        super("User " + reportedBy + " has already reported alert " + alertId);
    }
}
