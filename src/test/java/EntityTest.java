import entity.*;
import entity.Rooms.Classroom;
import entity.Rooms.Lunchroom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import utility.Director;
import view.GameView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for entity classes: Student, Staff, and related objects.
 * Tests cover name handling, statistics, physical characteristics, and basic entity creation.
 */
@DisplayName("Entity Tests")
public class EntityTest {

    @Nested
    @DisplayName("Student Name Tests")
    class StudentNameTests {
        
        @Test
        @DisplayName("Should set and get first name correctly")
        void testStudentFirstName() {
            Student student = new Student();
            student.studentName.setFirstName("Mark");
            assertEquals("Mark", student.studentName.getFirstName());
        }

        @Test
        @DisplayName("Should set and get last name correctly")
        void testStudentLastName() {
            Student student = new Student();
            student.studentName.setLastName("Tester");
            assertEquals("Tester", student.studentName.getLastName());
        }

        @Test
        @DisplayName("Should set and get nickname correctly")
        void testStudentNickname() {
            Student student = new Student();
            student.studentName.setNickname("Buddy");
            assertEquals("Buddy", student.studentName.getNickname());
        }

        @Test
        @DisplayName("Should set and get suffix correctly")
        void testStudentSuffix() {
            Student student = new Student();
            student.studentName.setSuffix("Jr.");
            assertEquals("Jr.", student.studentName.getSuffix());
        }
    }

    @Nested
    @DisplayName("Student Statistics Tests")
    class StudentStatisticsTests {
        
        @Test
        @DisplayName("Should set and get primary stats correctly")
        void testStudentPrimaryStats() {
            Student student = new Student();
            student.studentStatistics.setDetermination(2);
            student.studentStatistics.setStrength(3);
            student.studentStatistics.setAgility(4);
            student.studentStatistics.setCharisma(5);
            student.studentStatistics.setIntelligence(6);
            
            assertAll("Primary Stats",
                () -> assertEquals(2, student.studentStatistics.getDetermination()),
                () -> assertEquals(3, student.studentStatistics.getStrength()),
                () -> assertEquals(4, student.studentStatistics.getAgility()),
                () -> assertEquals(5, student.studentStatistics.getCharisma()),
                () -> assertEquals(6, student.studentStatistics.getIntelligence())
            );
        }

        @Test
        @DisplayName("Should set and get entertainment and energy correctly via EntityState")
        void testStudentEntertainmentAndEnergy() {
            Student student = new Student();
            entity.EntityState state = student.getEntityState();
            
            assertEquals(100.0, state.getEntertainment());
            assertEquals(100.0, state.getEnergy());
            assertFalse(state.isAsleep());
            
            state.setEntertainment(50.0);
            assertEquals(50.0, state.getEntertainment());
            
            state.setEnergy(0.0);
            assertEquals(0.0, state.getEnergy());
        }

        @Test
        @DisplayName("Should set and get physical characteristics correctly")
        void testStudentPhysicalCharacteristics() {
            Student student = new Student();
            student.studentStatistics.setBuild("Medium");
            student.studentStatistics.setHairColor("Brown");
            student.studentStatistics.setEyeColor("Blue");
            student.studentStatistics.setHeight(64.5);
            
            assertAll("Physical Characteristics",
                () -> assertEquals("Medium", student.studentStatistics.getBuild()),
                () -> assertEquals("Brown", student.studentStatistics.getHairColor()),
                () -> assertEquals("Blue", student.studentStatistics.getEyeColor()),
                () -> assertEquals(64.5, student.studentStatistics.getHeight(), 0.01)
            );
        }

        @Test
        @DisplayName("Should accumulate experience correctly")
        void testStudentExperience() {
            Student student = new Student();
            student.studentStatistics.setExperience(10);
            assertEquals(10, student.studentStatistics.getExperience());
            
            student.studentStatistics.setExperience(10);
            assertEquals(20, student.studentStatistics.getExperience());
        }
        
        @Test
        @DisplayName("Should set perception correctly")
        void testStudentPerception() {
            Student student = new Student();
            student.studentStatistics.setPerception(75);
            assertEquals(75, student.studentStatistics.getPerception());
        }
        
        @Test
        @DisplayName("Should set creativity correctly")
        void testStudentCreativity() {
            Student student = new Student();
            student.studentStatistics.setCreativity(60);
            assertEquals(60, student.studentStatistics.getCreativity());
        }
        
        @Test
        @DisplayName("Should set curiosity correctly")
        void testStudentCuriosity() {
            Student student = new Student();
            student.studentStatistics.setCuriosity(80);
            assertEquals(80, student.studentStatistics.getCuriosity());
        }

        @Test
        @DisplayName("Should treat instructional rooms as in class regardless of current activity")
        void testInstructionalRoomCountsAsInClass() {
            Student student = new Student();
            EntityState state = student.getEntityState();
            Classroom classroom = new Classroom();

            state.setCurrentRoom(classroom);
            state.setExpectedRoom(classroom);
            state.setCurrentActivity(ActivityType.TEXTING);

            assertTrue(state.isInClass());
        }

        @Test
        @DisplayName("Should not treat lunchrooms as in class even when expected")
        void testLunchroomDoesNotCountAsInClass() {
            Student student = new Student();
            EntityState state = student.getEntityState();
            Lunchroom lunchroom = new Lunchroom();

            state.setCurrentRoom(lunchroom);
            state.setExpectedRoom(lunchroom);
            state.setCurrentActivity(ActivityType.TEXTING);

            assertFalse(state.isInClass());
        }
    }

    @Nested
    @DisplayName("Staff Tests")
    class StaffTests {
        
        @Test
        @DisplayName("Should set and get staff name correctly")
        void testStaffName() {
            Staff staff = new Staff();
            staff.teacherName.setFirstName("Mark");
            staff.teacherName.setLastName("Tester");
            
            assertAll("Staff Name",
                () -> assertEquals("Mark", staff.teacherName.getFirstName()),
                () -> assertEquals("Tester", staff.teacherName.getLastName())
            );
        }

        @Test
        @DisplayName("Should set and get staff stats correctly")
        void testStaffStats() {
            Staff staff = new Staff();
            staff.teacherStatistics.setDetermination(2);
            staff.teacherStatistics.setStrength(3);
            staff.teacherStatistics.setAgility(4);
            staff.teacherStatistics.setCharisma(5);
            staff.teacherStatistics.setIntelligence(6);
            
            assertAll("Staff Stats",
                () -> assertEquals(2, staff.teacherStatistics.getDetermination()),
                () -> assertEquals(3, staff.teacherStatistics.getStrength()),
                () -> assertEquals(4, staff.teacherStatistics.getAgility()),
                () -> assertEquals(5, staff.teacherStatistics.getCharisma()),
                () -> assertEquals(6, staff.teacherStatistics.getIntelligence())
            );
        }

        @Test
        @DisplayName("Should set and get staff entertainment and energy correctly via EntityState")
        void testStaffEntertainmentAndEnergy() {
            Staff staff = new Staff();
            entity.EntityState state = staff.getEntityState();
            
            assertEquals(100.0, state.getEntertainment());
            assertEquals(100.0, state.getEnergy());
            assertFalse(state.isAsleep());
            
            state.setEntertainment(30.0);
            assertEquals(30.0, state.getEntertainment());
            
            state.setEnergy(0.0);
            assertEquals(0.0, state.getEnergy());
        }

        @Test
        @DisplayName("Should set and get staff physical characteristics correctly")
        void testStaffPhysicalCharacteristics() {
            Staff staff = new Staff();
            staff.teacherStatistics.setBuild("Medium");
            staff.teacherStatistics.setHairColor("Brown");
            staff.teacherStatistics.setEyeColor("Blue");
            staff.teacherStatistics.setHeight(68.0);
            
            assertAll("Staff Physical Characteristics",
                () -> assertEquals("Medium", staff.teacherStatistics.getBuild()),
                () -> assertEquals("Brown", staff.teacherStatistics.getHairColor()),
                () -> assertEquals("Blue", staff.teacherStatistics.getEyeColor())
            );
        }
        
        @Test
        @DisplayName("Should set staff type correctly")
        void testStaffType() {
            Staff staff = new Staff();
            staff.teacherStatistics.setStaffType(StaffType.MATH);
            assertEquals(StaffType.MATH, staff.teacherStatistics.getStaffType());
        }
    }

    @Nested
    @DisplayName("School and Director Tests")
    class SchoolTests {
        
        @Test
        @DisplayName("Should create school and director successfully")
        void testSchoolDirectorCreation() {
            StandardSchool school = new StandardSchool();
            Director director = new Director(school, new GameView());
            
            assertNotNull(school);
            assertNotNull(director);
        }
    }

    @Nested
    @DisplayName("Assignment Entity Tests")
    class AssignmentTests {
        
        @Test
        @DisplayName("Should create Homework entity")
        void testHomeworkCreation() {
            Homework homework = new Homework();
            assertNotNull(homework);
        }

        @Test
        @DisplayName("Should create Quiz entity")
        void testQuizCreation() {
            Quiz quiz = new Quiz();
            assertNotNull(quiz);
        }

        @Test
        @DisplayName("Should create Exam entity")
        void testExamCreation() {
            Exam exam = new Exam();
            assertNotNull(exam);
        }
    }
}
