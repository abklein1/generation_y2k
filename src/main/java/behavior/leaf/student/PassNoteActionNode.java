package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.leaf.ActionNode;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import utility.GameRandom;

/**
 * Behavior tree action node for passing a note to a friend.
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
        
        // Need friends to pass notes to
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
        
        // Update activity
        state.setCurrentActivity(ActivityType.PASSING_NOTE);
        
        // Calculate catch chance
        int agility = student.studentStatistics.getAgility();
        int charisma = student.studentStatistics.getCharisma();
        int catchChance = BASE_CATCH_CHANCE - (agility / 10) - (charisma / 20);
        catchChance = Math.max(5, catchChance);
        
        if (GameRandom.nextDouble(100) < catchChance) {
            context.setVariable("was_caught", true);
            context.setVariable("catch_type", "passing_note");
            return BehaviorStatus.FAILURE; // Failed because caught
        }
        
        // Success - decrease boredom
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.max(0, currentBoredom - BOREDOM_DECREASE));
        
        // Store friendship gain for processing
        context.setVariable("friendship_gained", FRIENDSHIP_GAIN);
        
        return BehaviorStatus.SUCCESS;
    }
}
