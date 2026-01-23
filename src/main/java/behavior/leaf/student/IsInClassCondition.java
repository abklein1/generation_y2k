package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.EntityState;
import entity.Student;

/**
 * Condition that checks if the student is currently in class.
 */
public class IsInClassCondition extends ConditionNode {
    
    public IsInClassCondition() {
        super("IsInClass");
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
        
        return state.isInClass();
    }
}
