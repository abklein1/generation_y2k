package simulation.action.actions;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import simulation.action.Action;
import simulation.action.ActionCategory;
import simulation.action.ActionResult;
import utility.GameRandom;

/**
 * Action for daydreaming in class.
 * No learning, but boredom decreases faster.
 * Risk of getting caught by the teacher.
 */
public class DaydreamAction implements Action {
    
    private static final int BOREDOM_DECREASE = 8;
    private static final int BASE_CATCH_CHANCE = 15; // 15% base chance to get caught
    
    @Override
    public String getName() {
        return "daydream";
    }
    
    @Override
    public String getDisplayName() {
        return "Daydream";
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
        state.setCurrentActivity(ActivityType.DAYDREAMING);
        
        // Decrease boredom
        int currentBoredom = student.studentStatistics.getBoredom();
        int newBoredom = Math.max(0, currentBoredom - BOREDOM_DECREASE);
        student.studentStatistics.setBoredom(newBoredom);
        
        // Check if caught
        int perception = student.studentStatistics.getPerception();
        int catchChance = BASE_CATCH_CHANCE - (perception / 10); // Higher perception = less likely caught
        
        if (GameRandom.nextDouble(100) < catchChance) {
            return ActionResult.caught(
                    "Got lost in daydreaming...",
                    "The teacher calls your name to answer a question!"
            ).withEffect("boredom_change", -BOREDOM_DECREASE);
        }
        
        return ActionResult.success("Mind wandering to more interesting things...")
                .withEffect("boredom_change", -BOREDOM_DECREASE)
                .withEffect("learning", 0);
    }
    
    @Override
    public ActionCategory getCategory() {
        return ActionCategory.CLASS;
    }
    
    @Override
    public double getSuccessProbability(BehaviorContext context) {
        if (context.getStudent() == null) {
            return 0.85;
        }
        int perception = context.getStudent().studentStatistics.getPerception();
        return Math.min(0.95, 0.85 + (perception - 50) * 0.002);
    }
    
    @Override
    public int getRiskLevel() {
        return 15; // Low risk
    }
}
