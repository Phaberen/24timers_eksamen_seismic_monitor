package dk.ek.seismicmonitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// EXCEPTION LAYER — one central place that turns our domain errors into HTTP responses.
// @RestControllerAdvice = "watch every controller; if one of these exceptions is thrown, handle it
// here". This keeps controllers and services clean: they just throw, this decides the status code.
// ProblemDetail is a standard JSON error body (status + message).
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Tried an illegal status change (e.g. closing a false alarm) -> 409 Conflict.
    @ExceptionHandler(IllegalAlertTransitionException.class)
    public ProblemDetail handleIllegalTransition(IllegalAlertTransitionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Asked for an alert id that doesn't exist -> 404 Not Found.
    @ExceptionHandler(AlertNotFoundException.class)
    public ProblemDetail handleAlertNotFound(AlertNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Same user tried to report the same alert twice -> 409 Conflict.
    @ExceptionHandler(DuplicateReportException.class)
    public ProblemDetail handleDuplicateReport(DuplicateReportException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}
