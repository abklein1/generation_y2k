package behavior;

import entity.EntityState;
import entity.Rooms.Room;
import entity.Staff;
import entity.StandardSchool;
import entity.Student;
import entity.Time;
import entity.Town;
import simulation.ClassroomDisciplineService;
import simulation.DayPhase;
import simulation.InteractionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    private ClassroomDisciplineService disciplineService;
    
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

    /**
     * Gets the classroom discipline service. Student misbehavior nodes use
     * it to report visible misbehavior; teacher nodes use it to assess the
     * room and react.
     *
     * @return the discipline service, or null if not set
     */
    public ClassroomDisciplineService getDisciplineService() {
        return disciplineService;
    }

    /**
     * Sets the classroom discipline service.
     *
     * @param disciplineService the discipline service
     */
    public void setDisciplineService(ClassroomDisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }
    
    // Transit group access

    /**
     * Returns the student's transit group (co-travelers during the morning
     * commute).  Returns an empty list if the student has no group or no
     * entity state.
     *
     * @return unmodifiable list of co-travelers (may include the student itself)
     */
    public List<Student> getTransitGroup() {
        if (student == null) {
            return Collections.emptyList();
        }
        EntityState state = student.getEntityState();
        if (state == null || state.getTransitGroup() == null) {
            return Collections.emptyList();
        }
        return state.getTransitGroup();
    }

    /**
     * Checks whether the student is currently commuting to school.
     *
     * @return true if in transit
     */
    public boolean isInTransit() {
        if (student == null) {
            return false;
        }
        EntityState state = student.getEntityState();
        return state != null && state.isInTransit();
    }

    /**
     * Returns the current day phase from the student's entity state.
     *
     * @return the current day phase, or null
     */
    public DayPhase getDayPhase() {
        if (student != null) {
            EntityState state = student.getEntityState();
            if (state != null) {
                return state.getCurrentPhase();
            }
        }
        if (staff != null) {
            EntityState state = staff.getEntityState();
            if (state != null) {
                return state.getCurrentPhase();
            }
        }
        return null;
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
