package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Student;

import java.util.List;

/**
 * Condition that checks if the student has a friend nearby or an adjacent
 * classmate they could interact with.
 *
 * <p>This condition gates social behavior sequences (pass note, whisper).
 * It checks two things:
 * <ol>
 *   <li>Does the student have friends registered in school? (notes can travel)</li>
 *   <li>Is there any adjacent student in the seating chart? (needed for whispering)</li>
 * </ol>
 * Either condition being true is sufficient since the individual action nodes
 * enforce their own stricter requirements (e.g. whisper requires adjacency).
 * </p>
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
        
        // If the student has friends in school, notes can always be passed
        if (!student.studentStatistics.getFriendsInSchool().isEmpty()) {
            return true;
        }
        
        // Otherwise, check if there's anyone adjacent (for whispering to non-friends)
        EntityState state = student.getEntityState();
        if (state == null || state.getCurrentRoom() == null || context.getTime() == null) {
            return false;
        }
        
        Room room = state.getCurrentRoom();
        int period = context.getTime().getCurrentPeriod();
        List<Student> adjacent = room.getAdjacentStudentsFor(student, period);
        
        return !adjacent.isEmpty();
    }
}
