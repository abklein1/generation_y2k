package entity;

import entity.Rooms.Room;

import java.io.Serializable;

/**
 * Tracks the current state of an entity (student or staff) in the simulation.
 * Maintains actual vs expected locations and current activity information.
 */
public class EntityState implements Serializable {
    
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
     * Checks if the entity is in class (current room matches expected and in classroom activity).
     *
     * @return true if in class
     */
    public boolean isInClass() {
        return isWhereExpected() && 
               currentActivity.requiresClassroom();
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
