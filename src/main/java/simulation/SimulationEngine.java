package simulation;

import behavior.BehaviorContext;
import behavior.BehaviorTree;
import constants.SimConstants;
import entity.*;
import entity.Radio.MusicGenre;
import entity.Radio.Radio;
import entity.Radio.RadioStation;
import entity.Radio.Song;
import entity.Rooms.OffCampus;
import entity.Rooms.Room;
import save.SimulationRuntimeSnapshot;
import utility.GameLogger;
import utility.AcademicProgressService;
import utility.PStatistics;
import utility.RadioReactionMessageLoader;
import utility.SocialLinkConnector;
import utility.TraversalStorage;
import utility.music.MusicPreference;
import utility.music.MusicTaste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    private int lastHomeworkAssignmentDay = -1;
    private LunchDestinationSelector lunchDestinationSelector;
    private boolean wasLunchA = false;
    private boolean wasLunchB = false;
    private Radio radio;

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
     * Wires the FM radio broadcast roster so the engine can advance the
     * "now playing" song on each station once per simulated minute.
     *
     * @param radio the town's radio container, or {@code null} to disable
     */
    public void setRadio(Radio radio) {
        this.radio = radio;
        if (radio != null && time != null) {
            radio.tick(time);
        }
    }

    /**
     * @return the currently wired radio container, or {@code null}.
     */
    public Radio getRadio() {
        return radio;
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

        // 1.6. Advance FM radio before commutes so "now playing" is current
        if (radio != null) {
            notifyCommuteRadioSongChanges(radio.tickAndCollectSongChanges(time));
        }

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

        // 3.1. Assign regular homework once per eligible school day.
        processHomeworkAssignments(currentDayPhase);

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
        logCommuteRadio(student, state);
    }

    /**
     * When a student starts a bus or car commute, pick a dial station and
     * record whatever is currently playing in their activity log.
     */
    private void logCommuteRadio(Student student, EntityState state) {
        if (radio == null || state == null || time == null) {
            return;
        }
        TransitMode mode = state.getTransitMode();
        if (!Radio.playsRadioDuringCommute(mode)) {
            return;
        }
        RadioStation station = radio.pickStationForCommute(mode,
                new Random(GameRandom.getSeed()
                        ^ System.identityHashCode(student)
                        ^ time.getDayCounter()
                        ^ time.getMinutesFromMidnight()));
        if (station == null) {
            return;
        }
        Song song = station.getCurrentSong();
        if (song == null) {
            radio.tick(time);
            song = station.getCurrentSong();
        }
        state.setCommuteRadioFrequencyMhz(station.getFrequencyMhz());
        logCommuteRadioListening(student, state, station, song);
    }

    /**
     * When a station rotates its song, log the new track for every student
     * still commuting on bus or car who is tuned to that frequency.
     */
    private void notifyCommuteRadioSongChanges(List<RadioStation> rotatedStations) {
        if (students == null || rotatedStations == null || rotatedStations.isEmpty()) {
            return;
        }
        for (RadioStation station : rotatedStations) {
            Song song = station.getCurrentSong();
            if (song == null) {
                continue;
            }
            for (Student student : students.values()) {
                EntityState state = student.getEntityState();
                if (state == null || !state.isInTransit()) {
                    continue;
                }
                if (!Radio.playsRadioDuringCommute(state.getTransitMode())) {
                    continue;
                }
                if (!state.hasCommuteRadio()) {
                    continue;
                }
                if (Double.compare(state.getCommuteRadioFrequencyMhz(),
                        station.getFrequencyMhz()) != 0) {
                    continue;
                }
                logCommuteRadioListening(student, state, station, song);
            }
        }
    }

    private void logCommuteRadioListening(Student student, EntityState state,
                                          RadioStation station, Song song) {
        String message = Radio.formatCommuteListeningEntry(station, song);
        if (message != null) {
            addEntityStatusLogEntry(state, message);
        }
        reactToCommuteSong(student, state, song);
    }

    /**
     * Nudges a commuting student's mood based on how their clique music taste
     * scores the song now playing, and logs a single flavor line. Liked songs
     * lift entertainment (with a little stress relief); disliked songs dip it,
     * damped by the listener's openness.
     */
    private void reactToCommuteSong(Student student, EntityState state,
                                    Song song) {
        if (student == null || state == null || song == null
                || student.studentStatistics == null) {
            return;
        }
        Set<MusicGenre> genres = song.getGenres();
        if (genres == null || genres.isEmpty()) {
            return;
        }
        MusicPreference taste = MusicTaste.forStudent(student);
        double best = -Double.MAX_VALUE;
        for (MusicGenre genre : genres) {
            best = Math.max(best, taste.weightFor(genre));
        }

        String name = student.studentName.getFirstName();
        if (best >= SimConstants.RADIO_REACTION_LIKE_THRESHOLD) {
            state.setEntertainment(state.getEntertainment()
                    + SimConstants.RADIO_REACTION_ENTERTAINMENT_BOOST);
            student.studentStatistics.getAllostaticLoad()
                    .applyRelaxationRecovery(
                            SimConstants.RADIO_REACTION_RELAXATION_RECOVERY);
            String template = RadioReactionMessageLoader.pickLikeMessage(
                    radioReactionRandom(student, song));
            addEntityStatusLogEntry(state, String.format(
                    template, name));
        } else if (best <= SimConstants.RADIO_REACTION_DISLIKE_THRESHOLD) {
            // Higher openness softens the sting of a disliked song.
            double penalty = SimConstants.RADIO_REACTION_ENTERTAINMENT_PENALTY
                    * (1.0 - taste.getOpenness());
            state.setEntertainment(state.getEntertainment() - penalty);
            String template = RadioReactionMessageLoader.pickDislikeMessage(
                    radioReactionRandom(student, song));
            addEntityStatusLogEntry(state, String.format(
                    template, name));
        }
    }

    private Random radioReactionRandom(Student student, Song song) {
        long seed = GameRandom.getSeed()
                ^ System.identityHashCode(student)
                ^ song.hashCode();
        if (time != null) {
            seed ^= ((long) time.getDayCounter() << 32);
            seed ^= time.getMinutesFromMidnight();
        }
        return new Random(seed);
    }

    /**
     * Places a student on campus after their commute completes.
     */
    private void completeArrival(Student student, EntityState state) {
        state.setInTransit(false);
        state.setArrivedAtSchool(true);
        state.clearCommuteRadio();
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
     * Secondary stats drained one-by-one each tick once an entity's energy
     * reaches 0. The strings here match the keys accepted by
     * {@link PStatistics#drainSecondaryStat(String, int, double)}.
     */
    private static final String[] SECONDARY_STAT_NAMES = {
            "creativity", "empathy", "adaptability", "initiative",
            "resilience", "curiosity", "responsibility", "openMindedness"
    };

    /**
     * Ticks physiological needs for every student and staff member. Energy
     * decay is scaled by the entity's determination, eased by socializing,
     * and partially refilled while eating. When energy hits 0 the entity
     * begins burning down secondary stats; once those are exhausted the
     * entity falls asleep. Edge-triggered status messages are emitted as
     * each need crosses below the critical threshold.
     */
    private void tickAllNeeds() {
        if (students != null) {
            for (Student student : students.values()) {
                EntityState state = student.getEntityState();
                if (state == null) {
                    continue;
                }
                String displayName = student.studentName.getFirstName()
                        + " " + student.studentName.getLastName();
                tickEntityNeeds(state, student.studentStatistics, displayName,
                        false);
            }
        }
        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                EntityState state = staffMember.getEntityState();
                if (state == null) {
                    continue;
                }
                String displayName = staffMember.teacherName.getFirstName()
                        + " " + staffMember.teacherName.getLastName();
                tickEntityNeeds(state, staffMember.teacherStatistics, displayName,
                        true);
            }
        }
    }

    /**
     * Advances all needs for a single entity, applies stress, fires
     * critical-need messages, and runs the exhaustion cascade.
     *
     * <p>When {@code isStaff} is true, all decay rates are multiplied by
     * the corresponding {@code STAFF_*_DECAY_MULTIPLIER} (modeling that
     * adults handle most of these needs off-screen) and per-tick
     * auto-refills are applied during transitions and lunch periods (the
     * equivalent of a quick coffee, restroom trip, or lounge bite that we
     * never explicitly simulate). This keeps teachers functional through
     * a normal school day even though they have no behavior tree driving
     * eating, drinking, or bathroom actions.</p>
     */
    private void tickEntityNeeds(EntityState state, PStatistics stats,
                                 String displayName, boolean isStaff) {
        boolean eating = isEatingLunch(state);
        boolean socializing = isSocializing(state);

        if (eating) {
            state.setHunger(state.getHunger()
                    + SimConstants.NEED_HUNGER_REFILL_PER_TICK);
            state.setThirst(state.getThirst()
                    + SimConstants.NEED_THIRST_REFILL_PER_TICK);
            state.setEnergy(state.getEnergy()
                    + SimConstants.NEED_ENERGY_REFILL_PER_TICK_EATING);
        }

        double determinationMult = SimConstants.NEED_ENERGY_DETERMINATION_BASE
                - SimConstants.NEED_ENERGY_DETERMINATION_SLOPE
                        * stats.getDetermination();
        if (determinationMult < 0) {
            determinationMult = 0;
        }
        double energyDecay = SimConstants.NEED_ENERGY_DECAY_PER_TICK
                * determinationMult;
        double energyDecayBored = SimConstants.NEED_ENERGY_DECAY_WHEN_BORED
                * determinationMult;
        if (socializing) {
            energyDecay *= SimConstants.NEED_ENERGY_DECAY_SOCIAL_MULTIPLIER;
            energyDecayBored *= SimConstants.NEED_ENERGY_DECAY_SOCIAL_MULTIPLIER;
        }

        double hungerDecay = eating ? 0 : SimConstants.NEED_HUNGER_DECAY_PER_TICK;
        double thirstDecay = eating ? 0 : SimConstants.NEED_THIRST_DECAY_PER_TICK;
        double bladderDecay = SimConstants.NEED_BLADDER_DECAY_PER_TICK;
        double bladderPostMealDecay = SimConstants.NEED_BLADDER_POST_MEAL_DECAY_PER_TICK;
        double entertainmentDecay = SimConstants.NEED_ENTERTAINMENT_DECAY_PER_TICK;

        if (isStaff) {
            hungerDecay        *= SimConstants.STAFF_HUNGER_DECAY_MULTIPLIER;
            thirstDecay        *= SimConstants.STAFF_THIRST_DECAY_MULTIPLIER;
            bladderDecay       *= SimConstants.STAFF_BLADDER_DECAY_MULTIPLIER;
            bladderPostMealDecay *= SimConstants.STAFF_BLADDER_DECAY_MULTIPLIER;
            entertainmentDecay *= SimConstants.STAFF_ENTERTAINMENT_DECAY_MULTIPLIER;
            energyDecay        *= SimConstants.STAFF_ENERGY_DECAY_MULTIPLIER;
            energyDecayBored   *= SimConstants.STAFF_ENERGY_DECAY_MULTIPLIER;
        }

        state.tickNeeds(
                hungerDecay,
                thirstDecay,
                bladderDecay,
                bladderPostMealDecay,
                entertainmentDecay,
                energyDecay,
                energyDecayBored);

        // Staff-only off-screen self-care: applied AFTER the normal decay
        // tick so the refill is visible as a slight regen against the
        // baseline drain. Net effect is roughly steady-state during
        // transitions / lunch instead of monotonic decay.
        if (isStaff) {
            applyStaffOffScreenRefills(state);
        }

        if (state.getBladder() < SimConstants.NEED_CRITICAL_THRESHOLD) {
            state.setNeedsBathroom(true);
        }

        applyNeedStress(state, stats.getAllostaticLoad());
        fireCriticalNeedMessages(state, displayName);
        runExhaustionCascade(state, stats, displayName);
    }

    /**
     * Applies the silent off-screen refills staff get during natural
     * breaks: between bells (transitions) and during either student
     * lunch period (A or B). Each window contributes a per-tick top-up to
     * needs so that adults never collapse into critical territory under
     * normal conditions, even though they have no behavior tree driving
     * eating, drinking, or bathroom actions.
     */
    private void applyStaffOffScreenRefills(EntityState state) {
        if (state == null || bellSchedule == null || time == null) {
            return;
        }

        boolean inTransition = bellSchedule.isTransitionTime(time);
        boolean inAnyLunch = bellSchedule.isLunchTime(time, "A")
                || bellSchedule.isLunchTime(time, "B");

        if (inTransition) {
            state.setHunger(state.getHunger()
                    + SimConstants.STAFF_TRANSITION_HUNGER_REFILL);
            state.setThirst(state.getThirst()
                    + SimConstants.STAFF_TRANSITION_THIRST_REFILL);
            state.setBladder(state.getBladder()
                    + SimConstants.STAFF_TRANSITION_BLADDER_REFILL);
            state.setEntertainment(state.getEntertainment()
                    + SimConstants.STAFF_TRANSITION_ENTERTAINMENT_REFILL);
            state.setEnergy(state.getEnergy()
                    + SimConstants.STAFF_TRANSITION_ENERGY_REFILL);
        }

        if (inAnyLunch) {
            state.setHunger(state.getHunger()
                    + SimConstants.STAFF_LUNCH_HUNGER_REFILL);
            state.setThirst(state.getThirst()
                    + SimConstants.STAFF_LUNCH_THIRST_REFILL);
            state.setBladder(state.getBladder()
                    + SimConstants.STAFF_LUNCH_BLADDER_REFILL);
            state.setEntertainment(state.getEntertainment()
                    + SimConstants.STAFF_LUNCH_ENTERTAINMENT_REFILL);
            state.setEnergy(state.getEnergy()
                    + SimConstants.STAFF_LUNCH_ENERGY_REFILL);
        }
    }

    private static boolean isEatingLunch(EntityState state) {
        ActivityType activity = state.getCurrentActivity();
        return activity == ActivityType.EATING_LUNCH
                || activity == ActivityType.EATING_LUNCH_OFF_CAMPUS;
    }

    private static boolean isSocializing(EntityState state) {
        ActivityType activity = state.getCurrentActivity();
        return activity == ActivityType.SOCIALIZING
                || activity == ActivityType.TALKING;
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
     * Emits one-shot story messages when a need crosses below the critical
     * threshold and has not yet been notified. Resets the notified flag
     * once the need recovers above the threshold so a future relapse
     * re-fires the message.
     */
    private void fireCriticalNeedMessages(EntityState state,
                                          String displayName) {
        checkCriticalEdge(state, displayName, EntityState.NeedType.HUNGER,
                state.getHunger(),
                SimConstants.NEED_HUNGER_CRITICAL_MESSAGE);
        checkCriticalEdge(state, displayName, EntityState.NeedType.THIRST,
                state.getThirst(),
                SimConstants.NEED_THIRST_CRITICAL_MESSAGE);
        checkCriticalEdge(state, displayName, EntityState.NeedType.BLADDER,
                state.getBladder(),
                SimConstants.NEED_BLADDER_CRITICAL_MESSAGE);
        checkCriticalEdge(state, displayName, EntityState.NeedType.ENTERTAINMENT,
                state.getEntertainment(),
                SimConstants.NEED_ENTERTAINMENT_CRITICAL_MESSAGE);
        checkCriticalEdge(state, displayName, EntityState.NeedType.ENERGY,
                state.getEnergy(),
                SimConstants.NEED_ENERGY_CRITICAL_MESSAGE);
    }

    private void checkCriticalEdge(EntityState state, String displayName,
                                   EntityState.NeedType need, double value,
                                   String formatStr) {
        boolean below = value < SimConstants.NEED_CRITICAL_THRESHOLD;
        boolean alreadyNotified = state.isCriticalNotified(need);
        if (below && !alreadyNotified) {
            addEntityStatusLogEntry(state, String.format(formatStr, displayName));
            state.setCriticalNotified(need, true);
        } else if (!below && alreadyNotified) {
            state.setCriticalNotified(need, false);
        }
    }

    private void addEntityStatusLogEntry(EntityState state, String message) {
        if (state == null || message == null || time == null) {
            return;
        }
        String timeStamp = String.format("[%02d:%02d]", time.getHour(), time.getMinute());
        state.addLogEntry(timeStamp + " " + message);
    }

    /**
     * While an entity has 0 energy and is not yet asleep, drains every
     * secondary stat by a small amount each tick. Once every secondary
     * stat has been driven to 0 the entity falls asleep and a story
     * message is emitted.
     */
    private void runExhaustionCascade(EntityState state, PStatistics stats,
                                      String displayName) {
        if (stats == null || state.isAsleep() || state.getEnergy() > 0) {
            return;
        }
        int amount = SimConstants.EXHAUSTION_SECONDARY_STAT_DRAIN_PER_TICK;
        double stress = SimConstants.EXHAUSTION_DRAIN_STRESS_FACTOR;
        for (String statName : SECONDARY_STAT_NAMES) {
            stats.drainSecondaryStat(statName, amount, stress);
        }
        if (allSecondaryStatsZero(stats)) {
            state.setAsleep(true);
            addEntityStatusLogEntry(state, String.format(
                    SimConstants.NEED_FELL_ASLEEP_MESSAGE, displayName));
        }
    }

    private static boolean allSecondaryStatsZero(PStatistics stats) {
        return stats.getCreativity() == 0
                && stats.getEmpathy() == 0
                && stats.getAdaptability() == 0
                && stats.getInitiative() == 0
                && stats.getResilience() == 0
                && stats.getCuriosity() == 0
                && stats.getResponsibility() == 0
                && stats.getOpenMindedness() == 0;
    }

    /**
     * Processes behavior trees for all students.
     *
     * <p>
     * Runs in three phases so that social interactions are captured for
     * <i>both</i> participants in the action log:
     * </p>
     * <ol>
     *   <li><b>Tick</b> — every eligible student's behavior tree is ticked.
     *       Action nodes register pending interactions and tentatively set
     *       their own activity, but no log entries are written yet.</li>
     *   <li><b>Resolve</b> — {@link InteractionManager#resolveInteractions()}
     *       picks winners by DET + CHR, mirrors the social activity onto the
     *       target, and propagates the {@code interaction_target} /
     *       {@code was_caught} flags to the target's behavior context. The
     *       set of confirmed targets is returned.</li>
     *   <li><b>Log</b> — every student that either ticked their tree or was
     *       drawn into someone else's confirmed interaction gets an action
     *       log entry, using the post-resolution state. This guarantees that
     *       when Danielle starts talking with Baby Carey, Baby's log line for
     *       that minute reads "Talking with Danielle Beddoe" — even if
     *       Baby's own behavior tree was on cooldown and never ticked.</li>
     * </ol>
     */
    private void processStudentBehaviors() {
        if (students == null) {
            return;
        }

        interactionManager.clearTick();

        Set<Student> tickedStudents = new HashSet<>();

        // Phase 1: Tick all behavior trees. Defer logging until after social
        // interaction conflicts are resolved so that targets get their log
        // entries written with the bilateral state, not the stale pre-resolution
        // state.
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
                    tickedStudents.add(student);
                }
            }
        }

        // Phase 2: Resolve social interaction conflicts. The highest DET + CHR
        // student wins when multiple target the same person; the resolver
        // mirrors the activity onto each confirmed target and returns the set
        // of targets that were drawn into an interaction this tick.
        Set<Student> confirmedTargets = interactionManager.resolveInteractions();

        // Phase 3: Log every student whose state changed this tick — those that
        // ticked their tree, plus the confirmed interaction targets (whose tree
        // may have been on cooldown and never ran, but who are now genuinely
        // engaged with another student).
        Set<Student> studentsToLog = new HashSet<>(tickedStudents);
        studentsToLog.addAll(confirmedTargets);
        for (Student student : studentsToLog) {
            BehaviorContext context = student.getBehaviorContext();
            if (context != null) {
                logStudentAction(student, context);
            }
            EntityState state = student.getEntityState();
            if (state != null) {
                state.resetDecisionCooldown(ACTION_DURATION_TICKS);
            }
        }

        // Phase 4: Tick down cooldowns and increment activity ticks for every
        // student that's on campus / in transit.
        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }
            if (!state.hasArrivedAtSchool() && !state.isInTransit()) {
                continue;
            }
            state.decrementDecisionCooldown();
            state.incrementTicksInActivity();
        }
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

        Object learning = context.getVariable("learning_gained");
        boolean finalActivityCanShowLearning = activity == ActivityType.ATTENDING_CLASS
                || activity == ActivityType.TAKING_NOTES;
        if (finalActivityCanShowLearning
                && learning instanceof Number learningNumber
                && learningNumber.doubleValue() > 0.0) {
            entry.append(" [Learning +")
                    .append(String.format("%.1f", learningNumber.doubleValue()))
                    .append("]");
        }

        state.addLogEntry(entry.toString());

        // Clear ephemeral context variables to avoid stale data
        context.removeVariable("was_caught");
        context.removeVariable("catch_type");
        context.removeVariable("interaction_target");
        context.removeVariable("friendship_gained");
        context.removeVariable("learning_gained");
    }

    private void processHomeworkAssignments(DayPhase currentDayPhase) {
        if (students == null || time == null || currentDayPhase != DayPhase.SCHOOL_DAY) {
            return;
        }
        int day = time.getDayCounter();
        if (lastHomeworkAssignmentDay == day) {
            return;
        }

        int assignedCount = 0;
        for (Student student : students.values()) {
            assignedCount += AcademicProgressService.assignHomeworkIfDue(student, time);
        }
        if (assignedCount > 0) {
            GameLogger.logDebug("Assigned " + assignedCount + " homework item(s) for day " + day);
        }
        lastHomeworkAssignmentDay = day;
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
                AcademicProgressService.resolveHomeworkForDay(student, time.getDayCounter());
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
        lastHomeworkAssignmentDay = -1;

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

    public SimulationRuntimeSnapshot createRuntimeSnapshot() {
        return new SimulationRuntimeSnapshot(isPaused, ticksPerUpdate,
                minutesPerTick, currentTick, currentTransitionIndex,
                lastProcessedMonth, lastHomeworkAssignmentDay, wasLunchA,
                wasLunchB);
    }

    public void restoreRuntimeSnapshot(SimulationRuntimeSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        this.isPaused = snapshot.isPaused();
        this.ticksPerUpdate = snapshot.getTicksPerUpdate();
        this.minutesPerTick = snapshot.getMinutesPerTick();
        this.currentTick = snapshot.getCurrentTick();
        this.currentTransitionIndex = snapshot.getCurrentTransitionIndex();
        this.lastProcessedMonth = snapshot.getLastProcessedMonth();
        this.lastHomeworkAssignmentDay = snapshot.getLastHomeworkAssignmentDay();
        this.wasLunchA = snapshot.wasLunchA();
        this.wasLunchB = snapshot.wasLunchB();
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
