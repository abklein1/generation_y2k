package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.leaf.ActionNode;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import utility.GameRandom;

/**
 * Behavior tree action node for daydreaming in class.
 */
public class DaydreamActionNode extends ActionNode {
    
    private static final int BOREDOM_DECREASE = 8;
    private static final int BASE_CATCH_CHANCE = 15;
    
    public DaydreamActionNode() {
        super("Daydream", 1);
    }
    
    @Override
    public boolean canExecute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        EntityState state = student.getEntityState();
        return state != null && state.isInClass();
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
        
        // Update activity
        state.setCurrentActivity(ActivityType.DAYDREAMING);
        
        // Decrease boredom
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.max(0, currentBoredom - BOREDOM_DECREASE));
        
        // Daydreaming is a non-stressful activity - slight allostatic load recovery
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_DAYDREAMING);
        
        // Only risk being caught if a teacher is present and class is in session
        if (hasTeacherPresent(state.getCurrentRoom())) {
            int perception = student.studentStatistics.getPerception();
            int catchChance = BASE_CATCH_CHANCE - (perception / 10);

            if (GameRandom.nextDouble(100) < catchChance) {
                context.setVariable("was_caught", true);
                context.setVariable("catch_type", "daydreaming");
                student.studentStatistics.drainSecondaryStat("resilience",
                        constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
                student.studentStatistics.drainSecondaryStat("adaptability",
                        constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
            }
        }
        
        return BehaviorStatus.SUCCESS;
    }

    private static boolean hasTeacherPresent(entity.Rooms.Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }
}
