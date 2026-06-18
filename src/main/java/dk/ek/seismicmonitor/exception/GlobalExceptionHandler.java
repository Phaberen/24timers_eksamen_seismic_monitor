package dk.ek.seismicmonitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// One place that maps domain exceptions to HTTP responses for all controllers,
// so the controllers themselves stay free of try/catch and status-code logic.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Illegal state-machine jump (e.g. FALSE_ALARM -> ACTIVE) -> 409 Conflict:
    // the request was understood but conflicts with the alert's current state.
    @ExceptionHandler(IllegalAlertTransitionException.class)
    public ProblemDetail handleIllegalTransition(IllegalAlertTransitionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Unknown alert id -> 404 Not Found.
    @ExceptionHandler(AlertNotFoundException.class)
    public ProblemDetail handleAlertNotFound(AlertNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Second report by the same user for one alert -> 409 Conflict.
    @ExceptionHandler(DuplicateReportException.class)
    public ProblemDetail handleDuplicateReport(DuplicateReportException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}