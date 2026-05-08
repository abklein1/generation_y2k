package utility.traits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable, generic representation of a 2-level trait JSON file:
 * {@code category -> subcategory -> [trait strings]}.
 *
 * <p>The dataset is purely structural and carries no domain semantics.
 * The category names (e.g. {@code "positive"}, {@code "neutral"},
 * {@code "negative"}, or {@code "new"}, {@code "used"}, ...) and the
 * subcategory names (e.g. {@code "eyes"}, {@code "nose"}, ...) are
 * discovered from the JSON at parse time. Any
 * {@link CategoryWeightFunction} that wants to interpret them does so
 * separately.</p>
 */
public final class TraitDataset implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> categories;
    private final List<String> subcategories;
    private final Map<String, Map<String, List<String>>> data;

    /**
     * Builds a dataset from already-parsed data.  Callers (typically
     * {@link TraitDatasetLoader}) are responsible for producing
     * defensive copies of any mutable inputs; this constructor wraps
     * everything in unmodifiable views so the dataset is safe to share.
     *
     * @param categories    encounter-ordered category names
     * @param subcategories encounter-ordered union of subcategory names
     *                      across all categories
     * @param data          {@code category -> subcategory -> traits}
     *                      map; missing cells are treated as empty
     */
    TraitDataset(List<String> categories,
                 List<String> subcategories,
                 Map<String, Map<String, List<String>>> data) {
        this.categories = Collections.unmodifiableList(new ArrayList<>(categories));
        this.subcategories = Collections.unmodifiableList(new ArrayList<>(subcategories));

        Map<String, Map<String, List<String>>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, List<String>>> e : data.entrySet()) {
            Map<String, List<String>> subCopy = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> s : e.getValue().entrySet()) {
                subCopy.put(s.getKey(),
                        Collections.unmodifiableList(new ArrayList<>(s.getValue())));
            }
            copy.put(e.getKey(), Collections.unmodifiableMap(subCopy));
        }
        this.data = Collections.unmodifiableMap(copy);
    }

    /**
     * @return the category names in the order they were encountered
     *         in the source JSON (never null)
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * @return the union of subcategory names across all categories,
     *         in encounter order (never null)
     */
    public List<String> getSubcategories() {
        return subcategories;
    }

    /**
     * Returns the trait strings for a given (category, subcategory)
     * cell, or an empty list if the cell is missing or empty.
     *
     * @param category    a category name from {@link #getCategories()}
     * @param subcategory a subcategory name from {@link #getSubcategories()}
     * @return an unmodifiable list of trait strings (never null)
     */
    public List<String> getTraits(String category, String subcategory) {
        Map<String, List<String>> subMap = data.get(category);
        if (subMap == null) {
            return List.of();
        }
        List<String> traits = subMap.get(subcategory);
        return traits == null ? List.of() : traits;
    }

    /**
     * Returns the subset of categories that actually contain at least
     * one trait for the given subcategory.  Used by the selector to
     * skip empty cells safely when a domain JSON is sparse.
     *
     * @param subcategory the subcategory to filter against
     * @return categories whose {@code (cat, subcategory)} cell is non-empty
     */
    public List<String> getCategoriesWithEntriesFor(String subcategory) {
        List<String> result = new ArrayList<>();
        for (String category : categories) {
            if (!getTraits(category, subcategory).isEmpty()) {
                result.add(category);
            }
        }
        return result;
    }
}
