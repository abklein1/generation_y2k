package simulation;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.BehaviorTree;
import constants.SimConstants;
import entity.*;
import entity.Rooms.Room;
import utility.SocialLinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Central controller for the game simulation loop.
 * Manages time progression, entity updates, and behavior tree execution.
 */
public class SimulationEngine {

    private Time time;
    private StandardSchool school;
    private HashMap<Integer, Student> students;
    private HashMap<Integer, Staff> staff;
    private BellScheduleManager bellSchedule;
    private boolean isPaused;
    private int ticksPerUpdate; // How many ticks to process per timer fire
    private int minutesPerTick; // In-game minutes per tick (fixed at 1)
    private int currentTick;
    private final List<SimulationListener> listeners;
    private final InteractionManager interactionManager;
    private SocialLinkConnector socialLinkConnector;

    // Simulation speed options (ticks per real-time second)
    public static final int SPEED_SLOW = 1; // 1 tick per second
    public static final int SPEED_NORMAL = 2; // 2 ticks per second
    public static final int SPEED_FAST = 4; // 4 ticks per second
    public static final int SPEED_VERY_FAST = 8; // 8 ticks per second

    // Fixed in-game time progression
    public static final int MINUTES_PER_TICK = 1; // Each tick = 1 in-game minute

    /**
     * Interface for listening to simulation events.
     */
    public interface SimulationListener {
        void onTick(int tickNumber, Time time);

        void onPeriodChange(int oldPeriod, int newPeriod);

        void onTransitionStart();

        void onTransitionEnd();

        void onLunchStart(String lunchPeriod);

        void onLunchEnd(String lunchPeriod);

        void onDayEnd();
    }

    /**
     * Creates a new simulation engine.
     */
    public SimulationEngine() {
        this.isPaused = true;
        this.ticksPerUpdate = SPEED_NORMAL;
        this.minutesPerTick = MINUTES_PER_TICK;
        this.currentTick = 0;
        this.listeners = new ArrayList<>();
        this.bellSchedule = new BellScheduleManager();
        this.interactionManager = new InteractionManager();
    }

    /**
     * Creates a simulation engine with existing game state.
     *
     * @param time     the game time
     * @param school   the school
     * @param students the student population
     * @param staff    the staff population
     */
    public SimulationEngine(Time time, StandardSchool school,
            HashMap<Integer, Student> students,
            HashMap<Integer, Staff> staff) {
        this();
        this.time = time;
        this.school = school;
        this.students = students;
        this.staff = staff;
    }

    /**
     * Initializes the simulation with game state.
     *
     * @param time     the game time
     * @param school   the school
     * @param students the student population
     * @param staff    the staff population
     */
    public void initialize(Time time, StandardSchool school,
            HashMap<Integer, Student> students,
            HashMap<Integer, Staff> staff) {
        this.time = time;
        this.school = school;
        this.students = students;
        this.staff = staff;
        this.bellSchedule = new BellScheduleManager();

        // Initialize entity states if needed
        initializeEntityStates();
    }

    /**
     * Sets the social link connector so that the interaction manager can update
     * relationship scores when social interactions are confirmed during simulation.
     *
     * @param socialLinkConnector the social link connector
     */
    public void setSocialLinkConnector(SocialLinkConnector socialLinkConnector) {
        this.socialLinkConnector = socialLinkConnector;
        this.interactionManager.setSocialLinkConnector(socialLinkConnector);
    }

    /**
     * Initializes EntityState for all entities that don't have one.
     */
    private void initializeEntityStates() {
        if (students != null) {
            for (Student student : students.values()) {
                if (student.getEntityState() == null) {
                    student.setEntityState(new EntityState());
                }
            }
        }

        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                if (staffMember.getEntityState() == null) {
                    staffMember.setEntityState(new EntityState());
                }
            }
        }
    }

    /**
     * Executes the simulation update. Processes multiple ticks based on current
     * speed.
     * Call this method on a fixed timer interval (e.g., once per second).
     */
    public void update() {
        if (isPaused || time == null) {
            return;
        }

        // Process the appropriate number of ticks based on speed
        for (int i = 0; i < ticksPerUpdate; i++) {
            processSingleTick();

            // Stop processing if day ended
            if (bellSchedule.isAfterSchool(time)) {
                break;
            }
        }
    }

    /**
     * Executes exactly one tick of the simulation (for step functionality).
     * This processes one tick regardless of speed setting.
     */
    public void tick() {
        if (time == null) {
            return;
        }
        processSingleTick();
    }

    /**
     * Internal method to process a single simulation tick.
     */
    private void processSingleTick() {
        int previousPeriod = bellSchedule.getCurrentPeriod(time);
        boolean wasTransition = bellSchedule.isTransitionTime(time);

        // 1. Advance time by fixed amount (1 minute per tick)
        time.stepForwardMinutes(minutesPerTick);
        currentTick++;

        // 2. Check for period transitions
        int currentPeriod = bellSchedule.getCurrentPeriod(time);
        boolean isTransition = bellSchedule.isTransitionTime(time);

        // Fire transition events
        if (!wasTransition && isTransition) {
            notifyTransitionStart();
        } else if (wasTransition && !isTransition) {
            notifyTransitionEnd();
        }

        // Fire period change event
        if (previousPeriod != currentPeriod && currentPeriod > 0) {
            notifyPeriodChange(previousPeriod, currentPeriod);
        }

        // 3. Update expected locations based on schedule
        updateExpectedLocations();

        // 4. Process NPC behavior trees (every tick, regardless of speed)
        processStudentBehaviors();
        processStaffBehaviors();

        // 5. Check for end of day
        if (bellSchedule.isAfterSchool(time)) {
            processEndOfDay();
            notifyDayEnd();
        }

        // 6. Notify listeners
        notifyTick();
    }

    /**
     * Updates expected locations for all entities based on current schedule.
     */
    private void updateExpectedLocations() {
        int currentPeriod = bellSchedule.getCurrentPeriod(time);

        if (students != null) {
            for (Student student : students.values()) {
                updateStudentExpectedLocation(student, currentPeriod);
            }
        }

        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                updateStaffExpectedLocation(staffMember, currentPeriod);
            }
        }
    }

    /**
     * Updates a student's expected location based on their schedule.
     *
     * @param student the student
     * @param period  the current period (1-4)
     */
    private void updateStudentExpectedLocation(Student student, int period) {
        EntityState state = student.getEntityState();
        if (state == null) {
            return;
        }

        // Check if it's lunch time for this student
        String lunchPeriod = state.getLunchPeriod();
        if (bellSchedule.isLunchTime(time, lunchPeriod)) {
            // Expected in cafeteria during their lunch period
            if (school != null && school.getLunchrooms() != null &&
                    school.getLunchrooms().length > 0) {
                state.setExpectedRoom(school.getLunchrooms()[0]);
            }
            return;
        }

        // Check if it's transition time
        if (bellSchedule.isTransitionTime(time)) {
            // During transitions, expected location is next class
            // For now, keep the expected room as the next scheduled class
        }

        // Get scheduled room for current period from student schedule
        if (period > 0) {
            Room scheduledRoom = getStudentScheduledRoom(student, period);
            if (scheduledRoom != null) {
                state.setExpectedRoom(scheduledRoom);
            }
        }
    }

    /**
     * Gets the room a student should be in for a given period.
     *
     * @param student the student
     * @param period  the period number (1-4)
     * @return the scheduled room, or null if not found
     */
    private Room getStudentScheduledRoom(Student student, int period) {
        // Access student's schedule
        StudentSchedule schedule = student.studentStatistics.getStudentSchedule();
        if (schedule == null) {
            return null;
        }

        // Period is 1-based, schedule is likely 0-based
        int periodIndex = period - 1;
        if (periodIndex < 0 || periodIndex >= schedule.size()) {
            return null;
        }

        StudentBlock block = schedule.get(periodIndex);
        if (block != null) {
            return block.getRoom();
        }

        return null;
    }

    /**
     * Updates a staff member's expected location.
     *
     * @param staffMember the staff member
     * @param period      the current period
     */
    private void updateStaffExpectedLocation(Staff staffMember, int period) {
        EntityState state = staffMember.getEntityState();
        if (state == null) {
            return;
        }

        // Get assigned room from school
        if (school != null) {
            Room assignedRoom = school.getClassroomByStaff(staffMember);
            if (assignedRoom != null) {
                state.setExpectedRoom(assignedRoom);
            }
        }
    }

    /**
     * Processes behavior trees for all students.
     * Social interactions are collected during tree ticking and resolved afterwards
     * so that when multiple students target the same person, the one with the
     * highest Determination + Charisma wins.
     */
    private void processStudentBehaviors() {
        if (students == null) {
            return;
        }

        // Clear the interaction manager for this tick
        interactionManager.clearTick();

        // Phase 1: Tick all behavior trees (social actions register pending
        // interactions)
        for (Student student : students.values()) {
            BehaviorTree tree = student.getBehaviorTree();
            if (tree != null) {
                BehaviorContext context = student.getBehaviorContext();
                if (context == null) {
                    context = new BehaviorContext(student, time, school);
                    context.setInteractionManager(interactionManager);
                    student.setBehaviorContext(context);
                } else {
                    // Update context with current time and ensure manager is set
                    context.setTime(time);
                    context.setInteractionManager(interactionManager);
                }

                // Tick the behavior tree
                tree.tick(context);
            }

            // Increment activity ticks
            EntityState state = student.getEntityState();
            if (state != null) {
                state.incrementTicksInActivity();

                // Process movement
                if (state.isMoving()) {
                    state.decrementMovementTicks();
                    if (state.getMovementTicksRemaining() <= 0) {
                        state.completeMovement();
                    }
                }
            }
        }

        // Phase 2: Resolve social interaction conflicts
        // The highest DET + CHR student wins when multiple target the same person
        interactionManager.resolveInteractions();
    }

    /**
     * Processes behavior trees for all staff.
     */
    private void processStaffBehaviors() {
        if (staff == null) {
            return;
        }

        for (Staff staffMember : staff.values()) {
            BehaviorTree tree = staffMember.getBehaviorTree();
            if (tree != null) {
                BehaviorContext context = staffMember.getBehaviorContext();
                if (context == null) {
                    context = new BehaviorContext(staffMember, time, school);
                    staffMember.setBehaviorContext(context);
                } else {
                    context.setTime(time);
                }

                tree.tick(context);
            }

            // Increment activity ticks
            EntityState state = staffMember.getEntityState();
            if (state != null) {
                state.incrementTicksInActivity();
            }
        }
    }

    /**
     * Processes end-of-day recovery for all entities.
     * This simulates the period after school where people go home, relax, and
     * sleep.
     * Secondary stats are replenished and allostatic load is reduced.
     */
    private void processEndOfDay() {
        // Process students
        if (students != null) {
            for (Student student : students.values()) {
                processEntitySleepRecovery(student.studentStatistics);

                // Reset entity state for new day
                EntityState state = student.getEntityState();
                if (state != null) {
                    state.resetForNewDay();
                }
            }
        }

        // Process staff
        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                processEntitySleepRecovery(staffMember.teacherStatistics);

                // Reset entity state for new day
                EntityState state = staffMember.getEntityState();
                if (state != null) {
                    state.resetForNewDay();
                }
            }
        }

        // Apply daily relationship decay: all social link scores drift toward neutral.
        // Family and best-friend bonds decay slower, incentivizing active maintenance.
        if (socialLinkConnector != null) {
            socialLinkConnector.applyDailyDecay();
        }
    }

    /**
     * Applies sleep recovery to a single entity's statistics.
     * Checks for allostatic overload before sleep, then replenishes secondary stats
     * and reduces allostatic load.
     *
     * @param stats the entity's statistics
     */
    private void processEntitySleepRecovery(utility.PStatistics stats) {
        if (stats == null) {
            return;
        }

        AllostaticLoad allostaticLoad = stats.getAllostaticLoad();
        if (allostaticLoad != null) {
            // Check overload status before applying recovery (tracks consecutive days)
            allostaticLoad.endOfDayCheck();

            // Apply sleep recovery to allostatic load
            allostaticLoad.applySleepRecovery(SimConstants.ALLOSTATIC_SLEEP_RECOVERY);
        }

        // Replenish all secondary stats to their max caps
        stats.replenishAllSecondaryStats();

        // Reset boredom
        stats.setBoredom(0);

        // Set sleep state
        stats.setSleepState(true);
    }

    // Simulation control methods

    /**
     * Starts or resumes the simulation.
     */
    public void start() {
        isPaused = false;
    }

    /**
     * Pauses the simulation.
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * Toggles pause state.
     */
    public void togglePause() {
        isPaused = !isPaused;
    }

    /**
     * Checks if the simulation is paused.
     *
     * @return true if paused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Sets the simulation speed (ticks per update cycle).
     *
     * @param ticksPerSecond number of ticks to process per update
     */
    public void setSpeed(int ticksPerSecond) {
        this.ticksPerUpdate = Math.max(1, Math.min(16, ticksPerSecond));
    }

    /**
     * Sets the simulation speed by index.
     * 0=Slow (1x), 1=Normal (2x), 2=Fast (4x), 3=Very Fast (8x)
     *
     * @param speedIndex the speed index
     */
    public void setSpeedByIndex(int speedIndex) {
        switch (speedIndex) {
            case 0 -> setSpeed(SPEED_SLOW);
            case 1 -> setSpeed(SPEED_NORMAL);
            case 2 -> setSpeed(SPEED_FAST);
            case 3 -> setSpeed(SPEED_VERY_FAST);
            default -> setSpeed(SPEED_NORMAL);
        }
    }

    /**
     * Gets the current speed (ticks per update).
     *
     * @return ticks per update
     */
    public int getSpeed() {
        return ticksPerUpdate;
    }

    /**
     * Gets the current tick number.
     *
     * @return the tick count
     */
    public int getCurrentTick() {
        return currentTick;
    }

    // Accessors

    public Time getTime() {
        return time;
    }

    public StandardSchool getSchool() {
        return school;
    }

    public HashMap<Integer, Student> getStudents() {
        return students;
    }

    public HashMap<Integer, Staff> getStaff() {
        return staff;
    }

    public BellScheduleManager getBellSchedule() {
        return bellSchedule;
    }

    // Listener management

    /**
     * Adds a simulation listener.
     *
     * @param listener the listener to add
     */
    public void addListener(SimulationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a simulation listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    // Notification methods

    private void notifyTick() {
        for (SimulationListener listener : listeners) {
            listener.onTick(currentTick, time);
        }
    }

    private void notifyPeriodChange(int oldPeriod, int newPeriod) {
        for (SimulationListener listener : listeners) {
            listener.onPeriodChange(oldPeriod, newPeriod);
        }
    }

    private void notifyTransitionStart() {
        for (SimulationListener listener : listeners) {
            listener.onTransitionStart();
        }
    }

    private void notifyTransitionEnd() {
        for (SimulationListener listener : listeners) {
            listener.onTransitionEnd();
        }
    }

    private void notifyLunchStart(String lunchPeriod) {
        for (SimulationListener listener : listeners) {
            listener.onLunchStart(lunchPeriod);
        }
    }

    private void notifyLunchEnd(String lunchPeriod) {
        for (SimulationListener listener : listeners) {
            listener.onLunchEnd(lunchPeriod);
        }
    }

    private void notifyDayEnd() {
        for (SimulationListener listener : listeners) {
            listener.onDayEnd();
        }
    }
}
