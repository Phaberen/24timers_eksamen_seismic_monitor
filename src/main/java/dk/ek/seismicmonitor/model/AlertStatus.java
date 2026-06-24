package dk.ek.seismicmonitor.model;

// MODEL LAYER — the four states an alert can be in, plus the rules for moving between them.
// Keeping the rules here (an enum with a method) means the allowed moves live in one obvious place.
public enum AlertStatus {
    UNDER_REVIEW,   // just created, an admin hasn't judged it yet (not shown to regular users)
    ACTIVE,         // admin confirmed it's a real alert
    FALSE_ALARM,    // admin decided it wasn't real (final)
    NOT_ACTIVE;     // the danger is over (final)

    // Answers "may I move from my current state to 'target'?".
    // The switch covers every state, so the compiler guarantees we didn't forget one.
    public boolean canTransitionTo(AlertStatus target) {
        return switch (this) {
            case UNDER_REVIEW -> target == ACTIVE || target == FALSE_ALARM; // confirm or dismiss
            case ACTIVE -> target == NOT_ACTIVE;                            // can only be closed
            case FALSE_ALARM, NOT_ACTIVE -> false;                         // final states, no way out
        };
    }
}
