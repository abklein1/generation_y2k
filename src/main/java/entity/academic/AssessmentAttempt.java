package entity.academic;

import java.io.Serializable;

/**
 * Tracks how many minutes a student actually sat a given in-class assessment.
 * Students who sat too little of the window receive a missing grade instead
 * of a score.
 */
public class AssessmentAttempt implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String assessmentId;
    private int minutesSat;
    private boolean resolved;

    public AssessmentAttempt(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public int getMinutesSat() {
        return minutesSat;
    }

    /** Counts one more minute of the student sitting this assessment. */
    public void recordMinute() {
        if (!resolved) {
            minutesSat++;
        }
    }

    public boolean isResolved() {
        return resolved;
    }

    public void markResolved() {
        resolved = true;
    }
}
