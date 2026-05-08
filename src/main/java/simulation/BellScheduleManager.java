package simulation;

import entity.Time;
import utility.GameLogger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Manages the bell schedule for the school day.
 * Parses bell_schedule.json and determines current periods, transitions, and
 * lunch.
 */
public class BellScheduleManager {

    private static final String SCHEDULE_PATH = "src/main/java/Resources/School/bell_schedule.json";

    private final List<ScheduleBlock> blocks;
    private final List<ScheduleBlock> transitions;
    private final ScheduleBlock lunchA;
    private final ScheduleBlock lunchB;
    private boolean scheduleLoaded;

    /**
     * Represents a time block in the schedule.
     */
    public static class ScheduleBlock {
        private final String name;
        private final int startHour;
        private final int startMinute;
        private final int endHour;
        private final int endMinute;
        private final int duration;
        private final boolean isLunch;
        private final boolean isTransition;
        private final int blockNumber; // 1-4 for regular blocks, 0 for transitions/lunch

        public ScheduleBlock(String name, String startTime, String endTime,
                int duration, boolean isLunch, boolean isTransition, int blockNumber) {
            this.name = name;
            this.duration = duration;
            this.isLunch = isLunch;
            this.isTransition = isTransition;
            this.blockNumber = blockNumber;

            // Parse start time
            int[] start = parseTime(startTime);
            this.startHour = start[0];
            this.startMinute = start[1];

            // Parse end time
            int[] end = parseTime(endTime);
            this.endHour = end[0];
            this.endMinute = end[1];
        }

        private int[] parseTime(String timeStr) {
            // Format: "HH:MM AM/PM"
            try {
                String[] parts = timeStr.split(" ");
                String[] timeParts = parts[0].split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                boolean isPM = parts[1].equalsIgnoreCase("PM");

                // Convert to 24-hour format
                if (isPM && hour != 12) {
                    hour += 12;
                } else if (!isPM && hour == 12) {
                    hour = 0;
                }

                return new int[] { hour, minute };
            } catch (Exception e) {
                return new int[] { 8, 0 }; // Default to 8:00 AM
            }
        }

        public String getName() {
            return name;
        }

        public int getStartHour() {
            return startHour;
        }

        public int getStartMinute() {
            return startMinute;
        }

        public int getEndHour() {
            return endHour;
        }

        public int getEndMinute() {
            return endMinute;
        }

        public int getDuration() {
            return duration;
        }

        public boolean isLunch() {
            return isLunch;
        }

        public boolean isTransition() {
            return isTransition;
        }

        public int getBlockNumber() {
            return blockNumber;
        }

        /**
         * Checks if a given time falls within this block.
         *
         * @param hour   the hour (24-hour format)
         * @param minute the minute
         * @return true if the time is within this block
         */
        public boolean containsTime(int hour, int minute) {
            int timeInMinutes = hour * 60 + minute;
            int startInMinutes = startHour * 60 + startMinute;
            int endInMinutes = endHour * 60 + endMinute;

            return timeInMinutes >= startInMinutes && timeInMinutes < endInMinutes;
        }

        /**
         * Gets the minutes remaining in this block.
         *
         * @param hour   current hour
         * @param minute current minute
         * @return minutes remaining, or 0 if past end
         */
        public int getMinutesRemaining(int hour, int minute) {
            int timeInMinutes = hour * 60 + minute;
            int endInMinutes = endHour * 60 + endMinute;

            return Math.max(0, endInMinutes - timeInMinutes);
        }

        /**
         * Gets the minutes elapsed in this block.
         *
         * @param hour   current hour
         * @param minute current minute
         * @return minutes elapsed since start
         */
        public int getMinutesElapsed(int hour, int minute) {
            int timeInMinutes = hour * 60 + minute;
            int startInMinutes = startHour * 60 + startMinute;

            return Math.max(0, timeInMinutes - startInMinutes);
        }

        @Override
        public String toString() {
            return String.format("%s (%02d:%02d - %02d:%02d)",
                    name, startHour, startMinute, endHour, endMinute);
        }
    }

    /**
     * Creates a new BellScheduleManager and loads the schedule.
     */
    public BellScheduleManager() {
        this.blocks = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.scheduleLoaded = false;

        ScheduleBlock tempLunchA = null;
        ScheduleBlock tempLunchB = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject schedule = (JSONObject) parser.parse(new FileReader(SCHEDULE_PATH, StandardCharsets.UTF_8));

            // Parse regular blocks
            for (int i = 1; i <= 4; i++) {
                JSONObject block = (JSONObject) schedule.get("Block " + i);
                if (block != null) {
                    boolean hasLunch = block.get("Lunch") != null && (Boolean) block.get("Lunch");
                    ScheduleBlock sb = new ScheduleBlock(
                            "Block " + i,
                            (String) block.get("Start Time"),
                            (String) block.get("End Time"),
                            ((Long) block.get("Duration")).intValue(),
                            hasLunch,
                            false,
                            i);
                    blocks.add(sb);
                }
            }

            // Parse transitions
            for (int i = 1; i <= 3; i++) {
                JSONObject trans = (JSONObject) schedule.get("Transition " + i);
                if (trans != null) {
                    ScheduleBlock sb = new ScheduleBlock(
                            "Transition " + i,
                            (String) trans.get("Start Time"),
                            (String) trans.get("End Time"),
                            10, // Default transition duration
                            false,
                            true,
                            0);
                    transitions.add(sb);
                }
            }

            // Parse lunch periods
            JSONObject lunchAObj = (JSONObject) schedule.get("Lunch A");
            if (lunchAObj != null) {
                tempLunchA = new ScheduleBlock(
                        "Lunch A",
                        (String) lunchAObj.get("Start Time"),
                        (String) lunchAObj.get("End Time"),
                        ((Long) lunchAObj.get("Duration")).intValue(),
                        true,
                        false,
                        0);
            }

            JSONObject lunchBObj = (JSONObject) schedule.get("Lunch B");
            if (lunchBObj != null) {
                tempLunchB = new ScheduleBlock(
                        "Lunch B",
                        (String) lunchBObj.get("Start Time"),
                        (String) lunchBObj.get("End Time"),
                        ((Long) lunchBObj.get("Duration")).intValue(),
                        true,
                        false,
                        0);
            }

            scheduleLoaded = true;

        } catch (IOException | ParseException e) {
            GameLogger.logDebug("Failed to load bell schedule: " + e.getMessage());
            // Create default schedule
            createDefaultSchedule();
            tempLunchA = new ScheduleBlock("Lunch A", "11:40 AM", "12:20 PM", 40, true, false, 0);
            tempLunchB = new ScheduleBlock("Lunch B", "12:40 PM", "01:20 PM", 40, true, false, 0);
        }

        this.lunchA = tempLunchA;
        this.lunchB = tempLunchB;
    }

    /**
     * Creates a default schedule if loading fails.
     */
    private void createDefaultSchedule() {
        blocks.add(new ScheduleBlock("Block 1", "08:20 AM", "09:50 AM", 90, false, false, 1));
        blocks.add(new ScheduleBlock("Block 2", "10:00 AM", "11:30 AM", 90, false, false, 2));
        blocks.add(new ScheduleBlock("Block 3", "11:40 AM", "01:20 PM", 100, true, false, 3));
        blocks.add(new ScheduleBlock("Block 4", "01:30 PM", "03:00 PM", 90, false, false, 4));

        transitions.add(new ScheduleBlock("Transition 1", "09:50 AM", "10:00 AM", 10, false, true, 0));
        transitions.add(new ScheduleBlock("Transition 2", "11:30 AM", "11:40 AM", 10, false, true, 0));
        transitions.add(new ScheduleBlock("Transition 3", "01:20 PM", "01:30 PM", 10, false, true, 0));
    }

    /**
     * Gets the current period/block number (1-4).
     *
     * @param time the current game time
     * @return the current block number, or 0 if not in a block
     */
    public int getCurrentPeriod(Time time) {
        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        for (ScheduleBlock block : blocks) {
            if (block.containsTime(hour, minute)) {
                return block.getBlockNumber();
            }
        }

        return 0; // Not in any block
    }

    /**
     * Gets the current schedule block.
     *
     * @param time the current game time
     * @return the current block, or null if outside schedule
     */
    public ScheduleBlock getCurrentBlock(Time time) {
        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Check regular blocks
        for (ScheduleBlock block : blocks) {
            if (block.containsTime(hour, minute)) {
                return block;
            }
        }

        // Check transitions
        for (ScheduleBlock trans : transitions) {
            if (trans.containsTime(hour, minute)) {
                return trans;
            }
        }

        return null;
    }

    /**
     * Checks if it's currently a transition time.
     *
     * @param time the current game time
     * @return true if in transition
     */
    public boolean isTransitionTime(Time time) {
        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        for (ScheduleBlock trans : transitions) {
            if (trans.containsTime(hour, minute)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if it's currently lunch time for a given lunch period.
     *
     * @param time        the current game time
     * @param lunchPeriod "A" or "B"
     * @return true if it's lunch time for that period
     */
    public boolean isLunchTime(Time time, String lunchPeriod) {
        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        if ("A".equalsIgnoreCase(lunchPeriod) && lunchA != null) {
            return lunchA.containsTime(hour, minute);
        } else if ("B".equalsIgnoreCase(lunchPeriod) && lunchB != null) {
            return lunchB.containsTime(hour, minute);
        }

        return false;
    }

    /**
     * Gets the minutes remaining in the current period.
     *
     * @param time the current game time
     * @return minutes remaining, or 0 if not in a period
     */
    public int getMinutesIntoPeriod(Time time) {
        ScheduleBlock current = getCurrentBlock(time);
        if (current == null) {
            return 0;
        }

        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        return current.getMinutesElapsed(hour, minute);
    }

    /**
     * Gets the minutes remaining in the current period.
     *
     * @param time the current game time
     * @return minutes remaining, or 0 if not in a period
     */
    public int getMinutesRemaining(Time time) {
        ScheduleBlock current = getCurrentBlock(time);
        if (current == null) {
            return 0;
        }

        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        return current.getMinutesRemaining(hour, minute);
    }

    /**
     * Checks if school is currently in session.
     *
     * @param time the current game time
     * @return true if during school hours
     */
    public boolean isSchoolInSession(Time time) {
        if (blocks.isEmpty()) {
            return false;
        }

        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int timeInMinutes = hour * 60 + minute;

        // School starts with first block, ends with last block
        ScheduleBlock first = blocks.get(0);
        ScheduleBlock last = blocks.get(blocks.size() - 1);

        int startOfDay = first.getStartHour() * 60 + first.getStartMinute();
        int endOfDay = last.getEndHour() * 60 + last.getEndMinute();

        return timeInMinutes >= startOfDay && timeInMinutes < endOfDay;
    }

    /**
     * Checks if it's before school starts.
     *
     * @param time the current game time
     * @return true if before school
     */
    public boolean isBeforeSchool(Time time) {
        if (blocks.isEmpty()) {
            return true;
        }

        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int timeInMinutes = hour * 60 + minute;

        ScheduleBlock first = blocks.get(0);
        int startOfDay = first.getStartHour() * 60 + first.getStartMinute();

        return timeInMinutes < startOfDay;
    }

    /**
     * Checks if school has ended for the day.
     *
     * @param time the current game time
     * @return true if after school
     */
    public boolean isAfterSchool(Time time) {
        if (blocks.isEmpty()) {
            return false;
        }

        Calendar cal = getCalendarFromTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int timeInMinutes = hour * 60 + minute;

        ScheduleBlock last = blocks.get(blocks.size() - 1);
        int endOfDay = last.getEndHour() * 60 + last.getEndMinute();

        return timeInMinutes >= endOfDay;
    }

    /**
     * Gets all schedule blocks.
     *
     * @return list of blocks
     */
    public List<ScheduleBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    /**
     * Gets all transitions.
     *
     * @return list of transitions
     */
    public List<ScheduleBlock> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    /**
     * Gets the Lunch A block.
     *
     * @return Lunch A schedule block
     */
    public ScheduleBlock getLunchA() {
        return lunchA;
    }

    /**
     * Gets the Lunch B block.
     *
     * @return Lunch B schedule block
     */
    public ScheduleBlock getLunchB() {
        return lunchB;
    }

    /**
     * Checks if the schedule was loaded successfully.
     *
     * @return true if loaded
     */
    public boolean isScheduleLoaded() {
        return scheduleLoaded;
    }

    /** Minutes-from-midnight boundary: after-school ends, evening begins. */
    private static final int AFTER_SCHOOL_END_MINUTES = 18 * 60; // 6:00 PM

    /** Minutes-from-midnight boundary: evening ends. */
    private static final int EVENING_END_MINUTES = 22 * 60; // 10:00 PM

    /**
     * Returns the current {@link DayPhase} based on the time of day and
     * day of week.
     *
     * @param time the current game time
     * @return the active day phase
     */
    public DayPhase getDayPhase(Time time) {
        if (time.isWeekend()) {
            return DayPhase.WEEKEND;
        }

        int minutesFromMidnight = time.getMinutesFromMidnight();

        if (isBeforeSchool(time)) {
            return DayPhase.PRE_SCHOOL;
        }
        if (!isAfterSchool(time)) {
            return DayPhase.SCHOOL_DAY;
        }
        if (minutesFromMidnight < AFTER_SCHOOL_END_MINUTES) {
            return DayPhase.AFTER_SCHOOL;
        }
        if (minutesFromMidnight < EVENING_END_MINUTES) {
            return DayPhase.EVENING;
        }
        // Past 10 PM — effectively end of day
        return DayPhase.EVENING;
    }

    /**
     * Helper to extract Calendar from Time object.
     *
     * @param time the game time
     * @return a Calendar with the current date/time
     */
    private Calendar getCalendarFromTime(Time time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time.getCurrentDate());
        return cal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BellScheduleManager{\n");
        sb.append("  Blocks:\n");
        for (ScheduleBlock block : blocks) {
            sb.append("    ").append(block).append("\n");
        }
        sb.append("  Transitions:\n");
        for (ScheduleBlock trans : transitions) {
            sb.append("    ").append(trans).append("\n");
        }
        sb.append("  Lunch A: ").append(lunchA).append("\n");
        sb.append("  Lunch B: ").append(lunchB).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
