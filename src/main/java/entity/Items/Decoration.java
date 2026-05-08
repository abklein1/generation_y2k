package entity.Items;

import java.io.Serializable;
import java.util.Objects;

/**
 * A decorative element attached to an item rather than to a body slot.
 * Decorations are how a clique's social identity gets expressed on
 * possessions (a studded phone case, a beaded lanyard, a band sticker
 * collage on a backpack), and are deliberately a separate concern from
 * the trait/condition descriptor system.
 *
 * <p>The {@code itemType} key (e.g. {@code "cellphone"}) identifies
 * which kind of item the decoration is for and matches the keys used
 * inside {@code clique_decorations.json}.  The {@code slot} is a
 * free-form string (e.g. {@code "screen"}, {@code "case"},
 * {@code "back"}, {@code "front"}, {@code "accessories"}) so different
 * item types can declare their own decoration anchor points without
 * needing additions to {@link EquipmentSlot}, which is reserved for
 * body slots on people.</p>
 */
public final class Decoration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String itemType;
    private final String slot;
    private final String color;

    /**
     * @param name     descriptor for the decoration itself (e.g.
     *                 {@code "studded black case"},
     *                 {@code "dangling skull charm"}); may already
     *                 incorporate color/material when authored that way
     * @param itemType the item-type key the decoration belongs to
     *                 (e.g. {@code "cellphone"})
     * @param slot     the slot key on the item this decoration occupies
     *                 (e.g. {@code "case"}, {@code "accessories"})
     * @param color    optional color drawn from the clique's palette;
     *                 may be {@code null} when the {@code name} already
     *                 carries the visual descriptor
     */
    public Decoration(String name, String itemType, String slot, String color) {
        this.name = name;
        this.itemType = itemType;
        this.slot = slot;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getItemType() {
        return itemType;
    }

    public String getSlot() {
        return slot;
    }

    public String getColor() {
        return color;
    }

    /**
     * Builds a human-readable display string for this decoration.
     * If a color is set, it is prepended unless the decoration name
     * already mentions that color (case-insensitive) so we don't
     * produce phrases like "black studded black case".
     */
    public String getDisplayName() {
        if (color == null || color.isBlank()) {
            return name;
        }
        if (name != null && name.toLowerCase().contains(color.toLowerCase())) {
            return name;
        }
        return color + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Decoration other)) {
            return false;
        }
        return Objects.equals(name, other.name)
                && Objects.equals(itemType, other.itemType)
                && Objects.equals(slot, other.slot)
                && Objects.equals(color, other.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, itemType, slot, color);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
