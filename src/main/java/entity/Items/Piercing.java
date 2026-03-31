package entity.Items;

/**
 * A piercing item that can be equipped in ear, nose, lip, eyebrow,
 * tongue, or navel slots. Extends WearableItem with an optional
 * size qualifier used for gauges and hoops.
 */
public class Piercing extends WearableItem {

    private static final long serialVersionUID = 1L;

    private final String size;

    /**
     * @param name     piercing style name (e.g. "stud", "small hoop", "snakebites")
     * @param material material descriptor (e.g. "surgical steel", "rose gold")
     * @param color    optional color; null when the material is self-colored
     * @param slot     body slot this piercing occupies
     * @param size     optional size qualifier (e.g. "small", "00g"); null if N/A
     */
    public Piercing(String name, String material, String color,
                    EquipmentSlot slot, String size) {
        super(name, material, color, slot);
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    /**
     * Builds the full display string, incorporating size when present.
     * <p>
     * Examples:
     * <ul>
     *   <li>"rose gold stud"</li>
     *   <li>"black titanium small hoop"</li>
     *   <li>"small, black gauge"</li>
     *   <li>"gunmetal 00g gauge"</li>
     * </ul>
     */
    @Override
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();

        if (size != null) {
            sb.append(size);
            sb.append(isGauge() ? ", " : " ");
        }

        if (getColor() != null) {
            sb.append(getColor()).append(" ");
        }
        sb.append(getMaterial()).append(" ").append(getName());
        return sb.toString();
    }

    private boolean isGauge() {
        String n = getName();
        return n != null && n.toLowerCase().contains("gauge");
    }
}
