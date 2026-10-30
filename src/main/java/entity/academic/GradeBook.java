package entity.academic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Per-course list of graded results with weighted-average helpers. The
 * weighted percent renormalizes over the categories that actually have
 * entries, so a course's grade is meaningful before every assessment type has
 * occurred.
 */
public class GradeBook implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<GradeEntry> entries = new ArrayList<>();

    /**
     * Records a graded result; null entries are ignored.
     *
     * @param entry the graded result to record
     */
    public void addEntry(GradeEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    public List<GradeEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Average score for one assessment category, or -1 when no entries exist.
     *
     * @param type the assessment category
     * @return the 0-100 average, or -1 if the category has no grades yet
     */
    public double getCategoryAverage(AssessmentType type) {
        double total = 0.0;
        int count = 0;
        for (GradeEntry entry : entries) {
            if (entry.getType() == type) {
                total += entry.getScore();
                count++;
            }
        }
        return count == 0 ? -1.0 : total / count;
    }

    /**
     * Weighted course percent over the supplied category weights,
     * renormalized to only the categories with recorded grades.
     * PARTICIPATION has no stored entries, so callers supply its live value.
     *
     * @param weights            category weights (percent of final grade)
     * @param participationScore live participation score 0-100, or -1 when
     *                           participation should be excluded
     * @return the 0-100 weighted percent, or -1 when nothing is gradable yet
     */
    public double getWeightedPercent(Map<AssessmentType, Integer> weights,
                                     double participationScore) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (Map.Entry<AssessmentType, Integer> weight : weights.entrySet()) {
            AssessmentType type = weight.getKey();
            double average;
            if (type == AssessmentType.PARTICIPATION) {
                average = participationScore;
            } else {
                average = getCategoryAverage(type);
            }
            if (average < 0.0) {
                continue;
            }
            weightedSum += average * weight.getValue();
            weightTotal += weight.getValue();
        }
        return weightTotal <= 0.0 ? -1.0 : weightedSum / weightTotal;
    }

    /**
     * Maps a percent to a letter grade (A 90 / B 80 / C 70 / D 60 / F).
     *
     * @param percent the 0-100 course percent
     * @return the letter grade
     */
    public static String letterGradeFor(double percent) {
        if (percent >= 90.0) {
            return "A";
        }
        if (percent >= 80.0) {
            return "B";
        }
        if (percent >= 70.0) {
            return "C";
        }
        if (percent >= 60.0) {
            return "D";
        }
        return "F";
    }

    /**
     * GPA bonus for advanced coursework: +1.0 for AP classes, +0.5 for
     * Honors classes, 0 otherwise.
     *
     * @param className the class name as scheduled
     * @return the grade-point bonus
     */
    public static double honorsBonusFor(String className) {
        String name = className == null ? "" : className;
        if (name.startsWith("AP ") || name.contains(" AP ")) {
            return 1.0;
        }
        if (name.toLowerCase(Locale.ROOT).contains("honors")) {
            return 0.5;
        }
        return 0.0;
    }

    /**
     * Maps a percent to 4.0-scale grade points.
     *
     * @param percent the 0-100 course percent
     * @return grade points 0.0-4.0
     */
    public static double gradePointsFor(double percent) {
        if (percent >= 90.0) {
            return 4.0;
        }
        if (percent >= 80.0) {
            return 3.0;
        }
        if (percent >= 70.0) {
            return 2.0;
        }
        if (percent >= 60.0) {
            return 1.0;
        }
        return 0.0;
    }
}
