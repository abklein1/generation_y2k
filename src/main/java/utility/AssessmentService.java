package utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.SimConstants;
import entity.Staff;
import entity.Student;
import entity.TeacherBlock;
import entity.Time;
import entity.academic.AssessmentAttempt;
import entity.academic.CourseProgress;
import entity.academic.GradeEntry;
import entity.academic.LessonPlan;
import entity.academic.ScheduledAssessment;
import entity.academic.StudentAcademicRecord;

/**
 * Runs scheduled assessments during the school day: when a class period
 * begins on an assessment's scheduled day the assessment goes in-progress
 * (students in that section switch to TAKING_ASSESSMENT via their behavior
 * trees), and when its window elapses — or the period ends — every enrolled
 * student is graded and the result lands in their course gradebook.
 */
public final class AssessmentService {

    private AssessmentService() {
    }

    /**
     * Advances assessment state for one simulation tick.
     *
     * @param staffMap the school's staff
     * @param time     the game clock
     */
    public static void processTick(HashMap<Integer, Staff> staffMap, Time time) {
        if (staffMap == null || time == null) {
            return;
        }

        int day = time.getDayCounter();
        int currentPeriod = time.getCurrentPeriod();
        int nowMinutes = time.getMinutesFromMidnight();
        String semester = time.getCurrentSemester();

        for (Map.Entry<Integer, Staff> entry : staffMap.entrySet()) {
            Staff teacher = entry.getValue();
            if (teacher == null || teacher.teacherStatistics == null
                    || teacher.teacherStatistics.getTeacherSchedule() == null) {
                continue;
            }
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                LessonPlan plan = block.getLessonPlan();
                if (plan == null || block.getSemester() == null
                        || !block.getSemester().equals(semester)) {
                    continue;
                }

                ScheduledAssessment inProgress = plan.getInProgressAssessment();
                if (inProgress != null) {
                    boolean windowElapsed = nowMinutes - inProgress.getStartMinutesFromMidnight()
                            >= inProgress.getType().getClassMinutes();
                    boolean periodOver = currentPeriod != block.getBlockNumber();
                    if (windowElapsed || periodOver) {
                        gradeAssessment(teacher, block, plan, inProgress, day);
                    }
                    continue;
                }

                if (currentPeriod == block.getBlockNumber()) {
                    ScheduledAssessment today = plan.getAssessmentForDay(day);
                    if (today != null && today.getStatus() == ScheduledAssessment.Status.SCHEDULED) {
                        today.begin(nowMinutes);
                        GameLogger.logDebug("[ASSESSMENT] " + today.getTitle() + " begins in "
                                + block.getClassName() + " (Block " + block.getBlockNumber()
                                + ", " + teacherName(teacher) + ")");
                    }
                }
            }
        }
    }

    /**
     * Grades every student enrolled in the section. Students who sat less
     * than the attendance threshold get a missing (zero) entry instead of a
     * score. Graded students receive a small understanding/skill bump from
     * the testing effect.
     */
    private static void gradeAssessment(Staff teacher, TeacherBlock block, LessonPlan plan,
                                        ScheduledAssessment assessment, int day) {
        List<Student> students = block.getClassPopulation();
        double scoreTotal = 0.0;
        int scoredCount = 0;
        int missingCount = 0;

        int requiredMinutes = (int) Math.ceil(assessment.getType().getClassMinutes()
                * SimConstants.ASSESSMENT_MIN_ATTENDANCE_FRACTION);

        if (students != null) {
            for (Student student : students) {
                if (student == null || student.studentStatistics == null) {
                    continue;
                }
                StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
                CourseProgress course = record.getOrCreateCourse(block.getClassName());
                course.setGradingPolicy(plan.getWeights(), block.getSemester());

                AssessmentAttempt attempt = record.getAttempt(assessment.getId());
                int minutesSat = attempt == null ? 0 : attempt.getMinutesSat();
                if (attempt != null) {
                    attempt.markResolved();
                }

                if (minutesSat < requiredMinutes) {
                    course.getGradeBook().addEntry(new GradeEntry(assessment.getType(),
                            assessment.getTitle(), 0.0, day, true));
                    student.studentStatistics.setNewGrade(0);
                    missingCount++;
                } else {
                    double score = AssessmentGrader.score(student, teacher, block.getClassName(),
                            plan.getCategory(), assessment);
                    course.getGradeBook().addEntry(new GradeEntry(assessment.getType(),
                            assessment.getTitle(), score, day, false));
                    student.studentStatistics.setNewGrade((int) Math.round(score));

                    // Testing effect: taking the assessment cements a little
                    // of the material.
                    course.addUnderstanding(SimConstants.ASSESSMENT_UNDERSTANDING_BUMP);
                    record.addSkillMastery(
                            AcademicProgressService.inferSkill(block.getClassName()),
                            SimConstants.ASSESSMENT_SKILL_BUMP);

                    scoreTotal += score;
                    scoredCount++;
                }
                course.markAssessmentTaken();
            }
        }

        double classAverage = scoredCount == 0 ? 0.0 : scoreTotal / scoredCount;
        assessment.markGraded(classAverage);
        GameLogger.logDebug(String.format(
                "[ASSESSMENT] %s graded in %s (Block %d, %s): avg %.1f over %d student(s), %d missing",
                assessment.getTitle(), block.getClassName(), block.getBlockNumber(),
                teacherName(teacher), classAverage, scoredCount, missingCount));
    }

    /**
     * Finds the in-progress assessment for a student's current class, if any.
     *
     * @param student the student
     * @param time    the game clock
     * @return the active assessment, or null
     */
    public static ScheduledAssessment getActiveAssessmentFor(Student student, Time time) {
        entity.StudentBlock block = AcademicProgressService.getCurrentClassBlock(student, time);
        LessonPlan plan = AcademicProgressService.getLessonPlanFor(block);
        return plan == null ? null : plan.getInProgressAssessment();
    }

    private static String teacherName(Staff teacher) {
        if (teacher == null || teacher.teacherName == null) {
            return "unknown teacher";
        }
        return teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName();
    }
}
