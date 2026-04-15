package entity;

/**
 * Enum defining what an entity can currently be doing.
 * Used by the simulation engine to track entity activities.
 */
public enum ActivityType {
    
    /**
     * Entity is paying attention in class.
     */
    ATTENDING_CLASS("Attending Class", true, false),
    
    /**
     * Entity is zoning out, not paying attention.
     */
    DAYDREAMING("Daydreaming", true, false),
    
    /**
     * Entity is talking to peers.
     */
    SOCIALIZING("Socializing", false, false),
    
    /**
     * Entity is moving between rooms.
     */
    TRANSITIONING("Transitioning", false, true),
    
    /**
     * Entity is using the restroom.
     */
    IN_BATHROOM("In Bathroom", false, false),
    
    /**
     * Entity is eating lunch in the cafeteria.
     */
    EATING_LUNCH("Eating Lunch", false, false),
    
    /**
     * Entity is not where they should be.
     */
    SKIPPING("Skipping", false, false),
    
    /**
     * Entity is taking notes in class.
     */
    TAKING_NOTES("Taking Notes", true, false),
    
    /**
     * Entity is asking a question in class.
     */
    ASKING_QUESTION("Asking Question", true, false),
    
    /**
     * Entity is passing a note to another student.
     */
    PASSING_NOTE("Passing Note", true, false),
    
    /**
     * Entity is whispering to another student.
     */
    WHISPERING("Whispering", true, false),
    
    /**
     * Entity is talking to another student.
     * Can happen in class (risky) or in hallways/lunchrooms (normal).
     */
    TALKING("Talking", false, false),
    
    /**
     * Entity is texting another student on their cell phone.
     * Can happen in class (risky, but quieter than talking) or outside class.
     */
    TEXTING("Texting", false, false),
    
    /**
     * Entity is studying in the library or study hall.
     */
    STUDYING("Studying", true, false),
    
    /**
     * Entity is idle/waiting.
     */
    IDLE("Idle", false, false),
    
    /**
     * Entity is at their locker.
     */
    AT_LOCKER("At Locker", false, false),
    
    /**
     * Entity is teaching (staff only).
     */
    TEACHING("Teaching", true, false),
    
    /**
     * Entity is supervising (staff only).
     */
    SUPERVISING("Supervising", false, false);
    
    private final String displayName;
    private final boolean requiresClassroom;
    private final boolean isMovement;
    
    /**
     * Creates an activity type.
     *
     * @param displayName the human-readable name
     * @param requiresClassroom if this activity must occur in a classroom
     * @param isMovement if this activity involves movement between locations
     */
    ActivityType(String displayName, boolean requiresClassroom, boolean isMovement) {
        this.displayName = displayName;
        this.requiresClassroom = requiresClassroom;
        this.isMovement = isMovement;
    }
    
    /**
     * Gets the display name of this activity.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this activity must occur in a classroom.
     *
     * @return true if requires classroom
     */
    public boolean requiresClassroom() {
        return requiresClassroom;
    }
    
    /**
     * Checks if this activity involves movement.
     *
     * @return true if movement activity
     */
    public boolean isMovement() {
        return isMovement;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
