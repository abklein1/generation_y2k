package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.leaf.ActionNode;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;

/**
 * Behavior tree action node for paying attention in class.
 */
public class PayAttentionActionNode extends ActionNode {
    
    private static final int LEARNING_GAIN = 5;
    private static final int BOREDOM_INCREASE = 2;
    
    public PayAttentionActionNode() {
        super("PayAttention", 1);
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
        state.setCurrentActivity(ActivityType.ATTENDING_CLASS);
        
        // Apply learning based on intelligence
        int intelligence = student.studentStatistics.getIntelligence();
        int learningBonus = (intelligence - 100) / 10;
        int totalLearning = LEARNING_GAIN + learningBonus;
        
        // Store learning in context for later processing
        context.setVariable("learning_gained", totalLearning);
        
        // Increase boredom slightly
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.min(100, currentBoredom + BOREDOM_INCREASE));
        
        // Drain curiosity from sustained attention
        student.studentStatistics.drainSecondaryStat("curiosity",
                constants.SimConstants.STAT_DRAIN_PAY_ATTENTION_CURIOSITY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_CURIOSITY);
        
        return BehaviorStatus.SUCCESS;
    }
}
