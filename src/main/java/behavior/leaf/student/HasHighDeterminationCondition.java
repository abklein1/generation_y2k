package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.Student;

/**
 * Condition that checks if the student has high determination.
 */
public class HasHighDeterminationCondition extends ConditionNode {
    
    private final int threshold;
    
    /**
     * Creates a determination check with default threshold of 60.
     */
    public HasHighDeterminationCondition() {
        this(60);
    }
    
    /**
     * Creates a determination check with a custom threshold.
     *
     * @param threshold determination level to check against
     */
    public HasHighDeterminationCondition(int threshold) {
        super("HasHighDetermination");
        this.threshold = threshold;
    }
    
    @Override
    public boolean check(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        int determination = student.studentStatistics.getDetermination();
        return determination >= threshold;
    }
}
