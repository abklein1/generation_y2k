package simulation.action.actions;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Student;
import simulation.InteractionManager;
import simulation.action.Action;
import simulation.action.ActionCategory;
import simulation.action.ActionResult;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for whispering to another student.
 * Requires the target to be in an adjacent seat (cardinal directions only).
 * Prefers adjacent friends, but can whisper to any adjacent student.
 */
public class WhisperAction implements Action {
    
    private static final int FRIENDSHIP_GAIN = 2;
    private static final int BASE_CATCH_CHANCE = 12; // 12% base chance
    private static final int FRIEND_PREFERENCE_CHANCE = 80;
    
    @Override
    public String getName() {
        return "whisper";
    }
    
    @Override
    public String getDisplayName() {
        return "Whisper";
    }
    
    @Override
    public int getDurationTicks() {
        return 1;
    }
    
    @Override
    public boolean canExecute(EntityState state, BehaviorContext context) {
        if (state == null || !state.isInClass()) {
            return false;
        }
        
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        // Must have at least one adjacent student to whisper to
        List<Student> adjacent = getAdjacentStudents(student, state, context);
        return !adjacent.isEmpty();
    }
    
    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }
        
        // Get adjacent students and select a target (prefer friends)
        List<Student> adjacent = getAdjacentStudents(student, state, context);
        if (adjacent.isEmpty()) {
            return ActionResult.failure("No adjacent students to whisper to");
        }
        
        Student target = selectWhisperTarget(student, adjacent);
        if (target != null) {
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, ActivityType.WHISPERING);
            }
            context.setVariable("interaction_target", target);
        }
        
        state.setCurrentActivity(ActivityType.WHISPERING);
        
        // Drain empathy slightly from social interaction
        student.studentStatistics.drainSecondaryStat("empathy",
                constants.SimConstants.STAT_DRAIN_WHISPER_EMPATHY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
        
        // Only risk being caught if a teacher is present
        if (hasTeacherPresent(state.getCurrentRoom())) {
            int perception = student.studentStatistics.getPerception();
            int catchChance = BASE_CATCH_CHANCE - (perception / 15);
            catchChance = Math.max(3, catchChance);

            if (GameRandom.nextDouble(100) < catchChance) {
                student.studentStatistics.drainSecondaryStat("resilience",
                        constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
                return ActionResult.caught(
                        "Was whispering to a friend when...",
                        "The teacher gives a warning look."
                );
            }
        }
        
        // Small entertainment boost (whispering relieves boredom)
        state.setEntertainment(state.getEntertainment() + 3);
        
        // Socializing is positive - slight allostatic recovery
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);
        
        return ActionResult.success("Shared a quick whisper with a friend")
                .withEffect("friendship", FRIENDSHIP_GAIN)
                .withEffect("entertainment_change", 3);
    }
    
    private static boolean hasTeacherPresent(Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }

    /**
     * Gets students adjacent to the given student in the current seating arrangement.
     */
    private List<Student> getAdjacentStudents(Student student, EntityState state,
                                               BehaviorContext context) {
        Room room = state.getCurrentRoom();
        if (room == null || context.getTime() == null) {
            return List.of();
        }
        
        int period = context.getTime().getCurrentPeriod();
        return room.getAdjacentStudentsFor(student, period);
    }
    
    /**
     * Selects a whisper target from adjacent students, preferring friends.
     */
    private Student selectWhisperTarget(Student student, List<Student> adjacent) {
        ArrayList<Student> friends = student.studentStatistics.getFriendsInSchool();
        
        // Find adjacent friends
        List<Student> adjacentFriends = new ArrayList<>();
        for (Student neighbor : adjacent) {
            if (friends.contains(neighbor)) {
                adjacentFriends.add(neighbor);
            }
        }
        
        // Prefer adjacent friends (80% chance)
        if (!adjacentFriends.isEmpty() && GameRandom.nextInt(100) < FRIEND_PREFERENCE_CHANCE) {
            return adjacentFriends.get(GameRandom.nextInt(adjacentFriends.size()));
        }
        
        // Fall back to any adjacent student
        return adjacent.get(GameRandom.nextInt(adjacent.size()));
    }
    
    @Override
    public ActionCategory getCategory() {
        return ActionCategory.SOCIAL;
    }
    
    @Override
    public double getSuccessProbability(BehaviorContext context) {
        return 0.88;
    }
    
    @Override
    public int getRiskLevel() {
        return 15; // Low risk
    }
}
