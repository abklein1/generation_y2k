package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.leaf.ActionNode;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Student;
import simulation.ClassroomDisciplineService;
import simulation.InteractionManager;

import static constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING;
import static constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY;
import static constants.SimConstants.STAT_DRAIN_IMPRESS_EMPATHY;

/**
 * Jealousy pursuit action: rather than tearing the rival down, the student
 * tries to outshine them by showing off for their crush. Consumes the
 * {@code jealousy_crush} context variable published by
 * {@link HasJealousRivalCondition} in the same sequence.
 *
 * <p>The score outcome (the crush warming to the initiator by a
 * charisma-scaled amount, or an embarrassing flop) is resolved by the
 * {@link InteractionManager} on confirmation. Raising the crush's opinion is
 * exactly what the existing crush-escalation pulse checks, so persistent
 * attention-seeking genuinely improves the student's odds.</p>
 *
 * <p>Showing off is meant to be seen, which in class makes it as risky as
 * talking; it is reported to the supervising teacher as such.</p>
 */
public class GetCrushAttentionActionNode extends ActionNode {

    private static final int ENTERTAINMENT_BOOST = 5;
    private static final int FRIENDSHIP_GAIN = 3;

    public GetCrushAttentionActionNode() {
        super("GetCrushAttention", 1);
    }

    @Override
    public boolean canExecute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.getEntityState() == null) {
            return false;
        }
        // Nobody dares clown around right after the teacher settled the class.
        EntityState state = student.getEntityState();
        if (state.isInClass()) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            if (discipline != null && discipline.isRoomCalmed(state.getCurrentRoom())) {
                return false;
            }
        }
        return context.getVariable("jealousy_crush") instanceof Student;
    }

    @Override
    public BehaviorStatus execute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.getEntityState() == null) {
            return BehaviorStatus.FAILURE;
        }
        Object crushVar = context.getVariable("jealousy_crush");
        if (!(crushVar instanceof Student crush)) {
            return BehaviorStatus.FAILURE;
        }

        EntityState state = student.getEntityState();

        // Register the interaction for conflict resolution; the manager
        // applies the charisma-scaled warmth (or the flop) on confirmation.
        InteractionManager manager = context.getInteractionManager();
        if (manager != null) {
            manager.registerInteraction(student, crush, ActivityType.IMPRESSING);
        }

        state.setCurrentActivity(ActivityType.IMPRESSING);
        context.setVariable("interaction_target", crush);

        // Performing for someone takes social energy, but it's honest effort
        student.studentStatistics.drainSecondaryStat("empathy",
                STAT_DRAIN_IMPRESS_EMPATHY, ALLOSTATIC_STRESS_FACTOR_EMPATHY);

        // Showing off is loud by design: in class it reads as talking.
        if (state.isInClass() && hasTeacherPresent(state.getCurrentRoom())) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            if (discipline != null) {
                int concealment = student.studentStatistics.getCharisma() / 15
                        + student.studentStatistics.getPerception() / 20;
                discipline.reportMisbehavior(student, state.getCurrentRoom(),
                        ActivityType.TALKING, concealment);
            }
        }

        // Flirting-adjacent excitement: strong entertainment boost
        state.setEntertainment(state.getEntertainment() + ENTERTAINMENT_BOOST);
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);

        context.setVariable("friendship_gained", FRIENDSHIP_GAIN);
        return BehaviorStatus.SUCCESS;
    }

    private static boolean hasTeacherPresent(Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }
}
