package utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Academic calendar")
class AcademicCalendarTest {

    @Test
    @DisplayName("First day of school is day counter 1")
    void firstDayOfSchoolIsDayOne() {
        assertEquals(1, AcademicCalendar.dayCounterFor(Calendar.AUGUST, 23));
    }

    @Test
    @DisplayName("Fall semester starts on day 1 and ends in December")
    void fallBoundsSpanAugustToDecember() {
        AcademicCalendar.SemesterBounds fall = AcademicCalendar.getSemesterBounds("Fall");
        assertEquals(1, fall.getFirstDay());
        assertEquals(AcademicCalendar.dayCounterFor(Calendar.DECEMBER, 17), fall.getLastDay());
        assertTrue(fall.getLastDay() > fall.getFirstDay());
    }

    @Test
    @DisplayName("Spring semester follows fall without overlapping")
    void springFollowsFall() {
        AcademicCalendar.SemesterBounds fall = AcademicCalendar.getSemesterBounds("Fall");
        AcademicCalendar.SemesterBounds spring = AcademicCalendar.getSemesterBounds("Spring");
        assertTrue(spring.getFirstDay() > fall.getLastDay());
        assertTrue(spring.getLastDay() > spring.getFirstDay());
    }

    @Test
    @DisplayName("Quarter boundaries land inside their semesters")
    void quarterEndsInsideSemesters() {
        AcademicCalendar.SemesterBounds fall = AcademicCalendar.getSemesterBounds("Fall");
        int q1End = AcademicCalendar.getQuarterEnd("Fall");
        assertTrue(q1End > fall.getFirstDay() && q1End < fall.getLastDay());

        AcademicCalendar.SemesterBounds spring = AcademicCalendar.getSemesterBounds("Spring");
        int q3End = AcademicCalendar.getQuarterEnd("Spring");
        assertTrue(q3End > spring.getFirstDay() && q3End < spring.getLastDay());
    }

    @Test
    @DisplayName("Finals windows sit at the end of each semester")
    void finalsWindowsAtSemesterEnd() {
        for (String semester : new String[] { "Fall", "Spring" }) {
            AcademicCalendar.SemesterBounds bounds = AcademicCalendar.getSemesterBounds(semester);
            AcademicCalendar.SemesterBounds finals = AcademicCalendar.getFinalsWindow(semester);
            assertTrue(finals.getFirstDay() > bounds.getFirstDay());
            assertEquals(bounds.getLastDay(), finals.getLastDay());
        }
    }

    @Test
    @DisplayName("Snapping moves weekend days to the following Monday")
    void snapToWeekdaySkipsWeekends() {
        // Aug 28 2004 was a Saturday (day counter 6); Monday is day 8.
        int saturday = AcademicCalendar.dayCounterFor(Calendar.AUGUST, 28);
        assertTrue(AcademicCalendar.isWeekend(saturday));
        int snapped = AcademicCalendar.snapToWeekday(saturday);
        assertFalse(AcademicCalendar.isWeekend(snapped));
        assertEquals(saturday + 2, snapped);

        // Weekdays stay put.
        assertEquals(1, AcademicCalendar.snapToWeekday(1));
    }

    @Test
    @DisplayName("Instructional days exclude weekends and the winter gap")
    void instructionalDayChecks() {
        assertTrue(AcademicCalendar.isInstructionalDay(1));
        int saturday = AcademicCalendar.dayCounterFor(Calendar.AUGUST, 28);
        assertFalse(AcademicCalendar.isInstructionalDay(saturday));
        // Dec 20 2004 (Monday) is after the fall semester ends.
        int winterGap = AcademicCalendar.dayCounterFor(Calendar.DECEMBER, 20);
        assertFalse(AcademicCalendar.isInstructionalDay(winterGap));
    }
}
