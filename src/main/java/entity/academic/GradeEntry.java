package entity.academic;

import java.io.Serializable;

/**
 * One graded result in a course gradebook. Missing entries score zero and are
 * flagged so the UI can distinguish "bombed it" from "never took it".
 */
public class GradeEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AssessmentType type;
    private final String title;
    private final double score;
    private final int day;
    private final boolean missing;

    /**
     * Creates a graded result; the score is clamped to 0-100.
     *
     * @param type the assessment category this grade counts toward
     * @param title display title of the assessment
     * @param score earned score, 0-100
     * @param day simulation day the grade was recorded
     * @param missing true when the student never sat the assessment
     */
    public GradeEntry(AssessmentType type, String title, double score, int day, boolean missing) {
        this.type = type;
        this.title = title;
        this.score = Math.max(0.0, Math.min(100.0, score));
        this.day = day;
        this.missing = missing;
    }

    public AssessmentType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public double getScore() {
        return score;
    }

    public int getDay() {
        return day;
    }

    public boolean isMissing() {
        return missing;
    }
}
