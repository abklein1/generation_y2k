package utility;

import behavior.BehaviorContext;
import behavior.BehaviorTree;
import behavior.StudentBehaviorTreeBuilder;
import entity.ActivityType;
import entity.EntityState;
import entity.Staff;
import entity.Student;
import entity.StudentBlock;
import entity.Time;
import entity.Rooms.Classroom;
import entity.academic.AcademicSkill;
import entity.academic.CourseProgress;
import entity.academic.HomeworkAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Academic progress service")
class AcademicProgressServiceTest {

    @BeforeEach
    void setUp() {
        GameRandom.initialize(42L);
    }

    @Test
    @DisplayName("Records class learning and drains academic effort pools")
    void recordsClassLearningAndDrainsPools() {
        Student student = createStudentWithStats(120, 70, 65, 20, 20);

        double gained = AcademicProgressService.recordClassLearning(
                student, "Algebra I", 5, ActivityType.ATTENDING_CLASS);

        CourseProgress algebra = student.studentStatistics.getAcademicRecord().getCourse("Algebra I");
        assertTrue(gained > 0.0);
        assertTrue(algebra.getUnderstanding() > 0.0);
        assertTrue(student.studentStatistics.getAcademicRecord().getSkillMastery(AcademicSkill.MATH) > 0.0);
        assertTrue(student.studentStatistics.getInitiative() < 20);
        assertTrue(student.studentStatistics.getResponsibility() < 20);
    }

    @Test
    @DisplayName("Assigns homework on regular intervals instead of every day")
    void assignsHomeworkOnIntervalsOnly() {
        Student student = createStudentWithStats(100, 50, 50, 30, 30);
        addScheduledClass(student, "Biology", "Fall");
        Time time = new Time();

        assertEquals(0, AcademicProgressService.assignHomeworkIfDue(student, time));

        time.incrementDayCounter();

        assertEquals(1, AcademicProgressService.assignHomeworkIfDue(student, time));
        assertEquals(1, student.studentStatistics.getAcademicRecord().getPendingHomeworkCount());
        assertEquals(0, AcademicProgressService.assignHomeworkIfDue(student, time));
    }

    @Test
    @DisplayName("Resolves due homework using prior understanding")
    void resolvesDueHomeworkUsingUnderstanding() {
        Student student = createStudentWithStats(120, 80, 70, 80, 80);
        CourseProgress course = student.studentStatistics.getAcademicRecord().getOrCreateCourse("World History");
        course.addUnderstanding(80.0);
        student.studentStatistics.getAcademicRecord().addHomework(new HomeworkAssignment(
                "world_history", "World History", 12, 5, 1, 1));

        int completed = AcademicProgressService.resolveHomeworkForDay(student, 1);

        assertEquals(1, completed);
        assertEquals(1, course.getCompletedHomework());
        assertTrue(course.getUnderstanding() > 80.0);
        assertTrue(student.studentStatistics.getAcademicRecord().getSkillMastery(AcademicSkill.HISTORY) > 0.0);
    }

    @Test
    @DisplayName("Academic pressure gets class-time priority over social behavior")
    void academicPressureGetsClassPriority() {
        Student student = createStudentWithStats(100, 20, 50, 50, 50);
        student.studentStatistics.setCharisma(90);
        addScheduledClass(student, "Algebra I", "Fall");
        student.studentStatistics.getAcademicRecord().addHomework(new HomeworkAssignment(
                "algebra_i", "Algebra I", 12, 5, 1, 2));

        Classroom classroom = new Classroom();
        EntityState state = student.getEntityState();
        state.setCurrentRoom(classroom);
        state.setExpectedRoom(classroom);

        Time time = new Time();
        time.stepForwardMinutes(81);
        BehaviorTree tree = StudentBehaviorTreeBuilder.buildTree(student);
        tree.tick(new BehaviorContext(student, time, null));

        ActivityType activity = state.getCurrentActivity();
        assertTrue(activity == ActivityType.ATTENDING_CLASS || activity == ActivityType.TAKING_NOTES);
    }

    @Test
    @DisplayName("Experienced teachers improve class learning")
    void experiencedTeachersImproveLearning() {
        Time time = new Time();
        time.stepForwardMinutes(81);

        Student newTeacherStudent = createStudentWithStats(100, 50, 50, 50, 50);
        addScheduledClass(newTeacherStudent, "Chemistry", "Fall",
                createTeacher(0, 100, 50));

        Student experiencedTeacherStudent = createStudentWithStats(100, 50, 50, 50, 50);
        addScheduledClass(experiencedTeacherStudent, "Chemistry", "Fall",
                createTeacher(20, 120, 70));

        double newTeacherLearning = AcademicProgressService.recordCurrentClassLearning(
                newTeacherStudent, time, 5, ActivityType.ATTENDING_CLASS);
        double experiencedTeacherLearning = AcademicProgressService.recordCurrentClassLearning(
                experiencedTeacherStudent, time, 5, ActivityType.ATTENDING_CLASS);

        assertTrue(experiencedTeacherLearning > newTeacherLearning);
    }

    @Test
    @DisplayName("Experienced teachers ramp homework complexity")
    void experiencedTeachersRampHomeworkComplexity() {
        Student student = createStudentWithStats(100, 50, 50, 50, 50);
        addScheduledClass(student, "Geometry", "Fall", createTeacher(20, 120, 70));
        Time time = new Time();

        time.incrementDayCounter();
        assertEquals(1, AcademicProgressService.assignHomeworkIfDue(student, time));
        HomeworkAssignment first = student.studentStatistics.getAcademicRecord()
                .getHomeworkAssignments().get(0);
        first.markCompleted();

        time.incrementDayCounter();
        time.incrementDayCounter();
        assertEquals(1, AcademicProgressService.assignHomeworkIfDue(student, time));
        HomeworkAssignment second = student.studentStatistics.getAcademicRecord()
                .getHomeworkAssignments().get(1);

        assertTrue(second.getEffort() > first.getEffort());
        assertTrue(second.getProblemCount() >= first.getProblemCount());
    }

    private Student createStudentWithStats(int intelligence, int determination, int perception,
                                           int initiative, int responsibility) {
        Student student = new Student();
        student.studentStatistics.setIntelligence(intelligence);
        student.studentStatistics.setDetermination(determination);
        student.studentStatistics.setPerception(perception);
        student.studentStatistics.setInitiative(initiative);
        student.studentStatistics.setResponsibility(responsibility);
        return student;
    }

    private void addScheduledClass(Student student, String className, String semester) {
        addScheduledClass(student, className, semester, null);
    }

    private void addScheduledClass(Student student, String className, String semester, Staff teacher) {
        StudentBlock block = new StudentBlock();
        block.setBlockNumber(1);
        block.setClassName(className);
        block.setSemester(semester);
        block.setLunch(false);
        block.setTeacher(teacher);
        student.studentStatistics.addStudentSchedule(block);
    }

    private Staff createTeacher(int yearsOfExperience, int intelligence, int perception) {
        Staff teacher = new Staff();
        teacher.teacherStatistics.setYearsOfExperience(yearsOfExperience);
        teacher.teacherStatistics.setIntelligence(intelligence);
        teacher.teacherStatistics.setPerception(perception);
        return teacher;
    }
}
