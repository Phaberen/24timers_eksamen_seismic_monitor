package dk.ek.seismicmonitor.exception;

// EXCEPTION LAYER — thrown when code asks for an alert id that isn't in the database.
// Extends RuntimeException (an "unchecked" exception) so we don't have to declare it on every
// method; the GlobalExceptionHandler turns it into a 404 response.
public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long id) {
        super("No earthquake alert with id " + id); // becomes the message shown in the error body
    }
}
