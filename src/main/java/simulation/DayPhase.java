package simulation;

/**
 * Represents the broad phase of the current day in the simulation.
 * Used to dispatch phase-specific logic and to gate future
 * after-school / evening / weekend content.
 */
public enum DayPhase {

    /** 7:00 AM to Block 1 start — commute and pre-school socializing. */
    PRE_SCHOOL("Before School"),

    /** Block 1 start to Block 4 end — classes, transitions, lunch. */
    SCHOOL_DAY("School Day"),

    /** Block 4 end to 6:00 PM — clubs, sports, detention (future). */
    AFTER_SCHOOL("After School"),

    /** 6:00 PM to 10:00 PM — homework, neighborhood socializing (future). */
    EVENING("Evening"),

    /** All day Saturday/Sunday (future). */
    WEEKEND("Weekend");

    private final String displayName;

    DayPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
