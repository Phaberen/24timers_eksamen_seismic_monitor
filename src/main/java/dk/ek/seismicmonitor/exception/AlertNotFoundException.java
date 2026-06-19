package dk.ek.seismicmonitor.exception;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long id) {
        super("No earthquake alert with id " + id);
    }
}
