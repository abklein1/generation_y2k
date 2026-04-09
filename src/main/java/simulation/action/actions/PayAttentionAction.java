package simulation.action.actions;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import simulation.action.Action;
import simulation.action.ActionCategory;
import simulation.action.ActionResult;

/**
 * Action for paying attention in class.
 * Increases learning but slowly drains entertainment.
 */
public class PayAttentionAction implements Action {
    
    private static final int LEARNING_GAIN = 5;
    private static final int ENTERTAINMENT_DRAIN = 2;
    
    @Override
    public String getName() {
        return "pay_attention";
    }
    
    @Override
    public String getDisplayName() {
        return "Pay Attention";
    }
    
    @Override
    public int getDurationTicks() {
        return 1;
    }
    
    @Override
    public boolean canExecute(EntityState state, BehaviorContext context) {
        // Must be in class
        return state != null && state.isInClass();
    }
    
    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }
        
        // Update activity
        state.setCurrentActivity(ActivityType.ATTENDING_CLASS);
        
        // Apply effects based on intelligence
        int intelligence = student.studentStatistics.getIntelligence();
        int learningBonus = (intelligence - 100) / 10; // Bonus/penalty based on intelligence
        int totalLearning = LEARNING_GAIN + learningBonus;
        
        // Decrease entertainment (paying attention is tedious)
        state.setEntertainment(state.getEntertainment() - ENTERTAINMENT_DRAIN);
        
        // Drain curiosity from sustained attention
        student.studentStatistics.drainSecondaryStat("curiosity",
                constants.SimConstants.STAT_DRAIN_PAY_ATTENTION_CURIOSITY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_CURIOSITY);
        
        return ActionResult.success("Paying attention to the lesson")
                .withEffect("learning", totalLearning)
                .withEffect("entertainment_change", -ENTERTAINMENT_DRAIN);
    }
    
    @Override
    public ActionCategory getCategory() {
        return ActionCategory.CLASS;
    }
    
    @Override
    public double getSuccessProbability(BehaviorContext context) {
        return 1.0; // Always succeeds
    }
    
    @Override
    public int getRiskLevel() {
        return 0; // No risk
    }
}
