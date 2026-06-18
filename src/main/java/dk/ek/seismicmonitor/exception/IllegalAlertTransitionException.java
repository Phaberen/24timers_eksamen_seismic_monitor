package dk.ek.seismicmonitor.exception;

import dk.ek.seismicmonitor.model.AlertStatus;

// Thrown when an alert is asked to change to a status that the state machine
// (AlertStatus.canTransitionTo) does not allow, e.g. FALSE_ALARM -> ACTIVE.
public class IllegalAlertTransitionException extends RuntimeException {

    public IllegalAlertTransitionException(AlertStatus from, AlertStatus to) {
        super("Illegal alert status transition: " + from + " -> " + to);
    }
}