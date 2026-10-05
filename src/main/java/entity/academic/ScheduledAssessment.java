package entity.academic;

import java.io.Serializable;

/**
 * One dated assessment on a lesson plan. Status moves from SCHEDULED to
 * IN_PROGRESS when the class period begins on the scheduled day, then to
 * GRADED once scores are recorded.
 */
public class ScheduledAssessment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Lifecycle of a scheduled assessment. */
    public enum Status {
        SCHEDULED, IN_PROGRESS, GRADED
    }

    private final String id;
    private final AssessmentType type;
    private final String title;
    private final int scheduledDay;
    private final int difficulty;
    private Status status;
    private double classAverage;
    private int startMinutesFromMidnight = -1;

    /**
     * Creates a scheduled assessment; difficulty is clamped to 1-100.
     *
     * @param id stable unique id used to track per-student attempts
     * @param type the kind of assessment
     * @param title display title
     * @param scheduledDay simulation day counter the assessment is held on
     * @param difficulty difficulty rating, 1-100
     */
    public ScheduledAssessment(String id, AssessmentType type, String title,
                               int scheduledDay, int difficulty) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.scheduledDay = scheduledDay;
        this.difficulty = Math.max(1, Math.min(100, difficulty));
        this.status = Status.SCHEDULED;
    }

    public String getId() {
        return id;
    }

    public AssessmentType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getScheduledDay() {
        return scheduledDay;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isGraded() {
        return status == Status.GRADED;
    }

    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }

    /** Marks the assessment as underway, recording when it started. */
    public void begin(int minutesFromMidnight) {
        this.status = Status.IN_PROGRESS;
        this.startMinutesFromMidnight = minutesFromMidnight;
    }

    /** Marks the assessment graded and stores the class average. */
    public void markGraded(double classAverage) {
        this.status = Status.GRADED;
        this.classAverage = classAverage;
    }

    public double getClassAverage() {
        return classAverage;
    }

    public int getStartMinutesFromMidnight() {
        return startMinutesFromMidnight;
    }
}
