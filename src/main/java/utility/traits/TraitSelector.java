package utility.traits;

import utility.GameRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure utility that draws a list of trait strings out of a
 * {@link TraitDataset} for some target, biased by a
 * {@link CategoryWeightFunction}.
 *
 * <p>The selection algorithm picks a number of distinct subcategories
 * (e.g. for a student, distinct body parts), then for each subcategory
 * rolls a category from the weight function and finally picks a single
 * trait string from that {@code (category, subcategory)} cell.  Empty
 * cells are skipped: if the chosen category has no entries for a given
 * subcategory, the selector reweights across the remaining categories
 * that do have entries, falling back to uniform when the weight
 * function provides no positive weights at all.</p>
 *
 * <p>All randomness flows through {@link GameRandom} so simulations
 * remain seed-reproducible.</p>
 */
public final class TraitSelector {

    private TraitSelector() {
    }

    /**
     * Selects a list of trait strings from the dataset, drawing each
     * from a distinct subcategory.  The caller supplies an inclusive
     * count window; the actual count is rolled within that window and
     * may shrink if the dataset has fewer subcategories or if some
     * subcategories have no eligible category cells.
     *
     * @param dataset     the trait dataset to draw from
     * @param target      the target whose stats drive category weights;
     *                    may be {@code null} if the weight function
     *                    accepts null targets
     * @param weightFn    the category weight function for this domain
     * @param minCount    the minimum number of traits to draw (inclusive)
     * @param maxCount    the maximum number of traits to draw (inclusive)
     * @param <T>         the target type
     * @return a freshly-allocated list of trait strings, possibly empty
     */
    public static <T> List<String> selectTraits(TraitDataset dataset,
                                                T target,
                                                CategoryWeightFunction<T> weightFn,
                                                int minCount,
                                                int maxCount) {
        if (dataset == null || weightFn == null) {
            return new ArrayList<>();
        }

        List<String> categories = dataset.getCategories();
        Map<String, Double> weights = normalizeWeights(
                weightFn.weights(target, categories), categories);

        int traitCount = GameRandom.nextInt(minCount, maxCount);

        List<String> subcategories = new ArrayList<>(dataset.getSubcategories());
        GameRandom.shuffle(subcategories);

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < traitCount && i < subcategories.size(); i++) {
            String subcategory = subcategories.get(i);
            String category = pickCategoryForSubcategory(
                    dataset, subcategory, weights);
            if (category == null) {
                continue;
            }
            List<String> traits = dataset.getTraits(category, subcategory);
            if (traits.isEmpty()) {
                continue;
            }
            selected.add(traits.get(GameRandom.nextInt(traits.size())));
        }
        return selected;
    }

    /**
     * A weight function that returns equal weight for every category.
     * Useful as a default for datasets whose domain hasn't yet
     * registered a domain-specific weight function.
     *
     * @param <T> the target type (ignored)
     * @return a weight function yielding uniform weights
     */
    public static <T> CategoryWeightFunction<T> uniformWeights() {
        return (target, categories) -> {
            Map<String, Double> map = new HashMap<>();
            for (String category : categories) {
                map.put(category, 1.0);
            }
            return map;
        };
    }

    /**
     * Picks a category name for the given subcategory using the
     * supplied (already-normalized) weights.  Categories without
     * entries for the subcategory are excluded.  If no positively-
     * weighted category has entries, falls back to a uniform draw over
     * any category that does have entries.
     *
     * @return the chosen category name, or {@code null} if no category
     *         in the dataset has entries for this subcategory
     */
    private static String pickCategoryForSubcategory(TraitDataset dataset,
                                                     String subcategory,
                                                     Map<String, Double> weights) {
        List<String> eligible = dataset.getCategoriesWithEntriesFor(subcategory);
        if (eligible.isEmpty()) {
            return null;
        }

        double total = 0.0;
        for (String category : eligible) {
            total += Math.max(0.0, weights.getOrDefault(category, 0.0));
        }

        double roll = GameRandom.nextDouble();
        if (total <= 0.0) {
            int idx = (int) Math.floor(roll * eligible.size());
            if (idx >= eligible.size()) {
                idx = eligible.size() - 1;
            }
            return eligible.get(idx);
        }

        double cumulative = 0.0;
        double target = roll * total;
        for (String category : eligible) {
            cumulative += Math.max(0.0, weights.getOrDefault(category, 0.0));
            if (target < cumulative) {
                return category;
            }
        }
        return eligible.get(eligible.size() - 1);
    }

    /**
     * Returns a copy of {@code raw} that:
     * <ul>
     *   <li>contains every category in {@code categories} (missing keys default to 0.0)</li>
     *   <li>clamps negative weights to 0.0</li>
     * </ul>
     * The result is unnormalized; the actual normalization happens
     * per-subcategory in {@link #pickCategoryForSubcategory} so empty
     * cells can be re-weighted out cleanly.
     */
    private static Map<String, Double> normalizeWeights(Map<String, Double> raw,
                                                        List<String> categories) {
        Map<String, Double> result = new HashMap<>();
        for (String category : categories) {
            double w = 0.0;
            if (raw != null) {
                Double v = raw.get(category);
                if (v != null && v > 0.0) {
                    w = v;
                }
            }
            result.put(category, w);
        }
        return result;
    }
}
