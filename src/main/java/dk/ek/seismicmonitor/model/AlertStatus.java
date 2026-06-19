package dk.ek.seismicmonitor.model;

public enum AlertStatus {
    UNDER_REVIEW,
    ACTIVE,
    FALSE_ALARM,
    NOT_ACTIVE;

    public boolean canTransitionTo(AlertStatus target) {
        return switch (this) {
            case UNDER_REVIEW -> target == ACTIVE || target == FALSE_ALARM;
            case ACTIVE -> target == NOT_ACTIVE;
            case FALSE_ALARM, NOT_ACTIVE -> false;
        };
    }
}
