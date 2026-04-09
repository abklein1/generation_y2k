package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.EntityState;
import entity.Student;

/**
 * Condition that checks if the student's entertainment has dropped below a threshold
 * (i.e. the student is bored).
 */
public class IsBoredCondition extends ConditionNode {
    
    private final int threshold;
    
    /**
     * Creates a boredom check with default threshold of 50.
     * Returns true when entertainment is below this value.
     */
    public IsBoredCondition() {
        this(50);
    }
    
    /**
     * Creates a boredom check with a custom threshold.
     *
     * @param threshold entertainment level below which the student is considered bored
     */
    public IsBoredCondition(int threshold) {
        super("IsBored");
        this.threshold = threshold;
    }
    
    @Override
    public boolean check(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        EntityState state = student.getEntityState();
        if (state == null) {
            return false;
        }
        return state.getEntertainment() < threshold;
    }
}
