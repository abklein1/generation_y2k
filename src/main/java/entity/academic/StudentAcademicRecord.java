package entity.academic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-student academic state for the current lightweight coursework model.
 */
public class StudentAcademicRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double MAX_SKILL_MASTERY = 100.0;

    private final Map<String, CourseProgress> courseProgressByKey;
    private final EnumMap<AcademicSkill, Double> skillMastery;
    private final List<HomeworkAssignment> homeworkAssignments;
    // Added with the grading system; null in saves that predate it.
    private Map<String, AssessmentAttempt> assessmentAttempts;
    // Added with the history generator; null in saves that predate it.
    private Transcript transcript;

    public StudentAcademicRecord() {
        this.courseProgressByKey = new LinkedHashMap<>();
        this.skillMastery = new EnumMap<>(AcademicSkill.class);
        this.homeworkAssignments = new ArrayList<>();
        this.assessmentAttempts = new LinkedHashMap<>();
        for (AcademicSkill skill : AcademicSkill.values()) {
            skillMastery.put(skill, 0.0);
        }
    }

    public CourseProgress getOrCreateCourse(String className) {
        String courseKey = normalizeCourseKey(className);
        return courseProgressByKey.computeIfAbsent(courseKey,
                key -> new CourseProgress(key, sanitizeClassName(className)));
    }

    public CourseProgress getCourse(String className) {
        return courseProgressByKey.get(normalizeCourseKey(className));
    }

    public Map<String, CourseProgress> getCourseProgressByKey() {
        return Collections.unmodifiableMap(courseProgressByKey);
    }

    public void addSkillMastery(AcademicSkill skill, double amount) {
        double current = skillMastery.getOrDefault(skill, 0.0);
        skillMastery.put(skill, Math.max(0.0, Math.min(MAX_SKILL_MASTERY, current + amount)));
    }

    public double getSkillMastery(AcademicSkill skill) {
        return skillMastery.getOrDefault(skill, 0.0);
    }

    public Map<AcademicSkill, Double> getSkillMastery() {
        return Collections.unmodifiableMap(skillMastery);
    }

    public void addHomework(HomeworkAssignment homework) {
        if (homework != null) {
            homeworkAssignments.add(homework);
        }
    }

    public List<HomeworkAssignment> getHomeworkAssignments() {
        return Collections.unmodifiableList(homeworkAssignments);
    }

    public List<HomeworkAssignment> getPendingHomework() {
        List<HomeworkAssignment> pending = new ArrayList<>();
        for (HomeworkAssignment homework : homeworkAssignments) {
            if (!homework.isResolved()) {
                pending.add(homework);
            }
        }
        return pending;
    }

    public int getPendingHomeworkCount() {
        int count = 0;
        for (HomeworkAssignment homework : homeworkAssignments) {
            if (!homework.isResolved()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasOpenHomeworkForCourse(String className) {
        String courseKey = normalizeCourseKey(className);
        for (HomeworkAssignment homework : homeworkAssignments) {
            if (!homework.isResolved() && homework.getCourseKey().equals(courseKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasHomeworkAssignedForCourseOnDay(String className, int day) {
        String courseKey = normalizeCourseKey(className);
        for (HomeworkAssignment homework : homeworkAssignments) {
            if (homework.getCourseKey().equals(courseKey) && homework.getAssignedDay() == day) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the attempt tracker for an in-progress assessment, creating it
     * on first touch.
     *
     * @param assessmentId the scheduled assessment's id
     * @return the attempt, never null
     */
    public AssessmentAttempt getOrCreateAttempt(String assessmentId) {
        return attemptsMap().computeIfAbsent(assessmentId, AssessmentAttempt::new);
    }

    /**
     * Returns the attempt for an assessment, or null when the student never
     * sat any of it.
     *
     * @param assessmentId the scheduled assessment's id
     * @return the attempt, or null
     */
    public AssessmentAttempt getAttempt(String assessmentId) {
        return attemptsMap().get(assessmentId);
    }

    private Map<String, AssessmentAttempt> attemptsMap() {
        if (assessmentAttempts == null) {
            assessmentAttempts = new LinkedHashMap<>();
        }
        return assessmentAttempts;
    }

    /** The permanent transcript, created lazily so old saves keep loading. */
    public Transcript getTranscript() {
        if (transcript == null) {
            transcript = new Transcript();
        }
        return transcript;
    }

    /**
     * Cumulative weighted GPA (4.0 scale plus AP/Honors bonus) over all
     * finalized transcript courses.
     *
     * @return the GPA, or -1 when no course has been finalized yet
     */
    public double getCumulativeGpa() {
        return getTranscript().getCumulativeGpa(true);
    }

    /**
     * Cumulative unweighted GPA (plain 4.0 scale) over all finalized
     * transcript courses.
     *
     * @return the GPA, or -1 when no course has been finalized yet
     */
    public double getCumulativeUnweightedGpa() {
        return getTranscript().getCumulativeGpa(false);
    }

    /**
     * Freezes a finished semester onto the transcript: every graded course
     * whose semester matches (or was never tagged) becomes a
     * {@link TranscriptEntry} and is removed from the live course map, so
     * the operation is idempotent and {@link #getOverallGpa()} keeps
     * reflecting only in-progress coursework.
     *
     * @param semester             the semester that just ended ("Fall"/"Spring")
     * @param schoolYear           start year of the school year (2004 = 2004-05)
     * @param gradeLevel           the student's grade level this year
     * @param teacherNameByCourseKey optional map of course key to teacher
     *                             display name; may be null
     * @return the finalized entries, in course order (possibly empty)
     */
    public List<TranscriptEntry> finalizeSemester(String semester, int schoolYear,
                                                  String gradeLevel,
                                                  Map<String, String> teacherNameByCourseKey) {
        List<TranscriptEntry> finalized = new ArrayList<>();
        if (semester == null || semester.isBlank()) {
            return finalized;
        }
        List<String> finalizedKeys = new ArrayList<>();
        for (Map.Entry<String, CourseProgress> entry : courseProgressByKey.entrySet()) {
            CourseProgress course = entry.getValue();
            String courseSemester = course.getSemester();
            if (courseSemester != null && !semester.equalsIgnoreCase(courseSemester)) {
                continue;
            }
            double percent = course.getCurrentPercent();
            if (percent < 0.0) {
                continue;
            }
            String teacherName = teacherNameByCourseKey == null
                    ? null : teacherNameByCourseKey.get(entry.getKey());
            finalized.add(new TranscriptEntry(schoolYear, gradeLevel, semester,
                    course.getClassName(), percent,
                    constants.SimConstants.TRANSCRIPT_CREDIT_PER_SEMESTER_COURSE,
                    teacherName));
            finalizedKeys.add(entry.getKey());
        }
        for (TranscriptEntry entry : finalized) {
            getTranscript().addEntry(entry);
        }
        for (String key : finalizedKeys) {
            courseProgressByKey.remove(key);
        }
        return finalized;
    }

    /**
     * GPA (4.0 scale) over courses in one semester, weighted +1.0 for AP and
     * +0.5 for Honors classes. Courses without any grades are skipped.
     *
     * @param semester "Fall" or "Spring"
     * @return the GPA, or -1 when no graded courses exist for the semester
     */
    public double getSemesterGpa(String semester) {
        return computeGpa(semester);
    }

    /**
     * GPA (4.0 scale) over all graded courses with AP/Honors weighting.
     *
     * @return the GPA, or -1 when no course has grades yet
     */
    public double getOverallGpa() {
        return computeGpa(null);
    }

    private double computeGpa(String semesterFilter) {
        double totalPoints = 0.0;
        int courseCount = 0;
        for (CourseProgress course : courseProgressByKey.values()) {
            if (semesterFilter != null
                    && !semesterFilter.equalsIgnoreCase(course.getSemester())) {
                continue;
            }
            double percent = course.getCurrentPercent();
            if (percent < 0.0) {
                continue;
            }
            double points = GradeBook.gradePointsFor(percent)
                    + GradeBook.honorsBonusFor(course.getClassName());
            totalPoints += points;
            courseCount++;
        }
        return courseCount == 0 ? -1.0 : totalPoints / courseCount;
    }

    public static String normalizeCourseKey(String className) {
        return sanitizeClassName(className).toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private static String sanitizeClassName(String className) {
        if (className == null || className.isBlank()) {
            return "General Study";
        }
        return className.trim();
    }
}
