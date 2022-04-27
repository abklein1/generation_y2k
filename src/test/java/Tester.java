import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Tester {

    @Test
    public void StudentTestName(){
        Student student = new Student();
        student.studentName.setFirstName("Mark");
        student.studentName.setLastName("Tester");
        assertEquals("Mark", student.studentName.getFirstName());
        assertEquals("Tester", student.studentName.getLastName());
    }

    @Test
    public void StudentTestStats(){
        Student student = new Student();
        student.studentStatistics.setDetermination(2);
        student.studentStatistics.setStrength(3);
        student.studentStatistics.setAgility(4);
        student.studentStatistics.setCharisma(5);
        student.studentStatistics.setBoredom(1);
        student.studentStatistics.setIntelligence(6);
        assertEquals(2, student.studentStatistics.getDetermination());
        assertEquals(3, student.studentStatistics.getStrength());
        assertEquals(4, student.studentStatistics.getAgility());
        assertEquals(5, student.studentStatistics.getCharisma());
        assertEquals(1, student.studentStatistics.getBoredom());
        assertEquals(6, student.studentStatistics.getIntelligence());
    }

    @Test
    public void StudentSleepCheck(){
        Student student = new Student();
        student.studentStatistics.setSleepState(true);
        assertEquals(true, student.studentStatistics.getSleepState());
    }

    @Test
    public void StudentSetSecondaryChar(){
        Student student = new Student();
        student.studentStatistics.setBuild("Medium");
        student.studentStatistics.setHairColor("Brown");
        student.studentStatistics.setEyeColor("Blue");
        student.studentStatistics.setHeight(44);
        assertEquals("Medium", student.studentStatistics.getBuild());
        assertEquals("Brown", student.studentStatistics.getHairColor());
        assertEquals("Blue", student.studentStatistics.getEyeColor());
        assertEquals(44, student.studentStatistics.getHeight());
    }

    @Test
    public void StudentTestExperience(){
        Student student = new Student();
        student.studentStatistics.setExperience(10);
        assertEquals(10, student.studentStatistics.getExperience());
        student.studentStatistics.setExperience(10);
        assertEquals(20, student.studentStatistics.getExperience());
    }

    @Test
    public void TeacherTestName(){
        Staff staff = new Staff();
        staff.teacherName.setFirstName("Mark");
        staff.teacherName.setLastName("Tester");
        assertEquals("Mark", staff.teacherName.getFirstName());
        assertEquals("Tester", staff.teacherName.getLastName());
    }

    @Test
    public void TeacherTestStats(){
        Staff staff = new Staff();
        staff.teacherStatistics.setDetermination(2);
        staff.teacherStatistics.setStrength(3);
        staff.teacherStatistics.setAgility(4);
        staff.teacherStatistics.setCharisma(5);
        staff.teacherStatistics.setBoredom(1);
        staff.teacherStatistics.setIntelligence(6);
        assertEquals(2, staff.teacherStatistics.getDetermination());
        assertEquals(3, staff.teacherStatistics.getStrength());
        assertEquals(4, staff.teacherStatistics.getAgility());
        assertEquals(5, staff.teacherStatistics.getCharisma());
        assertEquals(1, staff.teacherStatistics.getBoredom());
        assertEquals(6, staff.teacherStatistics.getIntelligence());
    }

    @Test
    public void TeacherSleepCheck(){
        Staff staff = new Staff();
        staff.teacherStatistics.setSleepState(true);
        assertEquals(true, staff.teacherStatistics.getSleepState());
    }

    @Test
    public void TeacherSetSecondaryChar(){
        Staff staff = new Staff();
        staff.teacherStatistics.setBuild("Medium");
        staff.teacherStatistics.setHairColor("Brown");
        staff.teacherStatistics.setEyeColor("Blue");
        staff.teacherStatistics.setHeight(44);
        assertEquals("Medium", staff.teacherStatistics.getBuild());
        assertEquals("Brown", staff.teacherStatistics.getHairColor());
        assertEquals("Blue", staff.teacherStatistics.getEyeColor());
        assertEquals(44, staff.teacherStatistics.getHeight());
    }

    @Test
    public void TestSchoolDirector(){
        StandardSchool stand = new StandardSchool();
        Director director = new Director(stand);
        assertNotNull(stand);
    }

    @Test
    public void TestBossGeneration(){
        Homework homework = new Homework();
        assertNotNull(homework);
        Quiz quiz = new Quiz();
        assertNotNull(quiz);
        Exam exam = new Exam();
        assertNotNull(exam);
    }
}
