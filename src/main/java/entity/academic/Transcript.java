package entity.academic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * A student's permanent academic history: every finalized semester course,
 * ordered chronologically, with cumulative and per-year GPA helpers. Entries
 * are appended when a semester is finalized (live sim) or backfilled by the
 * history generator at world creation.
 */
public class Transcript implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<TranscriptEntry> entries = new ArrayList<>();

    /**
     * Appends a finalized course result; null entries are ignored.
     *
     * @param entry the finalized result to record
     */
    public void addEntry(TranscriptEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    public List<TranscriptEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Returns the entries for one school year, in recorded order.
     *
     * @param schoolYear start year of the school year (2001 = 2001-02)
     * @return the matching entries, possibly empty
     */
    public List<TranscriptEntry> getEntriesForYear(int schoolYear) {
        List<TranscriptEntry> result = new ArrayList<>();
        for (TranscriptEntry entry : entries) {
            if (entry.getSchoolYear() == schoolYear) {
                result.add(entry);
            }
        }
        return result;
    }

    /** Distinct school years on the transcript, ascending. */
    public List<Integer> getSchoolYears() {
        return new ArrayList<>(new TreeSet<>(entries.stream()
                .map(TranscriptEntry::getSchoolYear).toList()));
    }

    /**
     * Credit-weighted GPA over the whole transcript.
     *
     * @param weighted true to apply the AP/Honors grade-point bonus
     * @return the GPA, or -1 when the transcript is empty
     */
    public double getCumulativeGpa(boolean weighted) {
        return gpaOver(entries, weighted);
    }

    /**
     * Credit-weighted GPA over one school year.
     *
     * @param schoolYear start year of the school year
     * @param weighted   true to apply the AP/Honors grade-point bonus
     * @return the GPA, or -1 when the year has no entries
     */
    public double getYearGpa(int schoolYear, boolean weighted) {
        return gpaOver(getEntriesForYear(schoolYear), weighted);
    }

    /** Total credits earned across all finalized courses. */
    public double getTotalCredits() {
        double total = 0.0;
        for (TranscriptEntry entry : entries) {
            total += entry.getCredit();
        }
        return total;
    }

    /**
     * Checks whether a class appears anywhere on the transcript.
     *
     * @param className the class name to look for
     * @return true when a finalized entry exists for the class
     */
    public boolean hasCompleted(String className) {
        String key = StudentAcademicRecord.normalizeCourseKey(className);
        for (TranscriptEntry entry : entries) {
            if (entry.getCourseKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static double gpaOver(List<TranscriptEntry> scope, boolean weighted) {
        double points = 0.0;
        double credits = 0.0;
        for (TranscriptEntry entry : scope) {
            double entryPoints = weighted
                    ? entry.getWeightedGradePoints() : entry.getGradePoints();
            points += entryPoints * entry.getCredit();
            credits += entry.getCredit();
        }
        return credits <= 0.0 ? -1.0 : points / credits;
    }
}
