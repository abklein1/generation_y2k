package utility.traits;

import entity.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.SimConstants.STUDENT_POP_CHARISMA_MEAN;
import static constants.SimConstants.STUDENT_POP_CHARISMA_STANDARD_DEVIATION;

/**
 * {@link CategoryWeightFunction} for {@link Student} targets that maps
 * charisma to category weights for the {@code positive / neutral /
 * negative} unique-trait dataset.
 *
 * <p>Charisma is approximately {@code N(50, 15)}, so the z-score drives
 * the bias: high-charisma students lean toward the {@code positive}
 * pool, low-charisma students lean toward {@code negative}, and
 * {@code neutral} carries a constant baseline weight so even very
 * average students still draw a healthy mix of plain descriptors.
 * This is the same math that previously lived inline in
 * {@code StudentPopGenerator.applyUniqueTraits}; the category names
 * {@code "positive"}, {@code "neutral"}, and {@code "negative"} only
 * appear here and in the JSON.</p>
 */
public final class StudentCharismaWeightFunction
        implements CategoryWeightFunction<Student> {

    static final String CATEGORY_POSITIVE = "positive";
    static final String CATEGORY_NEUTRAL = "neutral";
    static final String CATEGORY_NEGATIVE = "negative";

    @Override
    public Map<String, Double> weights(Student target, List<String> categories) {
        Map<String, Double> weights = new HashMap<>();
        if (target == null) {
            // No charisma signal -> equal weight across the three
            // sentiment buckets so generation still produces a plausible
            // mix instead of silently degenerating.
            weights.put(CATEGORY_POSITIVE, 1.0);
            weights.put(CATEGORY_NEUTRAL, 1.0);
            weights.put(CATEGORY_NEGATIVE, 1.0);
            return weights;
        }

        int charisma = target.studentStatistics.getCharisma();
        double z = (charisma - STUDENT_POP_CHARISMA_MEAN)
                / (double) STUDENT_POP_CHARISMA_STANDARD_DEVIATION;

        weights.put(CATEGORY_POSITIVE, Math.max(0.0, z));
        weights.put(CATEGORY_NEGATIVE, Math.max(0.0, -z));
        weights.put(CATEGORY_NEUTRAL, 1.0);
        return weights;
    }
}
