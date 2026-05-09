package utility.traits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.SimConstants.STUDENT_POP_AGILITY_MEAN;
import static constants.SimConstants.STUDENT_POP_AGILITY_STANDARD_DEVIATION;
import static constants.SimConstants.STUDENT_POP_LUCK_MEAN;
import static constants.SimConstants.STUDENT_POP_LUCK_STANDARD_DEVIATION;

/**
 * {@link CategoryWeightFunction} for phones that maps phone age, owner
 * income, agility, and luck to weights over the
 * {@code excellent / good / fair / damaged} categories declared in
 * {@code cellphone_traits.json}.
 *
 * <p>Internally the inputs collapse into a single continuous "condition
 * score": phone age pushes the score down (older phones lean toward
 * worse condition), high income pushes it up (richer families maintain
 * and replace tech), high agility / luck push it up (an agile, lucky
 * owner is less likely to drop or break things).  That score is then
 * matched against four anchor points -- one per category -- and a
 * Gaussian-shaped weight is emitted around each anchor.  All four
 * weights stay strictly positive so even a beat-up phone has some
 * chance of being labelled "good", just heavily de-weighted.</p>
 *
 * <p>A null context falls back to flat 1.0 across all four buckets,
 * mirroring how {@link StudentCharismaWeightFunction} handles a null
 * student.</p>
 */
public final class PhoneConditionWeightFunction
        implements CategoryWeightFunction<PhoneConditionContext> {

    static final String CATEGORY_EXCELLENT = "excellent";
    static final String CATEGORY_GOOD = "good";
    static final String CATEGORY_FAIR = "fair";
    static final String CATEGORY_DAMAGED = "damaged";

    /**
     * Where on the condition-score axis each category lives.  Picked so
     * a brand-new phone owned by an average student lands near "good",
     * a high-income lucky student leans "excellent", and several years
     * of age + bad luck slides the distribution toward "damaged".
     */
    private static final double ANCHOR_EXCELLENT = 1.5;
    private static final double ANCHOR_GOOD = 0.5;
    private static final double ANCHOR_FAIR = -0.5;
    private static final double ANCHOR_DAMAGED = -1.5;

    /**
     * Width of the Gaussian falloff around each anchor.  Larger values
     * mean buckets bleed into each other more (less aggressive bias);
     * smaller values mean a given score very strongly prefers a single
     * bucket.  1.5 keeps neighbors viable and far buckets faint.
     */
    private static final double WEIGHT_BANDWIDTH = 1.5;

    /**
     * Symmetric clamp on the raw condition score so a 90-year-old phone
     * with terrible luck doesn't produce a score that maps to
     * essentially zero weight everywhere except the most extreme bucket.
     */
    private static final double SCORE_CLAMP = 3.0;

    @Override
    public Map<String, Double> weights(PhoneConditionContext target,
                                       List<String> categories) {
        Map<String, Double> weights = new HashMap<>();
        if (target == null) {
            weights.put(CATEGORY_EXCELLENT, 1.0);
            weights.put(CATEGORY_GOOD, 1.0);
            weights.put(CATEGORY_FAIR, 1.0);
            weights.put(CATEGORY_DAMAGED, 1.0);
            return weights;
        }

        double score = computeScore(target);

        weights.put(CATEGORY_EXCELLENT, gaussian(score, ANCHOR_EXCELLENT));
        weights.put(CATEGORY_GOOD, gaussian(score, ANCHOR_GOOD));
        weights.put(CATEGORY_FAIR, gaussian(score, ANCHOR_FAIR));
        weights.put(CATEGORY_DAMAGED, gaussian(score, ANCHOR_DAMAGED));
        return weights;
    }

    /**
     * Combines the four condition inputs into a single signed score.
     * Higher = better condition; lower = worse.  Exposed package-private
     * for testing and so {@code applyConditionTraits} can pick the
     * argmax category to label the phone with.
     *
     * @param ctx the context (must be non-null)
     * @return a clamped score in roughly {@code [-SCORE_CLAMP, +SCORE_CLAMP]}
     */
    static double computeScore(PhoneConditionContext ctx) {
        double score = 0.0;
        score -= ctx.getPhoneAgeYears();
        score += incomeShift(ctx.getIncomeLevel());
        score += zScore(ctx.getAgility(),
                STUDENT_POP_AGILITY_MEAN, STUDENT_POP_AGILITY_STANDARD_DEVIATION);
        score += zScore(ctx.getLuck(),
                STUDENT_POP_LUCK_MEAN, STUDENT_POP_LUCK_STANDARD_DEVIATION);
        return clamp(score);
    }

    /**
     * Returns the dominant (argmax) condition category for the score
     * derived from the given context.  Used to label the phone after
     * the trait selector has already drawn its lines, so the displayed
     * condition stays consistent even when the selector happens to mix
     * in lines from neighbouring buckets.
     *
     * @param ctx the context (must be non-null)
     * @return the category name with the highest weight
     */
    public static String dominantCategory(PhoneConditionContext ctx) {
        double score = ctx == null ? 0.0 : computeScore(ctx);
        String best = CATEGORY_GOOD;
        double bestWeight = -1.0;
        String[] cats = {CATEGORY_EXCELLENT, CATEGORY_GOOD,
                CATEGORY_FAIR, CATEGORY_DAMAGED};
        double[] anchors = {ANCHOR_EXCELLENT, ANCHOR_GOOD,
                ANCHOR_FAIR, ANCHOR_DAMAGED};
        for (int i = 0; i < cats.length; i++) {
            double w = gaussian(score, anchors[i]);
            if (w > bestWeight) {
                bestWeight = w;
                best = cats[i];
            }
        }
        return best;
    }

    private static double incomeShift(String incomeLevel) {
        if (incomeLevel == null) {
            return 0.0;
        }
        switch (incomeLevel) {
            case "high":
                return 1.0;
            case "low":
                return -1.0;
            default:
                return 0.0;
        }
    }

    private static double zScore(int value, int mean, int stddev) {
        if (stddev <= 0) {
            return 0.0;
        }
        return (value - mean) / (double) stddev;
    }

    private static double clamp(double v) {
        if (v > SCORE_CLAMP) {
            return SCORE_CLAMP;
        }
        if (v < -SCORE_CLAMP) {
            return -SCORE_CLAMP;
        }
        return v;
    }

    private static double gaussian(double score, double anchor) {
        double diff = score - anchor;
        return Math.exp(-(diff * diff) / WEIGHT_BANDWIDTH);
    }
}
