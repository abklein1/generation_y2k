package utility.traits;

import java.util.List;
import java.util.Map;

/**
 * Maps a target object's stats to a weight per trait category, used by
 * {@link TraitSelector} to bias which category traits are drawn from.
 *
 * <p>Implementations encode the domain-specific knowledge that links a
 * target's stats to category names declared in the JSON.  For example,
 * a student weight function maps charisma to
 * {@code positive / neutral / negative} weights, while a hypothetical
 * cell phone weight function might map condition to
 * {@code new / used / worn / damaged} weights.</p>
 *
 * <p>Returned weights need not be normalized; the selector handles
 * normalization.  Categories the function does not include in the
 * returned map are treated as weight {@code 0.0} (eligible only as a
 * last-resort fallback when no positively-weighted category has
 * entries for a given subcategory).</p>
 *
 * @param <T> the target type whose stats drive the weighting
 */
@FunctionalInterface
public interface CategoryWeightFunction<T> {

    /**
     * Computes the unnormalized category weights for the given target.
     *
     * @param target     the target instance whose stats drive the weights;
     *                   may be {@code null} for stat-free defaults
     * @param categories the categories declared by the active dataset,
     *                   in encounter order
     * @return a map from category name to a non-negative weight; entries
     *         absent from the map are treated as {@code 0.0}
     */
    Map<String, Double> weights(T target, List<String> categories);
}
