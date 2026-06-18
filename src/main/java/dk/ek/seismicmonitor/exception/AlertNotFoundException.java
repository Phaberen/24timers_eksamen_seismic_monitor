package dk.ek.seismicmonitor.exception;

// Thrown when an alert is looked up by id but does not exist. Mapped to 404.
public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long id) {
        super("No earthquake alert with id " + id);
    }
}