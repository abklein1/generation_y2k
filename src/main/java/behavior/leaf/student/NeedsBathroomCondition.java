package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.EntityState;
import entity.Student;

/**
 * Condition that checks if the student needs to use the bathroom.
 */
public class NeedsBathroomCondition extends ConditionNode {
    
    public NeedsBathroomCondition() {
        super("NeedsBathroom");
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
        
        return state.needsBathroom();
    }
}
