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
 * Action for passing a note to another student.
 * Notes can travel across the room so adjacency is not required.
 * Strongly prefers friends as targets over random classmates.
 */
public class PassNoteAction implements Action {
    
    private static final int FRIENDSHIP_GAIN = 5;
    private static final int BASE_CATCH_CHANCE = 25; // 25% base chance
    private static final int FRIEND_PREFERENCE_CHANCE = 80;
    
    @Override
    public String getName() {
        return "pass_note";
    }
    
    @Override
    public String getDisplayName() {
        return "Pass a Note";
    }
    
    @Override
    public int getDurationTicks() {
        return 1;
    }
    
    @Override
    public boolean canExecute(EntityState state, BehaviorContext context) {
        if (state == null || !state.isInClass()) {
            return false;
        }
        
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        
        // Need a friend in school or at least a classmate in the room
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
        
        // Select a target: prefer friends, fall back to any classmate
        Student target = selectTarget(student, state);
        if (target != null) {
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, ActivityType.PASSING_NOTE);
            }
            context.setVariable("interaction_target", target);
        }
        
        state.setCurrentActivity(ActivityType.PASSING_NOTE);
        
        if (hasTeacherPresent(state.getCurrentRoom())) {
            // Drain empathy (social effort) and responsibility (breaking rules)
            student.studentStatistics.drainSecondaryStat("empathy",
                    constants.SimConstants.STAT_DRAIN_PASS_NOTE_EMPATHY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
            student.studentStatistics.drainSecondaryStat("responsibility",
                    constants.SimConstants.STAT_DRAIN_PASS_NOTE_RESPONSIBILITY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);

            // Calculate catch chance based on agility and charisma
            int agility = student.studentStatistics.getAgility();
            int charisma = student.studentStatistics.getCharisma();
            int catchChance = BASE_CATCH_CHANCE - (agility / 10) - (charisma / 20);
            catchChance = Math.max(5, catchChance);

            if (GameRandom.nextDouble(100) < catchChance) {
                student.studentStatistics.drainSecondaryStat("resilience",
                        constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
                student.studentStatistics.drainSecondaryStat("adaptability",
                        constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
                return ActionResult.caught(
                        "Tried to pass a note but...",
                        "The teacher intercepts the note and reads it aloud!"
                ).withEffect("friendship", -2)
                 .withEffect("reputation", -5);
            }
        }
        
        // Decrease boredom from social interaction
        int currentBoredom = student.studentStatistics.getBoredom();
        student.studentStatistics.setBoredom(Math.max(0, currentBoredom - 5));
        
        // Socializing is positive - slight allostatic recovery
        student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);
        
        return ActionResult.success("Successfully passed a note to a friend")
                .withEffect("friendship", FRIENDSHIP_GAIN)
                .withEffect("boredom_change", -5);
    }
    
    private static boolean hasTeacherPresent(Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }

    /**
     * Selects a target for passing the note, preferring friends in the same room.
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
        
        // Find friends who are also in this room
        List<Student> friendsInRoom = new ArrayList<>();
        for (Student friend : friends) {
            if (classmates.contains(friend)) {
                friendsInRoom.add(friend);
            }
        }
        
        // Prefer friends
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
        if (context.getStudent() == null) {
            return 0.75;
        }
        int agility = context.getStudent().studentStatistics.getAgility();
        int charisma = context.getStudent().studentStatistics.getCharisma();
        return Math.min(0.95, 0.75 + (agility + charisma - 100) * 0.002);
    }
    
    @Override
    public int getRiskLevel() {
        return 35; // Medium risk
    }
}
