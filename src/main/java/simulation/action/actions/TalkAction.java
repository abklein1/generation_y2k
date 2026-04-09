package simulation.action.actions;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Student;
import simulation.InteractionManager;
import simulation.action.Action;
import simulation.action.ActionCategory;
import simulation.action.ActionResult;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for talking to another student.
 * Context-aware: risky in class (high catch chance), normal outside class.
 *
 * <p><b>In class:</b> Voice carries across the room so adjacency is not required,
 * but talking is loud and very obvious to the teacher. Higher risk than whispering
 * or passing notes. Drains empathy and responsibility.</p>
 *
 * <p><b>Outside class</b> (hallways, lunchrooms, between periods): Talking is
 * completely normal and expected. No risk of being caught. Provides strong
 * allostatic load recovery and significant entertainment boost. Prefers friends
 * as targets.</p>
 */
public class TalkAction implements Action {
    
    private static final int FRIENDSHIP_GAIN = 3;
    private static final int FRIEND_PREFERENCE_CHANCE = 80;
    
    @Override
    public String getName() {
        return "talk";
    }
    
    @Override
    public String getDisplayName() {
        return "Talk";
    }
    
    @Override
    public int getDurationTicks() {
        return 1;
    }
    
    @Override
    public boolean canExecute(EntityState state, BehaviorContext context) {
        if (state == null) {
            return false;
        }
        
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        // Need someone to talk to
        if (!student.studentStatistics.getFriendsInSchool().isEmpty()) {
            return true;
        }
        Room room = state.getCurrentRoom();
        return room != null && room.getStudents() != null && room.getStudents().size() > 1;
    }
    
    @Override
    public ActionResult execute(EntityState state, BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return ActionResult.failure("No student in context");
        }
        
        boolean inClass = state.isInClass() && hasTeacherPresent(state.getCurrentRoom());
        
        // Select a target (prefer friends)
        Student target = selectTarget(student, state);
        if (target != null) {
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, ActivityType.TALKING);
            }
            context.setVariable("interaction_target", target);
        }
        
        state.setCurrentActivity(ActivityType.TALKING);
        
        if (inClass) {
            return executeInClass(student, state, context);
        } else {
            return executeOutOfClass(student, state, context);
        }
    }
    
    /**
     * Executes the talk action when in class. High risk of being caught.
     */
    private ActionResult executeInClass(Student student, EntityState state, BehaviorContext context) {
        // Drain empathy and responsibility (talking in class breaks rules)
        student.studentStatistics.drainSecondaryStat("empathy",
                constants.SimConstants.STAT_DRAIN_TALK_EMPATHY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
        student.studentStatistics.drainSecondaryStat("responsibility",
                constants.SimConstants.STAT_DRAIN_TALK_RESPONSIBILITY,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);
        
        // High catch chance - talking is loud and obvious
        int charisma = student.studentStatistics.getCharisma();
        int perception = student.studentStatistics.getPerception();
        int catchChance = constants.SimConstants.TALK_IN_CLASS_BASE_CATCH_CHANCE
                - (charisma / 15) - (perception / 20);
        catchChance = Math.max(10, catchChance);
        
        if (GameRandom.nextDouble(100) < catchChance) {
            student.studentStatistics.drainSecondaryStat("resilience",
                    constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
            student.studentStatistics.drainSecondaryStat("adaptability",
                    constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
            return ActionResult.caught(
                    "Was talking to a classmate when...",
                    "The teacher calls them out in front of the class!"
            ).withEffect("friendship", -1)
             .withEffect("reputation", -3);
        }
        
        // Success - entertainment boost and slight recovery
        state.setEntertainment(state.getEntertainment() + 5);
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);
        
        return ActionResult.success("Had a quick chat with a classmate")
                .withEffect("friendship", FRIENDSHIP_GAIN)
                .withEffect("entertainment_change", 5);
    }
    
    /**
     * Executes the talk action outside of class. No risk, restorative.
     */
    private ActionResult executeOutOfClass(Student student, EntityState state, BehaviorContext context) {
        // No risk of being caught - talking is expected behavior
        student.getEntityState().setEntertainment(student.getEntityState().getEntertainment() + 8);
        
        // Strong allostatic recovery - free-time socializing is restorative
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_TALKING);
        
        // Light empathy drain (social energy is still spent, just not stressfully)
        student.studentStatistics.drainSecondaryStat("empathy", 1,
                constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY * 0.3);
        
        return ActionResult.success("Had a nice conversation with a friend")
                .withEffect("friendship", FRIENDSHIP_GAIN)
                .withEffect("entertainment_change", 8);
    }
    
    private static boolean hasTeacherPresent(Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }

    /**
     * Selects a talk target, preferring friends in the same room.
     */
    private Student selectTarget(Student student, EntityState state) {
        ArrayList<Student> friends = student.studentStatistics.getFriendsInSchool();
        Room room = state.getCurrentRoom();
        
        List<Student> classmates = new ArrayList<>();
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
        
        // Prefer friends (80% chance)
        if (!friendsInRoom.isEmpty() && GameRandom.nextInt(100) < FRIEND_PREFERENCE_CHANCE) {
            return friendsInRoom.get(GameRandom.nextInt(friendsInRoom.size()));
        }
        
        if (!friends.isEmpty() && classmates.isEmpty()) {
            return friends.get(GameRandom.nextInt(friends.size()));
        }
        
        if (!classmates.isEmpty()) {
            return classmates.get(GameRandom.nextInt(classmates.size()));
        }
        
        if (!friends.isEmpty()) {
            return friends.get(GameRandom.nextInt(friends.size()));
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
            return 0.60;
        }
        
        EntityState state = student.getEntityState();
        if (state != null && !state.isInClass()) {
            // Outside class, talking always succeeds
            return 1.0;
        }
        
        // In class, success depends on charisma and perception
        int charisma = student.studentStatistics.getCharisma();
        int perception = student.studentStatistics.getPerception();
        return Math.min(0.90, 0.60 + (charisma + perception - 100) * 0.002);
    }
    
    @Override
    public int getRiskLevel() {
        // Reported as high-risk (in class context); the action itself
        // handles the fact that it's zero-risk outside class
        return 50;
    }
}
