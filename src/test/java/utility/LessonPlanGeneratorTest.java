package utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import entity.Staff;
import entity.TeacherBlock;
import entity.TeacherTemperament;
import entity.academic.AssessmentType;
import entity.academic.CourseCategory;
import entity.academic.LessonPlan;
import entity.academic.ScheduledAssessment;

@DisplayName("Lesson plan generator")
class LessonPlanGeneratorTest {

    @BeforeEach
    void setUp() {
        GameRandom.initialize(42L);
    }

    @Test
    @DisplayName("Core classes get the quiz/test/midterm/final structure")
    void coreClassStructure() {
        Staff teacher = createTeacher(10, TeacherTemperament.BALANCED);
        addBlock(teacher, "Algebra I", "Fall", 1);
        LessonPlan plan = generate(teacher).get(0);

        assertEquals(CourseCategory.CORE_ACADEMIC, plan.getCategory());
        assertEquals(1, countType(plan, AssessmentType.MIDTERM));
        assertEquals(1, countType(plan, AssessmentType.FINAL));
        assertEquals(3, countType(plan, AssessmentType.TEST));
        assertEquals(6, countType(plan, AssessmentType.QUIZ));
        assertEquals(2, plan.getHomeworkIntervalDays());
    }

    @Test
    @DisplayName("PE grades on performance: skills evals, no homework")
    void physicalEducationStructure() {
        Staff teacher = createTeacher(10, TeacherTemperament.BALANCED);
        addBlock(teacher, "Team Sports", "Fall", 1);
        LessonPlan plan = generate(teacher).get(0);

        assertEquals(CourseCategory.PHYSICAL_EDUCATION, plan.getCategory());
        assertEquals(0, plan.getHomeworkIntervalDays());
        assertTrue(countType(plan, AssessmentType.SKILLS_EVAL) >= 3);
        assertEquals(0, countType(plan, AssessmentType.TEST));
        assertTrue(plan.getWeights().containsKey(AssessmentType.PARTICIPATION));
    }

    @Test
    @DisplayName("Arts classes grade on projects and participation")
    void artsStructure() {
        Staff teacher = createTeacher(10, TeacherTemperament.BALANCED);
        addBlock(teacher, "Band", "Spring", 2);
        LessonPlan plan = generate(teacher).get(0);

        assertEquals(CourseCategory.PERFORMING_ARTS, plan.getCategory());
        assertEquals(4, countType(plan, AssessmentType.PROJECT));
        assertTrue(plan.getWeights().containsKey(AssessmentType.PARTICIPATION));
    }

    @Test
    @DisplayName("All assessments land on weekdays inside the semester with no same-day collisions")
    void datesAreValidAndCollisionFree() {
        Staff teacher = createTeacher(0, TeacherTemperament.ERRATIC);
        addBlock(teacher, "Biology", "Fall", 1);
        LessonPlan plan = generate(teacher).get(0);

        AcademicCalendar.SemesterBounds fall = AcademicCalendar.getSemesterBounds("Fall");
        Set<Integer> usedDays = new HashSet<>();
        for (ScheduledAssessment assessment : plan.getAssessments()) {
            int day = assessment.getScheduledDay();
            assertTrue(day >= fall.getFirstDay() && day <= fall.getLastDay(),
                    assessment.getTitle() + " outside semester: day " + day);
            assertFalse(AcademicCalendar.isWeekend(day),
                    assessment.getTitle() + " on a weekend: day " + day);
            assertTrue(usedDays.add(day),
                    "Two assessments on day " + day);
        }
    }

    @Test
    @DisplayName("Strict teachers schedule an extra quiz over lenient ones")
    void strictTeachersAddQuizzes() {
        Staff strict = createTeacher(10, TeacherTemperament.STRICT);
        addBlock(strict, "Chemistry", "Fall", 1);
        int strictQuizzes = countType(generate(strict).get(0), AssessmentType.QUIZ);

        Staff lenient = createTeacher(10, TeacherTemperament.LENIENT);
        addBlock(lenient, "Chemistry", "Fall", 1);
        int lenientQuizzes = countType(generate(lenient).get(0), AssessmentType.QUIZ);

        assertEquals(7, strictQuizzes);
        assertEquals(5, lenientQuizzes);
    }

    @Test
    @DisplayName("Parallel sections of the same course share assessment dates")
    void parallelSectionsShareDates() {
        Staff teacher = createTeacher(10, TeacherTemperament.BALANCED);
        addBlock(teacher, "English I", "Fall", 1);
        addBlock(teacher, "English I", "Fall", 3);
        List<LessonPlan> plans = generate(teacher);

        assertEquals(2, plans.size());
        List<Integer> daysA = new ArrayList<>();
        List<Integer> daysB = new ArrayList<>();
        plans.get(0).getAssessments().forEach(a -> daysA.add(a.getScheduledDay()));
        plans.get(1).getAssessments().forEach(a -> daysB.add(a.getScheduledDay()));
        assertEquals(daysA, daysB);
    }

    @Test
    @DisplayName("Same seed produces the same lesson plan")
    void seedDeterminism() {
        GameRandom.initialize(7L);
        Staff first = createTeacher(5, TeacherTemperament.BALANCED);
        addBlock(first, "World History", "Spring", 2);
        List<Integer> firstDays = new ArrayList<>();
        generate(first).get(0).getAssessments()
                .forEach(a -> firstDays.add(a.getScheduledDay()));

        GameRandom.initialize(7L);
        Staff second = createTeacher(5, TeacherTemperament.BALANCED);
        addBlock(second, "World History", "Spring", 2);
        List<Integer> secondDays = new ArrayList<>();
        generate(second).get(0).getAssessments()
                .forEach(a -> secondDays.add(a.getScheduledDay()));

        assertEquals(firstDays, secondDays);
    }

    @Test
    @DisplayName("Generation is idempotent: existing plans are kept")
    void idempotentGeneration() {
        Staff teacher = createTeacher(10, TeacherTemperament.BALANCED);
        addBlock(teacher, "Geometry", "Fall", 1);
        HashMap<Integer, Staff> staffMap = new HashMap<>();
        staffMap.put(0, teacher);

        assertEquals(1, LessonPlanGenerator.ensureLessonPlans(staffMap));
        LessonPlan original = firstBlock(teacher).getLessonPlan();
        assertNotNull(original);

        assertEquals(0, LessonPlanGenerator.ensureLessonPlans(staffMap));
        assertTrue(original == firstBlock(teacher).getLessonPlan());
    }

    private List<LessonPlan> generate(Staff teacher) {
        HashMap<Integer, Staff> staffMap = new HashMap<>();
        staffMap.put(0, teacher);
        LessonPlanGenerator.ensureLessonPlans(staffMap);
        List<LessonPlan> plans = new ArrayList<>();
        for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
            if (block.getLessonPlan() != null) {
                plans.add(block.getLessonPlan());
            }
        }
        return plans;
    }

    private TeacherBlock firstBlock(Staff teacher) {
        return teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule().get(0);
    }

    private int countType(LessonPlan plan, AssessmentType type) {
        int count = 0;
        for (ScheduledAssessment assessment : plan.getAssessments()) {
            if (assessment.getType() == type) {
                count++;
            }
        }
        return count;
    }

    private Staff createTeacher(int yearsOfExperience, TeacherTemperament temperament) {
        Staff teacher = new Staff();
        teacher.teacherStatistics.setYearsOfExperience(yearsOfExperience);
        teacher.teacherStatistics.setTemperament(temperament);
        teacher.teacherStatistics.setIntelligence(100);
        teacher.teacherStatistics.setPerception(50);
        return teacher;
    }

    private void addBlock(Staff teacher, String className, String semester, int blockNumber) {
        TeacherBlock block = new TeacherBlock();
        block.setClassName(className);
        block.setSemester(semester);
        block.setBlockNumber(blockNumber);
        teacher.teacherStatistics.addTeacherSchedule(block);
    }
}
