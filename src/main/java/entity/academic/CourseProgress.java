package entity.academic;

import java.io.Serializable;

/**
 * In-memory academic progress for one scheduled class.
 */
public class CourseProgress implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double MIN_UNDERSTANDING = 0.0;
    private static final double MAX_UNDERSTANDING = 100.0;

    private final String courseKey;
    private final String className;
    private double understanding;
    private int attentionPoints;
    private int assignedHomework;
    private int completedHomework;
    private int missingHomework;

    public CourseProgress(String courseKey, String className) {
        this.courseKey = courseKey;
        this.className = className;
    }

    public String getCourseKey() {
        return courseKey;
    }

    public String getClassName() {
        return className;
    }

    public double getUnderstanding() {
        return understanding;
    }

    public void addUnderstanding(double amount) {
        understanding = clamp(understanding + amount, MIN_UNDERSTANDING, MAX_UNDERSTANDING);
    }

    public int getAttentionPoints() {
        return attentionPoints;
    }

    public void addAttentionPoints(int points) {
        attentionPoints += Math.max(0, points);
    }

    public int getAssignedHomework() {
        return assignedHomework;
    }

    public void recordHomeworkAssigned() {
        assignedHomework++;
    }

    public int getCompletedHomework() {
        return completedHomework;
    }

    public void recordHomeworkCompleted() {
        completedHomework++;
    }

    public int getMissingHomework() {
        return missingHomework;
    }

    public void recordHomeworkMissing() {
        missingHomework++;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
