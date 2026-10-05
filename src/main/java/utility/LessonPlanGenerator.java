package utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import constants.SimConstants;
import entity.Staff;
import entity.TeacherBlock;
import entity.TeacherTemperament;
import entity.academic.AssessmentType;
import entity.academic.CourseCategory;
import entity.academic.LessonPlan;
import entity.academic.LessonPlanTemplate;
import entity.academic.ScheduledAssessment;

/**
 * Builds each teacher's semester lesson plans at simulation start. Assessment
 * mix depends on the class type (core classes get quizzes/tests/exams; PE and
 * electives get skills evaluations and projects), and the teacher's experience
 * and temperament shape dates and difficulty. Parallel sections of the same
 * course taught by the same teacher share assessment dates.
 */
public final class LessonPlanGenerator {

    /** Settling-in days at the start of a semester with no assessments. */
    private static final int SEMESTER_LEAD_IN_DAYS = 7;
    /** Buffer days kept clear just before finals week. */
    private static final int PRE_FINALS_BUFFER_DAYS = 3;
    private static final double DIFFICULTY_SEMESTER_RAMP = 10.0;
    private static final int TEMPERAMENT_DIFFICULTY_SHIFT = 5;

    private LessonPlanGenerator() {
    }

    /**
     * Ensures every teaching block in the staff map has a lesson plan.
     * Idempotent: blocks that already carry a plan (e.g. from a save) are
     * skipped, so this is safe to call on every startup and load.
     *
     * @param staffMap the school's staff keyed by id
     * @return the number of blocks that received a new plan
     */
    public static int ensureLessonPlans(HashMap<Integer, Staff> staffMap) {
        if (staffMap == null) {
            return 0;
        }

        int plansCreated = 0;
        for (Map.Entry<Integer, Staff> entry : staffMap.entrySet()) {
            plansCreated += ensureLessonPlansForTeacher(entry.getKey(), entry.getValue());
        }
        if (plansCreated > 0) {
            GameLogger.logGeneration("Generated lesson plans for " + plansCreated + " class section(s)");
        }
        return plansCreated;
    }

    private static int ensureLessonPlansForTeacher(int staffId, Staff teacher) {
        if (teacher == null || teacher.teacherStatistics == null
                || teacher.teacherStatistics.getTeacherSchedule() == null) {
            return 0;
        }

        // Group blocks by course+semester so parallel sections share dates.
        Map<String, List<TeacherBlock>> sections = new LinkedHashMap<>();
        for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
            if (block == null || block.getClassName() == null || block.getClassName().isBlank()
                    || block.getSemester() == null || block.getSemester().isBlank()) {
                continue;
            }
            String key = block.getClassName() + "|" + block.getSemester();
            sections.computeIfAbsent(key, k -> new ArrayList<>()).add(block);
        }

        int plansCreated = 0;
        for (List<TeacherBlock> blocks : sections.values()) {
            TeacherBlock first = blocks.get(0);
            String className = first.getClassName();
            String semester = first.getSemester();

            CourseCategory category = CourseCategory.fromClassName(className);
            LessonPlanTemplate template = LessonPlanTemplate.forCategory(category);
            List<PlannedItem> items = planAssessments(template, semester, teacher);

            for (TeacherBlock block : blocks) {
                if (block.getLessonPlan() != null) {
                    continue;
                }
                block.setLessonPlan(new LessonPlan(category, template.getWeights(),
                        template.getHomeworkIntervalDays(),
                        materialize(items, staffId, className, semester, block.getBlockNumber())));
                plansCreated++;
            }
        }
        return plansCreated;
    }

    /** A dated assessment before it is copied into per-section instances. */
    private static final class PlannedItem {
        final AssessmentType type;
        final String title;
        final int day;
        final int difficulty;

        PlannedItem(AssessmentType type, String title, int day, int difficulty) {
            this.type = type;
            this.title = title;
            this.day = day;
            this.difficulty = difficulty;
        }
    }

    private static List<ScheduledAssessment> materialize(List<PlannedItem> items, int staffId,
                                                         String className, String semester,
                                                         int blockNumber) {
        List<ScheduledAssessment> assessments = new ArrayList<>(items.size());
        String courseKey = entity.academic.StudentAcademicRecord.normalizeCourseKey(className);
        int index = 0;
        for (PlannedItem item : items) {
            String id = "t" + staffId + "_" + courseKey + "_" + semester.toLowerCase(Locale.ROOT)
                    + "_b" + blockNumber + "_" + index++;
            assessments.add(new ScheduledAssessment(id, item.type, item.title,
                    item.day, item.difficulty));
        }
        return assessments;
    }

    private static List<PlannedItem> planAssessments(LessonPlanTemplate template, String semester,
                                                     Staff teacher) {
        AcademicCalendar.SemesterBounds bounds = AcademicCalendar.getSemesterBounds(semester);
        AcademicCalendar.SemesterBounds finals = AcademicCalendar.getFinalsWindow(semester);
        int windowStart = bounds.getFirstDay() + SEMESTER_LEAD_IN_DAYS;
        int windowEnd = finals.getFirstDay() - PRE_FINALS_BUFFER_DAYS;

        TeacherTemperament temperament = teacher.teacherStatistics.getTemperament();
        double experienceRatio = clamp(teacher.teacherStatistics.getYearsOfExperience()
                / (double) SimConstants.ACADEMIC_TEACHER_EXPERIENCE_CAP_YEARS, 0.0, 1.0);
        int jitter = (int) Math.round(SimConstants.ASSESSMENT_MAX_DATE_JITTER_DAYS
                * (1.0 - experienceRatio));
        if (temperament == TeacherTemperament.ERRATIC) {
            jitter += SimConstants.ASSESSMENT_ERRATIC_EXTRA_JITTER_DAYS;
        }

        int quizCount = template.getQuizCount();
        if (temperament == TeacherTemperament.STRICT) {
            quizCount++;
        } else if (temperament == TeacherTemperament.LENIENT && quizCount > 1) {
            quizCount--;
        }

        Set<Integer> usedDays = new HashSet<>();
        List<PlannedItem> items = new ArrayList<>();

        // Midterm snaps to the quarter boundary.
        if (template.hasMidterm()) {
            int day = reserveDay(AcademicCalendar.getQuarterEnd(semester), 0,
                    windowStart, windowEnd, usedDays);
            items.add(item(AssessmentType.MIDTERM, "Midterm Exam", day, 0.5,
                    temperament, experienceRatio));
        }

        // Unit tests at even fractions of the instructional window.
        for (int i = 0; i < template.getTestCount(); i++) {
            double fraction = (i + 1) / (double) (template.getTestCount() + 1);
            int target = windowStart + (int) Math.round((windowEnd - windowStart) * fraction);
            int day = reserveDay(target, jitter, windowStart, windowEnd, usedDays);
            items.add(item(AssessmentType.TEST, "Unit " + (i + 1) + " Test", day, fraction,
                    temperament, experienceRatio));
        }

        for (int i = 0; i < quizCount; i++) {
            double fraction = (i + 1) / (double) (quizCount + 1);
            int target = windowStart + (int) Math.round((windowEnd - windowStart) * fraction);
            int day = reserveDay(target, jitter, windowStart, windowEnd, usedDays);
            items.add(item(AssessmentType.QUIZ, "Quiz " + (i + 1), day, fraction,
                    temperament, experienceRatio));
        }

        for (int i = 0; i < template.getProjectCount(); i++) {
            double fraction = (i + 1) / (double) (template.getProjectCount() + 1);
            int target = windowStart + (int) Math.round((windowEnd - windowStart) * fraction);
            int day = reserveDay(target, jitter, windowStart, windowEnd, usedDays);
            items.add(item(AssessmentType.PROJECT, "Project " + (i + 1), day, fraction,
                    temperament, experienceRatio));
        }

        for (int i = 0; i < template.getSkillsEvalCount(); i++) {
            double fraction = (i + 1) / (double) (template.getSkillsEvalCount() + 1);
            int target = windowStart + (int) Math.round((windowEnd - windowStart) * fraction);
            int day = reserveDay(target, jitter, windowStart, windowEnd, usedDays);
            items.add(item(AssessmentType.SKILLS_EVAL, "Skills Evaluation " + (i + 1), day,
                    fraction, temperament, experienceRatio));
        }

        // Final lands inside the finals window. Its type comes from the
        // template so PE's final counts into the skills-eval weight while
        // core/elective finals use the FINAL weight.
        if (template.hasFinal()) {
            int target = finals.getFirstDay()
                    + GameRandom.nextInt(0, Math.max(0, finals.getLastDay() - finals.getFirstDay()));
            int day = reserveDay(target, 0, finals.getFirstDay(), finals.getLastDay(), usedDays);
            AssessmentType finalType = template.getFinalType();
            String title = finalType == AssessmentType.FINAL ? "Final Exam"
                    : "Final " + finalType.getDisplayName();
            items.add(new PlannedItem(finalType, title, day,
                    resolveDifficulty(AssessmentType.FINAL, 1.0, temperament, experienceRatio)));
        }

        return items;
    }

    private static PlannedItem item(AssessmentType type, String title, int day,
                                    double semesterFraction, TeacherTemperament temperament,
                                    double experienceRatio) {
        return new PlannedItem(type, title, day,
                resolveDifficulty(type, semesterFraction, temperament, experienceRatio));
    }

    /**
     * Picks a difficulty inside the type's band, ramping up through the
     * semester. Experienced teachers calibrate (narrow spread); strict ones
     * push difficulty up, lenient ones down.
     */
    private static int resolveDifficulty(AssessmentType type, double semesterFraction,
                                         TeacherTemperament temperament, double experienceRatio) {
        int min = type.getMinDifficulty();
        int max = type.getMaxDifficulty();
        int base = GameRandom.nextInt(min, Math.max(min, max));
        double calibrated = base * experienceRatio
                + ((min + max) / 2.0) * (1.0 - experienceRatio) * 0.4
                + base * (1.0 - experienceRatio) * 0.6;
        double ramp = semesterFraction * DIFFICULTY_SEMESTER_RAMP;
        double shift = 0.0;
        if (temperament == TeacherTemperament.STRICT) {
            shift = TEMPERAMENT_DIFFICULTY_SHIFT;
        } else if (temperament == TeacherTemperament.LENIENT) {
            shift = -TEMPERAMENT_DIFFICULTY_SHIFT;
        }
        return (int) clamp(Math.round(calibrated + ramp + shift), 1, 100);
    }

    /**
     * Applies jitter, snaps to a weekday, clamps to the window, and walks
     * forward past already-used days so one section never has two
     * assessments on the same day.
     */
    private static int reserveDay(int target, int jitter, int windowStart, int windowEnd,
                                  Set<Integer> usedDays) {
        int day = target;
        if (jitter > 0) {
            day += GameRandom.nextInt(-jitter, jitter);
        }
        day = (int) clamp(day, windowStart, windowEnd);
        day = AcademicCalendar.snapToWeekday(day);
        while (usedDays.contains(day)) {
            day = AcademicCalendar.snapToWeekday(day + 1);
        }
        usedDays.add(day);
        return day;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
