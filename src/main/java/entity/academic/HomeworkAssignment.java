package entity.academic;

import java.io.Serializable;

/**
 * A lightweight homework unit assigned by a scheduled course.
 */
public class HomeworkAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String courseKey;
    private final String className;
    private final int effort;
    private final int problemCount;
    private final int assignedDay;
    private final int dueDay;
    private double progress;
    private boolean completed;
    private boolean missing;

    public HomeworkAssignment(String courseKey, String className, int effort,
                              int problemCount, int assignedDay, int dueDay) {
        this.courseKey = courseKey;
        this.className = className;
        this.effort = Math.max(1, effort);
        this.problemCount = Math.max(1, problemCount);
        this.assignedDay = assignedDay;
        this.dueDay = dueDay;
    }

    public String getCourseKey() {
        return courseKey;
    }

    public String getClassName() {
        return className;
    }

    public int getEffort() {
        return effort;
    }

    public int getProblemCount() {
        return problemCount;
    }

    public int getAssignedDay() {
        return assignedDay;
    }

    public int getDueDay() {
        return dueDay;
    }

    public double getProgress() {
        return progress;
    }

    public void addProgress(double amount) {
        if (isResolved()) {
            return;
        }
        progress = Math.max(0.0, Math.min(100.0, progress + amount));
        if (progress >= 100.0) {
            completed = true;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        progress = 100.0;
        completed = true;
        missing = false;
    }

    public boolean isMissing() {
        return missing;
    }

    public void markMissing() {
        missing = true;
        completed = false;
    }

    public boolean isResolved() {
        return completed || missing;
    }

    public boolean isDueOnOrBefore(int day) {
        return !isResolved() && dueDay <= day;
    }
}
