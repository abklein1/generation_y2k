package entity.Items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A composed outfit worn by a person, organized as a list of
 * {@link ClothingItem}s grouped by layer (e.g. "outerwear", "tops",
 * "bottoms", "shoes", "accessories"). Order of insertion is preserved
 * so callers can iterate inner-to-outer when describing or rendering.
 *
 * <p>The {@code outfitType} identifies which recipe in
 * {@code outfit_types.json} produced this outfit, which downstream
 * code can use to format descriptions or to validate that required
 * layers are present.</p>
 */
public final class Outfit implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String outfitType;
    private final List<ClothingItem> items;

    /**
     * Creates an empty outfit with no recipe.
     * Useful as a default placeholder before generation runs.
     */
    public Outfit() {
        this(null);
    }

    /**
     * @param outfitType the recipe key from {@code outfit_types.json}
     *                   (e.g. "shirt_and_pants"); may be {@code null}
     */
    public Outfit(String outfitType) {
        this.outfitType = outfitType;
        this.items = new ArrayList<>();
    }

    public String getOutfitType() {
        return outfitType;
    }

    /**
     * Adds a clothing item to this outfit.
     * No layer validation happens here so callers retain control over
     * how layer limits are enforced (generation honors limits via the
     * outfit type guide).
     *
     * @param item the clothing item to add
     */
    public void addItem(ClothingItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Returns an unmodifiable view of all items in this outfit, in the
     * order they were added.
     */
    public List<ClothingItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns the items belonging to the given layer
     * (e.g. "outerwear", "tops"). Comparison is case-insensitive.
     *
     * @param layer the layer key to filter on
     * @return list of items in that layer (may be empty)
     */
    public List<ClothingItem> getItemsByLayer(String layer) {
        if (layer == null) {
            return List.of();
        }
        List<ClothingItem> matched = new ArrayList<>();
        for (ClothingItem item : items) {
            if (layer.equalsIgnoreCase(item.getLayer())) {
                matched.add(item);
            }
        }
        return matched;
    }

    /**
     * Returns a map from layer name to ordered list of items in that
     * layer. The map preserves insertion order so iteration produces
     * a stable, predictable ordering.
     */
    public Map<String, List<ClothingItem>> getItemsGroupedByLayer() {
        Map<String, List<ClothingItem>> grouped = new LinkedHashMap<>();
        for (ClothingItem item : items) {
            grouped.computeIfAbsent(item.getLayer(), k -> new ArrayList<>())
                    .add(item);
        }
        return grouped;
    }

    /**
     * Returns the total warmth of this outfit: the sum of every item's
     * warmth value. Used to match outfits against the day's weather and
     * to drive the wearer's body-temperature drift.
     */
    public int getTotalWarmth() {
        int total = 0;
        for (ClothingItem item : items) {
            total += item.getWarmth();
        }
        return total;
    }

    /**
     * Returns true when this outfit has no clothing items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    /**
     * Builds a comma-separated description of every item in this outfit
     * using each item's {@link ClothingItem#getDisplayName()}.
     *
     * <p>Example: {@code "denim jacket, black band t-shirt, skinny
     * jeans, black sneakers"}.</p>
     *
     * @return description string; empty string when the outfit is empty
     */
    public String getDescription() {
        if (items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items.get(i).getDisplayName());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
