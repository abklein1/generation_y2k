package utility;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import constants.SimConstants;
import entity.Staff;
import entity.StaffType;
import entity.Student;
import entity.StudentBlock;
import entity.academic.CourseCategory;
import entity.academic.CourseProgress;
import entity.academic.GradeEntry;
import entity.academic.AssessmentType;
import entity.academic.LessonPlanTemplate;
import entity.academic.ScheduledAssessment;
import entity.academic.StudentAcademicRecord;
import entity.academic.TranscriptEntry;

/**
 * Backfills academic history for upperclassmen at world generation, Dwarf
 * Fortress style: prior school years are simulated at coarse (course/semester)
 * granularity rather than tick-by-tick, but every synthetic grade flows
 * through the same scoring formulas the live simulation uses
 * ({@link AssessmentGrader} and
 * {@link AcademicProgressService#gradeHomework}), so historical GPAs have the
 * same statistical shape as the grades the engine will produce going forward.
 *
 * <p>For each prior year the generator reconstructs a plausible class list
 * with {@link StudentClassDeterminer} (keeping the student's current AP track
 * coherent with their past), synthesizes semester-end understanding and
 * attention from the student's traits plus a persistent per-student arc,
 * scores the full {@link LessonPlanTemplate} assessment load, and freezes the
 * result onto the student's {@link entity.academic.Transcript}. Skill mastery is seeded from
 * completed courses so upperclassmen enter the live year knowing more than
 * freshmen.</p>
 *
 * <p>Must run inside the seeded {@link GameRandom} stream (after scheduling,
 * before the sim starts) so history is deterministic per world seed.</p>
 */
public final class SchoolHistoryGenerator {

    /** Subjects whose academic track (AP/Honors/On-Level) is kept sticky. */
    private static final String[] TRACKED_SUBJECTS = {"English", "Math", "Science", "History"};

    private SchoolHistoryGenerator() {
    }

    /** Summary statistics for the generation log and tests. */
    public static final class HistoryReport {
        private int studentsWithHistory;
        private int transcriptEntries;
        private double gpaTotal;
        private int gpaCount;

        public int getStudentsWithHistory() {
            return studentsWithHistory;
        }

        public int getTranscriptEntries() {
            return transcriptEntries;
        }

        /** Mean cumulative weighted GPA over students that received history. */
        public double getMeanGpa() {
            return gpaCount == 0 ? -1.0 : gpaTotal / gpaCount;
        }
    }

    /**
     * Generates transcripts for every non-freshman student covering the school
     * years they were already enrolled (sophomores one year, juniors two,
     * seniors three).
     *
     * @param studentHashMap    the enrolled students
     * @param staffHashMap      current staff, used to credit plausible teachers
     * @param currentSchoolYear start year of the current school year (2004)
     * @param log               optional progress logger; may be null
     * @return summary statistics of the generated history
     */
    public static HistoryReport generate(HashMap<Integer, Student> studentHashMap,
                                         HashMap<Integer, Staff> staffHashMap,
                                         int currentSchoolYear,
                                         Consumer<String> log) {
        HistoryReport report = new HistoryReport();
        if (studentHashMap == null || studentHashMap.isEmpty()) {
            return report;
        }

        Map<StaffType, List<Staff>> teachersByType = indexTeachers(staffHashMap);

        for (Student student : studentHashMap.values()) {
            int priorYears = priorYearCount(student.studentStatistics.getGradeLevel());
            if (priorYears <= 0) {
                continue;
            }
            generateStudentHistory(student, teachersByType, currentSchoolYear,
                    priorYears, report);
        }

        if (log != null) {
            log.accept(String.format(
                    "Academic history generated: %d students, %d transcript entries, mean GPA %.2f",
                    report.studentsWithHistory, report.transcriptEntries, report.getMeanGpa()));
        }
        return report;
    }

    // ------------------------------------------------------------ per student

    private static void generateStudentHistory(Student student,
                                               Map<StaffType, List<Staff>> teachersByType,
                                               int currentSchoolYear, int priorYears,
                                               HistoryReport report) {
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();
        boolean apathetic = AcademicProgressService.isAcademicallyApathetic(student);
        // Persistent academic arc: some students have always been stronger or
        // weaker than their raw stats suggest.
        double arcOffset = GameRandom.nextGaussian(0.0, SimConstants.HISTORY_STUDENT_ARC_STD_DEV);
        Map<String, String> subjectPaths = determineSubjectPaths(student);

        for (int k = 0; k < priorYears; k++) {
            int schoolYear = currentSchoolYear - priorYears + k;
            String gradeLabel = gradeLabelFor(k);
            // Earlier years score slightly lower: students mature.
            double maturityPenalty = (priorYears - 1 - k)
                    * SimConstants.HISTORY_MATURITY_GAIN_PER_YEAR;
            double yearOffset = GameRandom.nextGaussian(0.0,
                    SimConstants.HISTORY_YEAR_NOISE_STD_DEV) - maturityPenalty;

            for (Map.Entry<String, String> course : buildYearCourses(student,
                    gradeLabel, subjectPaths).entrySet()) {
                simulateCourse(student, record, course.getKey(), course.getValue(),
                        schoolYear, gradeLabel, arcOffset + yearOffset,
                        apathetic, teachersByType, currentSchoolYear);
            }
        }

        report.studentsWithHistory++;
        report.transcriptEntries += record.getTranscript().getEntries().size();
        double gpa = record.getCumulativeGpa();
        if (gpa >= 0.0) {
            report.gpaTotal += gpa;
            report.gpaCount++;
        }
    }

    /**
     * Reconstructs one prior year's class list, split into semesters.
     * Multi-course subjects alternate Fall/Spring in order (so "Spanish I"
     * lands before "Spanish II"); single-course subjects alternate via a
     * toggle to keep the two semesters roughly balanced.
     *
     * @return ordered map of class name to semester ("Fall"/"Spring")
     */
    private static Map<String, String> buildYearCourses(Student student, String gradeLabel,
                                                        Map<String, String> subjectPaths) {
        List<List<String>> subjectLists = new ArrayList<>();
        subjectLists.add(StudentClassDeterminer.determineEnglishClasses(gradeLabel,
                subjectPaths.get("English")));
        subjectLists.add(StudentClassDeterminer.determineMathClasses(gradeLabel,
                subjectPaths.get("Math")));
        subjectLists.add(StudentClassDeterminer.determineScienceClasses(gradeLabel,
                subjectPaths.get("Science")));
        subjectLists.add(StudentClassDeterminer.determineHistoryClasses(gradeLabel,
                subjectPaths.get("History")));
        subjectLists.add(StudentClassDeterminer.determineLanguageClasses(gradeLabel, student));
        subjectLists.add(StudentClassDeterminer.determinePhysEdClasses(gradeLabel, student));
        subjectLists.add(StudentClassDeterminer.determineVocationalClasses(gradeLabel, student));

        Map<String, String> coursesBySemester = new LinkedHashMap<>();
        boolean fallToggle = true;
        for (List<String> subjectList : subjectLists) {
            if (subjectList.size() >= 2) {
                for (int i = 0; i < subjectList.size(); i++) {
                    coursesBySemester.putIfAbsent(subjectList.get(i),
                            i % 2 == 0 ? "Fall" : "Spring");
                }
            } else if (subjectList.size() == 1) {
                coursesBySemester.putIfAbsent(subjectList.get(0),
                        fallToggle ? "Fall" : "Spring");
                fallToggle = !fallToggle;
            }
        }
        return coursesBySemester;
    }

    // ------------------------------------------------------------- per course

    private static void simulateCourse(Student student, StudentAcademicRecord record,
                                       String className, String semester, int schoolYear,
                                       String gradeLabel, double studentYearOffset,
                                       boolean apathetic,
                                       Map<StaffType, List<Staff>> teachersByType,
                                       int currentSchoolYear) {
        CourseCategory category = CourseCategory.fromClassName(className);
        LessonPlanTemplate template = LessonPlanTemplate.forCategory(category);

        // Scratch course state: never attached to the student's live record.
        CourseProgress course = new CourseProgress(
                StudentAcademicRecord.normalizeCourseKey(className), className);
        course.setGradingPolicy(template.getWeights(), semester);
        course.addUnderstanding(synthesizeUnderstanding(student, studentYearOffset));
        course.addAttentionPoints(synthesizeAttention(student));

        Staff teacher = pickTeacher(teachersByType, className,
                currentSchoolYear - schoolYear);

        scoreAssessments(student, teacher, className, category, template, course);
        scoreHomework(student, course, template, semester, apathetic);

        double percent = course.getCurrentPercent();
        if (percent < 0.0) {
            return;
        }
        record.getTranscript().addEntry(new TranscriptEntry(schoolYear, gradeLabel,
                semester, className, percent,
                SimConstants.TRANSCRIPT_CREDIT_PER_SEMESTER_COURSE,
                teacher == null ? null : teacher.teacherName.getFullName()));
        student.studentStatistics.addToCompletedClasses(className);
        // Completed coursework seeds real skill mastery so upperclassmen
        // enter the live year ahead of freshmen.
        record.addSkillMastery(AcademicProgressService.inferSkill(className),
                course.getUnderstanding() * SimConstants.HISTORY_SKILL_MASTERY_GAIN_SCALE);
    }

    private static void scoreAssessments(Student student, Staff teacher, String className,
                                         CourseCategory category, LessonPlanTemplate template,
                                         CourseProgress course) {
        Map<AssessmentType, Integer> counts = new EnumMap<>(AssessmentType.class);
        counts.put(AssessmentType.QUIZ, template.getQuizCount());
        counts.put(AssessmentType.TEST, template.getTestCount());
        counts.put(AssessmentType.PROJECT, template.getProjectCount());
        counts.put(AssessmentType.SKILLS_EVAL, template.getSkillsEvalCount());
        counts.put(AssessmentType.MIDTERM, template.hasMidterm() ? 1 : 0);
        counts.merge(template.hasFinal() ? template.getFinalType() : AssessmentType.QUIZ,
                template.hasFinal() ? 1 : 0, Integer::sum);

        for (Map.Entry<AssessmentType, Integer> entry : counts.entrySet()) {
            AssessmentType type = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                int difficulty = GameRandom.nextInt(type.getMinDifficulty(),
                        type.getMaxDifficulty());
                ScheduledAssessment assessment = new ScheduledAssessment(
                        "history", type, type.getDisplayName(), 0, difficulty);
                double score = AssessmentGrader.score(student, teacher, className,
                        category, assessment, course);
                course.getGradeBook().addEntry(new GradeEntry(type,
                        type.getDisplayName() + " " + (i + 1), score, 0, false));
            }
        }
    }

    private static void scoreHomework(Student student, CourseProgress course,
                                      LessonPlanTemplate template, String semester,
                                      boolean apathetic) {
        int interval = template.getHomeworkIntervalDays();
        if (interval <= 0) {
            return;
        }
        AcademicCalendar.SemesterBounds bounds = AcademicCalendar.getSemesterBounds(semester);
        int count = Math.min(SimConstants.HISTORY_MAX_HOMEWORK_PER_COURSE,
                Math.max(0, (bounds.getLastDay() - bounds.getFirstDay()) / interval));
        for (int i = 0; i < count; i++) {
            int effort = GameRandom.nextInt(SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT,
                    SimConstants.ACADEMIC_HOMEWORK_MAX_EFFORT);
            int problems = problemsForEffort(effort);
            GradeEntry entry = AcademicProgressService.gradeHomework(student, course,
                    effort, problems, i + 1, i + 1, apathetic);
            course.getGradeBook().addEntry(entry);
        }
    }

    /** Mirrors the live effort-to-problem-count mapping. */
    private static int problemsForEffort(int effort) {
        int span = SimConstants.ACADEMIC_HOMEWORK_MAX_EFFORT
                - SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT;
        double ratio = span == 0 ? 0.0
                : (effort - SimConstants.ACADEMIC_HOMEWORK_MIN_EFFORT) / (double) span;
        int problemSpan = SimConstants.ACADEMIC_HOMEWORK_MAX_PROBLEMS
                - SimConstants.ACADEMIC_HOMEWORK_MIN_PROBLEMS;
        return SimConstants.ACADEMIC_HOMEWORK_MIN_PROBLEMS
                + (int) Math.round(problemSpan * ratio);
    }

    // --------------------------------------------------------- synthesized state

    /**
     * Semester-end understanding from traits, the student's persistent arc,
     * the year offset, and per-course variation.
     */
    private static double synthesizeUnderstanding(Student student, double studentYearOffset) {
        double traits = (student.studentStatistics.getIntelligence() - 100)
                * SimConstants.HISTORY_UNDERSTANDING_INTELLIGENCE_WEIGHT
                + (student.studentStatistics.getDetermination() - 50)
                * SimConstants.HISTORY_UNDERSTANDING_DETERMINATION_WEIGHT
                + (student.studentStatistics.getResponsibility() - 50)
                * SimConstants.HISTORY_UNDERSTANDING_RESPONSIBILITY_WEIGHT
                + (student.studentStatistics.getInitiative() - 50)
                * SimConstants.HISTORY_UNDERSTANDING_INITIATIVE_WEIGHT;
        double courseNoise = GameRandom.nextGaussian(0.0,
                SimConstants.HISTORY_COURSE_NOISE_STD_DEV);
        return clamp(SimConstants.HISTORY_UNDERSTANDING_BASE + traits
                + studentYearOffset + courseNoise, 2.0, 98.0);
    }

    /** Accumulated attention points from determination and responsibility. */
    private static int synthesizeAttention(Student student) {
        int attention = (student.studentStatistics.getDetermination()
                - SimConstants.HISTORY_ATTENTION_STAT_FLOOR)
                * SimConstants.HISTORY_ATTENTION_DETERMINATION_SCALE
                + (student.studentStatistics.getResponsibility()
                - SimConstants.HISTORY_ATTENTION_STAT_FLOOR)
                * SimConstants.HISTORY_ATTENTION_RESPONSIBILITY_SCALE;
        return Math.max(0, Math.min(SimConstants.HISTORY_ATTENTION_MAX, attention));
    }

    // -------------------------------------------------------------- track/path

    /**
     * Determines each core subject's sticky academic track. A subject is "AP"
     * when the student's current schedule contains an AP class of that
     * subject (keeping history coherent with the present); otherwise one
     * probability roll decides, capped at "Honors" so clearly on-level
     * students don't acquire surprise AP pasts.
     */
    private static Map<String, String> determineSubjectPaths(Student student) {
        Map<String, String> paths = new LinkedHashMap<>();

        for (StudentBlock block : student.studentStatistics.getStudentSchedule()
                .getClassScheduleCopy()) {
            String className = block == null ? null : block.getClassName();
            if (className == null || !className.startsWith("AP ")) {
                continue;
            }
            String subject = subjectFor(className);
            if (subject != null) {
                paths.put(subject, "AP");
            }
        }

        int intelligence = student.studentStatistics.getIntelligence();
        int determination = student.studentStatistics.getDetermination();
        String income = student.studentStatistics.getIncomeLevel();
        if (income == null) {
            income = "average";
        }
        for (String subject : TRACKED_SUBJECTS) {
            if (!paths.containsKey(subject)) {
                String rolled = StudentClassDeterminer.classProbabilityLoader(
                        intelligence, income, determination);
                paths.put(subject, "AP".equals(rolled) ? "Honors" : rolled);
            }
        }
        return paths;
    }

    private static String subjectFor(String className) {
        StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(className);
        switch (type) {
            case ENGLISH:
                return "English";
            case MATH:
                return "Math";
            case SCIENCE:
                return "Science";
            case HISTORY:
                return "History";
            default:
                return null;
        }
    }

    // ----------------------------------------------------------------- teachers

    private static Map<StaffType, List<Staff>> indexTeachers(HashMap<Integer, Staff> staffHashMap) {
        Map<StaffType, List<Staff>> byType = new EnumMap<>(StaffType.class);
        if (staffHashMap == null) {
            return byType;
        }
        for (Staff staff : staffHashMap.values()) {
            if (staff == null || staff.teacherStatistics == null
                    || !(staff.teacherStatistics.getStaffType() instanceof StaffType)) {
                continue;
            }
            byType.computeIfAbsent((StaffType) staff.teacherStatistics.getStaffType(),
                    key -> new ArrayList<>()).add(staff);
        }
        return byType;
    }

    /**
     * Picks a plausible teacher for a historical class: a current staff
     * member of the right subject who was already teaching that many years
     * ago. Returns null when no one qualifies (the grader is null-safe).
     */
    private static Staff pickTeacher(Map<StaffType, List<Staff>> teachersByType,
                                     String className, int yearsAgo) {
        StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(className);
        List<Staff> candidates = teachersByType.get(type);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        List<Staff> experienced = new ArrayList<>();
        for (Staff staff : candidates) {
            if (staff.teacherStatistics.getYearsOfExperience() >= yearsAgo) {
                experienced.add(staff);
            }
        }
        if (experienced.isEmpty()) {
            return null;
        }
        return experienced.get(GameRandom.nextInt(experienced.size()));
    }

    // -------------------------------------------------------------------- misc

    /** Number of already-completed school years for a grade level. */
    private static int priorYearCount(String gradeLevel) {
        if (gradeLevel == null) {
            return 0;
        }
        switch (gradeLevel) {
            case "Sophomore":
                return 1;
            case "Junior":
                return 2;
            case "Senior":
                return 3;
            default:
                return 0;
        }
    }

    /** Grade label for the k-th prior year (0 = freshman year). */
    private static String gradeLabelFor(int k) {
        switch (k) {
            case 0:
                return "Freshman";
            case 1:
                return "Sophomore";
            default:
                return "Junior";
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
