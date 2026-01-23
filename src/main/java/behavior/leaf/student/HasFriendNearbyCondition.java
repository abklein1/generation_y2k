package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.Student;

/**
 * Condition that checks if the student has a friend nearby.
 * For now, simplified to check if they have any friends in school.
 */
public class HasFriendNearbyCondition extends ConditionNode {
    
    public HasFriendNearbyCondition() {
        super("HasFriendNearby");
    }
    
    @Override
    public boolean check(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        // Simplified: check if student has friends
        // TODO: Enhance to check seat proximity
        return !student.studentStatistics.getFriendsInSchool().isEmpty();
    }
}
