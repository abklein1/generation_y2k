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

    public StudentAcademicRecord() {
        this.courseProgressByKey = new LinkedHashMap<>();
        this.skillMastery = new EnumMap<>(AcademicSkill.class);
        this.homeworkAssignments = new ArrayList<>();
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
