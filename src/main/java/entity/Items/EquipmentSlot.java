package entity.Items;

/**
 * Defines body locations where wearable items can be equipped.
 * Each slot has a display name for natural-language descriptions
 * and a visibility flag indicating whether equipped items
 * contribute to appearance descriptions.
 */
public enum EquipmentSlot {

    LEFT_EAR("left ear", true),
    RIGHT_EAR("right ear", true),
    NOSE("nose", true),
    LIPS("lips", true),
    EYEBROW("eyebrow", true),
    TONGUE("tongue", false),
    NAVEL("navel", false),

    UPPER_TORSO("upper torso", true),
    LOWER_TORSO("lower torso", true),
    LEFT_WRIST("left wrist", true),
    RIGHT_WRIST("right wrist", true),
    NECK("neck", true);

    private final String displayName;
    private final boolean visible;

    EquipmentSlot(String displayName, boolean visible) {
        this.displayName = displayName;
        this.visible = visible;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Resolves a JSON slot key (e.g. "left ear") to the corresponding enum value.
     *
     * @param key the JSON key string
     * @return the matching EquipmentSlot, or null if no match
     */
    public static EquipmentSlot fromJsonKey(String key) {
        for (EquipmentSlot slot : values()) {
            if (slot.displayName.equals(key)) {
                return slot;
            }
        }
        return null;
    }
}
