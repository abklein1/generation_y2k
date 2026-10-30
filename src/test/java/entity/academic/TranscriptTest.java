package entity.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Transcript")
class TranscriptTest {

    @Test
    @DisplayName("Empty transcript reports no GPA, credits, or years")
    void emptyTranscriptHasNoGpa() {
        Transcript transcript = new Transcript();

        assertTrue(transcript.isEmpty());
        assertEquals(-1.0, transcript.getCumulativeGpa(true), 0.001);
        assertEquals(-1.0, transcript.getCumulativeGpa(false), 0.001);
        assertEquals(-1.0, transcript.getYearGpa(2001, true), 0.001);
        assertEquals(0.0, transcript.getTotalCredits(), 0.001);
        assertTrue(transcript.getSchoolYears().isEmpty());
        assertFalse(transcript.hasCompleted("English I"));
    }

    @Test
    @DisplayName("Cumulative GPA is credit-weighted")
    void cumulativeGpaIsCreditWeighted() {
        Transcript transcript = new Transcript();
        transcript.addEntry(entry(2001, "Freshman", "Fall", "English I", 92.0));
        transcript.addEntry(entry(2001, "Freshman", "Spring", "Biology", 74.0));

        // A (4.0) + C (2.0) / 2 credits = 3.0
        assertEquals(3.0, transcript.getCumulativeGpa(false), 0.001);
        assertEquals(3.0, transcript.getCumulativeGpa(true), 0.001);
        assertEquals(2.0, transcript.getTotalCredits(), 0.001);
        assertTrue(transcript.hasCompleted("English I"));
        assertTrue(transcript.hasCompleted("Biology"));
    }

    @Test
    @DisplayName("AP and Honors classes add the weighted bonus")
    void weightingAppliesApAndHonorsBonus() {
        Transcript transcript = new Transcript();
        transcript.addEntry(entry(2003, "Junior", "Fall", "Honors English III", 85.0));
        transcript.addEntry(entry(2003, "Junior", "Spring", "AP Biology", 85.0));

        assertEquals(3.0, transcript.getCumulativeGpa(false), 0.001);
        // (3.0 + 0.5) + (3.0 + 1.0) / 2 = 3.75
        assertEquals(3.75, transcript.getCumulativeGpa(true), 0.001);
        assertEquals(3.75, transcript.getYearGpa(2003, true), 0.001);
        assertEquals(-1.0, transcript.getYearGpa(2001, true), 0.001);
    }

    @Test
    @DisplayName("Year helpers isolate school years")
    void yearHelpersIsolateSchoolYears() {
        Transcript transcript = new Transcript();
        transcript.addEntry(entry(2001, "Freshman", "Fall", "English I", 95.0));
        transcript.addEntry(entry(2002, "Sophomore", "Fall", "English II", 72.0));

        assertEquals(1, transcript.getEntriesForYear(2001).size());
        assertEquals(1, transcript.getEntriesForYear(2002).size());
        assertEquals(0, transcript.getEntriesForYear(2003).size());
        assertEquals(2, transcript.getSchoolYears().size());
        assertEquals(2001, transcript.getSchoolYears().get(0));
        assertEquals(2002, transcript.getSchoolYears().get(1));
        assertEquals("2001-02", transcript.getEntriesForYear(2001).get(0).getSchoolYearLabel());
        assertEquals(4.0, transcript.getYearGpa(2001, false), 0.001);
        assertEquals(2.0, transcript.getYearGpa(2002, false), 0.001);
    }

    @Test
    @DisplayName("Null entries are ignored")
    void nullEntriesAreIgnored() {
        Transcript transcript = new Transcript();
        transcript.addEntry(null);
        assertTrue(transcript.isEmpty());
    }

    private TranscriptEntry entry(int schoolYear, String gradeLevel, String semester,
                                  String className, double percent) {
        return new TranscriptEntry(schoolYear, gradeLevel, semester, className,
                percent, 1.0, null);
    }
}
