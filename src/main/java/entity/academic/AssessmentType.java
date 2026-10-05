package entity.academic;

import constants.SimConstants;

/**
 * Kinds of graded work a lesson plan can contain. {@code classMinutes} is how
 * long the assessment occupies class time when taken in-class; types that are
 * never sat in class (homework, participation) use zero.
 */
public enum AssessmentType {

    HOMEWORK("Homework", 0, 30, 60, false),
    QUIZ("Quiz", SimConstants.ASSESSMENT_QUIZ_MINUTES, 35, 65, true),
    TEST("Test", SimConstants.ASSESSMENT_TEST_MINUTES, 45, 75, true),
    MIDTERM("Midterm Exam", SimConstants.ASSESSMENT_EXAM_MINUTES, 55, 80, true),
    FINAL("Final Exam", SimConstants.ASSESSMENT_EXAM_MINUTES, 60, 85, true),
    PROJECT("Project", SimConstants.ASSESSMENT_PROJECT_MINUTES, 40, 70, true),
    SKILLS_EVAL("Skills Evaluation", SimConstants.ASSESSMENT_SKILLS_EVAL_MINUTES, 40, 70, true),
    PARTICIPATION("Participation", 0, 0, 0, false);

    private final String displayName;
    private final int classMinutes;
    private final int minDifficulty;
    private final int maxDifficulty;
    private final boolean scheduled;

    AssessmentType(String displayName, int classMinutes, int minDifficulty,
                   int maxDifficulty, boolean scheduled) {
        this.displayName = displayName;
        this.classMinutes = classMinutes;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
        this.scheduled = scheduled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getClassMinutes() {
        return classMinutes;
    }

    public int getMinDifficulty() {
        return minDifficulty;
    }

    public int getMaxDifficulty() {
        return maxDifficulty;
    }

    /** True when this type appears on the lesson-plan calendar. */
    public boolean isScheduled() {
        return scheduled;
    }

    /** True for midterm/final exams, which carry the longest pressure window. */
    public boolean isExam() {
        return this == MIDTERM || this == FINAL;
    }

    /** Days before the scheduled date during which students feel pressure. */
    public int getPressureWindowDays() {
        switch (this) {
        case QUIZ:
            return SimConstants.ASSESSMENT_PRESSURE_DAYS_QUIZ;
        case TEST:
            return SimConstants.ASSESSMENT_PRESSURE_DAYS_TEST;
        case MIDTERM:
        case FINAL:
            return SimConstants.ASSESSMENT_PRESSURE_DAYS_EXAM;
        case PROJECT:
        case SKILLS_EVAL:
            return SimConstants.ASSESSMENT_PRESSURE_DAYS_PROJECT;
        default:
            return 0;
        }
    }
}
