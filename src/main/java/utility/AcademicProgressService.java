package utility;

import constants.SimConstants;
import entity.ActivityType;
import entity.Staff;
import entity.Student;
import entity.StudentBlock;
import entity.TeacherBlock;
import entity.Time;
import entity.academic.AcademicSkill;
import entity.academic.AssessmentType;
import entity.academic.CourseProgress;
import entity.academic.GradeEntry;
import entity.academic.HomeworkAssignment;
import entity.academic.LessonPlan;
import entity.academic.ScheduledAssessment;
import entity.academic.StudentAcademicRecord;

import java.util.List;

/**
 * Central service for lightweight learning, skills, and homework effects.
 */
public final class AcademicProgressService {

    private static final String GENERAL_STUDY = "General Study";

    private AcademicProgressService() {
    }

    public static double recordClassLearning(Student student, String className,
                                             int learningAmount, ActivityType activityType) {
        return recordClassLearning(student, className, null, learningAmount, activityType);
    }

    public static double recordCurrentClassLearning(Student student, Time time,
                                                    int learningAmount, ActivityType activityType) {
        StudentBlock block = getCurrentClassBlock(student, time);
        if (block == null) {
            return 0.0;
        }
        String className = block.getClassName();
        return recordClassLearning(student, className, block, learningAmount, activityType);
    }

    public static double recordClassLearning(Student student, String className, StudentBlock block,
                                             int learningAmount, ActivityType activityType) {
        if (student == null || student.studentStatistics == null || learningAmount <= 0) {
            return 0.0;
        }

        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        CourseProgress course = record.getOrCreateCourse(className);

        double scaledLearning = learningAmount
                * calculateLearningMultiplier(student)
                * calculateTeacherLearningMultiplier(block == null ? null : block.getTeacher())
                * SimConstants.ACADEMIC_ATTENTION_UNDERSTANDING_SCALE;
        course.addUnderstanding(scaledLearning);
        course.addAttentionPoints(learningAmount);
        record.addSkillMastery(inferSkill(className), scaledLearning * 0.5);
        student.studentStatistics.setExperience((int) Math.max(1, Math.round(scaledLearning)));

        if (activityType == ActivityType.ATTENDING_CLASS) {
            drainAttentionPools(student, SimConstants.STAT_DRAIN_PAY_ATTENTION_INITIATIVE,
                    SimConstants.STAT_DRAIN_PAY_ATTENTION_RESPONSIBILITY,
                    calculateTeacherStressMultiplier(block == null ? null : block.getTeacher()));
        } else if (activityType == ActivityType.TAKING_NOTES) {
            drainAttentionPools(student, SimConstants.STAT_DRAIN_TAKE_NOTES_INITIATIVE,
                    SimConstants.STAT_DRAIN_TAKE_NOTES_RESPONSIBILITY,
                    calculateTeacherStressMultiplier(block == null ? null : block.getTeacher()));
        }

        return scaledLearning;
    }

    public static int assignHomeworkIfDue(Student student, Time time) {
        if (student == null || student.studentStatistics == null || time == null
                || time.getDayCounter() <= 0) {
            return 0;
        }

        int assignedCount = 0;
        int day = time.getDayCounter();
        String semester = time.getCurrentSemester();
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        List<StudentBlock> blocks = student.studentStatistics.getStudentSchedule().getClassScheduleCopy();
        for (StudentBlock block : blocks) {
            if (!isAssignableClassBlock(block, semester)) {
                continue;
            }

            // The section's lesson plan controls cadence: PE assigns none,
            // electives assign less often, and nothing is assigned on a day
            // the class already has a test or project. Sections without a
            // plan keep the legacy fixed interval.
            LessonPlan plan = getLessonPlanFor(block);
            int interval = plan == null
                    ? SimConstants.ACADEMIC_HOMEWORK_ASSIGNMENT_INTERVAL_DAYS
                    : plan.getHomeworkIntervalDays();
            if (interval <= 0 || day % interval != 0) {
                continue;
            }
            if (plan != null && plan.getAssessmentForDay(day) != null) {
                continue;
            }

            String className = block.getClassName();
            if (record.hasOpenHomeworkForCourse(className)
                    || record.hasHomeworkAssignedForCourseOnDay(className, day)) {
                continue;
            }

            CourseProgress course = record.getOrCreateCourse(className);
            if (plan != null) {
                course.setGradingPolicy(plan.getWeights(), block.getSemester());
            }
            HomeworkAssignment homework = createHomework(block, course, day);
            record.addHomework(homework);
            course.recordHomeworkAssigned();
            assignedCount++;
        }
        return assignedCount;
    }

    public static int resolveHomeworkForDay(Student student, int day) {
        if (student == null || student.studentStatistics == null) {
            return 0;
        }

        int completedCount = 0;
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        boolean apathetic = isAcademicallyApathetic(student);
        for (HomeworkAssignment homework : record.getPendingHomework()) {
            if (!homework.isDueOnOrBefore(day)) {
                continue;
            }

            CourseProgress course = record.getOrCreateCourse(homework.getClassName());
            GradeEntry entry = gradeHomework(student, course, homework.getEffort(),
                    homework.getProblemCount(), homework.getAssignedDay(), day, apathetic);
            drainHomeworkPools(student);
            if (!entry.isMissing()) {
                homework.markCompleted();
                course.recordHomeworkCompleted();
                course.addUnderstanding(SimConstants.ACADEMIC_HOMEWORK_UNDERSTANDING_GAIN);
                record.addSkillMastery(inferSkill(homework.getClassName()),
                        SimConstants.ACADEMIC_HOMEWORK_SKILL_GAIN);
                completedCount++;
            } else {
                homework.markMissing();
                course.recordHomeworkMissing();
            }
            student.studentStatistics.setNewGrade((int) entry.getScore());
            course.getGradeBook().addEntry(entry);
        }
        return completedCount;
    }

    /**
     * Grades one homework assignment without side effects: the completion
     * score is computed from the student's traits and the supplied course
     * state, then mapped to a graded or missing {@link GradeEntry}. Used by
     * the live homework resolver and by the history generator against scratch
     * course state.
     *
     * @param student      the student doing the homework
     * @param course       course state supplying understanding
     * @param effort       the assignment's effort rating
     * @param problemCount the assignment's problem count
     * @param assignedDay  day the homework was assigned (for the entry title)
     * @param day          day the grade is recorded
     * @param apathetic    true when the student is academically apathetic
     * @return the resulting grade entry (missing=true when not completed)
     */
    public static GradeEntry gradeHomework(Student student, CourseProgress course, int effort,
                                           int problemCount, int assignedDay, int day,
                                           boolean apathetic) {
        double completionScore = calculateHomeworkCompletionScore(student, course,
                effort, problemCount);
        if (apathetic) {
            completionScore -= SimConstants.GRADING_APATHY_HOMEWORK_PENALTY;
        }
        if (completionScore >= 50.0) {
            int grade = (int) Math.min(100.0, 70.0 + completionScore / 3.0);
            return new GradeEntry(AssessmentType.HOMEWORK,
                    "Homework (day " + assignedDay + ")", grade, day, false);
        }
        int grade = (int) Math.max(0.0, 65.0 - (50.0 - completionScore) / 2.0);
        return new GradeEntry(AssessmentType.HOMEWORK,
                "Homework (day " + assignedDay + ")", grade, day, true);
    }

    /**
     * Students with rock-bottom determination and responsibility simply do
     * not care about school work: they feel no pre-assessment pressure and
     * put less effort into homework.
     *
     * @param student the student to check
     * @return true when the student is academically apathetic
     */
    public static boolean isAcademicallyApathetic(Student student) {
        if (student == null || student.studentStatistics == null) {
            return false;
        }
        int determination = student.studentStatistics.getDetermination();
        int responsibility = student.studentStatistics.getResponsibility();
        return (determination < SimConstants.GRADING_APATHY_DETERMINATION_MAX
                && responsibility < SimConstants.GRADING_APATHY_RESPONSIBILITY_MAX)
                || (determination + responsibility) / 2 < SimConstants.GRADING_APATHY_COMBINED_MAX;
    }

    public static boolean hasAcademicPressure(Student student, String className) {
        if (student == null || student.studentStatistics == null) {
            return false;
        }

        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        CourseProgress course = record.getCourse(className);
        if (record.hasOpenHomeworkForCourse(className)) {
            return true;
        }
        return course != null
                && course.getUnderstanding() < SimConstants.ACADEMIC_LOW_UNDERSTANDING_THRESHOLD
                && course.getAssignedHomework() > 0;
    }

    /**
     * Time-aware pressure check: apathetic students never feel it, and
     * everyone else also feels pressure when a quiz/test/exam/project on the
     * class's lesson plan is close. Daydreamer-type students only react to
     * exams; students currently holding a D or F react one day earlier.
     *
     * @param student   the student
     * @param className the class in question
     * @param time      the game clock (null falls back to homework-only checks)
     * @return true when the student should prioritize academics right now
     */
    public static boolean hasAcademicPressure(Student student, String className, Time time) {
        if (student == null || student.studentStatistics == null) {
            return false;
        }
        if (isAcademicallyApathetic(student)) {
            return false;
        }
        if (hasAcademicPressure(student, className)) {
            return true;
        }
        if (time == null) {
            return false;
        }

        StudentBlock block = findScheduledBlock(student, className, time.getCurrentSemester());
        LessonPlan plan = getLessonPlanFor(block);
        if (plan == null) {
            return false;
        }

        boolean daydreamerLike = student.studentStatistics.getCuriosity() >= 60
                && student.studentStatistics.getDetermination() < 45;
        CourseProgress course = student.studentStatistics.getAcademicRecord().getCourse(className);
        double percent = course == null ? -1.0 : course.getCurrentPercent();
        boolean strugglingGrade = percent >= 0.0 && percent < 70.0;

        int day = time.getDayCounter();
        for (ScheduledAssessment assessment : plan.getAssessments()) {
            if (assessment.isGraded() || assessment.getScheduledDay() < day) {
                continue;
            }
            if (daydreamerLike && !assessment.getType().isExam()) {
                continue;
            }
            int window = assessment.getType().getPressureWindowDays() + (strugglingGrade ? 1 : 0);
            if (assessment.getScheduledDay() - day <= window) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the teacher-side block (and therefore the lesson plan) that
     * matches a student's scheduled block.
     *
     * @param block the student's scheduled block
     * @return the matching teacher block, or null
     */
    public static TeacherBlock findTeacherBlock(StudentBlock block) {
        if (block == null || block.getTeacher() == null
                || block.getTeacher().teacherStatistics == null
                || block.getTeacher().teacherStatistics.getTeacherSchedule() == null) {
            return null;
        }
        for (TeacherBlock teacherBlock
                : block.getTeacher().teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
            if (teacherBlock.getBlockNumber() == block.getBlockNumber()
                    && teacherBlock.getClassName() != null
                    && teacherBlock.getClassName().equals(block.getClassName())
                    && teacherBlock.getSemester() != null
                    && teacherBlock.getSemester().equals(block.getSemester())) {
                return teacherBlock;
            }
        }
        return null;
    }

    /**
     * Returns the lesson plan governing a student's scheduled block, or null
     * when the section has none.
     *
     * @param block the student's scheduled block
     * @return the lesson plan, or null
     */
    public static LessonPlan getLessonPlanFor(StudentBlock block) {
        TeacherBlock teacherBlock = findTeacherBlock(block);
        return teacherBlock == null ? null : teacherBlock.getLessonPlan();
    }

    /**
     * Finds the student's scheduled block for a class in the given semester.
     *
     * @param student   the student
     * @param className the class name
     * @param semester  "Fall" or "Spring"
     * @return the matching block, or null
     */
    public static StudentBlock findScheduledBlock(Student student, String className, String semester) {
        if (student == null || student.studentStatistics == null || className == null) {
            return null;
        }
        for (StudentBlock block
                : student.studentStatistics.getStudentSchedule().getClassScheduleCopy()) {
            if (block != null && className.equals(block.getClassName())
                    && (semester == null || semester.equals(block.getSemester()))) {
                return block;
            }
        }
        return null;
    }

    public static String getCurrentClassName(Student student, Time time) {
        StudentBlock block = getCurrentClassBlock(student, time);
        if (block == null || block.getClassName() == null || block.getClassName().isBlank()) {
            return GENERAL_STUDY;
        }
        return block.getClassName();
    }

    public static StudentBlock getCurrentClassBlock(Student student, Time time) {
        if (student == null || student.studentStatistics == null || time == null) {
            return null;
        }
        return student.studentStatistics.getStudentSchedule()
                .getByBlockNumber(time.getCurrentPeriod(), time.getCurrentSemester());
    }

    public static AcademicSkill inferSkill(String className) {
        String lower = className == null ? "" : className.toLowerCase();
        if (lower.contains("math") || lower.contains("algebra") || lower.contains("geometry")
                || lower.contains("calculus")) {
            return AcademicSkill.MATH;
        }
        if (lower.contains("english") || lower.contains("literature") || lower.contains("reading")) {
            return AcademicSkill.READING;
        }
        if (lower.contains("writing") || lower.contains("composition")) {
            return AcademicSkill.WRITING;
        }
        if (lower.contains("science") || lower.contains("biology") || lower.contains("chemistry")
                || lower.contains("physics")) {
            return AcademicSkill.SCIENCE;
        }
        if (lower.contains("history") || lower.contains("government") || lower.contains("civics")) {
            return AcademicSkill.HISTORY;
        }
        return AcademicSkill.GENERAL_STUDY;
    }

    private static boolean isAssignableClassBlock(StudentBlock block, String semester) {
        return block != null
                && !block.isLunch()
                && block.getClassName() != null
                && !block.getClassName().isBlank()
                && block.getSemester().equals(semester);
    }

    private static HomeworkAssignment createHomework(StudentBlock block, CourseProgress course, int day) {
        int effort = calculateHomeworkEffort(block.getTeacher(), course.getAssignedHomework());
        int problems = calculateHomeworkProblems(effort);
        return new HomeworkAssignment(
                StudentAcademicRecord.normalizeCourseKey(block.getClassName()),
                block.getClassName(),
                effort,
                problems,
                day,
                day + SimConstants.ACADEMIC_HOMEWORK_DUE_DAYS);
    }

    private static double calculateLearningMultiplier(Student student) {
        double intelligence = (student.studentStatistics.getIntelligence() - 100) / 100.0;
        double determination = (student.studentStatistics.getDetermination() - 50) / 100.0;
        double perception = (student.studentStatistics.getPerception() - 50) / 120.0;
        double multiplier = 1.0 + intelligence + determination + perception;
        return Math.max(0.35, Math.min(2.25, multiplier));
    }

    private static double calculateTeacherLearningMultiplier(Staff teacher) {
        if (teacher == null || teacher.teacherStatistics == null) {
            return 1.0;
        }

        double experienceRatio = clamp(teacher.teacherStatistics.getYearsOfExperience()
                / (double) SimConstants.ACADEMIC_TEACHER_EXPERIENCE_CAP_YEARS, 0.0, 1.0);
        double teacherAbility = ((teacher.teacherStatistics.getIntelligence() - 100) / 200.0)
                + ((teacher.teacherStatistics.getPerception() - 50) / 200.0);
        double multiplier = 1.0
                + experienceRatio * SimConstants.ACADEMIC_TEACHER_MAX_LEARNING_BONUS
                + teacherAbility;
        return clamp(multiplier, 0.75, 1.45);
    }

    private static double calculateTeacherStressMultiplier(Staff teacher) {
        if (teacher == null || teacher.teacherStatistics == null) {
            return 1.0;
        }

        double experienceRatio = clamp(teacher.teacherStatistics.getYearsOfExperience()
                / (double) SimConstants.ACADEMIC_TEACHER_EXPERIENCE_CAP_YEARS, 0.0, 1.0);
        double clarity = ((teacher.teacherStatistics.getIntelligence() - 100) / 250.0)
                + ((teacher.teacherStatistics.getPerception() - 50) / 250.0);
        double reduction = experienceRatio * SimConstants.ACADEMIC_TEACHER_MAX_STRESS_REDUCTION
                + Math.max(0.0, clarity);
        return clamp(1.0 - reduction, 0.55, 1.15);
    }

    private static int calculateHomeworkEffort(Staff teacher, int assignedHomeworkCount) {
        if (teacher == null || teacher.teacherStatistics == null) {
            return GameRandom.nextInt(SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT,
                    SimConstants.ACADEMIC_HOMEWORK_MAX_EFFORT);
        }

        int min = SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT;
        int max = SimConstants.ACADEMIC_HOMEWORK_MAX_EFFORT;
        double experienceRatio = clamp(teacher.teacherStatistics.getYearsOfExperience()
                / (double) SimConstants.ACADEMIC_TEACHER_EXPERIENCE_CAP_YEARS, 0.0, 1.0);
        int teacherClarity = Math.max(0,
                (teacher.teacherStatistics.getIntelligence() - 100)
                        + (teacher.teacherStatistics.getPerception() - 50));
        int variance = Math.max(1, (int) Math.round((max - min) * (1.0 - experienceRatio)
                - teacherClarity / 20.0));
        int ramp = Math.min(max - min,
                assignedHomeworkCount * SimConstants.ACADEMIC_TEACHER_CURRICULUM_RAMP_STEP);
        int target = min + ramp;
        return clampInt(target + GameRandom.nextInt(-variance, variance), min, max);
    }

    private static int calculateHomeworkProblems(int effort) {
        int span = SimConstants.ACADEMIC_HOMEWORK_MAX_EFFORT - SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT;
        double ratio = span == 0 ? 0.0
                : (effort - SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT) / (double) span;
        int problemSpan = SimConstants.ACADEMIC_HOMEWORK_MAX_PROBLEMS
                - SimConstants.ACADEMIC_HOMEWORK_MIN_PROBLEMS;
        return SimConstants.ACADEMIC_HOMEWORK_MIN_PROBLEMS
                + (int) Math.round(problemSpan * ratio);
    }

    private static double calculateHomeworkCompletionScore(Student student, CourseProgress course,
                                                           int effort, int problemCount) {
        double ability = student.studentStatistics.getIntelligence() * 0.25
                + student.studentStatistics.getDetermination() * 0.25
                + student.studentStatistics.getPerception() * 0.15
                + student.studentStatistics.getInitiative() * 0.20
                + student.studentStatistics.getResponsibility() * 0.15;
        double workloadPenalty = Math.max(0, effort - 18) * 1.1
                + Math.max(0, problemCount - 8) * 0.7;
        return ability + course.getUnderstanding() * 0.45 - workloadPenalty;
    }

    private static void drainAttentionPools(Student student, int initiativeDrain, int responsibilityDrain,
                                            double stressMultiplier) {
        student.studentStatistics.drainSecondaryStat("initiative", initiativeDrain,
                SimConstants.ALLOSTATIC_STRESS_FACTOR_INITIATIVE * stressMultiplier);
        student.studentStatistics.drainSecondaryStat("responsibility", responsibilityDrain,
                SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY * stressMultiplier);
    }

    private static void drainHomeworkPools(Student student) {
        student.studentStatistics.drainSecondaryStat("initiative",
                SimConstants.ACADEMIC_HOMEWORK_INITIATIVE_DRAIN,
                SimConstants.ALLOSTATIC_STRESS_FACTOR_INITIATIVE);
        student.studentStatistics.drainSecondaryStat("responsibility",
                SimConstants.ACADEMIC_HOMEWORK_RESPONSIBILITY_DRAIN,
                SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
