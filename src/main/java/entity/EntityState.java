package entity;

import entity.Rooms.Room;
import simulation.DayPhase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Tracks the current state of an entity (student or staff) in the simulation.
 * Maintains actual vs expected locations and current activity information.
 */
public class EntityState implements Serializable {

    /**
     * Tracked physiological needs that emit a one-shot status message when
     * they cross below the critical threshold. Used as a key for the
     * critical-notified flag set.
     */
    public enum NeedType {
        HUNGER, THIRST, BLADDER, ENTERTAINMENT, ENERGY
    }

    
    private Room currentRoom;           // Where they actually are
    private Room expectedRoom;          // Where schedule says they should be
    private int[] currentSeatCoords;    // Seat position if seated [row, col]
    private ActivityType currentActivity;
    private int ticksInActivity;        // How long in current activity
    private boolean isMoving;           // In transition between rooms
    private Room destinationRoom;       // Where they're heading
    private int movementTicksRemaining; // Ticks until arrival
    private boolean needsBathroom;      // Urgency flag
    private boolean hasPermissionToLeave; // If they asked and got permission
    private String lunchPeriod;         // "A" or "B"
    private int[] floorPosition;        // [row, col] on room's OccupancyGrid
    private transient Queue<Room> movementPath; // Room-by-room path during transitions
    private int decisionCooldown;       // Ticks until the next behavior tree re-evaluation

    // Physiological needs (0-100 scale, higher is better)
    private double hunger;              // 100 = full, 0 = starving
    private double thirst;              // 100 = hydrated, 0 = dehydrated
    private double bladder;             // 100 = empty, 0 = full/urgent
    private double temperature;         // 100 = too hot, 0 = freezing, 50 = ideal
    private int postMealBladderTicks;   // Ticks remaining of accelerated bladder decay after eating
    private double entertainment;       // 100 = entertained, 0 = completely bored
    private double energy;              // 100 = well-rested, 0 = exhausted
    private boolean asleep;             // true when energy reaches 0

    // Edge-triggered notification flags: set when a need has crossed below the
    // critical threshold and a status message has already been emitted. Cleared
    // when the need recovers above the threshold so a future relapse re-fires.
    private boolean hungerCriticalNotified;
    private boolean thirstCriticalNotified;
    private boolean bladderCriticalNotified;
    private boolean entertainmentCriticalNotified;
    private boolean energyCriticalNotified;

    // Lunch state tracking
    private boolean atLunch;            // Currently dispatched to lunch destination
    private Room preLunchRoom;          // Classroom the student left when lunch started
    private boolean canAffordOffCampus; // Cached eligibility for off-campus lunch (upperclassmen only)

    // Transit state (morning commute from neighborhood to school)
    private TransitMode transitMode;         // How this entity gets to school
    private int travelTimeMinutes;           // Duration of their commute in minutes
    private int departureTimeMinutes;        // Departure time as minutes from midnight
    private int transitTicksRemaining;       // Countdown ticks during active commute
    private boolean inTransit;               // Currently commuting to school
    private boolean arrivedAtSchool;         // Has arrived on campus today
    private transient List<Student> transitGroup; // Co-travelers for social interaction

    // Day phase tracking (extensibility for after-school / weekend)
    private DayPhase currentPhase;

    private static final int MAX_ACTION_LOG_SIZE = 50;
    private final transient LinkedList<String> actionLog = new LinkedList<>();
    
    /**
     * Creates a new entity state with default values.
     */
    public EntityState() {
        this.currentRoom = null;
        this.expectedRoom = null;
        this.currentSeatCoords = null;
        this.currentActivity = ActivityType.IDLE;
        this.ticksInActivity = 0;
        this.isMoving = false;
        this.destinationRoom = null;
        this.movementTicksRemaining = 0;
        this.needsBathroom = false;
        this.hasPermissionToLeave = false;
        this.lunchPeriod = "A"; // Default to A lunch
        this.hunger = 100.0;
        this.thirst = 100.0;
        this.bladder = 100.0;
        this.temperature = 50.0;
        this.postMealBladderTicks = 0;
        this.entertainment = 100.0;
        this.energy = 100.0;
        this.asleep = false;
        this.hungerCriticalNotified = false;
        this.thirstCriticalNotified = false;
        this.bladderCriticalNotified = false;
        this.entertainmentCriticalNotified = false;
        this.energyCriticalNotified = false;
        this.atLunch = false;
        this.preLunchRoom = null;
        this.canAffordOffCampus = false;
        this.transitMode = null;
        this.travelTimeMinutes = 0;
        this.departureTimeMinutes = 0;
        this.transitTicksRemaining = 0;
        this.inTransit = false;
        this.arrivedAtSchool = false;
        this.transitGroup = null;
        this.currentPhase = DayPhase.PRE_SCHOOL;
    }
    
    // Current Room
    
    public Room getCurrentRoom() {
        return currentRoom;
    }
    
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }
    
    // Expected Room
    
    public Room getExpectedRoom() {
        return expectedRoom;
    }
    
    public void setExpectedRoom(Room expectedRoom) {
        this.expectedRoom = expectedRoom;
    }
    
    // Seat Coordinates
    
    public int[] getCurrentSeatCoords() {
        return currentSeatCoords;
    }
    
    public void setCurrentSeatCoords(int[] currentSeatCoords) {
        this.currentSeatCoords = currentSeatCoords;
    }
    
    public void setCurrentSeatCoords(int row, int col) {
        this.currentSeatCoords = new int[]{row, col};
    }
    
    public boolean isSeated() {
        return currentSeatCoords != null;
    }
    
    // Current Activity
    
    public ActivityType getCurrentActivity() {
        return currentActivity;
    }
    
    public void setCurrentActivity(ActivityType currentActivity) {
        if (this.currentActivity != currentActivity) {
            this.currentActivity = currentActivity;
            this.ticksInActivity = 0;
        }
    }
    
    // Ticks in Activity
    
    public int getTicksInActivity() {
        return ticksInActivity;
    }
    
    public void incrementTicksInActivity() {
        this.ticksInActivity++;
    }
    
    public void resetTicksInActivity() {
        this.ticksInActivity = 0;
    }
    
    // Movement
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public void setMoving(boolean moving) {
        isMoving = moving;
    }
    
    public Room getDestinationRoom() {
        return destinationRoom;
    }
    
    public void setDestinationRoom(Room destinationRoom) {
        this.destinationRoom = destinationRoom;
    }
    
    public int getMovementTicksRemaining() {
        return movementTicksRemaining;
    }
    
    public void setMovementTicksRemaining(int movementTicksRemaining) {
        this.movementTicksRemaining = movementTicksRemaining;
    }
    
    public void decrementMovementTicks() {
        if (movementTicksRemaining > 0) {
            movementTicksRemaining--;
        }
    }
    
    /**
     * Starts movement to a destination room.
     *
     * @param destination the target room
     * @param ticksToArrive how many ticks until arrival
     */
    public void startMovement(Room destination, int ticksToArrive) {
        this.destinationRoom = destination;
        this.movementTicksRemaining = ticksToArrive;
        this.isMoving = true;
        this.currentActivity = ActivityType.TRANSITIONING;
        this.currentSeatCoords = null; // Not seated while moving
    }
    
    /**
     * Completes movement to the destination.
     */
    public void completeMovement() {
        if (destinationRoom != null) {
            this.currentRoom = destinationRoom;
        }
        this.destinationRoom = null;
        this.movementTicksRemaining = 0;
        this.isMoving = false;
        this.currentActivity = ActivityType.IDLE;
    }
    
    // Bathroom needs
    
    public boolean needsBathroom() {
        return needsBathroom;
    }
    
    public void setNeedsBathroom(boolean needsBathroom) {
        this.needsBathroom = needsBathroom;
    }
    
    // Permission
    
    public boolean hasPermissionToLeave() {
        return hasPermissionToLeave;
    }
    
    public void setHasPermissionToLeave(boolean hasPermissionToLeave) {
        this.hasPermissionToLeave = hasPermissionToLeave;
    }
    
    // Lunch Period
    
    public String getLunchPeriod() {
        return lunchPeriod;
    }
    
    public void setLunchPeriod(String lunchPeriod) {
        this.lunchPeriod = lunchPeriod;
    }

    // Lunch state

    public boolean isAtLunch() {
        return atLunch;
    }

    public void setAtLunch(boolean atLunch) {
        this.atLunch = atLunch;
    }

    public Room getPreLunchRoom() {
        return preLunchRoom;
    }

    public void setPreLunchRoom(Room preLunchRoom) {
        this.preLunchRoom = preLunchRoom;
    }

    public boolean canAffordOffCampus() {
        return canAffordOffCampus;
    }

    public void setCanAffordOffCampus(boolean canAffordOffCampus) {
        this.canAffordOffCampus = canAffordOffCampus;
    }

    // Transit state

    public TransitMode getTransitMode() {
        return transitMode;
    }

    public void setTransitMode(TransitMode transitMode) {
        this.transitMode = transitMode;
    }

    public int getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public void setTravelTimeMinutes(int travelTimeMinutes) {
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public int getDepartureTimeMinutes() {
        return departureTimeMinutes;
    }

    public void setDepartureTimeMinutes(int departureTimeMinutes) {
        this.departureTimeMinutes = departureTimeMinutes;
    }

    public int getTransitTicksRemaining() {
        return transitTicksRemaining;
    }

    public void setTransitTicksRemaining(int transitTicksRemaining) {
        this.transitTicksRemaining = transitTicksRemaining;
    }

    public void decrementTransitTicks() {
        if (transitTicksRemaining > 0) {
            transitTicksRemaining--;
        }
    }

    public boolean isInTransit() {
        return inTransit;
    }

    public void setInTransit(boolean inTransit) {
        this.inTransit = inTransit;
    }

    public boolean hasArrivedAtSchool() {
        return arrivedAtSchool;
    }

    public void setArrivedAtSchool(boolean arrivedAtSchool) {
        this.arrivedAtSchool = arrivedAtSchool;
    }

    public List<Student> getTransitGroup() {
        return transitGroup;
    }

    public void setTransitGroup(List<Student> transitGroup) {
        this.transitGroup = transitGroup;
    }

    // Day phase

    public DayPhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(DayPhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    /**
     * Returns the commuting ActivityType that corresponds to this entity's transit mode.
     *
     * @return the matching COMMUTING_* activity, or TRANSITIONING as fallback
     */
    public ActivityType getCommutingActivity() {
        if (transitMode == null) {
            return ActivityType.TRANSITIONING;
        }
        return switch (transitMode) {
            case WALK -> ActivityType.COMMUTING_WALK;
            case BUS -> ActivityType.COMMUTING_BUS;
            case DRIVE -> ActivityType.COMMUTING_DRIVE;
            case CARPOOL -> ActivityType.COMMUTING_CARPOOL;
        };
    }

    // Floor position (on OccupancyGrid)
    
    public int[] getFloorPosition() {
        return floorPosition;
    }
    
    public void setFloorPosition(int[] floorPosition) {
        this.floorPosition = floorPosition;
    }
    
    public void setFloorPosition(int row, int col) {
        this.floorPosition = new int[]{row, col};
    }
    
    // Movement path (room-by-room queue for transitions)
    
    public Queue<Room> getMovementPath() {
        return movementPath;
    }
    
    public void setMovementPath(Queue<Room> path) {
        this.movementPath = path;
    }
    
    /**
     * Polls the next room from the movement path queue.
     *
     * @return the next room, or null if the path is empty or not set
     */
    public Room pollNextPathRoom() {
        if (movementPath != null && !movementPath.isEmpty()) {
            return movementPath.poll();
        }
        return null;
    }
    
    public boolean hasPathRemaining() {
        return movementPath != null && !movementPath.isEmpty();
    }
    
    // Decision cooldown (prevents re-evaluating the behavior tree every tick)
    
    public int getDecisionCooldown() {
        return decisionCooldown;
    }
    
    public void resetDecisionCooldown(int ticks) {
        this.decisionCooldown = ticks;
    }
    
    public void decrementDecisionCooldown() {
        if (decisionCooldown > 0) {
            decisionCooldown--;
        }
    }
    
    // Utility methods
    
    /**
     * Checks if the entity is where they should be.
     *
     * @return true if current room matches expected room
     */
    public boolean isWhereExpected() {
        if (currentRoom == null && expectedRoom == null) {
            return true;
        }
        if (currentRoom == null || expectedRoom == null) {
            return false;
        }
        return currentRoom.equals(expectedRoom);
    }
    
    /**
     * Checks if the entity is skipping (not where expected and not in transition).
     *
     * @return true if skipping
     */
    public boolean isSkipping() {
        return !isWhereExpected() && !isMoving && 
               currentActivity != ActivityType.IN_BATHROOM;
    }
    
    /**
     * Checks if the entity is physically in their scheduled instructional space.
     *
     * @return true if in class
     */
    public boolean isInClass() {
        return isWhereExpected()
                && currentRoom != null
                && currentRoom.isInstructionalSpace();
    }
    
    // Physiological needs

    public double getHunger() {
        return hunger;
    }

    public void setHunger(double hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public double getThirst() {
        return thirst;
    }

    public void setThirst(double thirst) {
        this.thirst = Math.max(0, Math.min(100, thirst));
    }

    public double getBladder() {
        return bladder;
    }

    public void setBladder(double bladder) {
        this.bladder = Math.max(0, Math.min(100, bladder));
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = Math.max(0, Math.min(100, temperature));
    }

    public double getEntertainment() {
        return entertainment;
    }

    public void setEntertainment(double entertainment) {
        this.entertainment = Math.max(0, Math.min(100, entertainment));
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = Math.max(0, Math.min(100, energy));
    }

    public boolean isAsleep() {
        return asleep;
    }

    public void setAsleep(boolean asleep) {
        this.asleep = asleep;
    }

    /**
     * Returns whether a one-shot critical-need status message has already
     * been emitted for the given need (and not yet been reset by recovery).
     *
     * @param need the need to query
     * @return true if the entity has already been notified for this need
     */
    public boolean isCriticalNotified(NeedType need) {
        return switch (need) {
            case HUNGER -> hungerCriticalNotified;
            case THIRST -> thirstCriticalNotified;
            case BLADDER -> bladderCriticalNotified;
            case ENTERTAINMENT -> entertainmentCriticalNotified;
            case ENERGY -> energyCriticalNotified;
        };
    }

    /**
     * Records whether the one-shot critical-need notification for the given
     * need has been emitted. Set to true after firing the message; set back
     * to false when the need recovers above the critical threshold.
     *
     * @param need     the need to update
     * @param notified the new flag value
     */
    public void setCriticalNotified(NeedType need, boolean notified) {
        switch (need) {
            case HUNGER -> this.hungerCriticalNotified = notified;
            case THIRST -> this.thirstCriticalNotified = notified;
            case BLADDER -> this.bladderCriticalNotified = notified;
            case ENTERTAINMENT -> this.entertainmentCriticalNotified = notified;
            case ENERGY -> this.energyCriticalNotified = notified;
        }
    }

    /**
     * Signals that this person has eaten, triggering accelerated bladder
     * decay for the next 2 hours (120 ticks).
     */
    public void onAte() {
        this.postMealBladderTicks = 120;
    }

    /**
     * Advances all physiological needs by one simulation tick.
     * <p>
     * Does NOT toggle the {@code asleep} flag when energy reaches 0. The
     * exhaustion cascade (secondary-stat drain followed by sleep) is driven
     * by {@code SimulationEngine}, which has access to the entity's
     * {@code PStatistics} and can decide when sleep is warranted.
     *
     * @param hungerDecay              amount hunger decreases per tick
     * @param thirstDecay              amount thirst decreases per tick
     * @param bladderDecay             normal bladder decrease per tick
     * @param bladderPostMealDecay     bladder decrease per tick after eating
     * @param entertainmentDecay       amount entertainment decreases per tick
     * @param energyDecay              base energy decrease per tick
     * @param energyDecayWhenBored     energy decrease when entertainment is 0
     */
    public void tickNeeds(double hungerDecay, double thirstDecay,
                          double bladderDecay, double bladderPostMealDecay,
                          double entertainmentDecay, double energyDecay,
                          double energyDecayWhenBored) {
        this.hunger = Math.max(0, this.hunger - hungerDecay);
        this.thirst = Math.max(0, this.thirst - thirstDecay);

        if (postMealBladderTicks > 0) {
            this.bladder = Math.max(0, this.bladder - bladderPostMealDecay);
            postMealBladderTicks--;
        } else {
            this.bladder = Math.max(0, this.bladder - bladderDecay);
        }

        this.entertainment = Math.max(0, this.entertainment - entertainmentDecay);

        double actualEnergyDecay = (this.entertainment <= 0) ? energyDecayWhenBored : energyDecay;
        this.energy = Math.max(0, this.energy - actualEnergyDecay);
    }

    /**
     * Checks if a physiological need has crossed below the critical threshold.
     *
     * @param threshold the critical threshold (e.g. 30)
     * @return true if hunger, thirst, or bladder is below the threshold
     */
    public boolean hasNeedBelowThreshold(double threshold) {
        return hunger < threshold || thirst < threshold
                || bladder < threshold || entertainment < threshold;
    }

    /**
     * Resets all physiological needs to their start-of-day values.
     * Called when the person sleeps / at the end of each day.
     */
    public void resetNeeds() {
        this.hunger = 100.0;
        this.thirst = 100.0;
        this.bladder = 100.0;
        this.temperature = 50.0;
        this.postMealBladderTicks = 0;
        this.entertainment = 100.0;
        this.energy = 100.0;
        this.asleep = false;
        this.hungerCriticalNotified = false;
        this.thirstCriticalNotified = false;
        this.bladderCriticalNotified = false;
        this.entertainmentCriticalNotified = false;
        this.energyCriticalNotified = false;
    }

    /**
     * Resets the state for a new day.
     */
    public void resetForNewDay() {
        this.currentRoom = null;
        this.expectedRoom = null;
        this.currentSeatCoords = null;
        this.currentActivity = ActivityType.IDLE;
        this.ticksInActivity = 0;
        this.isMoving = false;
        this.destinationRoom = null;
        this.movementTicksRemaining = 0;
        this.needsBathroom = false;
        this.hasPermissionToLeave = false;
        this.floorPosition = null;
        this.movementPath = null;
        this.decisionCooldown = 0;
        this.atLunch = false;
        this.preLunchRoom = null;
        this.transitTicksRemaining = 0;
        this.inTransit = false;
        this.arrivedAtSchool = false;
        this.currentPhase = DayPhase.PRE_SCHOOL;
        resetNeeds();
    }
    
    /**
     * Appends a timestamped entry to this entity's action log.
     * The log is capped at {@value #MAX_ACTION_LOG_SIZE} entries (oldest are discarded).
     *
     * @param entry the human-readable log line
     */
    public void addLogEntry(String entry) {
        if (entry == null) {
            return;
        }
        actionLog.addLast(entry);
        while (actionLog.size() > MAX_ACTION_LOG_SIZE) {
            actionLog.removeFirst();
        }
    }
    
    /**
     * Returns an unmodifiable snapshot of the action log.
     *
     * @return the action log entries, oldest first
     */
    public List<String> getActionLog() {
        return Collections.unmodifiableList(new ArrayList<>(actionLog));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EntityState{");
        sb.append("currentRoom=").append(currentRoom != null ? currentRoom.getRoomName() : "null");
        sb.append(", expectedRoom=").append(expectedRoom != null ? expectedRoom.getRoomName() : "null");
        sb.append(", activity=").append(currentActivity);
        sb.append(", ticksInActivity=").append(ticksInActivity);
        if (isMoving) {
            sb.append(", moving to=").append(destinationRoom != null ? destinationRoom.getRoomName() : "null");
            sb.append(", ticksRemaining=").append(movementTicksRemaining);
        }
        sb.append("}");
        return sb.toString();
    }
}
