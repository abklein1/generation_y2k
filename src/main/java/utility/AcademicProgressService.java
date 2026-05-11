package utility;

import constants.SimConstants;
import entity.ActivityType;
import entity.Staff;
import entity.Student;
import entity.StudentBlock;
import entity.Time;
import entity.academic.AcademicSkill;
import entity.academic.CourseProgress;
import entity.academic.HomeworkAssignment;
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
                || !isHomeworkAssignmentDay(time.getDayCounter())) {
            return 0;
        }

        int assignedCount = 0;
        String semester = time.getCurrentSemester();
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        List<StudentBlock> blocks = student.studentStatistics.getStudentSchedule().getClassScheduleCopy();
        for (StudentBlock block : blocks) {
            if (!isAssignableClassBlock(block, semester)) {
                continue;
            }

            String className = block.getClassName();
            if (record.hasOpenHomeworkForCourse(className)
                    || record.hasHomeworkAssignedForCourseOnDay(className, time.getDayCounter())) {
                continue;
            }

            CourseProgress course = record.getOrCreateCourse(className);
            HomeworkAssignment homework = createHomework(block, course, time.getDayCounter());
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
        for (HomeworkAssignment homework : record.getPendingHomework()) {
            if (!homework.isDueOnOrBefore(day)) {
                continue;
            }

            CourseProgress course = record.getOrCreateCourse(homework.getClassName());
            double completionScore = calculateHomeworkCompletionScore(student, course, homework);
            drainHomeworkPools(student);

            if (completionScore >= 50.0) {
                homework.markCompleted();
                course.recordHomeworkCompleted();
                course.addUnderstanding(SimConstants.ACADEMIC_HOMEWORK_UNDERSTANDING_GAIN);
                record.addSkillMastery(inferSkill(homework.getClassName()),
                        SimConstants.ACADEMIC_HOMEWORK_SKILL_GAIN);
                student.studentStatistics.setNewGrade((int) Math.min(100.0, 70.0 + completionScore / 3.0));
                completedCount++;
            } else {
                homework.markMissing();
                course.recordHomeworkMissing();
                student.studentStatistics.setNewGrade((int) Math.max(0.0, 65.0 - (50.0 - completionScore) / 2.0));
            }
        }
        return completedCount;
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

    private static boolean isHomeworkAssignmentDay(int dayCounter) {
        return dayCounter > 0
                && dayCounter % SimConstants.ACADEMIC_HOMEWORK_ASSIGNMENT_INTERVAL_DAYS == 0;
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
                                                           HomeworkAssignment homework) {
        double ability = student.studentStatistics.getIntelligence() * 0.25
                + student.studentStatistics.getDetermination() * 0.25
                + student.studentStatistics.getPerception() * 0.15
                + student.studentStatistics.getInitiative() * 0.20
                + student.studentStatistics.getResponsibility() * 0.15;
        double workloadPenalty = Math.max(0, homework.getEffort() - 18) * 1.1
                + Math.max(0, homework.getProblemCount() - 8) * 0.7;
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
