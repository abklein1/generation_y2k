package simulation.action.actions;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.CellPhone;
import entity.EntityState;
import entity.Student;
import entity.Town;
import simulation.InteractionManager;
import simulation.action.Action;
import simulation.action.ActionCategory;
import simulation.action.ActionResult;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for texting another student on a cell phone.
 * Lower social benefit than talking but generally lower catch chance.
 * Catch chance varies with phone hardware: smaller phones are easier to hide
 * and phones with physical keyboards allow faster (less exposed) texting.
 *
 * <p><b>In class:</b> Requires SMS-capable phone. Catch chance starts at a
 * moderate base, adjusted by phone size and keyboard, then reduced by
 * perception. Lower stat drains and boredom relief than talking.</p>
 *
 * <p><b>Outside class:</b> No risk. Modest boredom relief and allostatic
 * recovery.</p>
 */
public class TextAction implements Action {

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public String getDisplayName() {
        return "Text";
    }

    @Override
    public int getDurationTicks() {
        return 1;
    }

    @Override
    public boolean canExecute(EntityState state, BehaviorContext context) {
        if (state == null || context == null) {
            return false;
        }
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        Town town = context.getTown();
        if (town == null || !town.hasPhone(student)) {
            return false;
        }
        CellPhone phone = town.getStudentPhone(student);
        return phone != null && phone.hasSms() && phone.getTextsRemaining() > 0;
    }

    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }

        Town town = context.getTown();
        if (town == null || !town.hasPhone(student)) {
            return ActionResult.failure("Student has no phone");
        }
        CellPhone phone = town.getStudentPhone(student);
        if (phone == null || !phone.hasSms()) {
            return ActionResult.failure("Phone does not support SMS");
        }
        if (!phone.useText()) {
            return ActionResult.failure("Monthly text limit reached");
        }

        Student target = selectTarget(student, state);
        if (target != null) {
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, ActivityType.TEXTING);
            }
            context.setVariable("interaction_target", target);
        }

        state.setCurrentActivity(ActivityType.TEXTING);

        boolean inClass = state.isInClass() && hasTeacherPresent(state.getCurrentRoom());
        if (inClass) {
            return executeInClass(student, phone, context);
        } else {
            return executeOutOfClass(student);
        }
    }

    private ActionResult executeInClass(Student student, CellPhone phone, BehaviorContext context) {
        student.studentStatistics.drainSecondaryStat("empathy",
                constants.SimConstants.STAT_DRAIN_TEXT_EMPATHY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
        student.studentStatistics.drainSecondaryStat("responsibility",
                constants.SimConstants.STAT_DRAIN_TEXT_RESPONSIBILITY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);

        int catchChance = computeCatchChance(phone, student);

        if (GameRandom.nextDouble(100) < catchChance) {
            student.studentStatistics.drainSecondaryStat("resilience",
                    constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
            student.studentStatistics.drainSecondaryStat("adaptability",
                    constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
            return ActionResult.caught(
                    "Was texting under the desk when...",
                    "The teacher spots the phone and confiscates it!"
            ).withEffect("friendship", -1);
        }

        int boredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(
                Math.max(0, boredom - constants.SimConstants.TEXT_BOREDOM_DECREASE_IN_CLASS));
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_TEXTING);

        return ActionResult.success("Sent a quick text to a friend")
                .withEffect("friendship", 2)
                .withEffect("boredom_change", -constants.SimConstants.TEXT_BOREDOM_DECREASE_IN_CLASS);
    }

    private ActionResult executeOutOfClass(Student student) {
        int boredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(
                Math.max(0, boredom - constants.SimConstants.TEXT_BOREDOM_DECREASE_OUT_OF_CLASS));
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_TEXTING);
        student.studentStatistics.drainSecondaryStat("empathy", 1,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY * 0.3);

        return ActionResult.success("Texted a friend between classes")
                .withEffect("friendship", 2)
                .withEffect("boredom_change", -constants.SimConstants.TEXT_BOREDOM_DECREASE_OUT_OF_CLASS);
    }

    private int computeCatchChance(CellPhone phone, Student student) {
        int catchChance = constants.SimConstants.TEXT_IN_CLASS_BASE_CATCH_CHANCE;

        String size = phone.getSize();
        if (size != null) {
            switch (size.toLowerCase()) {
                case "small" -> catchChance += constants.SimConstants.TEXT_PHONE_SIZE_MODIFIER_SMALL;
                case "large" -> catchChance += constants.SimConstants.TEXT_PHONE_SIZE_MODIFIER_LARGE;
                default -> catchChance += constants.SimConstants.TEXT_PHONE_SIZE_MODIFIER_MEDIUM;
            }
        }

        if (phone.hasKeyboard()) {
            catchChance += constants.SimConstants.TEXT_KEYBOARD_SPEED_MODIFIER;
        }

        catchChance -= student.studentStatistics.getPerception() / 20;
        return Math.max(5, catchChance);
    }

    private static boolean hasTeacherPresent(entity.Rooms.Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }

    private Student selectTarget(Student student, EntityState state) {
        ArrayList<Student> friends = student.studentStatistics.getFriendsInSchool();
        List<Student> classmates = new ArrayList<>();
        entity.Rooms.Room room = state.getCurrentRoom();
        if (room != null && room.getStudents() != null) {
            for (Student s : room.getStudents()) {
                if (s != null && s != student) {
                    classmates.add(s);
                }
            }
        }

        List<Student> friendsInRoom = new ArrayList<>();
        for (Student friend : friends) {
            if (classmates.contains(friend)) {
                friendsInRoom.add(friend);
            }
        }

        if (!friendsInRoom.isEmpty()) {
            return friendsInRoom.get(GameRandom.nextInt(friendsInRoom.size()));
        }
        if (!friends.isEmpty()) {
            return friends.get(GameRandom.nextInt(friends.size()));
        }
        if (!classmates.isEmpty()) {
            return classmates.get(GameRandom.nextInt(classmates.size()));
        }
        return null;
    }

    @Override
    public ActionCategory getCategory() {
        return ActionCategory.SOCIAL;
    }

    @Override
    public double getSuccessProbability(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return 0.80;
        }
        EntityState state = student.getEntityState();
        if (state != null && !state.isInClass()) {
            return 1.0;
        }
        int perception = student.studentStatistics.getPerception();
        return Math.min(0.95, 0.80 + (perception - 50) * 0.002);
    }

    @Override
    public int getRiskLevel() {
        return 25;
    }
}
