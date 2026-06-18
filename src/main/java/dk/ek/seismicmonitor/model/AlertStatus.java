package dk.ek.seismicmonitor.model;

public enum AlertStatus {
    UNDER_REVIEW,
    ACTIVE,
    FALSE_ALARM,
    NOT_ACTIVE;

    // The allowed state transitions for an earthquake alert. FALSE_ALARM and
    // NOT_ACTIVE are terminal, so they permit no further change.
    public boolean canTransitionTo(AlertStatus target) {
        return switch (this) {
            case UNDER_REVIEW -> target == ACTIVE || target == FALSE_ALARM;
            case ACTIVE -> target == NOT_ACTIVE;
            case FALSE_ALARM, NOT_ACTIVE -> false;
        };
    }
}