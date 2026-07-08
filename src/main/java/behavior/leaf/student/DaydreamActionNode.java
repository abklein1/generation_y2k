package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.leaf.ActionNode;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import simulation.ClassroomDisciplineService;

/**
 * Behavior tree action node for daydreaming in class.
 */
public class DaydreamActionNode extends ActionNode {
    
    private static final int ENTERTAINMENT_BOOST = 8;
    
    public DaydreamActionNode() {
        super("Daydream", 1);
    }
    
    @Override
    public boolean canExecute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        EntityState state = student.getEntityState();
        return state != null && state.isInClass();
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
        state.setCurrentActivity(ActivityType.DAYDREAMING);
        
        // Boost entertainment (relieves boredom)
        state.setEntertainment(state.getEntertainment() + ENTERTAINMENT_BOOST);
        
        // Daydreaming is a non-stressful activity - slight allostatic load recovery
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_DAYDREAMING);
        
        // Only risk being noticed if a teacher is present and class is in
        // session: report to the supervising teacher, whose stats decide.
        // Perceptive students snap back before the teacher looks their way.
        if (hasTeacherPresent(state.getCurrentRoom())) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            if (discipline != null) {
                int concealment = student.studentStatistics.getPerception() / 10;
                discipline.reportMisbehavior(student, state.getCurrentRoom(),
                        ActivityType.DAYDREAMING, concealment);
            }
        }
        
        return BehaviorStatus.SUCCESS;
    }

    private static boolean hasTeacherPresent(entity.Rooms.Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }
}
