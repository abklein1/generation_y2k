package behavior;

import entity.Rooms.Room;
import entity.Staff;
import entity.StandardSchool;
import entity.Student;
import entity.Time;
import entity.Town;
import simulation.InteractionManager;

import java.util.HashMap;

/**
 * Holds per-entity state during behavior tree traversal.
 * Provides access to game state and entity-specific variables.
 */
public class BehaviorContext {
    
    private Student student;
    private Staff staff;
    private Time time;
    private StandardSchool school;
    private Town town;
    private Room currentRoom;
    private final HashMap<String, Object> variables;
    private boolean isPlayer;
    private InteractionManager interactionManager;
    
    /**
     * Creates a new behavior context.
     */
    public BehaviorContext() {
        this.variables = new HashMap<>();
        this.isPlayer = false;
    }
    
    /**
     * Creates a behavior context for a student.
     *
     * @param student the student entity
     * @param time the game time
     * @param school the school instance
     */
    public BehaviorContext(Student student, Time time, StandardSchool school) {
        this();
        this.student = student;
        this.time = time;
        this.school = school;
    }
    
    /**
     * Creates a behavior context for a staff member.
     *
     * @param staff the staff entity
     * @param time the game time
     * @param school the school instance
     */
    public BehaviorContext(Staff staff, Time time, StandardSchool school) {
        this();
        this.staff = staff;
        this.time = time;
        this.school = school;
    }
    
    // Student getters/setters
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    // Staff getters/setters
    public Staff getStaff() {
        return staff;
    }
    
    public void setStaff(Staff staff) {
        this.staff = staff;
    }
    
    // Time getters/setters
    public Time getTime() {
        return time;
    }
    
    public void setTime(Time time) {
        this.time = time;
    }
    
    // School getters/setters
    public StandardSchool getSchool() {
        return school;
    }
    
    public void setSchool(StandardSchool school) {
        this.school = school;
    }
    
    // Town getters/setters
    public Town getTown() {
        return town;
    }
    
    public void setTown(Town town) {
        this.town = town;
    }
    
    // Current room getters/setters
    public Room getCurrentRoom() {
        return currentRoom;
    }
    
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }
    
    // Player flag
    public boolean isPlayer() {
        return isPlayer;
    }
    
    public void setPlayer(boolean player) {
        isPlayer = player;
    }
    
    // Interaction manager
    
    /**
     * Gets the interaction manager for resolving social interaction conflicts.
     *
     * @return the interaction manager, or null if not set
     */
    public InteractionManager getInteractionManager() {
        return interactionManager;
    }
    
    /**
     * Sets the interaction manager.
     *
     * @param interactionManager the interaction manager
     */
    public void setInteractionManager(InteractionManager interactionManager) {
        this.interactionManager = interactionManager;
    }
    
    // Variable storage methods
    
    /**
     * Sets a variable in the context.
     *
     * @param key the variable name
     * @param value the variable value
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    /**
     * Gets a variable from the context.
     *
     * @param key the variable name
     * @return the variable value, or null if not found
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * Gets a variable with a default value.
     *
     * @param key the variable name
     * @param defaultValue the default value if not found
     * @param <T> the type of the variable
     * @return the variable value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        Object value = variables.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }
    
    /**
     * Checks if a variable exists.
     *
     * @param key the variable name
     * @return true if the variable exists
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
    
    /**
     * Removes a variable from the context.
     *
     * @param key the variable name
     */
    public void removeVariable(String key) {
        variables.remove(key);
    }
    
    /**
     * Clears all variables from the context.
     */
    public void clearVariables() {
        variables.clear();
    }
    
    /**
     * Gets an integer variable.
     *
     * @param key the variable name
     * @param defaultValue the default value
     * @return the integer value
     */
    public int getIntVariable(String key, int defaultValue) {
        Object value = variables.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }
    
    /**
     * Gets a boolean variable.
     *
     * @param key the variable name
     * @param defaultValue the default value
     * @return the boolean value
     */
    public boolean getBoolVariable(String key, boolean defaultValue) {
        Object value = variables.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}
