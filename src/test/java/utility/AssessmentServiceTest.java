package utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import behavior.BehaviorContext;
import behavior.BehaviorTree;
import behavior.StudentBehaviorTreeBuilder;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Classroom;
import entity.Staff;
import entity.Student;
import entity.StudentBlock;
import entity.TeacherBlock;
import entity.Time;
import entity.academic.AssessmentType;
import entity.academic.CourseCategory;
import entity.academic.GradeEntry;
import entity.academic.LessonPlan;
import entity.academic.LessonPlanTemplate;
import entity.academic.ScheduledAssessment;
import entity.academic.StudentAcademicRecord;

@DisplayName("Assessment service")
class AssessmentServiceTest {

    private static final String CLASS_NAME = "Algebra I";

    @BeforeEach
    void setUp() {
        GameRandom.initialize(42L);
    }

    @Test
    @DisplayName("Starts a scheduled assessment when its period begins and grades it when the window ends")
    void runsFullAssessmentLifecycle() {
        Staff teacher = createTeacher();
        Student present = createStudent(110, 60, 55);
        Student absent = createStudent(100, 50, 50);
        ScheduledAssessment quiz = new ScheduledAssessment("quiz_1", AssessmentType.QUIZ,
                "Quiz 1", 1, 50);
        TeacherBlock block = wireSection(teacher, quiz, present, absent);

        HashMap<Integer, Staff> staffMap = new HashMap<>();
        staffMap.put(0, teacher);

        // 8:21 AM on day 1: block 1 is underway.
        Time time = new Time();
        time.stepForwardMinutes(81);
        AssessmentService.processTick(staffMap, time);
        assertTrue(quiz.isInProgress());

        // The present student sits the whole quiz; the absent one never shows.
        StudentAcademicRecord presentRecord = present.studentStatistics.getAcademicRecord();
        for (int minute = 0; minute < AssessmentType.QUIZ.getClassMinutes(); minute++) {
            presentRecord.getOrCreateAttempt(quiz.getId()).recordMinute();
        }

        time.stepForwardMinutes(AssessmentType.QUIZ.getClassMinutes() + 1);
        AssessmentService.processTick(staffMap, time);

        assertTrue(quiz.isGraded());
        assertTrue(quiz.getClassAverage() > 0.0);

        List<GradeEntry> presentEntries = presentRecord.getCourse(CLASS_NAME)
                .getGradeBook().getEntries();
        assertEquals(1, presentEntries.size());
        assertFalse(presentEntries.get(0).isMissing());
        assertTrue(presentEntries.get(0).getScore() > 0.0);

        List<GradeEntry> absentEntries = absent.studentStatistics.getAcademicRecord()
                .getCourse(CLASS_NAME).getGradeBook().getEntries();
        assertEquals(1, absentEntries.size());
        assertTrue(absentEntries.get(0).isMissing());
        assertEquals(0.0, absentEntries.get(0).getScore(), 0.001);

        // Grading stores the lesson plan's weights so letter grades resolve.
        assertEquals("Fall", presentRecord.getCourse(CLASS_NAME).getSemester());
        assertTrue(block.getLessonPlan().getInProgressAssessment() == null);
    }

    @Test
    @DisplayName("Students switch to TAKING_ASSESSMENT while a test is in progress")
    void behaviorTreeTakesAssessment() {
        Staff teacher = createTeacher();
        // Highly social student who would otherwise chat through class.
        Student student = createStudent(100, 20, 50);
        student.studentStatistics.setCharisma(90);
        ScheduledAssessment quiz = new ScheduledAssessment("quiz_bt", AssessmentType.QUIZ,
                "Quiz 1", 1, 50);
        wireSection(teacher, quiz, student);

        Classroom classroom = new Classroom();
        EntityState state = student.getEntityState();
        state.setCurrentRoom(classroom);
        state.setExpectedRoom(classroom);

        Time time = new Time();
        time.stepForwardMinutes(81);
        quiz.begin(time.getMinutesFromMidnight());

        BehaviorTree tree = StudentBehaviorTreeBuilder.buildTree(student);
        tree.tick(new BehaviorContext(student, time, null));

        assertEquals(ActivityType.TAKING_ASSESSMENT, state.getCurrentActivity());
        assertEquals(1, student.studentStatistics.getAcademicRecord()
                .getOrCreateAttempt(quiz.getId()).getMinutesSat());
    }

    /**
     * Builds a teacher block for period 1 Fall with a one-assessment lesson
     * plan, enrolls the students in it, and mirrors it on their schedules.
     */
    private TeacherBlock wireSection(Staff teacher, ScheduledAssessment assessment,
                                     Student... students) {
        TeacherBlock teacherBlock = new TeacherBlock();
        teacherBlock.setBlockNumber(1);
        teacherBlock.setClassName(CLASS_NAME);
        teacherBlock.setSemester("Fall");
        teacherBlock.addClassPopulationBlock(30);
        teacherBlock.setLessonPlan(new LessonPlan(CourseCategory.CORE_ACADEMIC,
                LessonPlanTemplate.forCategory(CourseCategory.CORE_ACADEMIC).getWeights(),
                2, List.of(assessment)));
        teacher.teacherStatistics.addTeacherSchedule(teacherBlock);

        for (Student student : students) {
            teacherBlock.addStudentToBlock(student);
            StudentBlock studentBlock = new StudentBlock();
            studentBlock.setBlockNumber(1);
            studentBlock.setClassName(CLASS_NAME);
            studentBlock.setSemester("Fall");
            studentBlock.setTeacher(teacher);
            student.studentStatistics.addStudentSchedule(studentBlock);
        }
        return teacherBlock;
    }

    private Student createStudent(int intelligence, int determination, int perception) {
        Student student = new Student();
        student.studentStatistics.setIntelligence(intelligence);
        student.studentStatistics.setDetermination(determination);
        student.studentStatistics.setPerception(perception);
        student.studentStatistics.setInitiative(50);
        student.studentStatistics.setResponsibility(50);
        return student;
    }

    private Staff createTeacher() {
        Staff teacher = new Staff();
        teacher.teacherStatistics.setYearsOfExperience(10);
        teacher.teacherStatistics.setIntelligence(100);
        teacher.teacherStatistics.setPerception(50);
        return teacher;
    }
}
