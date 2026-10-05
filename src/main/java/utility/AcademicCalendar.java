package utility;

import java.util.Calendar;

import constants.SimConstants;

/**
 * Maps calendar dates of the simulated school year onto {@link entity.Time}
 * day-counter values so lesson plans can be scheduled against real semester,
 * quarter, and finals-week boundaries.
 *
 * <p>The day counter starts at 1 on the first day of school
 * (Aug 23 {@code STARTING_YEAR}) and increments once per calendar day,
 * matching {@code Time.advanceToNextSchoolDay()} which bumps the counter for
 * weekends it skips. All conversions here use the same {@link Calendar} math
 * so computed days stay aligned with the running clock.</p>
 */
public final class AcademicCalendar {

    /** Inclusive day-counter bounds for one semester's instructional window. */
    public static final class SemesterBounds {
        private final int firstDay;
        private final int lastDay;

        SemesterBounds(int firstDay, int lastDay) {
            this.firstDay = firstDay;
            this.lastDay = lastDay;
        }

        public int getFirstDay() {
            return firstDay;
        }

        public int getLastDay() {
            return lastDay;
        }
    }

    private AcademicCalendar() {
    }

    /**
     * Converts a month/day of the simulated school year into a day-counter
     * value. Months Aug-Dec fall in {@code STARTING_YEAR}; Jan-Jul fall in
     * the following year.
     *
     * @param month 0-based month (matches java.util.Calendar)
     * @param day   day of month
     * @return the day counter for that date (1 = first day of school)
     */
    public static int dayCounterFor(int month, int day) {
        Calendar start = buildStartCalendar();
        Calendar target = (Calendar) start.clone();
        int year = month >= SimConstants.ACADEMIC_CALENDAR_FALL_START_MONTH
                ? SimConstants.STARTING_YEAR
                : SimConstants.STARTING_YEAR + 1;
        target.set(year, month, day);

        long diffMillis = target.getTimeInMillis() - start.getTimeInMillis();
        int diffDays = (int) Math.round(diffMillis / (24.0 * 60.0 * 60.0 * 1000.0));
        return 1 + diffDays;
    }

    /**
     * Returns the instructional window for a semester.
     *
     * @param semester "Fall" or "Spring"
     * @return inclusive first/last day-counter bounds
     */
    public static SemesterBounds getSemesterBounds(String semester) {
        if (isFall(semester)) {
            return new SemesterBounds(
                    dayCounterFor(SimConstants.ACADEMIC_CALENDAR_FALL_START_MONTH,
                            SimConstants.ACADEMIC_CALENDAR_FALL_START_DAY),
                    dayCounterFor(SimConstants.ACADEMIC_CALENDAR_FALL_END_MONTH,
                            SimConstants.ACADEMIC_CALENDAR_FALL_END_DAY));
        }
        return new SemesterBounds(
                dayCounterFor(SimConstants.ACADEMIC_CALENDAR_SPRING_START_MONTH,
                        SimConstants.ACADEMIC_CALENDAR_SPRING_START_DAY),
                dayCounterFor(SimConstants.ACADEMIC_CALENDAR_SPRING_END_MONTH,
                        SimConstants.ACADEMIC_CALENDAR_SPRING_END_DAY));
    }

    /**
     * Returns the day counter of the mid-semester quarter boundary
     * (Q1 end for Fall, Q3 end for Spring). Midterms snap to this date.
     *
     * @param semester "Fall" or "Spring"
     * @return the day counter of the quarter end
     */
    public static int getQuarterEnd(String semester) {
        if (isFall(semester)) {
            return dayCounterFor(SimConstants.ACADEMIC_CALENDAR_Q1_END_MONTH,
                    SimConstants.ACADEMIC_CALENDAR_Q1_END_DAY);
        }
        return dayCounterFor(SimConstants.ACADEMIC_CALENDAR_Q3_END_MONTH,
                SimConstants.ACADEMIC_CALENDAR_Q3_END_DAY);
    }

    /**
     * Returns the finals window at the end of a semester. Final exams are
     * placed inside these bounds.
     *
     * @param semester "Fall" or "Spring"
     * @return inclusive first/last day-counter bounds of finals week
     */
    public static SemesterBounds getFinalsWindow(String semester) {
        if (isFall(semester)) {
            return new SemesterBounds(
                    dayCounterFor(SimConstants.ACADEMIC_CALENDAR_FALL_FINALS_START_MONTH,
                            SimConstants.ACADEMIC_CALENDAR_FALL_FINALS_START_DAY),
                    dayCounterFor(SimConstants.ACADEMIC_CALENDAR_FALL_END_MONTH,
                            SimConstants.ACADEMIC_CALENDAR_FALL_END_DAY));
        }
        return new SemesterBounds(
                dayCounterFor(SimConstants.ACADEMIC_CALENDAR_SPRING_FINALS_START_MONTH,
                        SimConstants.ACADEMIC_CALENDAR_SPRING_FINALS_START_DAY),
                dayCounterFor(SimConstants.ACADEMIC_CALENDAR_SPRING_END_MONTH,
                        SimConstants.ACADEMIC_CALENDAR_SPRING_END_DAY));
    }

    /**
     * Checks whether a day counter falls inside either semester's
     * instructional window (weekends excluded).
     *
     * @param dayCounter the day counter to check
     * @return true when school work can be scheduled on this day
     */
    public static boolean isInstructionalDay(int dayCounter) {
        if (isWeekend(dayCounter)) {
            return false;
        }
        SemesterBounds fall = getSemesterBounds("Fall");
        SemesterBounds spring = getSemesterBounds("Spring");
        return (dayCounter >= fall.getFirstDay() && dayCounter <= fall.getLastDay())
                || (dayCounter >= spring.getFirstDay() && dayCounter <= spring.getLastDay());
    }

    /**
     * Snaps a day counter forward to the next weekday (Mon-Fri). Saturday
     * moves to Monday; Sunday moves to Monday.
     *
     * @param dayCounter the candidate day
     * @return the same day if already a weekday, otherwise the following Monday
     */
    public static int snapToWeekday(int dayCounter) {
        int result = dayCounter;
        while (isWeekend(result)) {
            result++;
        }
        return result;
    }

    /**
     * Checks whether a day counter lands on a Saturday or Sunday.
     *
     * @param dayCounter the day counter to check
     * @return true for weekend days
     */
    public static boolean isWeekend(int dayCounter) {
        Calendar calendar = buildStartCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, dayCounter - 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    private static boolean isFall(String semester) {
        return semester == null || semester.equalsIgnoreCase("Fall");
    }

    private static Calendar buildStartCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(SimConstants.STARTING_YEAR, SimConstants.STARTING_MONTH,
                SimConstants.STARTING_DATE);
        return calendar;
    }
}
