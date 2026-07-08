package behavior;

import behavior.composite.Selector;
import behavior.composite.Sequence;
import constants.SimConstants;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Staff;
import entity.Student;
import simulation.ClassroomDisciplineService;
import simulation.DayPhase;
import utility.GameRandom;

import java.util.List;

/**
 * Builds behavior trees for teachers.
 *
 * <p>
 * While supervising a class, a teacher first scans the room for this tick's
 * reported student misbehavior (a detection contest between teacher and
 * student stats, see {@link ClassroomDisciplineService}), then picks a
 * response, in priority order:
 * </p>
 *
 * <ol>
 *   <li><b>Settle the class</b> — the room is LOUD (enough students talking
 *       at once): call the whole class down. Every misbehaving student is
 *       interrupted with a reduced penalty and the room stays calm for a
 *       while.</li>
 *   <li><b>Reprimand an individual</b> — the room is quiet overall but one
 *       student has been noticed talking repeatedly: tell that student to be
 *       quiet (full penalty).</li>
 *   <li><b>Grade papers</b> — the class is behaving; responsible, driven
 *       teachers occasionally use the lull to grade, at the cost of reduced
 *       vigilance next tick.</li>
 *   <li><b>Teach</b> — the default, fully vigilant activity.</li>
 * </ol>
 *
 * <p>
 * Outside of class time (free blocks, lunch, empty room) the teacher grades;
 * outside the school day they idle. Every action a teacher takes benefits
 * from their experience multiplier
 * ({@link utility.TeacherStatistics#getExperienceMultiplier()}).
 * </p>
 */
public class TeacherBehaviorTreeBuilder {

    /**
     * Builds a complete behavior tree for a teacher.
     *
     * @param staff the teacher to build a tree for
     * @return the constructed behavior tree
     */
    public static BehaviorTree buildTree(Staff staff) {
        Selector root = new Selector("TeacherRoot");

        // Priority 1: Supervise the class currently in the room
        root.addChild(buildClassManagementSequence());

        // Priority 2: Free block / lunch — students gone, catch up on grading
        root.addChild(buildFreeBlockGradingSequence());

        // Priority 3: Outside the school day — idle
        root.addChild(new StaffIdleActionNode());

        return new BehaviorTree(staff.toString() + "'s BehaviorTree", root);
    }

    private static BehaviorNode buildClassManagementSequence() {
        Sequence classManagement = new Sequence("ClassManagement");

        classManagement.addChild(new IsSupervisingClassCondition());
        classManagement.addChild(new ScanForMisbehaviorActionNode());

        Selector response = new Selector("TeacherResponse");

        Sequence settle = new Sequence("SettleLoudClass");
        settle.addChild(new IsClassLoudCondition());
        settle.addChild(new SettleClassActionNode());
        response.addChild(settle);

        Sequence reprimand = new Sequence("ReprimandRepeatTalker");
        reprimand.addChild(new HasReprimandTargetCondition());
        reprimand.addChild(new ReprimandStudentActionNode());
        response.addChild(reprimand);

        response.addChild(new GradePapersActionNode());
        response.addChild(new TeachActionNode());

        classManagement.addChild(response);
        return classManagement;
    }

    private static BehaviorNode buildFreeBlockGradingSequence() {
        Sequence freeBlock = new Sequence("FreeBlockGrading");
        freeBlock.addChild(new IsFreeToGradeCondition());
        freeBlock.addChild(new FreeBlockGradeActionNode());
        return freeBlock;
    }

    // ==================== Shared helpers ====================

    private static Room getSupervisedRoom(BehaviorContext context) {
        Staff staff = context.getStaff();
        if (staff == null || staff.getEntityState() == null) {
            return null;
        }
        return staff.getEntityState().getCurrentRoom();
    }

    private static boolean hasStudents(Room room) {
        return room != null && room.getStudents() != null
                && !room.getStudents().isEmpty();
    }

    /**
     * Appends a line to this tick's discipline detail so the staff action
     * log can describe what the teacher actually did.
     */
    private static void appendDisciplineDetail(BehaviorContext context, String detail) {
        if (detail == null || detail.isEmpty()) {
            return;
        }
        String existing = context.getVariable("discipline_detail", "");
        if (existing.isEmpty()) {
            context.setVariable("discipline_detail", detail);
        } else {
            context.setVariable("discipline_detail", existing + "; " + detail);
        }
    }

    // ==================== Conditions ====================

    /**
     * The teacher is supervising a class right now: it is a class period on
     * a school day and there are students in their room.
     */
    private static class IsSupervisingClassCondition extends behavior.leaf.ConditionNode {
        public IsSupervisingClassCondition() {
            super("IsSupervisingClass");
        }

        @Override
        public boolean check(BehaviorContext context) {
            if (context.getDayPhase() != DayPhase.SCHOOL_DAY) {
                return false;
            }
            if (context.getTime() == null || context.getTime().getCurrentPeriod() <= 0) {
                return false;
            }
            return hasStudents(getSupervisedRoom(context));
        }
    }

    /**
     * The room noise has crossed the LOUD threshold — enough students are
     * talking at once that a class-wide response is warranted.
     */
    private static class IsClassLoudCondition extends behavior.leaf.ConditionNode {
        public IsClassLoudCondition() {
            super("IsClassLoud");
        }

        @Override
        public boolean check(BehaviorContext context) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            return discipline != null && discipline.isRoomLoud(getSupervisedRoom(context));
        }
    }

    /**
     * A specific student keeps talking while the rest of the class is
     * mostly quiet: they have crossed the repeat-notice threshold and are
     * still at it.
     */
    private static class HasReprimandTargetCondition extends behavior.leaf.ConditionNode {
        public HasReprimandTargetCondition() {
            super("HasReprimandTarget");
        }

        @Override
        public boolean check(BehaviorContext context) {
            ClassroomDisciplineService discipline = context.getDisciplineService();
            return discipline != null
                    && discipline.findReprimandTarget(getSupervisedRoom(context)) != null;
        }
    }

    /**
     * The teacher has a free block: school is in session but no students are
     * in the room (free period, or the class is at lunch).
     */
    private static class IsFreeToGradeCondition extends behavior.leaf.ConditionNode {
        public IsFreeToGradeCondition() {
            super("IsFreeToGrade");
        }

        @Override
        public boolean check(BehaviorContext context) {
            if (context.getDayPhase() != DayPhase.SCHOOL_DAY) {
                return false;
            }
            Room room = getSupervisedRoom(context);
            return room != null && !hasStudents(room);
        }
    }

    // ==================== Actions ====================

    /**
     * Sweeps the room for this tick's reported misbehavior. Detection is a
     * contest between the teacher's perception/experience and each student's
     * concealment (see {@link ClassroomDisciplineService#scanRoom}). Covert
     * acts caught here get the full individual penalty; noticed talkers are
     * tallied toward a reprimand.
     *
     * <p>Always succeeds so the response selector below it still runs — the
     * scan is the teacher's passive awareness, not their chosen activity.</p>
     */
    private static class ScanForMisbehaviorActionNode extends behavior.leaf.ActionNode {
        public ScanForMisbehaviorActionNode() {
            super("ScanForMisbehavior", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            ClassroomDisciplineService discipline = context.getDisciplineService();
            Room room = getSupervisedRoom(context);
            if (staff != null && discipline != null && room != null) {
                List<String> catches = discipline.scanRoom(staff, room);
                for (String caught : catches) {
                    appendDisciplineDetail(context, caught);
                }
            }
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Tells the entire class to quiet down. Every misbehaving student is
     * interrupted with the reduced "settled" penalty, and the room stays
     * calm for a window scaled by the teacher's authority and experience.
     */
    private static class SettleClassActionNode extends behavior.leaf.ActionNode {
        public SettleClassActionNode() {
            super("SettleClass", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            ClassroomDisciplineService discipline = context.getDisciplineService();
            Room room = getSupervisedRoom(context);
            if (staff == null || staff.getEntityState() == null
                    || discipline == null || room == null) {
                return BehaviorStatus.FAILURE;
            }

            int interrupted = discipline.settleClass(staff, room);
            staff.getEntityState().setCurrentActivity(ActivityType.SETTLING_CLASS);
            appendDisciplineDetail(context,
                    "told the class to settle down (" + interrupted
                            + (interrupted == 1 ? " student" : " students")
                            + " interrupted)");
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Reprimands the repeat talker individually: full caught penalty, with a
     * cooldown extended by the teacher's experience so the reprimand sticks.
     */
    private static class ReprimandStudentActionNode extends behavior.leaf.ActionNode {
        public ReprimandStudentActionNode() {
            super("ReprimandStudent", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            ClassroomDisciplineService discipline = context.getDisciplineService();
            Room room = getSupervisedRoom(context);
            if (staff == null || staff.getEntityState() == null
                    || discipline == null || room == null) {
                return BehaviorStatus.FAILURE;
            }

            Student target = discipline.findReprimandTarget(room);
            if (target == null) {
                return BehaviorStatus.FAILURE;
            }

            discipline.applyReprimand(staff, target);
            staff.getEntityState().setCurrentActivity(ActivityType.REPRIMANDING);
            appendDisciplineDetail(context,
                    "reprimanded " + target + " for repeatedly talking");
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Grades papers while the class is behaving. Responsible, determined
     * teachers grade more often; once started, a grading session lasts
     * several ticks. Grading reduces vigilance next tick (less for
     * experienced teachers, who grade while still watching the room).
     */
    private static class GradePapersActionNode extends behavior.leaf.ActionNode {
        public GradePapersActionNode() {
            super("GradePapers", 1);
        }

        @Override
        public boolean canExecute(BehaviorContext context) {
            Staff staff = context.getStaff();
            if (staff == null || staff.getEntityState() == null) {
                return false;
            }
            EntityState state = staff.getEntityState();

            // Continue an in-progress grading session
            int remaining = context.getIntVariable("grading_ticks_remaining", 0);
            if (state.getCurrentActivity() == ActivityType.GRADING && remaining > 0) {
                return true;
            }

            // Otherwise occasionally start one, weighted by conscientiousness
            int chance = (staff.teacherStatistics.getResponsibility()
                    + staff.teacherStatistics.getDetermination())
                    / SimConstants.GRADING_CHOICE_STAT_DIVISOR;
            if (GameRandom.nextDouble(100) < chance) {
                context.setVariable("grading_ticks_remaining",
                        SimConstants.GRADING_SESSION_TICKS);
                return true;
            }
            return false;
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            if (staff == null || staff.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            staff.getEntityState().setCurrentActivity(ActivityType.GRADING);
            int remaining = context.getIntVariable("grading_ticks_remaining", 0);
            context.setVariable("grading_ticks_remaining", Math.max(0, remaining - 1));
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * The default supervising activity: teaching, fully vigilant.
     */
    private static class TeachActionNode extends behavior.leaf.ActionNode {
        public TeachActionNode() {
            super("Teach", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            if (staff == null || staff.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            staff.getEntityState().setCurrentActivity(ActivityType.TEACHING);
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Grading during a free block / lunch — no students to supervise, so no
     * decision roll is needed.
     */
    private static class FreeBlockGradeActionNode extends behavior.leaf.ActionNode {
        public FreeBlockGradeActionNode() {
            super("FreeBlockGrade", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            if (staff == null || staff.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            staff.getEntityState().setCurrentActivity(ActivityType.GRADING);
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Fallback outside school hours.
     */
    private static class StaffIdleActionNode extends behavior.leaf.ActionNode {
        public StaffIdleActionNode() {
            super("StaffIdle", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Staff staff = context.getStaff();
            if (staff != null && staff.getEntityState() != null) {
                staff.getEntityState().setCurrentActivity(ActivityType.IDLE);
            }
            return BehaviorStatus.SUCCESS;
        }
    }
}
