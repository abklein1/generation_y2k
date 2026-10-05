package entity.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Grade book")
class GradeBookTest {

    @Test
    @DisplayName("Weighted percent renormalizes over categories with grades")
    void weightedPercentRenormalizes() {
        GradeBook book = new GradeBook();
        book.addEntry(new GradeEntry(AssessmentType.HOMEWORK, "HW 1", 80.0, 1, false));
        book.addEntry(new GradeEntry(AssessmentType.QUIZ, "Quiz 1", 60.0, 2, false));

        Map<AssessmentType, Integer> weights = new EnumMap<>(AssessmentType.class);
        weights.put(AssessmentType.HOMEWORK, 20);
        weights.put(AssessmentType.QUIZ, 20);
        weights.put(AssessmentType.TEST, 30);
        weights.put(AssessmentType.FINAL, 30);

        // Only homework and quiz exist: (80*20 + 60*20) / 40 = 70.
        assertEquals(70.0, book.getWeightedPercent(weights, -1.0), 0.001);
    }

    @Test
    @DisplayName("Participation feeds the weighted percent without stored entries")
    void participationCountsLive() {
        GradeBook book = new GradeBook();
        book.addEntry(new GradeEntry(AssessmentType.SKILLS_EVAL, "Eval 1", 90.0, 5, false));

        Map<AssessmentType, Integer> weights = new EnumMap<>(AssessmentType.class);
        weights.put(AssessmentType.PARTICIPATION, 50);
        weights.put(AssessmentType.SKILLS_EVAL, 50);

        // (80*50 + 90*50) / 100 = 85.
        assertEquals(85.0, book.getWeightedPercent(weights, 80.0), 0.001);
    }

    @Test
    @DisplayName("Missing work drags the average down as a zero")
    void missingWorkCountsAsZero() {
        GradeBook book = new GradeBook();
        book.addEntry(new GradeEntry(AssessmentType.TEST, "Unit 1 Test", 100.0, 10, false));
        book.addEntry(new GradeEntry(AssessmentType.TEST, "Unit 2 Test", 0.0, 20, true));

        assertEquals(50.0, book.getCategoryAverage(AssessmentType.TEST), 0.001);
    }

    @Test
    @DisplayName("Empty gradebook yields no percent")
    void emptyBookHasNoPercent() {
        GradeBook book = new GradeBook();
        Map<AssessmentType, Integer> weights = new EnumMap<>(AssessmentType.class);
        weights.put(AssessmentType.TEST, 100);
        assertTrue(book.getWeightedPercent(weights, -1.0) < 0.0);
    }

    @Test
    @DisplayName("Letter grades follow the standard scale")
    void letterGradeScale() {
        assertEquals("A", GradeBook.letterGradeFor(95.0));
        assertEquals("B", GradeBook.letterGradeFor(85.0));
        assertEquals("C", GradeBook.letterGradeFor(75.0));
        assertEquals("D", GradeBook.letterGradeFor(65.0));
        assertEquals("F", GradeBook.letterGradeFor(40.0));
    }

    @Test
    @DisplayName("Grade points map to the 4.0 scale")
    void gradePointScale() {
        assertEquals(4.0, GradeBook.gradePointsFor(92.0), 0.001);
        assertEquals(3.0, GradeBook.gradePointsFor(83.0), 0.001);
        assertEquals(2.0, GradeBook.gradePointsFor(71.0), 0.001);
        assertEquals(1.0, GradeBook.gradePointsFor(64.0), 0.001);
        assertEquals(0.0, GradeBook.gradePointsFor(30.0), 0.001);
    }
}
