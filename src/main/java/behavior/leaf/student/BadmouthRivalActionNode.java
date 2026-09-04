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
import static constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY;
import static constants.SimConstants.STAT_DRAIN_BADMOUTH_EMPATHY;
import static constants.SimConstants.STAT_DRAIN_BADMOUTH_RESPONSIBILITY;

/**
 * Jealousy sabotage action: the student corners their crush and talks trash
 * about the crush's partner. Consumes the {@code jealousy_crush} /
 * {@code jealousy_rival} context variables published by
 * {@link HasJealousRivalCondition} in the same sequence.
 *
 * <p>The score outcome (the crush souring on their partner, or a loyal crush
 * snapping back at the badmouther) is resolved by the
 * {@link InteractionManager} when the interaction is confirmed; this node
 * only registers the intent and pays the personal costs. Venting is
 * cathartic but corrosive: it drains empathy and responsibility harder than
 * ordinary chatting.</p>
 *
 * <p>In class this is as loud and risky as talking and is reported to the
 * supervising teacher as such.</p>
 */
public class BadmouthRivalActionNode extends ActionNode {

    private static final int ENTERTAINMENT_BOOST = 4;
    private static final int FRIENDSHIP_GAIN = 1;

    public BadmouthRivalActionNode() {
        super("BadmouthRival", 1);
    }

    @Override
    public boolean canExecute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.getEntityState() == null) {
            return false;
        }
        // Nobody dares start whispering trash right after the teacher
        // settled the class.
        EntityState state = student.getEntityState();
        if (state.isInClass()) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            if (discipline != null && discipline.isRoomCalmed(state.getCurrentRoom())) {
                return false;
            }
        }
        return context.getVariable("jealousy_crush") instanceof Student
                && context.getVariable("jealousy_rival") instanceof Student;
    }

    @Override
    public BehaviorStatus execute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.getEntityState() == null) {
            return BehaviorStatus.FAILURE;
        }
        Object crushVar = context.getVariable("jealousy_crush");
        Object rivalVar = context.getVariable("jealousy_rival");
        if (!(crushVar instanceof Student crush) || !(rivalVar instanceof Student rival)) {
            return BehaviorStatus.FAILURE;
        }

        EntityState state = student.getEntityState();

        // Register the interaction (about the rival) for conflict resolution;
        // the manager applies the asymmetric score outcome on confirmation.
        InteractionManager manager = context.getInteractionManager();
        if (manager != null) {
            manager.registerInteraction(student, crush, ActivityType.BADMOUTHING, rival);
        }

        state.setCurrentActivity(ActivityType.BADMOUTHING);
        context.setVariable("interaction_target", crush);

        // Trash talk is socially effortful and knowingly breaks the "be
        // decent" rules -- it costs more empathy than a normal chat.
        student.studentStatistics.drainSecondaryStat("empathy",
                STAT_DRAIN_BADMOUTH_EMPATHY, ALLOSTATIC_STRESS_FACTOR_EMPATHY);
        student.studentStatistics.drainSecondaryStat("responsibility",
                STAT_DRAIN_BADMOUTH_RESPONSIBILITY, ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);

        // In class this reads as ordinary (loud) talking to the teacher.
        if (state.isInClass() && hasTeacherPresent(state.getCurrentRoom())) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            if (discipline != null) {
                int concealment = student.studentStatistics.getCharisma() / 15
                        + student.studentStatistics.getPerception() / 20;
                discipline.reportMisbehavior(student, state.getCurrentRoom(),
                        ActivityType.TALKING, concealment);
            }
        }

        // Venting about the rival feels good in the moment
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
