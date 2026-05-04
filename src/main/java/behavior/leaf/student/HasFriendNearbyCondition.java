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

        EntityState state = student.getEntityState();
        if (state == null) {
            return false;
        }

        // During transit, the student can only socialize with co-travelers who
        // are actually walking/riding/driving with them right now.  A friend
        // still at home or already at school is not reachable.
        if (state.isInTransit()) {
            List<Student> group = state.getTransitGroup();
            if (group == null) {
                return false;
            }
            for (Student peer : group) {
                if (peer == null || peer == student) {
                    continue;
                }
                EntityState peerState = peer.getEntityState();
                if (peerState != null && peerState.isInTransit()) {
                    return true;
                }
            }
            return false;
        }

        // On campus the gate is permissive: notes can travel across the room
        // and friends elsewhere in school remain a possibility for the action
        // node to evaluate.  Whispering still also works if there's an
        // adjacent classmate.
        if (!student.studentStatistics.getFriendsInSchool().isEmpty()) {
            return true;
        }

        if (state.getCurrentRoom() == null || context.getTime() == null) {
            return false;
        }
        Room room = state.getCurrentRoom();
        int period = context.getTime().getCurrentPeriod();
        List<Student> adjacent = room.getAdjacentStudentsFor(student, period);
        return !adjacent.isEmpty();
    }
}
