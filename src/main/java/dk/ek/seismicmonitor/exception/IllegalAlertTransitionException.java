package dk.ek.seismicmonitor.exception;

import dk.ek.seismicmonitor.model.AlertStatus;

// EXCEPTION LAYER — thrown by the alert when someone tries an illegal status change.
// The GlobalExceptionHandler turns it into a 409 Conflict. The message names both states so the
// caller can see what was rejected.
public class IllegalAlertTransitionException extends RuntimeException {

    public IllegalAlertTransitionException(AlertStatus from, AlertStatus to) {
        super("Illegal alert status transition: " + from + " -> " + to);
    }
}
