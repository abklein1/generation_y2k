package entity.Items;

import java.io.Serializable;
import java.util.Objects;

/**
 * A clothing item worn by a person as part of an {@link Outfit}.
 *
 * <p>Clothing is intentionally kept distinct from {@link WearableItem}
 * (which models slot-bound accessories like piercings) because outfits
 * are composed in layers and grouped at the {@code clothingType} level
 * (e.g. {@code "tops"}, {@code "bottoms"}, {@code "outerwear"}). The
 * {@code clothingType} matches the keys used in
 * {@code clique_clothing.json} and the layer keys used in
 * {@code outfit_types.json}.</p>
 *
 * <p>Optional descriptors (material, color, pattern) follow the same
 * pattern used by piercings: the JSON authors may leave them out and
 * the display will skip them gracefully.</p>
 */
public final class ClothingItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String clothingType;
    private final String layer;
    private final String bodySlot;
    private final String material;
    private final String color;
    private final String pattern;

    /**
     * @param name         display name of the garment (e.g. "band t-shirt",
     *                     "skinny jeans", "denim jacket")
     * @param clothingType the inventory category key from
     *                     {@code clique_clothing.json}
     *                     (e.g. "tops", "bottoms", "outerwear")
     * @param layer        the outfit layer this garment occupies; typically
     *                     equals {@code clothingType} but may differ when a
     *                     single inventory category contributes to multiple
     *                     layers (e.g. a base shirt vs. an overshirt)
     * @param bodySlot     coarse body region (e.g. "upper torso", "legs",
     *                     "feet"); free-form to avoid coupling to
     *                     {@link EquipmentSlot}, which is reserved for
     *                     accessory slots
     * @param material     optional fabric/material descriptor
     *                     (e.g. "denim", "cotton"); may be {@code null}
     * @param color        optional color; may be {@code null}
     * @param pattern      optional pattern descriptor
     *                     (e.g. "plaid", "striped"); may be {@code null}
     */
    public ClothingItem(String name, String clothingType, String layer,
                        String bodySlot, String material, String color,
                        String pattern) {
        this.name = name;
        this.clothingType = clothingType;
        this.layer = layer;
        this.bodySlot = bodySlot;
        this.material = material;
        this.color = color;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public String getClothingType() {
        return clothingType;
    }

    public String getLayer() {
        return layer;
    }

    public String getBodySlot() {
        return bodySlot;
    }

    public String getMaterial() {
        return material;
    }

    public String getColor() {
        return color;
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * Builds a human-readable display string for this garment.
     * Prepends color, pattern, and material qualifiers when set, skipping
     * any qualifier that already appears (case-insensitive) inside the
     * garment name so we don't produce phrases like
     * "black black band t-shirt".
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>"black band t-shirt"</li>
     *   <li>"blue plaid flannel shirt"</li>
     *   <li>"denim jeans"</li>
     * </ul>
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(color) && !nameContains(color)) {
            sb.append(color).append(" ");
        }
        if (!isBlank(pattern) && !nameContains(pattern)) {
            sb.append(pattern).append(" ");
        }
        if (!isBlank(material) && !nameContains(material)) {
            sb.append(material).append(" ");
        }
        sb.append(name);
        return sb.toString().trim();
    }

    private boolean nameContains(String value) {
        return name != null && value != null
                && name.toLowerCase().contains(value.toLowerCase());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClothingItem other)) {
            return false;
        }
        return Objects.equals(name, other.name)
                && Objects.equals(clothingType, other.clothingType)
                && Objects.equals(layer, other.layer)
                && Objects.equals(bodySlot, other.bodySlot)
                && Objects.equals(material, other.material)
                && Objects.equals(color, other.color)
                && Objects.equals(pattern, other.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clothingType, layer, bodySlot,
                material, color, pattern);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
