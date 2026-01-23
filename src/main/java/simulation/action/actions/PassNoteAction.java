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
 * Action for passing a note to another student.
 * Builds friendship but has risk of getting caught.
 */
public class PassNoteAction implements Action {
    
    private static final int FRIENDSHIP_GAIN = 5;
    private static final int BASE_CATCH_CHANCE = 25; // 25% base chance
    
    @Override
    public String getName() {
        return "pass_note";
    }
    
    @Override
    public String getDisplayName() {
        return "Pass a Note";
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
        
        // Need a friend nearby (simplified check)
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        return !student.studentStatistics.getFriendsInSchool().isEmpty();
    }
    
    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }
        
        state.setCurrentActivity(ActivityType.PASSING_NOTE);
        
        // Calculate catch chance based on agility and charisma
        int agility = student.studentStatistics.getAgility();
        int charisma = student.studentStatistics.getCharisma();
        int catchChance = BASE_CATCH_CHANCE - (agility / 10) - (charisma / 20);
        catchChance = Math.max(5, catchChance); // Minimum 5% chance
        
        if (GameRandom.nextDouble(100) < catchChance) {
            return ActionResult.caught(
                    "Tried to pass a note but...",
                    "The teacher intercepts the note and reads it aloud!"
            ).withEffect("friendship", -2)
             .withEffect("reputation", -5);
        }
        
        // Decrease boredom from social interaction
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.max(0, currentBoredom - 5));
        
        return ActionResult.success("Successfully passed a note to a friend")
                .withEffect("friendship", FRIENDSHIP_GAIN)
                .withEffect("boredom_change", -5);
    }
    
    @Override
    public ActionCategory getCategory() {
        return ActionCategory.SOCIAL;
    }
    
    @Override
    public double getSuccessProbability(BehaviorContext context) {
        if (context.getStudent() == null) {
            return 0.75;
        }
        int agility = context.getStudent().studentStatistics.getAgility();
        int charisma = context.getStudent().studentStatistics.getCharisma();
        return Math.min(0.95, 0.75 + (agility + charisma - 100) * 0.002);
    }
    
    @Override
    public int getRiskLevel() {
        return 35; // Medium risk
    }
}
