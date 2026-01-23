package simulation.action;

/**
 * Categories of actions entities can perform.
 */
public enum ActionCategory {
    
    /**
     * Academic activities done in class.
     */
    CLASS("Class", "Actions performed during class time"),
    
    /**
     * Social interactions with other students.
     */
    SOCIAL("Social", "Interactions with other students"),
    
    /**
     * Movement between locations.
     */
    MOVEMENT("Movement", "Moving between rooms"),
    
    /**
     * Personal needs like bathroom breaks.
     */
    PERSONAL("Personal", "Personal needs and activities"),
    
    /**
     * Special actions that don't fit other categories.
     */
    SPECIAL("Special", "Special or unique actions");
    
    private final String displayName;
    private final String description;
    
    ActionCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
