package entity.academic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A teacher's semester plan for one class section: the resolved grading
 * weights, homework cadence, and the dated list of quizzes, tests, exams,
 * projects, and skills evaluations.
 */
public class LessonPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CourseCategory category;
    private final EnumMap<AssessmentType, Integer> weights;
    private final int homeworkIntervalDays;
    private final List<ScheduledAssessment> assessments;

    /**
     * Creates a lesson plan; assessments are sorted by scheduled day.
     *
     * @param category the course category this plan was built for
     * @param weights grading weight per assessment type (percent)
     * @param homeworkIntervalDays school days between homework assignments, 0 for none
     * @param assessments dated assessments for the semester
     */
    public LessonPlan(CourseCategory category, Map<AssessmentType, Integer> weights,
                      int homeworkIntervalDays, List<ScheduledAssessment> assessments) {
        this.category = category;
        this.weights = new EnumMap<>(AssessmentType.class);
        this.weights.putAll(weights);
        this.homeworkIntervalDays = homeworkIntervalDays;
        this.assessments = new ArrayList<>(assessments);
        this.assessments.sort((a, b) -> Integer.compare(a.getScheduledDay(), b.getScheduledDay()));
    }

    public CourseCategory getCategory() {
        return category;
    }

    public Map<AssessmentType, Integer> getWeights() {
        return Collections.unmodifiableMap(weights);
    }

    /** Days between homework assignments; 0 means this class assigns none. */
    public int getHomeworkIntervalDays() {
        return homeworkIntervalDays;
    }

    public List<ScheduledAssessment> getAssessments() {
        return Collections.unmodifiableList(assessments);
    }

    /**
     * Returns the assessment scheduled for a given day, if any. Lesson plans
     * never place two assessments on the same day for one section.
     *
     * @param day the day counter
     * @return the assessment for that day, or null
     */
    public ScheduledAssessment getAssessmentForDay(int day) {
        for (ScheduledAssessment assessment : assessments) {
            if (assessment.getScheduledDay() == day) {
                return assessment;
            }
        }
        return null;
    }

    /**
     * Returns the next ungraded assessment scheduled on or after a day.
     *
     * @param day the day counter to search from
     * @return the next upcoming assessment, or null when none remain
     */
    public ScheduledAssessment getNextAssessment(int day) {
        for (ScheduledAssessment assessment : assessments) {
            if (!assessment.isGraded() && assessment.getScheduledDay() >= day) {
                return assessment;
            }
        }
        return null;
    }

    /** True when any assessment is currently in progress. */
    public ScheduledAssessment getInProgressAssessment() {
        for (ScheduledAssessment assessment : assessments) {
            if (assessment.isInProgress()) {
                return assessment;
            }
        }
        return null;
    }
}
