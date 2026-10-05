package entity;

/**
 * A teacher's grading disposition. Affects lesson-plan difficulty and
 * scheduling jitter, plus a per-score bias and noise multiplier when grading.
 */
public enum TeacherTemperament {

    STRICT("Strict", -3.0, 0.8),
    BALANCED("Balanced", 0.0, 1.0),
    LENIENT("Lenient", 4.0, 0.9),
    ERRATIC("Erratic", 0.0, 1.8);

    private final String displayName;
    private final double gradeBias;
    private final double varianceMultiplier;

    TeacherTemperament(String displayName, double gradeBias, double varianceMultiplier) {
        this.displayName = displayName;
        this.gradeBias = gradeBias;
        this.varianceMultiplier = varianceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Flat points added to (lenient) or removed from (strict) every score. */
    public double getGradeBias() {
        return gradeBias;
    }

    /** Multiplier on grading noise; erratic teachers are far less consistent. */
    public double getVarianceMultiplier() {
        return varianceMultiplier;
    }
}
