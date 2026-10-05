package entity.academic;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

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
    // Added with the grading system; null in saves that predate it.
    private GradeBook gradeBook;
    private EnumMap<AssessmentType, Integer> gradeWeights;
    private String semester;
    // Attention points at the time of the last graded assessment, so prep
    // bonus only counts engagement earned since then.
    private int attentionAtLastAssessment;

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

    /** Lazily creates the gradebook so pre-grading saves keep loading. */
    public GradeBook getGradeBook() {
        if (gradeBook == null) {
            gradeBook = new GradeBook();
        }
        return gradeBook;
    }

    /**
     * Stores the grading policy (category weights and semester) from the
     * section's lesson plan so course percent and GPA can be computed without
     * re-resolving the teacher's block.
     */
    public void setGradingPolicy(Map<AssessmentType, Integer> weights, String semester) {
        if (weights != null) {
            this.gradeWeights = new EnumMap<>(weights);
        }
        if (semester != null && !semester.isBlank()) {
            this.semester = semester;
        }
    }

    public Map<AssessmentType, Integer> getGradeWeights() {
        if (gradeWeights == null) {
            return LessonPlanTemplate.forCategory(CourseCategory.fromClassName(className))
                    .getWeights();
        }
        return gradeWeights;
    }

    public String getSemester() {
        return semester;
    }

    /**
     * Live participation score derived from how engaged the student has been
     * in this class (understanding plus accumulated attention points).
     *
     * @return a 0-100 participation score
     */
    public double getParticipationScore() {
        double attentionComponent = Math.min(40.0, attentionPoints / 5.0);
        return clamp(understanding * 0.6 + attentionComponent, 0.0, 100.0);
    }

    /** Attention points earned since the course's last graded assessment. */
    public int getAttentionSinceLastAssessment() {
        return Math.max(0, attentionPoints - attentionAtLastAssessment);
    }

    /** Resets the prep-bonus window after an assessment is graded. */
    public void markAssessmentTaken() {
        attentionAtLastAssessment = attentionPoints;
    }

    /**
     * Current weighted course percent over the categories that have grades,
     * or -1 when nothing gradable has happened yet.
     *
     * @return the 0-100 course percent, or -1
     */
    public double getCurrentPercent() {
        Map<AssessmentType, Integer> weights = getGradeWeights();
        double participation = weights.containsKey(AssessmentType.PARTICIPATION)
                ? getParticipationScore() : -1.0;
        return getGradeBook().getWeightedPercent(weights, participation);
    }

    /**
     * Current letter grade, or "-" when no grade exists yet.
     *
     * @return the letter grade A-F, or "-"
     */
    public String getLetterGrade() {
        double percent = getCurrentPercent();
        return percent < 0.0 ? "-" : GradeBook.letterGradeFor(percent);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
