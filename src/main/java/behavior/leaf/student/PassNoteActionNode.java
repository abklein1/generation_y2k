package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.TargetSelector;
import behavior.leaf.ActionNode;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Student;
import simulation.InteractionManager;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Behavior tree action node for passing a note to another student.
 * Notes can travel across the room so adjacency is not required, but
 * the student strongly prefers to target friends over non-friends.
 *
 * <p>Target selection priority:
 * <ol>
 *   <li>Friends in the same room (strong preference)</li>
 *   <li>Any other student in the room (fallback)</li>
 * </ol>
 * </p>
 */
public class PassNoteActionNode extends ActionNode {
    
    private static final int FRIENDSHIP_GAIN = 5;
    private static final int BOREDOM_DECREASE = 5;
    private static final int BASE_CATCH_CHANCE = 25;
    
    public PassNoteActionNode() {
        super("PassNote", 1);
    }
    
    @Override
    public boolean canExecute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        EntityState state = student.getEntityState();
        if (state == null || !state.isInClass()) {
            return false;
        }
        
        // Need at least one other student in the room or a friend in school
        Room room = state.getCurrentRoom();
        if (room != null && room.getStudents() != null && room.getStudents().size() > 1) {
            return true;
        }
        return !student.studentStatistics.getFriendsInSchool().isEmpty();
    }
    
    @Override
    public BehaviorStatus execute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return BehaviorStatus.FAILURE;
        }
        
        EntityState state = student.getEntityState();
        if (state == null) {
            return BehaviorStatus.FAILURE;
        }
        
        // Select a target using tiered social preference
        Student target = TargetSelector.selectTarget(student, getCandidates(student, state));
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        
        // Register the interaction with the manager for conflict resolution
        InteractionManager manager = context.getInteractionManager();
        if (manager != null) {
            manager.registerInteraction(student, target, ActivityType.PASSING_NOTE);
        }
        
        // Tentatively set activity (may be reverted by the manager during resolution)
        state.setCurrentActivity(ActivityType.PASSING_NOTE);
        
        // Store the target in context for later reference
        context.setVariable("interaction_target", target);
        
        // Drain empathy (social effort) and responsibility (breaking rules)
        student.studentStatistics.drainSecondaryStat("empathy",
                constants.SimConstants.STAT_DRAIN_PASS_NOTE_EMPATHY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
        student.studentStatistics.drainSecondaryStat("responsibility",
                constants.SimConstants.STAT_DRAIN_PASS_NOTE_RESPONSIBILITY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);
        
        // Calculate catch chance
        int agility = student.studentStatistics.getAgility();
        int charisma = student.studentStatistics.getCharisma();
        int catchChance = BASE_CATCH_CHANCE - (agility / 10) - (charisma / 20);
        catchChance = Math.max(5, catchChance);
        
        if (GameRandom.nextDouble(100) < catchChance) {
            context.setVariable("was_caught", true);
            context.setVariable("catch_type", "passing_note");
            // Getting caught is stressful - drain resilience and adaptability
            student.studentStatistics.drainSecondaryStat("resilience",
                    constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
            student.studentStatistics.drainSecondaryStat("adaptability",
                    constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
            return BehaviorStatus.FAILURE; // Failed because caught
        }
        
        // Success - decrease boredom and slight allostatic recovery (socializing is positive)
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.max(0, currentBoredom - BOREDOM_DECREASE));
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);
        
        // Store friendship gain for processing
        context.setVariable("friendship_gained", FRIENDSHIP_GAIN);
        
        return BehaviorStatus.SUCCESS;
    }
    
    private List<Student> getCandidates(Student student, EntityState state) {
        List<Student> candidates = new ArrayList<>();
        Room room = state.getCurrentRoom();
        if (room != null && room.getStudents() != null) {
            for (Student s : room.getStudents()) {
                if (s != null && s != student) {
                    candidates.add(s);
                }
            }
        }
        if (candidates.isEmpty()) {
            for (Student friend : student.studentStatistics.getFriendsInSchool()) {
                if (friend != student) {
                    candidates.add(friend);
                }
            }
        }
        return candidates;
    }
}
