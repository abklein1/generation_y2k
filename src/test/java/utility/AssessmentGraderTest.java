package utility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import entity.Staff;
import entity.Student;
import entity.TeacherTemperament;
import entity.academic.AssessmentType;
import entity.academic.CourseCategory;
import entity.academic.CourseProgress;
import entity.academic.ScheduledAssessment;

@DisplayName("Assessment grader")
class AssessmentGraderTest {

    @Test
    @DisplayName("Higher course understanding raises test scores")
    void understandingRaisesScores() {
        Student prepared = createStudent(100, 50, 50);
        prepared.studentStatistics.getAcademicRecord()
                .getOrCreateCourse("Algebra I").addUnderstanding(90.0);
        Student unprepared = createStudent(100, 50, 50);

        ScheduledAssessment test = quiz(50);
        GameRandom.initialize(42L);
        double preparedScore = AssessmentGrader.score(prepared, null, "Algebra I",
                CourseCategory.CORE_ACADEMIC, test);
        GameRandom.initialize(42L);
        double unpreparedScore = AssessmentGrader.score(unprepared, null, "Algebra I",
                CourseCategory.CORE_ACADEMIC, test);

        assertTrue(preparedScore > unpreparedScore);
    }

    @Test
    @DisplayName("Intelligence raises scores on knowledge assessments")
    void intelligenceRaisesScores() {
        Student bright = createStudent(130, 50, 50);
        Student average = createStudent(100, 50, 50);

        ScheduledAssessment test = quiz(50);
        GameRandom.initialize(42L);
        double brightScore = AssessmentGrader.score(bright, null, "Biology",
                CourseCategory.CORE_ACADEMIC, test);
        GameRandom.initialize(42L);
        double averageScore = AssessmentGrader.score(average, null, "Biology",
                CourseCategory.CORE_ACADEMIC, test);

        assertTrue(brightScore > averageScore);
    }

    @Test
    @DisplayName("Lenient teachers grade higher than strict ones")
    void lenientBeatsStrict() {
        Student student = createStudent(100, 50, 50);
        student.studentStatistics.getAcademicRecord()
                .getOrCreateCourse("English I").addUnderstanding(50.0);

        Staff strict = createTeacher(10, TeacherTemperament.STRICT);
        Staff lenient = createTeacher(10, TeacherTemperament.LENIENT);

        ScheduledAssessment test = quiz(50);
        GameRandom.initialize(42L);
        double strictScore = AssessmentGrader.score(student, strict, "English I",
                CourseCategory.CORE_ACADEMIC, test);
        GameRandom.initialize(42L);
        double lenientScore = AssessmentGrader.score(student, lenient, "English I",
                CourseCategory.CORE_ACADEMIC, test);

        assertTrue(lenientScore > strictScore);
    }

    @Test
    @DisplayName("Experience shrinks grading variance")
    void experienceReducesVariance() {
        Student student = createStudent(100, 50, 50);
        student.studentStatistics.getAcademicRecord()
                .getOrCreateCourse("US History").addUnderstanding(50.0);
        Staff rookie = createTeacher(0, TeacherTemperament.BALANCED);
        Staff veteran = createTeacher(20, TeacherTemperament.BALANCED);
        ScheduledAssessment test = quiz(50);

        double rookieSpread = scoreSpread(student, rookie, test);
        double veteranSpread = scoreSpread(student, veteran, test);

        assertTrue(veteranSpread < rookieSpread,
                "veteran spread " + veteranSpread + " should be below rookie " + rookieSpread);
    }

    @Test
    @DisplayName("PE skills evaluations reward strength and agility, not intelligence")
    void skillsEvalUsesAthleticStats() {
        Student athlete = createStudent(85, 50, 50);
        athlete.studentStatistics.setStrength(80);
        athlete.studentStatistics.setAgility(85);

        Student scholar = createStudent(130, 50, 50);
        scholar.studentStatistics.setStrength(25);
        scholar.studentStatistics.setAgility(30);

        ScheduledAssessment eval = new ScheduledAssessment("eval", AssessmentType.SKILLS_EVAL,
                "Skills Evaluation 1", 20, 50);
        GameRandom.initialize(42L);
        double athleteScore = AssessmentGrader.score(athlete, null, "Team Sports",
                CourseCategory.PHYSICAL_EDUCATION, eval);
        GameRandom.initialize(42L);
        double scholarScore = AssessmentGrader.score(scholar, null, "Team Sports",
                CourseCategory.PHYSICAL_EDUCATION, eval);

        assertTrue(athleteScore > scholarScore);
    }

    @Test
    @DisplayName("Scratch course state is used instead of the live record")
    void scratchCourseOverridesLiveUnderstanding() {
        Student student = createStudent(100, 50, 50);
        student.studentStatistics.getAcademicRecord()
                .getOrCreateCourse("Algebra I").addUnderstanding(10.0);

        CourseProgress prepared = new CourseProgress("algebra_i", "Algebra I");
        prepared.addUnderstanding(90.0);
        ScheduledAssessment test = quiz(50);

        GameRandom.initialize(42L);
        double liveScore = AssessmentGrader.score(student, null, "Algebra I",
                CourseCategory.CORE_ACADEMIC, test);
        GameRandom.initialize(42L);
        double scratchScore = AssessmentGrader.score(student, null, "Algebra I",
                CourseCategory.CORE_ACADEMIC, test, prepared);

        assertTrue(scratchScore > liveScore);
    }

    @Test
    @DisplayName("Harder assessments score lower")
    void difficultyLowersScores() {
        Student student = createStudent(100, 50, 50);
        GameRandom.initialize(42L);
        double easyScore = AssessmentGrader.score(student, null, "Algebra I",
                CourseCategory.CORE_ACADEMIC, quiz(20));
        GameRandom.initialize(42L);
        double hardScore = AssessmentGrader.score(student, null, "Algebra I",
                CourseCategory.CORE_ACADEMIC, quiz(90));

        assertTrue(easyScore > hardScore);
    }

    private double scoreSpread(Student student, Staff teacher, ScheduledAssessment test) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int seed = 0; seed < 40; seed++) {
            GameRandom.initialize(1000L + seed);
            double score = AssessmentGrader.score(student, teacher, "US History",
                    CourseCategory.CORE_ACADEMIC, test);
            min = Math.min(min, score);
            max = Math.max(max, score);
        }
        return max - min;
    }

    private ScheduledAssessment quiz(int difficulty) {
        return new ScheduledAssessment("quiz", AssessmentType.QUIZ, "Quiz 1", 10, difficulty);
    }

    private Student createStudent(int intelligence, int determination, int perception) {
        Student student = new Student();
        student.studentStatistics.setIntelligence(intelligence);
        student.studentStatistics.setDetermination(determination);
        student.studentStatistics.setPerception(perception);
        student.studentStatistics.setLuck(0);
        return student;
    }

    private Staff createTeacher(int yearsOfExperience, TeacherTemperament temperament) {
        Staff teacher = new Staff();
        teacher.teacherStatistics.setYearsOfExperience(yearsOfExperience);
        teacher.teacherStatistics.setTemperament(temperament);
        return teacher;
    }
}
