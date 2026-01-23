package simulation;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.BehaviorTree;
import entity.*;
import entity.Rooms.Room;

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
    private int tickIntervalMinutes;
    private int currentTick;
    private final List<SimulationListener> listeners;
    
    // Simulation speed options
    public static final int SPEED_SLOW = 10;      // 10 minutes per tick
    public static final int SPEED_NORMAL = 5;     // 5 minutes per tick  
    public static final int SPEED_FAST = 2;       // 2 minutes per tick
    public static final int SPEED_VERY_FAST = 1;  // 1 minute per tick
    
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
        this.tickIntervalMinutes = SPEED_NORMAL;
        this.currentTick = 0;
        this.listeners = new ArrayList<>();
        this.bellSchedule = new BellScheduleManager();
    }
    
    /**
     * Creates a simulation engine with existing game state.
     *
     * @param time the game time
     * @param school the school
     * @param students the student population
     * @param staff the staff population
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
     * @param time the game time
     * @param school the school
     * @param students the student population
     * @param staff the staff population
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
     * Executes one tick of the simulation.
     */
    public void tick() {
        if (isPaused || time == null) {
            return;
        }
        
        int previousPeriod = bellSchedule.getCurrentPeriod(time);
        boolean wasTransition = bellSchedule.isTransitionTime(time);
        
        // 1. Advance time
        time.stepForwardMinutes(tickIntervalMinutes);
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
        
        // 4. Process NPC behavior trees
        processStudentBehaviors();
        processStaffBehaviors();
        
        // 5. Check for end of day
        if (bellSchedule.isAfterSchool(time)) {
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
     * @param period the current period (1-4)
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
     * @param period the period number (1-4)
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
     * @param period the current period
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
     */
    private void processStudentBehaviors() {
        if (students == null) {
            return;
        }
        
        for (Student student : students.values()) {
            BehaviorTree tree = student.getBehaviorTree();
            if (tree != null) {
                BehaviorContext context = student.getBehaviorContext();
                if (context == null) {
                    context = new BehaviorContext(student, time, school);
                    student.setBehaviorContext(context);
                } else {
                    // Update context with current time
                    context.setTime(time);
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
     * Sets the simulation speed.
     *
     * @param minutesPerTick minutes to advance each tick
     */
    public void setSpeed(int minutesPerTick) {
        this.tickIntervalMinutes = Math.max(1, Math.min(60, minutesPerTick));
    }
    
    /**
     * Gets the current speed.
     *
     * @return minutes per tick
     */
    public int getSpeed() {
        return tickIntervalMinutes;
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
