package utility;

import constants.SimConstants;
import entity.EntityState;
import entity.Staff;
import entity.Student;
import entity.TeacherTemperament;
import entity.academic.AssessmentType;
import entity.academic.CourseCategory;
import entity.academic.CourseProgress;
import entity.academic.ScheduledAssessment;
import entity.academic.StudentAcademicRecord;

/**
 * Computes a student's score on an in-class assessment. Scores are driven by
 * course understanding and skill mastery, base stats (intelligence,
 * perception, determination — or performance stats for PE/arts/vocational
 * work), current energy and stress, luck, assessment difficulty, and finally
 * the grading teacher's temperament and experience.
 */
public final class AssessmentGrader {

    private AssessmentGrader() {
    }

    /**
     * Scores one student on one assessment.
     *
     * @param student    the student being graded
     * @param teacher    the grading teacher (null-safe)
     * @param className  the class the assessment belongs to
     * @param category   the course category, which decides the scoring formula
     * @param assessment the assessment being graded
     * @return a 0-100 score
     */
    public static double score(Student student, Staff teacher, String className,
                               CourseCategory category, ScheduledAssessment assessment) {
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        CourseProgress course = record.getOrCreateCourse(className);

        double raw = knowledgeTerm(student, record, course, className, category, assessment.getType())
                + baseStatTerm(student)
                + prepBonus(course)
                - energyPenalty(student)
                - stressPenalty(student)
                + luckNoise(student)
                - (assessment.getDifficulty() - 50.0) * SimConstants.GRADING_DIFFICULTY_SLOPE;

        raw = applyTeacherAdjustment(raw, teacher);
        return clamp(raw, 0.0, 100.0);
    }

    /**
     * Picks the formula for what the assessment actually measures: knowledge
     * for quizzes/tests/exams, physical performance for PE skills work, and
     * creative or practical output for projects.
     */
    private static double knowledgeTerm(Student student, StudentAcademicRecord record,
                                        CourseProgress course, String className,
                                        CourseCategory category, AssessmentType type) {
        double understanding = course.getUnderstanding();
        boolean performanceFinal = type == AssessmentType.FINAL
                && category == CourseCategory.PHYSICAL_EDUCATION;
        boolean projectFinal = type == AssessmentType.FINAL && isProjectCategory(category);

        if (type == AssessmentType.SKILLS_EVAL || performanceFinal) {
            return 20.0
                    + student.studentStatistics.getStrength() * 0.3
                    + student.studentStatistics.getAgility() * 0.5
                    + student.studentStatistics.getDetermination() * 0.2
                    + understanding * 0.2;
        }
        if (type == AssessmentType.PROJECT || projectFinal) {
            if (category == CourseCategory.VISUAL_ARTS
                    || category == CourseCategory.PERFORMING_ARTS) {
                return 15.0
                        + student.studentStatistics.getCreativity() * 0.4
                        + student.studentStatistics.getPerception() * 0.2
                        + understanding * 0.3;
            }
            return 15.0
                    + student.studentStatistics.getIntelligence() * 0.25
                    + student.studentStatistics.getInitiative() * 0.2
                    + student.studentStatistics.getAdaptability() * 0.15
                    + understanding * 0.3;
        }

        double skill = record.getSkillMastery(AcademicProgressService.inferSkill(className));
        return SimConstants.GRADING_KNOWLEDGE_BASE
                + understanding * SimConstants.GRADING_UNDERSTANDING_WEIGHT
                + skill * SimConstants.GRADING_SKILL_WEIGHT;
    }

    private static boolean isProjectCategory(CourseCategory category) {
        return category == CourseCategory.VISUAL_ARTS
                || category == CourseCategory.PERFORMING_ARTS
                || category == CourseCategory.COMPUTER_SCIENCE
                || category == CourseCategory.VOCATIONAL
                || category == CourseCategory.BUSINESS
                || category == CourseCategory.CONSUMER_SCIENCE;
    }

    private static double baseStatTerm(Student student) {
        return (student.studentStatistics.getIntelligence() - 100) / 4.0
                + (student.studentStatistics.getPerception() - 50) / 10.0
                + (student.studentStatistics.getDetermination() - 50) / 10.0;
    }

    private static double prepBonus(CourseProgress course) {
        double fraction = Math.min(1.0, course.getAttentionSinceLastAssessment()
                / (double) SimConstants.GRADING_PREP_ATTENTION_POINTS_FOR_MAX);
        return fraction * SimConstants.GRADING_PREP_BONUS_MAX;
    }

    private static double energyPenalty(Student student) {
        EntityState state = student.getEntityState();
        if (state == null) {
            return 0.0;
        }
        double energy = state.getEnergy();
        if (energy >= SimConstants.GRADING_LOW_ENERGY_THRESHOLD) {
            return 0.0;
        }
        double deficit = (SimConstants.GRADING_LOW_ENERGY_THRESHOLD - energy)
                / SimConstants.GRADING_LOW_ENERGY_THRESHOLD;
        return deficit * SimConstants.GRADING_LOW_ENERGY_MAX_PENALTY;
    }

    private static double stressPenalty(Student student) {
        if (student.studentStatistics.getAllostaticLoad() == null) {
            return 0.0;
        }
        double loadPercent = student.studentStatistics.getAllostaticLoad().getLoadPercentage();
        if (loadPercent <= 70.0) {
            return 0.0;
        }
        return Math.min(1.0, (loadPercent - 70.0) / 30.0)
                * SimConstants.GRADING_STRESS_MAX_PENALTY;
    }

    /** Lucky students get narrower, slightly favorable noise; unlucky the reverse. */
    private static double luckNoise(Student student) {
        int luck = student.studentStatistics.getLuck();
        double spread = clamp(1.0 - luck / 150.0, 0.5, 1.5)
                * SimConstants.GRADING_BASE_NOISE_STD_DEV;
        return GameRandom.nextGaussian(luck / 10.0, spread);
    }

    /**
     * Teacher's hand on the red pen: temperament biases the score, experience
     * shrinks grading noise and finds partial credit on weak work, and erratic
     * teachers get noisier the more stressed they are.
     */
    private static double applyTeacherAdjustment(double raw, Staff teacher) {
        if (teacher == null || teacher.teacherStatistics == null) {
            return raw;
        }

        TeacherTemperament temperament = teacher.teacherStatistics.getTemperament();
        double experienceRatio = clamp(teacher.teacherStatistics.getYearsOfExperience()
                / (double) SimConstants.ACADEMIC_TEACHER_EXPERIENCE_CAP_YEARS, 0.0, 1.0);

        double noiseStdDev = (SimConstants.GRADING_TEACHER_NOISE_MAX
                - (SimConstants.GRADING_TEACHER_NOISE_MAX - SimConstants.GRADING_TEACHER_NOISE_MIN)
                * experienceRatio)
                * temperament.getVarianceMultiplier();
        if (temperament == TeacherTemperament.ERRATIC
                && teacher.teacherStatistics.getAllostaticLoad() != null) {
            noiseStdDev *= 1.0
                    + teacher.teacherStatistics.getAllostaticLoad().getLoadPercentage() / 100.0;
        }

        double adjusted = raw + temperament.getGradeBias()
                + GameRandom.nextGaussian(0.0, noiseStdDev);
        if (adjusted < 60.0) {
            adjusted += experienceRatio * SimConstants.GRADING_TEACHER_PARTIAL_CREDIT_MAX;
        }
        return adjusted;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
