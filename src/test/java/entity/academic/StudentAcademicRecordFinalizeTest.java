package entity.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import entity.Student;

@DisplayName("Student academic record semester finalization")
class StudentAcademicRecordFinalizeTest {

    @Test
    @DisplayName("Finalizing Fall moves graded courses to the transcript and leaves Spring")
    void finalizeFallMovesGradedCourses() {
        Student student = new Student();
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();

        CourseProgress algebra = addGradedCourse(record, "Algebra I", "Fall", 88.0);
        addGradedCourse(record, "Biology", "Spring", 91.0);
        record.getOrCreateCourse("English I").setGradingPolicy(
                LessonPlanTemplate.forCategory(CourseCategory.CORE_ACADEMIC).getWeights(), "Fall");

        List<TranscriptEntry> finalized = record.finalizeSemester("Fall", 2004, "Freshman",
                Map.of(StudentAcademicRecord.normalizeCourseKey("Algebra I"), "Ms. Rivera"));

        assertEquals(1, finalized.size());
        TranscriptEntry entry = finalized.get(0);
        assertEquals("Algebra I", entry.getClassName());
        assertEquals("Fall", entry.getSemester());
        assertEquals(2004, entry.getSchoolYear());
        assertEquals("Freshman", entry.getGradeLevel());
        assertEquals("Ms. Rivera", entry.getTeacherName());
        assertEquals(algebra.getCurrentPercent(), entry.getFinalPercent(), 0.001);

        assertNull(record.getCourse("Algebra I"));
        assertNotNull(record.getCourse("Biology"));
        assertNotNull(record.getCourse("English I"));
        assertEquals(1, record.getTranscript().getEntries().size());
        assertEquals(record.getTranscript().getCumulativeGpa(true), record.getCumulativeGpa(), 0.001);
        assertTrue(record.getOverallGpa() >= 0.0);

        for (TranscriptEntry finalizedEntry : finalized) {
            student.studentStatistics.addToCompletedClasses(finalizedEntry.getClassName());
        }
        assertTrue(student.studentStatistics.getCompletedClasses().contains("Algebra I"));
        assertEquals(1, student.studentStatistics.getCompletedClasses().size());
    }

    @Test
    @DisplayName("Finalizing the same semester twice is a no-op")
    void finalizeIsIdempotent() {
        StudentAcademicRecord record = new StudentAcademicRecord();
        addGradedCourse(record, "World History", "Fall", 80.0);

        List<TranscriptEntry> first = record.finalizeSemester("Fall", 2004, "Sophomore", null);
        List<TranscriptEntry> second = record.finalizeSemester("Fall", 2004, "Sophomore", null);

        assertEquals(1, first.size());
        assertTrue(second.isEmpty());
        assertEquals(1, record.getTranscript().getEntries().size());
        assertTrue(record.getCourseProgressByKey().isEmpty());
    }

    @Test
    @DisplayName("Blank or unknown semester finalizes nothing")
    void blankSemesterFinalizesNothing() {
        StudentAcademicRecord record = new StudentAcademicRecord();
        addGradedCourse(record, "Health", "Fall", 90.0);

        assertTrue(record.finalizeSemester(null, 2004, "Freshman", null).isEmpty());
        assertTrue(record.finalizeSemester(" ", 2004, "Freshman", null).isEmpty());
        assertNotNull(record.getCourse("Health"));
    }

    private CourseProgress addGradedCourse(StudentAcademicRecord record, String className,
                                           String semester, double score) {
        CourseProgress course = record.getOrCreateCourse(className);
        course.setGradingPolicy(
                LessonPlanTemplate.forCategory(CourseCategory.fromClassName(className)).getWeights(),
                semester);
        course.getGradeBook().addEntry(new GradeEntry(AssessmentType.TEST, "Test 1", score, 10, false));
        return course;
    }
}
