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
 * Action for whispering to another student.
 * Lower risk than passing notes, smaller social gain.
 */
public class WhisperAction implements Action {
    
    private static final int FRIENDSHIP_GAIN = 2;
    private static final int BASE_CATCH_CHANCE = 12; // 12% base chance
    
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
        return student != null && !student.studentStatistics.getFriendsInSchool().isEmpty();
    }
    
    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }
        
        state.setCurrentActivity(ActivityType.WHISPERING);
        
        // Calculate catch chance
        int perception = student.studentStatistics.getPerception();
        int catchChance = BASE_CATCH_CHANCE - (perception / 15);
        catchChance = Math.max(3, catchChance);
        
        if (GameRandom.nextDouble(100) < catchChance) {
            return ActionResult.caught(
                    "Was whispering to a friend when...",
                    "The teacher gives a warning look."
            );
        }
        
        // Small boredom decrease
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.max(0, currentBoredom - 3));
        
        return ActionResult.success("Shared a quick whisper with a friend")
                .withEffect("friendship", FRIENDSHIP_GAIN)
                .withEffect("boredom_change", -3);
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
