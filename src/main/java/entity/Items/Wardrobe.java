package entity.Items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A student's owned clothing, organized as the ordered list of outfits
 * pre-generated at school creation. During the first school week each
 * outfit is worn once (tracked by {@link #getNextUnwornIndex()}); after
 * every outfit has been worn, daily dressing recombines the flattened
 * pool of owned {@link ClothingItem}s into new outfits.
 *
 * <p>Unworn outfits ahead of the cursor may be reordered by
 * {@link #wearBestFit(int)} so the outfit whose warmth best matches the
 * day's weather is consumed first; outfits behind the cursor are always
 * ones that have already been worn.</p>
 */
public final class Wardrobe implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Outfit> outfits;
    private int nextUnwornIndex;

    public Wardrobe() {
        this.outfits = new ArrayList<>();
        this.nextUnwornIndex = 0;
    }

    /**
     * Adds a pre-generated outfit to this wardrobe. Null or empty
     * outfits are ignored so the wardrobe only ever holds wearable sets.
     */
    public void addOutfit(Outfit outfit) {
        if (outfit != null && !outfit.isEmpty()) {
            outfits.add(outfit);
        }
    }

    /**
     * Returns an unmodifiable view of every outfit in this wardrobe,
     * worn and unworn alike.
     */
    public List<Outfit> getOutfits() {
        return Collections.unmodifiableList(outfits);
    }

    public boolean isEmpty() {
        return outfits.isEmpty();
    }

    public int size() {
        return outfits.size();
    }

    /**
     * Returns the index of the next outfit that has not yet been worn.
     * Equal to {@link #size()} once every outfit has been worn.
     */
    public int getNextUnwornIndex() {
        return nextUnwornIndex;
    }

    /**
     * Returns true while at least one pre-generated outfit has not yet
     * been worn (i.e. during the first school week).
     */
    public boolean hasUnwornOutfits() {
        return nextUnwornIndex < outfits.size();
    }

    /**
     * Wears the next unworn outfit in order, advancing the cursor.
     *
     * @return the outfit now being worn, or {@code null} when every
     *         outfit has already been worn
     */
    public Outfit wearNext() {
        if (!hasUnwornOutfits()) {
            return null;
        }
        return outfits.get(nextUnwornIndex++);
    }

    /**
     * Wears the unworn outfit whose total warmth is closest to the given
     * ideal, swapping it forward so the cursor semantics stay intact.
     *
     * @param idealWarmth the target total outfit warmth for the day
     * @return the outfit now being worn, or {@code null} when every
     *         outfit has already been worn
     */
    public Outfit wearBestFit(int idealWarmth) {
        if (!hasUnwornOutfits()) {
            return null;
        }
        int bestIndex = nextUnwornIndex;
        int bestDistance = Math.abs(
                outfits.get(bestIndex).getTotalWarmth() - idealWarmth);
        for (int i = nextUnwornIndex + 1; i < outfits.size(); i++) {
            int distance = Math.abs(
                    outfits.get(i).getTotalWarmth() - idealWarmth);
            if (distance < bestDistance) {
                bestIndex = i;
                bestDistance = distance;
            }
        }
        Collections.swap(outfits, nextUnwornIndex, bestIndex);
        return outfits.get(nextUnwornIndex++);
    }

    /**
     * Returns the flattened pool of every clothing item owned across all
     * outfits. Recombined outfits draw exclusively from this pool.
     */
    public List<ClothingItem> getAllItems() {
        List<ClothingItem> pool = new ArrayList<>();
        for (Outfit outfit : outfits) {
            pool.addAll(outfit.getItems());
        }
        return pool;
    }

    /**
     * Returns every owned item belonging to the given layer
     * (e.g. "tops", "outerwear"). Comparison is case-insensitive.
     */
    public List<ClothingItem> getItemsByLayer(String layer) {
        if (layer == null) {
            return List.of();
        }
        List<ClothingItem> matched = new ArrayList<>();
        for (Outfit outfit : outfits) {
            matched.addAll(outfit.getItemsByLayer(layer));
        }
        return matched;
    }
}
