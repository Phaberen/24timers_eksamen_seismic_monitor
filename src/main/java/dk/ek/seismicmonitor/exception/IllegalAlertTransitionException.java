package dk.ek.seismicmonitor.exception;

import dk.ek.seismicmonitor.model.AlertStatus;

public class IllegalAlertTransitionException extends RuntimeException {

    public IllegalAlertTransitionException(AlertStatus from, AlertStatus to) {
        super("Illegal alert status transition: " + from + " -> " + to);
    }
}
