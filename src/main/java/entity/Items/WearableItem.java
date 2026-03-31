package entity.Items;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for items that can be equipped on a body part.
 * Tracks the item's name, material, optional color, target slot,
 * and any stat modifiers it provides while equipped.
 */
public abstract class WearableItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String material;
    private final String color;
    private final EquipmentSlot slot;
    private final Map<String, Integer> statModifiers;

    /**
     * @param name     display name of the item (e.g. "small hoop", "stud")
     * @param material material descriptor (e.g. "surgical steel", "rose gold")
     * @param color    optional color coating; null when the material is self-colored
     * @param slot     body slot this item occupies
     */
    protected WearableItem(String name, String material, String color,
                           EquipmentSlot slot) {
        this.name = name;
        this.material = material;
        this.color = color;
        this.slot = slot;
        this.statModifiers = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public String getColor() {
        return color;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public Map<String, Integer> getStatModifiers() {
        return Collections.unmodifiableMap(statModifiers);
    }

    public void setStatModifier(String stat, int value) {
        statModifiers.put(stat, value);
    }

    public int getStatModifier(String stat) {
        return statModifiers.getOrDefault(stat, 0);
    }

    /**
     * Builds the full display string for this item.
     * Subclasses may override to prepend size or other qualifiers.
     * <p>
     * Examples: "rose gold stud", "black titanium small hoop"
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (color != null) {
            sb.append(color).append(" ");
        }
        sb.append(material).append(" ").append(name);
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
