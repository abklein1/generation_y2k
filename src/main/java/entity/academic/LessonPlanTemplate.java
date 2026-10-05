package entity.academic;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Per-category grading structure: category weights (percent of final grade),
 * how many of each scheduled assessment a semester contains, and the homework
 * cadence. Gym and electives keep a performance-based structure instead of the
 * core quiz/test/exam ladder.
 */
public final class LessonPlanTemplate {

    private final Map<AssessmentType, Integer> weights;
    private final int quizCount;
    private final int testCount;
    private final int projectCount;
    private final int skillsEvalCount;
    private final boolean hasMidterm;
    private final boolean hasFinal;
    private final AssessmentType finalType;
    private final int homeworkIntervalDays;

    private LessonPlanTemplate(Map<AssessmentType, Integer> weights, int quizCount,
                               int testCount, int projectCount, int skillsEvalCount,
                               boolean hasMidterm, boolean hasFinal, AssessmentType finalType,
                               int homeworkIntervalDays) {
        this.weights = Collections.unmodifiableMap(weights);
        this.quizCount = quizCount;
        this.testCount = testCount;
        this.projectCount = projectCount;
        this.skillsEvalCount = skillsEvalCount;
        this.hasMidterm = hasMidterm;
        this.hasFinal = hasFinal;
        this.finalType = finalType;
        this.homeworkIntervalDays = homeworkIntervalDays;
    }

    /**
     * Returns the grading template for a course category.
     *
     * @param category the course category
     * @return the matching template (GENERAL gets a lighter core structure)
     */
    public static LessonPlanTemplate forCategory(CourseCategory category) {
        switch (category == null ? CourseCategory.GENERAL : category) {
        case CORE_ACADEMIC:
            return new LessonPlanTemplate(
                    weights(AssessmentType.HOMEWORK, 20, AssessmentType.QUIZ, 20,
                            AssessmentType.TEST, 30, AssessmentType.MIDTERM, 10,
                            AssessmentType.FINAL, 20),
                    6, 3, 0, 0, true, true, AssessmentType.FINAL, 2);
        case WORLD_LANGUAGE:
            return new LessonPlanTemplate(
                    weights(AssessmentType.HOMEWORK, 20, AssessmentType.QUIZ, 30,
                            AssessmentType.TEST, 20, AssessmentType.PROJECT, 10,
                            AssessmentType.FINAL, 20),
                    8, 2, 1, 0, false, true, AssessmentType.FINAL, 2);
        case PHYSICAL_EDUCATION:
            return new LessonPlanTemplate(
                    weights(AssessmentType.PARTICIPATION, 50, AssessmentType.SKILLS_EVAL, 40,
                            AssessmentType.QUIZ, 10),
                    2, 0, 0, 3, false, true, AssessmentType.SKILLS_EVAL, 0);
        case VISUAL_ARTS:
        case PERFORMING_ARTS:
            return new LessonPlanTemplate(
                    weights(AssessmentType.PARTICIPATION, 30, AssessmentType.PROJECT, 50,
                            AssessmentType.QUIZ, 10, AssessmentType.FINAL, 10),
                    2, 0, 4, 0, false, true, AssessmentType.FINAL, 5);
        case COMPUTER_SCIENCE:
        case VOCATIONAL:
        case BUSINESS:
        case CONSUMER_SCIENCE:
            return new LessonPlanTemplate(
                    weights(AssessmentType.PARTICIPATION, 25, AssessmentType.PROJECT, 40,
                            AssessmentType.QUIZ, 15, AssessmentType.FINAL, 20),
                    3, 0, 3, 0, false, true, AssessmentType.FINAL, 4);
        case GENERAL:
        default:
            return new LessonPlanTemplate(
                    weights(AssessmentType.HOMEWORK, 25, AssessmentType.QUIZ, 25,
                            AssessmentType.TEST, 30, AssessmentType.FINAL, 20),
                    4, 2, 0, 0, false, true, AssessmentType.FINAL, 3);
        }
    }

    private static Map<AssessmentType, Integer> weights(Object... pairs) {
        EnumMap<AssessmentType, Integer> map = new EnumMap<>(AssessmentType.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((AssessmentType) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }

    public Map<AssessmentType, Integer> getWeights() {
        return weights;
    }

    public int getQuizCount() {
        return quizCount;
    }

    public int getTestCount() {
        return testCount;
    }

    public int getProjectCount() {
        return projectCount;
    }

    public int getSkillsEvalCount() {
        return skillsEvalCount;
    }

    public boolean hasMidterm() {
        return hasMidterm;
    }

    public boolean hasFinal() {
        return hasFinal;
    }

    /** The type used for the end-of-semester final (exam, project, or skills eval). */
    public AssessmentType getFinalType() {
        return finalType;
    }

    /** Days between homework assignments; 0 means the class assigns none. */
    public int getHomeworkIntervalDays() {
        return homeworkIntervalDays;
    }
}
