package simulation.action.actions;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import simulation.action.Action;
import simulation.action.ActionCategory;
import simulation.action.ActionResult;

/**
 * Action for taking notes in class.
 * Higher learning if high intelligence, moderate boredom increase.
 */
public class TakeNotesAction implements Action {
    
    private static final int BASE_LEARNING = 7;
    private static final int BOREDOM_INCREASE = 1;
    
    @Override
    public String getName() {
        return "take_notes";
    }
    
    @Override
    public String getDisplayName() {
        return "Take Notes";
    }
    
    @Override
    public int getDurationTicks() {
        return 1;
    }
    
    @Override
    public boolean canExecute(EntityState state, BehaviorContext context) {
        return state != null && state.isInClass();
    }
    
    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }
        
        state.setCurrentActivity(ActivityType.TAKING_NOTES);
        
        // Calculate learning based on intelligence
        int intelligence = student.studentStatistics.getIntelligence();
        int learningMultiplier = 1;
        if (intelligence >= 120) {
            learningMultiplier = 2; // High intelligence gets double learning
        } else if (intelligence >= 100) {
            learningMultiplier = 1;
        }
        
        int totalLearning = BASE_LEARNING * learningMultiplier;
        
        // Boredom increases slightly
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.min(100, currentBoredom + BOREDOM_INCREASE));
        
        return ActionResult.success("Diligently taking notes")
                .withEffect("learning", totalLearning)
                .withEffect("boredom_change", BOREDOM_INCREASE);
    }
    
    @Override
    public ActionCategory getCategory() {
        return ActionCategory.CLASS;
    }
    
    @Override
    public int getRiskLevel() {
        return 0;
    }
}
