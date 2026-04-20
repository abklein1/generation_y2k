package simulation;

import behavior.BehaviorContext;
import behavior.BehaviorTree;
import constants.SimConstants;
import entity.*;
import entity.Rooms.OffCampus;
import entity.Rooms.Room;
import utility.SocialLinkConnector;
import utility.TraversalStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utility.GameRandom;

/**
 * Central controller for the game simulation loop.
 * Manages time progression, entity updates, and behavior tree execution.
 */
public class SimulationEngine {

    private Time time;
    private StandardSchool school;
    private Town town;
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
    private RoomOccupancyManager roomOccupancyManager;
    private TraversalStorage traversalStorage;
    private int currentTransitionIndex;
    private int lastProcessedMonth = -1;
    private LunchDestinationSelector lunchDestinationSelector;
    private boolean wasLunchA = false;
    private boolean wasLunchB = false;

    // Simulation speed options (ticks per real-time second)
    public static final int SPEED_SLOW = 1; // 1 tick per second
    public static final int SPEED_NORMAL = 2; // 2 ticks per second
    public static final int SPEED_FAST = 4; // 4 ticks per second
    public static final int SPEED_VERY_FAST = 8; // 8 ticks per second

    // Fixed in-game time progression
    public static final int MINUTES_PER_TICK = 1; // Each tick = 1 in-game minute

    // How many ticks (minutes) an action lasts before the student re-evaluates
    private static final int ACTION_DURATION_TICKS = 5;

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
        this.lastProcessedMonth = time.getMonth();
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
        this.lastProcessedMonth = time.getMonth();

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
     * Sets the town reference so behavior contexts can access phone data.
     *
     * @param town the town
     */
    public void setTown(Town town) {
        this.town = town;
    }

    public void setRoomOccupancyManager(RoomOccupancyManager manager) {
        this.roomOccupancyManager = manager;
    }

    public void setTraversalStorage(TraversalStorage storage) {
        this.traversalStorage = storage;
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

        // 1.5. Update day phase on all entities
        DayPhase currentDayPhase = bellSchedule.getDayPhase(time);
        updateAllDayPhases(currentDayPhase);

        // 1.75. Process morning transit (commutes from neighborhoods)
        if (currentDayPhase == DayPhase.PRE_SCHOOL) {
            processMorningTransit();
        }

        // 2. Check for period transitions
        int currentPeriod = bellSchedule.getCurrentPeriod(time);
        boolean isTransition = bellSchedule.isTransitionTime(time);

        // Fire transition events
        if (!wasTransition && isTransition) {
            notifyTransitionStart();
            initiateTransitionMovement();
        } else if (wasTransition && !isTransition) {
            finalizeTransitionArrival(currentPeriod);
            notifyTransitionEnd();
        }

        // Fire period change event
        if (previousPeriod != currentPeriod && currentPeriod > 0) {
            notifyPeriodChange(previousPeriod, currentPeriod);
        }

        // 3. Update expected locations based on schedule
        updateExpectedLocations();

        // 3.25. Handle mid-block lunch transitions (enter/exit lunch)
        processLunchTransitions();

        // 3.5. Advance students along their transition paths
        if (isTransition) {
            advanceStudentMovement();
        }

        // 3.75. Tick physiological needs for all entities
        tickAllNeeds();

        // 4. Process NPC behavior trees
        // During pre-school, only run trees for students who have arrived
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

    // ==================== Morning Transit ====================

    /**
     * Sets the current {@link DayPhase} on every entity's state.
     */
    private void updateAllDayPhases(DayPhase phase) {
        if (students != null) {
            for (Student student : students.values()) {
                EntityState state = student.getEntityState();
                if (state != null) {
                    state.setCurrentPhase(phase);
                }
            }
        }
        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                EntityState state = staffMember.getEntityState();
                if (state != null) {
                    state.setCurrentPhase(phase);
                }
            }
        }
    }

    /**
     * Processes morning commutes for all students.
     * <ul>
     *   <li>Students whose departure time has arrived begin their commute.</li>
     *   <li>Students already in transit tick down their remaining travel time.</li>
     *   <li>Students whose travel time reaches 0 are placed on campus.</li>
     * </ul>
     * Commuting students can socialize with their transit group via the
     * normal behavior tree (the tree runs for in-transit students and they
     * can target transit group members).
     */
    private void processMorningTransit() {
        if (students == null) {
            return;
        }

        int currentMinutes = time.getMinutesFromMidnight();

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null || state.hasArrivedAtSchool()) {
                continue;
            }

            if (state.isInTransit()) {
                // Already commuting — tick down
                state.decrementTransitTicks();
                if (state.getTransitTicksRemaining() <= 0) {
                    completeArrival(student, state);
                }
            } else if (currentMinutes >= state.getDepartureTimeMinutes()) {
                // Time to leave home
                beginCommute(student, state);
            }
        }
    }

    /**
     * Starts a student's commute from their neighborhood.
     */
    private void beginCommute(Student student, EntityState state) {
        state.setInTransit(true);
        state.setTransitTicksRemaining(state.getTravelTimeMinutes());
        state.setCurrentActivity(state.getCommutingActivity());
        state.setCurrentRoom(null);
        state.setExpectedRoom(null);
    }

    /**
     * Places a student on campus after their commute completes.
     */
    private void completeArrival(Student student, EntityState state) {
        state.setInTransit(false);
        state.setArrivedAtSchool(true);
        state.setCurrentActivity(ActivityType.IDLE);

        // Place in first-period room or a common area
        Room room = getStudentScheduledRoom(student, 1);
        if (room == null) {
            room = getFreePeriodRoom();
        }
        if (room != null) {
            state.setCurrentRoom(room);
            state.setExpectedRoom(room);
            if (roomOccupancyManager != null) {
                roomOccupancyManager.enterRoom(student, room);
            }
        }
    }

    // ==================== Expected Locations ====================

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
     * During lunch, the expected room is the lunch destination (set by
     * {@link #processLunchTransitions}). Outside lunch the normal
     * schedule/free-period logic applies.
     *
     * @param student the student
     * @param period  the current period (1-4)
     */
    private void updateStudentExpectedLocation(Student student, int period) {
        EntityState state = student.getEntityState();
        if (state == null) {
            return;
        }

        // Skip students who are still commuting or haven't left home yet
        if (!state.hasArrivedAtSchool() && bellSchedule.isBeforeSchool(time)) {
            return;
        }

        // While at lunch the expected room is managed by processLunchTransitions
        if (state.isAtLunch()) {
            return;
        }

        // Check if it's transition time
        if (bellSchedule.isTransitionTime(time)) {
            // During transitions, expected location is next class
        }

        // Get scheduled room for current period from student schedule
        if (period > 0) {
            Room scheduledRoom = getStudentScheduledRoom(student, period);
            if (scheduledRoom != null) {
                state.setExpectedRoom(scheduledRoom);
            } else {
                Room freeRoom = getFreePeriodRoom();
                if (freeRoom != null) {
                    state.setExpectedRoom(freeRoom);
                }
            }
        }
    }

    /**
     * Handles mid-block lunch transitions: dispatching students to their
     * lunch destination when their lunch window begins, and returning them
     * to their classroom when it ends.  Also fires the lunch start/end
     * notifications for UI logging.
     */
    private void processLunchTransitions() {
        if (students == null || school == null) {
            return;
        }

        boolean isLunchA = bellSchedule.isLunchTime(time, "A");
        boolean isLunchB = bellSchedule.isLunchTime(time, "B");

        // Fire lunch start/end notifications on edges
        if (isLunchA && !wasLunchA) {
            notifyLunchStart("A");
        } else if (!isLunchA && wasLunchA) {
            notifyLunchEnd("A");
        }
        if (isLunchB && !wasLunchB) {
            notifyLunchStart("B");
        } else if (!isLunchB && wasLunchB) {
            notifyLunchEnd("B");
        }

        // Lazily create the selector on first use
        if (lunchDestinationSelector == null) {
            lunchDestinationSelector = new LunchDestinationSelector(
                    school, socialLinkConnector);
        }

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }
            String lunchPeriod = state.getLunchPeriod();
            boolean isMyLunch = bellSchedule.isLunchTime(time, lunchPeriod);

            if (isMyLunch && !state.isAtLunch()) {
                sendStudentToLunch(student, state);
            } else if (!isMyLunch && state.isAtLunch()) {
                returnStudentFromLunch(student, state);
            }
        }

        wasLunchA = isLunchA;
        wasLunchB = isLunchB;
    }

    /**
     * Transfers a student from their classroom to a lunch destination.
     */
    private void sendStudentToLunch(Student student, EntityState state) {
        state.setPreLunchRoom(state.getCurrentRoom());

        Room destination = lunchDestinationSelector.selectDestination(student);
        if (destination == null) {
            return;
        }

        state.setExpectedRoom(destination);

        boolean offCampus = destination instanceof OffCampus;
        if (offCampus) {
            // Remove from the physical grid; they leave campus
            if (roomOccupancyManager != null) {
                roomOccupancyManager.removeStudentFromCurrentRoom(student);
            }
            state.setCurrentRoom(destination);
            state.setCurrentActivity(ActivityType.EATING_LUNCH_OFF_CAMPUS);
        } else {
            if (roomOccupancyManager != null) {
                roomOccupancyManager.transferStudent(
                        student, state.getCurrentRoom(), destination);
            } else {
                state.setCurrentRoom(destination);
            }
            state.setCurrentActivity(ActivityType.EATING_LUNCH);
        }

        state.setAtLunch(true);
        state.resetDecisionCooldown(0);
        state.onAte();
    }

    /**
     * Returns a student from their lunch destination back to their
     * pre-lunch classroom.
     */
    private void returnStudentFromLunch(Student student, EntityState state) {
        Room returnRoom = state.getPreLunchRoom();
        if (returnRoom == null) {
            // Fallback: use their scheduled room for the current period
            int period = bellSchedule.getCurrentPeriod(time);
            returnRoom = getStudentScheduledRoom(student, period);
            if (returnRoom == null) {
                returnRoom = getFreePeriodRoom();
            }
        }

        if (returnRoom != null) {
            boolean wasOffCampus = state.getCurrentRoom() instanceof OffCampus;
            if (wasOffCampus) {
                if (roomOccupancyManager != null) {
                    roomOccupancyManager.enterRoom(student, returnRoom);
                }
                state.setCurrentRoom(returnRoom);
            } else {
                if (roomOccupancyManager != null) {
                    roomOccupancyManager.transferStudent(
                            student, state.getCurrentRoom(), returnRoom);
                } else {
                    state.setCurrentRoom(returnRoom);
                }
            }
            state.setExpectedRoom(returnRoom);
        }

        state.setAtLunch(false);
        state.setPreLunchRoom(null);
        state.setCurrentActivity(ActivityType.IDLE);
        state.resetDecisionCooldown(0);
    }

    /**
     * Gets the room a student should be in for a given period in the
     * current semester.
     *
     * @param student the student
     * @param period  the period number (1-4)
     * @return the scheduled room, or null if not found
     */
    private Room getStudentScheduledRoom(Student student, int period) {
        StudentSchedule schedule = student.studentStatistics.getStudentSchedule();
        if (schedule == null) {
            return null;
        }

        String semester = time.getCurrentSemester();
        StudentBlock block = schedule.getByBlockNumber(period, semester);
        if (block != null) {
            return block.getRoom();
        }

        return null;
    }

    /**
     * Returns a common-area room for a student who has no class scheduled
     * during the current period.  Picks randomly from libraries, courtyards,
     * lunchrooms, and hallways (in that preference order, falling through
     * when a category is empty).
     *
     * @return a common-area Room, or null if the school has none
     */
    private Room getFreePeriodRoom() {
        if (school == null) {
            return null;
        }

        List<Room> candidates = new ArrayList<>();
        addIfPresent(candidates, school.getLibraries());
        addIfPresent(candidates, school.getCourtyards());
        addIfPresent(candidates, school.getLunchrooms());
        addIfPresent(candidates, school.getHallways());

        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(GameRandom.nextInt(candidates.size()));
    }

    private static void addIfPresent(List<Room> list, Room[] rooms) {
        if (rooms != null) {
            java.util.Collections.addAll(list, rooms);
        }
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
     * Ticks physiological needs (hunger, thirst, bladder) for every student
     * and staff member. When bladder drops below the critical threshold the
     * entity's {@code needsBathroom} flag is set automatically.
     */
    private void tickAllNeeds() {
        if (students != null) {
            for (Student student : students.values()) {
                EntityState state = student.getEntityState();
                if (state != null) {
                    if (isEatingLunch(state)) {
                        state.setHunger(state.getHunger()
                                + SimConstants.NEED_HUNGER_REFILL_PER_TICK);
                        state.setThirst(state.getThirst()
                                + SimConstants.NEED_THIRST_REFILL_PER_TICK);
                    }
                    state.tickNeeds(
                            isEatingLunch(state) ? 0 : SimConstants.NEED_HUNGER_DECAY_PER_TICK,
                            isEatingLunch(state) ? 0 : SimConstants.NEED_THIRST_DECAY_PER_TICK,
                            SimConstants.NEED_BLADDER_DECAY_PER_TICK,
                            SimConstants.NEED_BLADDER_POST_MEAL_DECAY_PER_TICK,
                            SimConstants.NEED_ENTERTAINMENT_DECAY_PER_TICK,
                            SimConstants.NEED_ENERGY_DECAY_PER_TICK,
                            SimConstants.NEED_ENERGY_DECAY_WHEN_BORED);
                    if (state.getBladder() < SimConstants.NEED_CRITICAL_THRESHOLD) {
                        state.setNeedsBathroom(true);
                    }
                    applyNeedStress(state, student.studentStatistics.getAllostaticLoad());
                }
            }
        }
        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                EntityState state = staffMember.getEntityState();
                if (state != null) {
                    state.tickNeeds(
                            SimConstants.NEED_HUNGER_DECAY_PER_TICK,
                            SimConstants.NEED_THIRST_DECAY_PER_TICK,
                            SimConstants.NEED_BLADDER_DECAY_PER_TICK,
                            SimConstants.NEED_BLADDER_POST_MEAL_DECAY_PER_TICK,
                            SimConstants.NEED_ENTERTAINMENT_DECAY_PER_TICK,
                            SimConstants.NEED_ENERGY_DECAY_PER_TICK,
                            SimConstants.NEED_ENERGY_DECAY_WHEN_BORED);
                    if (state.getBladder() < SimConstants.NEED_CRITICAL_THRESHOLD) {
                        state.setNeedsBathroom(true);
                    }
                    applyNeedStress(state, staffMember.teacherStatistics.getAllostaticLoad());
                }
            }
        }
    }

    private static boolean isEatingLunch(EntityState state) {
        ActivityType activity = state.getCurrentActivity();
        return activity == ActivityType.EATING_LUNCH
                || activity == ActivityType.EATING_LUNCH_OFF_CAMPUS;
    }

    /**
     * Increases allostatic load for each physiological need that has dropped
     * below the critical threshold. The stress is applied every tick the
     * need remains unmet, so prolonged deprivation compounds over time.
     */
    private void applyNeedStress(EntityState state, AllostaticLoad allostaticLoad) {
        if (allostaticLoad == null) {
            return;
        }
        if (state.getHunger() < SimConstants.NEED_CRITICAL_THRESHOLD) {
            allostaticLoad.increaseLoad(SimConstants.NEED_HUNGER_ALLOSTATIC_STRESS);
        }
        if (state.getThirst() < SimConstants.NEED_CRITICAL_THRESHOLD) {
            allostaticLoad.increaseLoad(SimConstants.NEED_THIRST_ALLOSTATIC_STRESS);
        }
        if (state.getBladder() < SimConstants.NEED_CRITICAL_THRESHOLD) {
            allostaticLoad.increaseLoad(SimConstants.NEED_BLADDER_ALLOSTATIC_STRESS);
        }
        if (state.getEntertainment() < SimConstants.NEED_CRITICAL_THRESHOLD) {
            allostaticLoad.increaseLoad(SimConstants.NEED_ENTERTAINMENT_ALLOSTATIC_STRESS);
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
        // interactions).
        // Actions last ~5 minutes: the tree is only re-evaluated when the
        // student's cooldown counter has elapsed, or when the student is
        // idle/transitioning (needs a new decision immediately).
        for (Student student : students.values()) {
            EntityState preCheckState = student.getEntityState();

            // Skip students who haven't left home yet
            if (preCheckState != null && !preCheckState.hasArrivedAtSchool()
                    && !preCheckState.isInTransit()) {
                continue;
            }

            BehaviorTree tree = student.getBehaviorTree();
            if (tree != null) {
                BehaviorContext context = student.getBehaviorContext();
                if (context == null) {
                    context = new BehaviorContext(student, time, school);
                    context.setInteractionManager(interactionManager);
                    context.setTown(town);
                    student.setBehaviorContext(context);
                } else {
                    context.setTime(time);
                    context.setInteractionManager(interactionManager);
                    context.setTown(town);
                }

                EntityState state = student.getEntityState();
                boolean shouldDecide = true;
                if (state != null) {
                    ActivityType current = state.getCurrentActivity();
                    boolean isActiveAction = current != ActivityType.IDLE
                            && current != ActivityType.TRANSITIONING
                            && !current.isMovement();
                    if (isActiveAction && state.getDecisionCooldown() > 0) {
                        shouldDecide = false;
                    }
                }

                if (shouldDecide) {
                    tree.tick(context);
                    logStudentAction(student, context);
                    if (state != null) {
                        state.resetDecisionCooldown(ACTION_DURATION_TICKS);
                    }
                }
            }

            // Tick down cooldowns each tick
            EntityState state = student.getEntityState();
            if (state != null) {
                state.decrementDecisionCooldown();
                state.incrementTicksInActivity();
            }
        }

        // Phase 2: Resolve social interaction conflicts
        // The highest DET + CHR student wins when multiple target the same person
        interactionManager.resolveInteractions();
    }

    /**
     * Builds a human-readable log entry from the student's post-tick state
     * and appends it to the student's action log on their EntityState.
     * Includes location context so the reader knows where the student is.
     */
    private void logStudentAction(Student student, BehaviorContext context) {
        EntityState state = student.getEntityState();
        if (state == null) {
            return;
        }

        ActivityType activity = state.getCurrentActivity();
        String timeStamp = String.format("[%02d:%02d]",
                time.getHour(), time.getMinute());

        StringBuilder entry = new StringBuilder(timeStamp);

        String locationDesc = buildLocationDescription(state, activity);
        if (locationDesc != null) {
            entry.append(" ").append(locationDesc);
        } else {
            entry.append(" ").append(activity.getDisplayName());
            appendRoomContext(entry, state, activity);
        }

        Object target = context.getVariable("interaction_target");
        if (target instanceof Student targetStudent) {
            entry.append(" with ").append(targetStudent.studentName.getFirstName())
                    .append(" ").append(targetStudent.studentName.getLastName());
        }

        boolean wasCaught = context.getBoolVariable("was_caught", false);
        if (wasCaught) {
            String catchType = context.getVariable("catch_type", "");
            entry.append(" [CAUGHT");
            if (!catchType.isEmpty()) {
                entry.append(": ").append(catchType);
            }
            entry.append("]");
        }

        state.addLogEntry(entry.toString());

        // Clear ephemeral context variables to avoid stale data
        context.removeVariable("was_caught");
        context.removeVariable("catch_type");
        context.removeVariable("interaction_target");
        context.removeVariable("friendship_gained");
    }

    /**
     * Returns a full location-aware description for transition/idle states,
     * or null when the default activity display name should be used instead.
     */
    private String buildLocationDescription(EntityState state, ActivityType activity) {
        boolean beforeSchool = bellSchedule.isBeforeSchool(time);
        boolean afterSchool = bellSchedule.isAfterSchool(time);
        boolean inTransition = bellSchedule.isTransitionTime(time);

        // Commuting descriptions
        if (state.isInTransit()) {
            TransitMode mode = state.getTransitMode();
            int remaining = state.getTransitTicksRemaining();
            String modeStr = (mode != null) ? mode.getDisplayName() : "Commuting";
            return modeStr + " to school (" + remaining + " min remaining)";
        }

        if (beforeSchool && isPassiveWaitingActivity(activity)) {
            Room room = state.getCurrentRoom();
            if (room != null) {
                return "Standing outside before class (" + room.getRoomName() + ")";
            }
            return "Standing outside before class";
        }

        if (afterSchool && isPassiveWaitingActivity(activity)) {
            Room room = state.getCurrentRoom();
            if (room != null) {
                return "Leaving school (" + room.getRoomName() + ")";
            }
            return "Leaving school";
        }

        if (inTransition || activity == ActivityType.TRANSITIONING) {
            if (state.isMoving() && state.getDestinationRoom() != null) {
                return "Walking to " + state.getDestinationRoom().getRoomName();
            }
            Room room = state.getCurrentRoom();
            if (room != null) {
                return "Walking in " + room.getRoomName();
            }
            return "Walking in hallway";
        }

        if (activity == ActivityType.IDLE && !state.isInClass()) {
            Room room = state.getCurrentRoom();
            if (room != null) {
                return "Standing around in " + room.getRoomName();
            }
            return "Standing around in hallway";
        }

        return null;
    }

    /**
     * Appends a short room tag to the log entry for in-class or named-location
     * activities (e.g. "Taking Notes in Room 201").
     */
    private void appendRoomContext(StringBuilder entry, EntityState state,
                                   ActivityType activity) {
        Room room = state.getCurrentRoom();
        if (room != null) {
            entry.append(" in ").append(room.getRoomName());
        }
    }

    private boolean isPassiveWaitingActivity(ActivityType activity) {
        return activity == ActivityType.IDLE
                || activity == ActivityType.SOCIALIZING;
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
        // Reset phone text limits at the start of each new month
        int currentMonth = time.getMonth();
        if (currentMonth != lastProcessedMonth) {
            resetMonthlyTextLimits();
            lastProcessedMonth = currentMonth;
        }

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

        // Clear all OccupancyGrids for the new day
        if (roomOccupancyManager != null) {
            roomOccupancyManager.clearAllGrids();
        }
        currentTransitionIndex = 0;
        wasLunchA = false;
        wasLunchB = false;

        // Apply daily relationship decay: all social link scores drift toward neutral.
        // Family and best-friend bonds decay slower, incentivizing active maintenance.
        if (socialLinkConnector != null) {
            socialLinkConnector.applyDailyDecay();
        }

        // Advance clock to next school day morning so unpausing doesn't
        // re-trigger end-of-day immediately.
        time.advanceToNextSchoolDay();

        // Re-place all entities in their first-period rooms for the new day
        placeEntitiesForNewDay();
    }

    /**
     * Prepares all entities for a new day.  Students start off-campus (at
     * their neighborhood) and will commute in via {@link #processMorningTransit()}.
     * Staff are placed directly in their assigned rooms (simplified commute).
     */
    private void placeEntitiesForNewDay() {
        if (students != null) {
            for (Student student : students.values()) {
                EntityState state = student.getEntityState();
                if (state == null) {
                    continue;
                }

                // Students start at home — no room, not arrived
                state.setCurrentRoom(null);
                state.setExpectedRoom(null);
                state.setCurrentActivity(ActivityType.IDLE);
                state.setArrivedAtSchool(false);
                state.setInTransit(false);
                state.setCurrentPhase(DayPhase.PRE_SCHOOL);
            }
        }

        // Staff arrive directly between 7:30-8:00 AM (simplified)
        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                EntityState state = staffMember.getEntityState();
                if (state == null) {
                    continue;
                }

                Room assignedRoom = (school != null) ? school.getClassroomByStaff(staffMember) : null;
                if (assignedRoom != null) {
                    state.setCurrentRoom(assignedRoom);
                    state.setExpectedRoom(assignedRoom);
                    state.setCurrentActivity(ActivityType.IDLE);
                }
                state.setArrivedAtSchool(true);
                state.setCurrentPhase(DayPhase.PRE_SCHOOL);
            }
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
    }

    /**
     * Resets the monthly text allowance on every student and staff phone
     * in the town back to the plan's limit.
     */
    private void resetMonthlyTextLimits() {
        if (town == null) {
            return;
        }
        for (Map.Entry<Student, CellPhone> entry : town.getAllStudentPhones().entrySet()) {
            entry.getValue().resetTextLimit();
        }
        for (Map.Entry<Staff, CellPhone> entry : town.getAllStaffPhones().entrySet()) {
            entry.getValue().resetTextLimit();
        }
    }

    // ==================== Transition Movement ====================

    /**
     * Called once when a transition period begins.
     * Loads each student's pre-computed path from TraversalStorage and
     * stores it as a movement queue on their EntityState.  Students whose
     * next period is a free period (no precomputed path) are still marked
     * as transitioning so they vacate their current classroom;
     * {@link #finalizeTransitionArrival} will place them in a common area.
     */
    private void initiateTransitionMovement() {
        if (students == null || traversalStorage == null) {
            return;
        }
        String semester = time.getCurrentSemester();
        int nextPeriod = currentTransitionIndex + 2;

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            List<Room> path = traversalStorage.getPath(
                    student, currentTransitionIndex, semester);

            state.setCurrentActivity(ActivityType.TRANSITIONING);

            if (path.isEmpty()) {
                Room dest = getStudentScheduledRoom(student, nextPeriod);
                if (dest == null) {
                    dest = getFreePeriodRoom();
                }
                state.setMovementPath(null);
                state.setMoving(dest != null);
                state.setDestinationRoom(dest);
                continue;
            }

            LinkedList<Room> queue = new LinkedList<>();
            for (int i = 1; i < path.size(); i++) {
                queue.add(path.get(i));
            }
            state.setMovementPath(queue);
            state.setMoving(true);
            if (!queue.isEmpty()) {
                state.setDestinationRoom(queue.peekLast());
            }
        }

        currentTransitionIndex++;
    }

    /**
     * Called each tick during a transition period.
     * Advances each student one room along their movement path queue,
     * paced so they arrive before the transition ends.
     */
    private void advanceStudentMovement() {
        if (students == null || roomOccupancyManager == null) {
            return;
        }

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null || !state.hasPathRemaining()) {
                continue;
            }

            Room nextRoom = state.pollNextPathRoom();
            if (nextRoom != null) {
                Room currentRoom = state.getCurrentRoom();
                roomOccupancyManager.transferStudent(student, currentRoom, nextRoom);
            }
        }
    }

    /**
     * Called once when a transition period ends.
     * Ensures every student has arrived at their destination and is placed
     * on the correct room's OccupancyGrid for the new period.  Students
     * with no class scheduled are sent to a free-period common area.
     */
    private void finalizeTransitionArrival(int newPeriod) {
        if (students == null) {
            return;
        }
        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            // Clear any remaining path
            state.setMovementPath(null);
            state.setMoving(false);
            state.setDestinationRoom(null);

            if (newPeriod <= 0 || roomOccupancyManager == null) {
                continue;
            }

            Room targetRoom = getStudentScheduledRoom(student, newPeriod);
            if (targetRoom == null) {
                targetRoom = getFreePeriodRoom();
            }
            if (targetRoom == null) {
                continue;
            }

            Room currentRoom = state.getCurrentRoom();
            if (currentRoom != targetRoom) {
                roomOccupancyManager.transferStudent(student, currentRoom, targetRoom);
            }
            state.setExpectedRoom(targetRoom);
            state.setCurrentActivity(ActivityType.IDLE);
        }
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
