package entity;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static constants.SimConstants.*;

public class Time {
    private final Calendar calendar;
    private final Format f;
    private final SimpleDateFormat simpleDateFormat;
    private int dayCounter;
    
    // Period time boundaries (in minutes from midnight)
    private static final int BLOCK_1_START = 8 * 60 + 20;  // 8:20 AM
    private static final int BLOCK_1_END = 9 * 60 + 50;    // 9:50 AM
    private static final int BLOCK_2_START = 10 * 60;       // 10:00 AM
    private static final int BLOCK_2_END = 11 * 60 + 30;   // 11:30 AM
    private static final int BLOCK_3_START = 11 * 60 + 40; // 11:40 AM
    private static final int BLOCK_3_END = 13 * 60 + 20;   // 1:20 PM
    private static final int BLOCK_4_START = 13 * 60 + 30; // 1:30 PM
    private static final int BLOCK_4_END = 15 * 60;        // 3:00 PM

    public Time() {
        this.dayCounter = 1;
        this.calendar = Calendar.getInstance();
        this.calendar.set(Calendar.YEAR, STARTING_YEAR);
        this.calendar.set(Calendar.MONTH, STARTING_MONTH);
        this.calendar.set(Calendar.DATE, STARTING_DATE);
        this.calendar.set(Calendar.HOUR_OF_DAY, STARTING_HOUR);
        this.calendar.set(Calendar.MINUTE, STARTING_MINUTE);
        this.simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm aaa");
        this.f = new SimpleDateFormat("EEEE");
    }

    public String getDayName() {
        return f.format(calendar.getTime());
    }

    public String getFormattedDate() {
        return simpleDateFormat.format(calendar.getTime());
    }

    public void incrementDayCounter() {
        dayCounter++;
        calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    public int getDayCounter() {
        return dayCounter;
    }

    public Date getCurrentDate() {
        return calendar.getTime();
    }

    public void stepForwardMinutes(int minutes) {
        calendar.add(Calendar.MINUTE, minutes);
    }

    public void stepForwardHours(int hours) {
        calendar.add(Calendar.HOUR, hours);
    }
    
    /**
     * Gets the current hour in 24-hour format.
     *
     * @return the current hour (0-23)
     */
    public int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    /**
     * Gets the current minute.
     *
     * @return the current minute (0-59)
     */
    public int getMinute() {
        return calendar.get(Calendar.MINUTE);
    }
    
    /**
     * Gets the current time in minutes from midnight.
     *
     * @return minutes since midnight
     */
    public int getMinutesFromMidnight() {
        return getHour() * 60 + getMinute();
    }
    
    /**
     * Gets the current period/block number (1-4).
     * Returns 0 if not during a regular class period.
     *
     * @return the current block number, or 0 if not in a block
     */
    public int getCurrentPeriod() {
        int timeInMinutes = getMinutesFromMidnight();
        
        if (timeInMinutes >= BLOCK_1_START && timeInMinutes < BLOCK_1_END) {
            return 1;
        } else if (timeInMinutes >= BLOCK_2_START && timeInMinutes < BLOCK_2_END) {
            return 2;
        } else if (timeInMinutes >= BLOCK_3_START && timeInMinutes < BLOCK_3_END) {
            return 3;
        } else if (timeInMinutes >= BLOCK_4_START && timeInMinutes < BLOCK_4_END) {
            return 4;
        }
        
        return 0;
    }
    
    /**
     * Checks if the current time is during a transition period.
     *
     * @return true if in transition between classes
     */
    public boolean isTransitionTime() {
        int timeInMinutes = getMinutesFromMidnight();
        
        // Transition 1: 9:50 - 10:00
        if (timeInMinutes >= BLOCK_1_END && timeInMinutes < BLOCK_2_START) {
            return true;
        }
        // Transition 2: 11:30 - 11:40
        if (timeInMinutes >= BLOCK_2_END && timeInMinutes < BLOCK_3_START) {
            return true;
        }
        // Transition 3: 1:20 - 1:30
        if (timeInMinutes >= BLOCK_3_END && timeInMinutes < BLOCK_4_START) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets how many minutes have passed since the current period started.
     *
     * @return minutes into the period, or 0 if not in a period
     */
    public int getMinutesIntoPeriod() {
        int timeInMinutes = getMinutesFromMidnight();
        int period = getCurrentPeriod();
        
        switch (period) {
            case 1:
                return timeInMinutes - BLOCK_1_START;
            case 2:
                return timeInMinutes - BLOCK_2_START;
            case 3:
                return timeInMinutes - BLOCK_3_START;
            case 4:
                return timeInMinutes - BLOCK_4_START;
            default:
                return 0;
        }
    }
    
    /**
     * Gets how many minutes remain in the current period.
     *
     * @return minutes remaining, or 0 if not in a period
     */
    public int getMinutesRemainingInPeriod() {
        int timeInMinutes = getMinutesFromMidnight();
        int period = getCurrentPeriod();
        
        switch (period) {
            case 1:
                return BLOCK_1_END - timeInMinutes;
            case 2:
                return BLOCK_2_END - timeInMinutes;
            case 3:
                return BLOCK_3_END - timeInMinutes;
            case 4:
                return BLOCK_4_END - timeInMinutes;
            default:
                return 0;
        }
    }
    
    /**
     * Checks if school is currently in session.
     *
     * @return true if during school hours
     */
    public boolean isSchoolInSession() {
        int timeInMinutes = getMinutesFromMidnight();
        return timeInMinutes >= BLOCK_1_START && timeInMinutes < BLOCK_4_END;
    }
    
    /**
     * Checks if it's before school starts.
     *
     * @return true if before first period
     */
    public boolean isBeforeSchool() {
        return getMinutesFromMidnight() < BLOCK_1_START;
    }
    
    /**
     * Checks if school has ended for the day.
     *
     * @return true if after last period
     */
    public boolean isAfterSchool() {
        return getMinutesFromMidnight() >= BLOCK_4_END;
    }
    
    /**
     * Returns the current academic semester based on the calendar month.
     * August through December is "Fall"; January through July is "Spring".
     *
     * @return "Fall" or "Spring"
     */
    public String getCurrentSemester() {
        int month = calendar.get(Calendar.MONTH);
        return month >= Calendar.AUGUST ? "Fall" : "Spring";
    }

    /**
     * Checks if it's a weekend day.
     *
     * @return true if Saturday or Sunday
     */
    public boolean isWeekend() {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }
    
    /**
     * Gets the current calendar month (0-based: 0=January, 11=December).
     *
     * @return the current month
     */
    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    /**
     * Advances the clock to the next school day morning (skipping weekends).
     * Sets the time to STARTING_HOUR:STARTING_MINUTE and increments the day counter.
     */
    public void advanceToNextSchoolDay() {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dayCounter++;

        // Skip weekends
        while (isWeekend()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            dayCounter++;
        }

        calendar.set(Calendar.HOUR_OF_DAY, STARTING_HOUR);
        calendar.set(Calendar.MINUTE, STARTING_MINUTE);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Resets the time to the starting values.
     * Call this when starting a new game/simulation.
     */
    public void reset() {
        this.dayCounter = 1;
        this.calendar.set(Calendar.YEAR, STARTING_YEAR);
        this.calendar.set(Calendar.MONTH, STARTING_MONTH);
        this.calendar.set(Calendar.DATE, STARTING_DATE);
        this.calendar.set(Calendar.HOUR_OF_DAY, STARTING_HOUR);
        this.calendar.set(Calendar.MINUTE, STARTING_MINUTE);
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.MILLISECOND, 0);
    }

}
