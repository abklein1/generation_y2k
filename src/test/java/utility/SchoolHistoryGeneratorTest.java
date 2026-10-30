package utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;

import javax.swing.JTextArea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import constants.SimConstants;
import entity.Staff;
import entity.StaffType;
import entity.Student;
import entity.StudentBlock;
import entity.academic.AcademicSkill;
import entity.academic.Transcript;
import entity.academic.TranscriptEntry;

@DisplayName("School history generator")
class SchoolHistoryGeneratorTest {

    @BeforeEach
    void setUp() {
        GameRandom.initialize(42L);
    }

    @Test
    @DisplayName("Freshmen receive no transcript")
    void freshmenHaveEmptyTranscripts() {
        HashMap<Integer, Student> students = new HashMap<>();
        students.put(1, createStudent("Freshman", 100, 50));

        SchoolHistoryGenerator.HistoryReport report = SchoolHistoryGenerator.generate(
                students, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        assertEquals(0, report.getStudentsWithHistory());
        assertTrue(students.get(1).studentStatistics.getAcademicRecord().getTranscript().isEmpty());
        assertEquals(-1.0, students.get(1).studentStatistics.getAcademicRecord().getCumulativeGpa(), 0.001);
        assertTrue(students.get(1).studentStatistics.getCompletedClasses().isEmpty());
    }

    @Test
    @DisplayName("Upperclassmen receive one prior year per grade above freshman")
    void priorYearCountsMatchGradeLevel() {
        HashMap<Integer, Student> students = new HashMap<>();
        students.put(1, createStudent("Sophomore", 100, 50));
        students.put(2, createStudent("Junior", 100, 50));
        students.put(3, createStudent("Senior", 100, 50));

        SchoolHistoryGenerator.generate(students, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        assertYears(students.get(1), 2003);
        assertYears(students.get(2), 2002, 2003);
        assertYears(students.get(3), 2001, 2002, 2003);
        assertEquals("Freshman", firstGradeLevel(students.get(1)));
        assertEquals("Freshman", firstGradeLevel(students.get(3)));
        assertEquals("Junior", lastGradeLevel(students.get(3)));
    }

    @Test
    @DisplayName("The same seed produces the same transcripts")
    void sameSeedIsDeterministic() {
        GameRandom.initialize(99L);
        HashMap<Integer, Student> first = cohort(12);
        SchoolHistoryGenerator.generate(first, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        GameRandom.initialize(99L);
        HashMap<Integer, Student> second = cohort(12);
        SchoolHistoryGenerator.generate(second, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        for (Integer id : first.keySet()) {
            List<TranscriptEntry> a = first.get(id).studentStatistics.getAcademicRecord()
                    .getTranscript().getEntries();
            List<TranscriptEntry> b = second.get(id).studentStatistics.getAcademicRecord()
                    .getTranscript().getEntries();
            assertEquals(a.size(), b.size());
            for (int i = 0; i < a.size(); i++) {
                assertEquals(a.get(i).getClassName(), b.get(i).getClassName());
                assertEquals(a.get(i).getFinalPercent(), b.get(i).getFinalPercent(), 0.0001);
                assertEquals(a.get(i).getSchoolYear(), b.get(i).getSchoolYear());
            }
        }
    }

    @Test
    @DisplayName("Higher intelligence yields a higher mean GPA")
    void intelligenceRaisesMeanGpa() {
        GameRandom.initialize(7L);
        HashMap<Integer, Student> gifted = repeatingGrade("Junior", 130, 70, 20);
        SchoolHistoryGenerator.generate(gifted, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        GameRandom.initialize(7L);
        HashMap<Integer, Student> struggling = repeatingGrade("Junior", 85, 35, 20);
        SchoolHistoryGenerator.generate(struggling, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        assertTrue(meanGpa(gifted) > meanGpa(struggling),
                "gifted " + meanGpa(gifted) + " should beat struggling " + meanGpa(struggling));
    }

    @Test
    @DisplayName("An AP senior's math track includes Precalculus")
    void apSeniorHasPrecalculusPrerequisite() {
        Student senior = createStudent("Senior", 125, 70);
        StudentBlock block = new StudentBlock();
        block.setClassName("AP Calculus AB");
        block.setSemester("Fall");
        senior.studentStatistics.addStudentSchedule(block);

        HashMap<Integer, Student> students = new HashMap<>();
        students.put(1, senior);
        SchoolHistoryGenerator.generate(students, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        assertTrue(senior.studentStatistics.getAcademicRecord().getTranscript()
                .hasCompleted("Precalculus"));
        assertTrue(senior.studentStatistics.getCompletedClasses().contains("Precalculus"));
    }

    @Test
    @DisplayName("Generated percents stay in 0-100 and seed skill mastery")
    void percentsStayInRangeAndSeedSkills() {
        HashMap<Integer, Student> students = new HashMap<>();
        students.put(1, createStudent("Senior", 100, 50));

        SchoolHistoryGenerator.generate(students, new HashMap<>(), SimConstants.STARTING_YEAR, null);

        Transcript transcript = students.get(1).studentStatistics.getAcademicRecord().getTranscript();
        assertFalse(transcript.isEmpty());
        for (TranscriptEntry entry : transcript.getEntries()) {
            assertTrue(entry.getFinalPercent() >= 0.0 && entry.getFinalPercent() <= 100.0,
                    entry.getClassName() + " percent " + entry.getFinalPercent());
        }
        double math = students.get(1).studentStatistics.getAcademicRecord()
                .getSkillMastery(AcademicSkill.MATH);
        assertTrue(math > 0.0, "seniors should enter with some math mastery");
    }

    @Test
    @DisplayName("Cohort mean GPA lands in a 2004-plausible band")
    void cohortMeanGpaIsPlausible() {
        HashMap<Integer, Student> students = mixedCohort(80);
        SchoolHistoryGenerator.HistoryReport report = SchoolHistoryGenerator.generate(
                students, staffPool(), SimConstants.STARTING_YEAR, null);

        double mean = report.getMeanGpa();
        assertTrue(mean >= 2.6 && mean <= 3.3,
                "cohort mean GPA " + mean + " should sit near 2.8-3.1");
        assertTrue(report.getTranscriptEntries() > 0);
        assertEquals(60, report.getStudentsWithHistory());
    }

    @Test
    @DisplayName("Live inspector Grades tab shows cumulative GPA and transcript for upperclassmen")
    void inspectorGradesAreaShowsGeneratedHistory() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student senior = createStudent("Senior", 110, 60);
        Student freshman = createStudent("Freshman", 110, 60);
        students.put(1, senior);
        students.put(2, freshman);
        SchoolHistoryGenerator.generate(students, staffPool(), SimConstants.STARTING_YEAR, null);

        JTextArea seniorArea = new JTextArea();
        Inspector.updateStudentGradesArea(senior, seniorArea);
        String seniorText = seniorArea.getText();
        assertTrue(seniorText.contains("Cumulative GPA:"), seniorText);
        assertTrue(seniorText.contains("Transcript"), seniorText);
        assertTrue(seniorText.contains("2001-02 Freshman"), seniorText);
        assertTrue(seniorText.contains("2003-04 Junior"), seniorText);

        JTextArea freshmanArea = new JTextArea();
        Inspector.updateStudentGradesArea(freshman, freshmanArea);
        String freshmanText = freshmanArea.getText();
        assertFalse(freshmanText.contains("Cumulative GPA:"), freshmanText);
        assertFalse(freshmanText.contains("Transcript"), freshmanText);
    }

    @Test
    @DisplayName("Empty or null student maps produce an empty report")
    void emptyInputIsSafe() {
        SchoolHistoryGenerator.HistoryReport empty = SchoolHistoryGenerator.generate(
                new HashMap<>(), null, SimConstants.STARTING_YEAR, null);
        assertEquals(0, empty.getStudentsWithHistory());
        assertEquals(-1.0, empty.getMeanGpa(), 0.001);

        SchoolHistoryGenerator.HistoryReport missing = SchoolHistoryGenerator.generate(
                null, null, SimConstants.STARTING_YEAR, null);
        assertEquals(0, missing.getStudentsWithHistory());
    }

    private void assertYears(Student student, int... expectedYears) {
        List<Integer> years = student.studentStatistics.getAcademicRecord()
                .getTranscript().getSchoolYears();
        assertEquals(expectedYears.length, years.size(), years.toString());
        for (int i = 0; i < expectedYears.length; i++) {
            assertEquals(expectedYears[i], years.get(i));
        }
    }

    private String firstGradeLevel(Student student) {
        return student.studentStatistics.getAcademicRecord().getTranscript()
                .getEntries().get(0).getGradeLevel();
    }

    private String lastGradeLevel(Student student) {
        List<TranscriptEntry> entries = student.studentStatistics.getAcademicRecord()
                .getTranscript().getEntries();
        return entries.get(entries.size() - 1).getGradeLevel();
    }

    private double meanGpa(HashMap<Integer, Student> students) {
        double total = 0.0;
        int count = 0;
        for (Student student : students.values()) {
            double gpa = student.studentStatistics.getAcademicRecord().getCumulativeGpa();
            if (gpa >= 0.0) {
                total += gpa;
                count++;
            }
        }
        return count == 0 ? -1.0 : total / count;
    }

    private HashMap<Integer, Student> cohort(int count) {
        HashMap<Integer, Student> students = new HashMap<>();
        String[] grades = {"Sophomore", "Junior", "Senior"};
        for (int i = 0; i < count; i++) {
            students.put(i, createStudent(grades[i % grades.length], 100, 50));
        }
        return students;
    }

    private HashMap<Integer, Student> repeatingGrade(String grade, int intelligence,
                                                     int determination, int count) {
        HashMap<Integer, Student> students = new HashMap<>();
        for (int i = 0; i < count; i++) {
            students.put(i, createStudent(grade, intelligence, determination));
        }
        return students;
    }

    private HashMap<Integer, Student> mixedCohort(int count) {
        HashMap<Integer, Student> students = new HashMap<>();
        String[] grades = {"Freshman", "Sophomore", "Junior", "Senior"};
        for (int i = 0; i < count; i++) {
            Student student = createStudent(grades[i % grades.length],
                    (int) GameRandom.nextGaussian(100, 15),
                    (int) GameRandom.nextGaussian(50, 15));
            student.studentStatistics.setPerception((int) GameRandom.nextGaussian(50, 15));
            student.studentStatistics.setLuck((int) GameRandom.nextGaussian(0, 15));
            students.put(i, student);
        }
        return students;
    }

    private HashMap<Integer, Staff> staffPool() {
        HashMap<Integer, Staff> staff = new HashMap<>();
        StaffType[] types = {
                StaffType.ENGLISH, StaffType.MATH, StaffType.SCIENCE, StaffType.HISTORY,
                StaffType.LANGUAGES, StaffType.PHYSICAL_ED, StaffType.VOCATIONAL
        };
        int id = 0;
        for (StaffType type : types) {
            Staff teacher = new Staff();
            teacher.teacherStatistics.setStaffType(type);
            teacher.teacherStatistics.setYearsOfExperience(8);
            staff.put(id++, teacher);
        }
        return staff;
    }

    private Student createStudent(String gradeLevel, int intelligence, int determination) {
        Student student = new Student();
        student.studentStatistics.setGradeLevel(gradeLevel);
        student.studentStatistics.setGender("Female");
        student.studentStatistics.setIncomeLevel("middle");
        student.studentStatistics.setIntelligence(intelligence);
        student.studentStatistics.setDetermination(determination);
        student.studentStatistics.setPerception(50);
        student.studentStatistics.setCharisma(50);
        student.studentStatistics.setStrength(50);
        student.studentStatistics.setAgility(50);
        student.studentStatistics.setLuck(0);
        student.studentStatistics.setInitiative(50);
        student.studentStatistics.setResponsibility(50);
        student.studentStatistics.setCreativity(50);
        student.studentStatistics.setCuriosity(50);
        return student;
    }
}
