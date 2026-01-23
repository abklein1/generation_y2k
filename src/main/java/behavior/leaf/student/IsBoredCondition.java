package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.Student;

/**
 * Condition that checks if the student's boredom is above a threshold.
 */
public class IsBoredCondition extends ConditionNode {
    
    private final int threshold;
    
    /**
     * Creates a boredom check with default threshold of 50.
     */
    public IsBoredCondition() {
        this(50);
    }
    
    /**
     * Creates a boredom check with a custom threshold.
     *
     * @param threshold boredom level to check against (0-100)
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
        
        int boredom = student.studentStatistics.getBoredom();
        return boredom >= threshold;
    }
}
