package behavior;

import behavior.composite.Selector;
import behavior.composite.Sequence;
import behavior.composite.RandomSelector;
import behavior.leaf.student.*;
import entity.CellPhone;
import entity.Student;
import entity.Town;
import simulation.InteractionManager;
import utility.GameRandom;

import java.util.ArrayList;

/**
 * Builds behavior trees for students based on their stats.
 * Different personality types get different behavior tree structures.
 */
public class StudentBehaviorTreeBuilder {
    
    /**
     * Builds a complete behavior tree for a student.
     * The tree structure is influenced by the student's stats.
     *
     * @param student the student to build a tree for
     * @return the constructed behavior tree
     */
    public static BehaviorTree buildTree(Student student) {
        // Determine student personality type based on stats
        PersonalityType type = determinePersonalityType(student);
        
        BehaviorNode root = buildRootSelector(student, type);
        return new BehaviorTree(student.toString() + "'s BehaviorTree", root);
    }
    
    /**
     * Determines the student's personality type based on their stats.
     */
    private static PersonalityType determinePersonalityType(Student student) {
        int determination = student.studentStatistics.getDetermination();
        int charisma = student.studentStatistics.getCharisma();
        int intelligence = student.studentStatistics.getIntelligence();
        int curiosity = student.studentStatistics.getCuriosity();
        
        // Studious: High determination and intelligence
        if (determination >= 60 && intelligence >= 110) {
            return PersonalityType.STUDIOUS;
        }
        
        // Social: High charisma, lower determination
        if (charisma >= 60 && determination < 50) {
            return PersonalityType.SOCIAL;
        }
        
        // Daydreamer: High curiosity, lower determination
        if (curiosity >= 60 && determination < 45) {
            return PersonalityType.DAYDREAMER;
        }
        
        // Troublemaker: Low determination, low responsibility
        int responsibility = student.studentStatistics.getResponsibility();
        if (determination < 35 && responsibility < 40) {
            return PersonalityType.TROUBLEMAKER;
        }
        
        // Default: Average student
        return PersonalityType.AVERAGE;
    }
    
    /**
     * Builds the root selector node for the behavior tree.
     */
    private static BehaviorNode buildRootSelector(Student student, PersonalityType type) {
        Selector root = new Selector("Root");
        
        // Priority 1: Handle urgent needs (bathroom, etc.)
        root.addChild(buildUrgentNeedsSequence());
        
        // Priority 2: Class behavior (when in class)
        root.addChild(buildClassBehaviorSequence(student, type));
        
        // Priority 3: Non-class behavior (hallways, lunchroom, before/after school)
        root.addChild(buildOutOfClassBehavior(type));
        
        // Priority 4: Default idle action
        root.addChild(new IdleActionNode());
        
        return root;
    }
    
    /**
     * Builds the sequence for handling urgent needs.
     */
    private static BehaviorNode buildUrgentNeedsSequence() {
        Sequence urgentNeeds = new Sequence("HandleUrgentNeeds");
        
        urgentNeeds.addChild(new NeedsBathroomCondition());
        urgentNeeds.addChild(new AskToLeaveActionNode());
        urgentNeeds.addChild(new GoToBathroomActionNode());
        
        return urgentNeeds;
    }
    
    /**
     * Builds the class behavior sequence based on personality type.
     */
    private static BehaviorNode buildClassBehaviorSequence(Student student, PersonalityType type) {
        Sequence classBehavior = new Sequence("ClassBehavior");
        
        // First, check if in class
        classBehavior.addChild(new IsInClassCondition());
        
        // Then, select class activity based on personality
        classBehavior.addChild(buildClassActivitySelector(student, type));
        
        return classBehavior;
    }
    
    /**
     * Builds the activity selector for class time.
     */
    private static BehaviorNode buildClassActivitySelector(Student student, PersonalityType type) {
        Selector activitySelector = new Selector("ClassActivities");
        
        switch (type) {
            case STUDIOUS:
                activitySelector.addChild(buildStudiousSequence());
                activitySelector.addChild(buildEngagedSequence(student));
                activitySelector.addChild(buildBoredSequence());
                break;
                
            case SOCIAL:
                activitySelector.addChild(buildSocialSequence());
                activitySelector.addChild(buildDistractedSequence());
                activitySelector.addChild(buildEngagedSequence(student));
                break;
                
            case DAYDREAMER:
                activitySelector.addChild(buildBoredSequence());
                activitySelector.addChild(buildDistractedSequence());
                activitySelector.addChild(buildEngagedSequence(student));
                break;
                
            case TROUBLEMAKER:
                activitySelector.addChild(buildDistractedSequence());
                activitySelector.addChild(buildSocialSequence());
                activitySelector.addChild(buildBoredSequence());
                break;
                
            case AVERAGE:
            default:
                // Average students use random selection weighted by stats
                activitySelector.addChild(buildEngagedSequence(student));
                activitySelector.addChild(buildDistractedSequence());
                activitySelector.addChild(buildBoredSequence());
                break;
        }
        
        return activitySelector;
    }
    
    /**
     * Builds a sequence for engaged/studious behavior.
     */
    private static BehaviorNode buildStudiousSequence() {
        Sequence studious = new Sequence("StudiousBehavior");
        studious.addChild(new HasHighDeterminationCondition(50));
        studious.addChild(new TakeNotesActionNode());
        return studious;
    }
    
    /**
     * Builds a sequence for engaged behavior.
     */
    private static BehaviorNode buildEngagedSequence(Student student) {
        Sequence engaged = new Sequence("EngagedBehavior");
        
        // High determination students pay attention
        int determinationThreshold = 60 - (student.studentStatistics.getIntelligence() - 100) / 5;
        engaged.addChild(new HasHighDeterminationCondition(determinationThreshold));
        engaged.addChild(new PayAttentionActionNode());
        
        return engaged;
    }
    
    /**
     * Builds a sequence for distracted/social behavior (in class).
     * Includes whispering (adjacent), passing notes (room-wide), and talking (room-wide, risky).
     */
    private static BehaviorNode buildDistractedSequence() {
        Sequence distracted = new Sequence("DistractedBehavior");
        distracted.addChild(new HasFriendNearbyCondition());
        
        // Random choice between passing note, whispering, talking, and texting
        RandomSelector socialChoice = new RandomSelector("SocialChoice");
        socialChoice.addChild(new PassNoteActionNode());
        socialChoice.addChild(new WhisperActionNode());
        socialChoice.addChild(new TalkActionNode());
        socialChoice.addChild(new TextActionNode());
        
        distracted.addChild(socialChoice);
        return distracted;
    }
    
    /**
     * Builds a sequence for social behavior (in class).
     * Social personality types prefer talking and passing notes over quieter options.
     */
    private static BehaviorNode buildSocialSequence() {
        Sequence social = new Sequence("SocialBehavior");
        social.addChild(new HasFriendNearbyCondition());
        
        // Social types get a random choice weighted toward louder actions
        RandomSelector socialChoice = new RandomSelector("SocialTypeChoice");
        socialChoice.addChild(new TalkActionNode());
        socialChoice.addChild(new PassNoteActionNode());
        socialChoice.addChild(new TextActionNode());
        
        social.addChild(socialChoice);
        return social;
    }
    
    /**
     * Builds a sequence for bored behavior.
     */
    private static BehaviorNode buildBoredSequence() {
        Sequence bored = new Sequence("BoredBehavior");
        bored.addChild(new IsBoredCondition(50));
        bored.addChild(new DaydreamActionNode());
        return bored;
    }
    
    /**
     * Builds the out-of-class behavior selector.
     * Covers hallways, lunchrooms, before/after school, and transitions.
     * Students can socialize (if friends/peers are available), hang out
     * at their locker, daydream, or just stand around.
     */
    private static BehaviorNode buildOutOfClassBehavior(PersonalityType type) {
        Sequence outOfClass = new Sequence("OutOfClassBehavior");

        // Gate: only when NOT in class
        outOfClass.addChild(new IsNotInClassCondition());

        Selector activities = new Selector("OutOfClassActivities");

        // Option 1: Socialize (requires a friend or peer nearby)
        Sequence socialize = new Sequence("OutOfClassSocialize");
        socialize.addChild(new HasFriendNearbyCondition());
        RandomSelector socialChoice = new RandomSelector("OutOfClassSocialChoice");
        socialChoice.addChild(new TalkActionNode());
        socialChoice.addChild(new TextActionNode());
        socialize.addChild(socialChoice);
        activities.addChild(socialize);

        // Option 2: Hang out at locker (personality-influenced)
        activities.addChild(new HangOutAtLockerActionNode());

        // Option 3: Daydream / zone out (daydreamers and troublemakers prefer this)
        activities.addChild(new OutOfClassDaydreamActionNode());

        // Option 4: Just stand around (always succeeds -- soft fallback)
        activities.addChild(new StandAroundActionNode());

        outOfClass.addChild(activities);
        return outOfClass;
    }
    
    /**
     * Personality types that influence behavior tree structure.
     */
    private enum PersonalityType {
        STUDIOUS,      // High determination, pays attention
        SOCIAL,        // High charisma, tends to socialize
        DAYDREAMER,    // High curiosity, tends to zone out
        TROUBLEMAKER,  // Low determination, seeks disruption
        AVERAGE        // Balanced, context-dependent behavior
    }
    
    /**
     * Simple action node for asking to leave class.
     */
    private static class AskToLeaveActionNode extends behavior.leaf.ActionNode {
        public AskToLeaveActionNode() {
            super("AskToLeave", 1);
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            // For now, always grant permission
            student.getEntityState().setHasPermissionToLeave(true);
            return BehaviorStatus.SUCCESS;
        }
    }
    
    /**
     * Simple action node for going to bathroom.
     */
    private static class GoToBathroomActionNode extends behavior.leaf.ActionNode {
        public GoToBathroomActionNode() {
            super("GoToBathroom", 2); // Takes 2 ticks
        }
        
        @Override
        public boolean canExecute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return false;
            }
            return student.getEntityState().hasPermissionToLeave();
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            entity.EntityState state = student.getEntityState();
            
            if (!isComplete()) {
                state.setCurrentActivity(entity.ActivityType.IN_BATHROOM);
                return BehaviorStatus.RUNNING;
            }
            
            // Done — bladder relieved, clear flags
            state.setBladder(100);
            state.setNeedsBathroom(false);
            state.setHasPermissionToLeave(false);
            state.setCurrentActivity(entity.ActivityType.IDLE);
            return BehaviorStatus.SUCCESS;
        }
    }
    
    /**
     * Condition that checks if the student is NOT currently in class.
     * Used to gate non-class behaviors like free-time socializing.
     */
    private static class IsNotInClassCondition extends behavior.leaf.ConditionNode {
        public IsNotInClassCondition() {
            super("IsNotInClass");
        }
        
        @Override
        public boolean check(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return false;
            }
            return !student.getEntityState().isInClass();
        }
    }
    
    /**
     * Talk action node: context-aware social action.
     * <ul>
     *   <li><b>In class:</b> Can reach anyone in the room (voice carries).
     *       High risk of being caught (louder than whispering or passing notes).
     *       Drains empathy and responsibility. Prefers friends.</li>
     *   <li><b>Outside class</b> (hallways, lunchrooms): Normal expected behavior.
     *       No risk of being caught. Provides strong allostatic recovery and
     *       significant entertainment boost. Prefers friends.</li>
     * </ul>
     */
    private static class TalkActionNode extends behavior.leaf.ActionNode {
        private static final int FRIENDSHIP_GAIN = 3;
        private static final int ENTERTAINMENT_BOOST_IN_CLASS = 5;
        private static final int ENTERTAINMENT_BOOST_OUT_OF_CLASS = 8;
        
        public TalkActionNode() {
            super("Talk", 1);
        }
        
        @Override
        public boolean canExecute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return false;
            }
            
            // Need someone to talk to: friends or classmates in the room
            if (!student.studentStatistics.getFriendsInSchool().isEmpty()) {
                return true;
            }
            entity.Rooms.Room room = student.getEntityState().getCurrentRoom();
            return room != null && room.getStudents() != null && room.getStudents().size() > 1;
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            entity.EntityState state = student.getEntityState();
            boolean inClass = state.isInClass() && hasTeacherPresent(state.getCurrentRoom());
            
            // Select a target using tiered social preference
            Student target = TargetSelector.selectTarget(student, getCandidates(student, state));
            if (target == null) {
                return BehaviorStatus.FAILURE;
            }
            
            // Register the interaction for conflict resolution
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, entity.ActivityType.TALKING);
            }
            
            state.setCurrentActivity(entity.ActivityType.TALKING);
            context.setVariable("interaction_target", target);
            
            if (inClass) {
                // --- IN CLASS: risky behavior ---
                // Drain empathy (social effort) and responsibility (breaking class rules)
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
                    context.setVariable("was_caught", true);
                    context.setVariable("catch_type", "talking");
                    student.studentStatistics.drainSecondaryStat("resilience",
                            constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                            constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
                    student.studentStatistics.drainSecondaryStat("adaptability",
                            constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                            constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
                    // Treat being caught as a completed outcome so the selector
                    // does not immediately replace this with another action.
                    return BehaviorStatus.SUCCESS;
                }
                
                // Success in class - entertainment boost and slight recovery
                state.setEntertainment(state.getEntertainment() + ENTERTAINMENT_BOOST_IN_CLASS);
                student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                        constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);
            } else {
                // --- OUTSIDE CLASS: normal, expected behavior ---
                // No risk of being caught. Socializing is relaxing and restorative.
                state.setEntertainment(state.getEntertainment() + ENTERTAINMENT_BOOST_OUT_OF_CLASS);
                
                // Stronger allostatic recovery since this is free-time socializing
                student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                        constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_TALKING);
                
                // Light empathy drain (social energy is still spent, just not stressfully)
                student.studentStatistics.drainSecondaryStat("empathy", 1,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY * 0.3);
            }
            
            context.setVariable("friendship_gained", FRIENDSHIP_GAIN);
            return BehaviorStatus.SUCCESS;
        }
        
        private java.util.List<Student> getCandidates(Student student, entity.EntityState state) {
            java.util.List<Student> candidates = new ArrayList<>();
            entity.Rooms.Room room = state.getCurrentRoom();
            if (room != null && room.getStudents() != null) {
                for (Student s : room.getStudents()) {
                    if (s != null && s != student) {
                        candidates.add(s);
                    }
                }
            }
            if (candidates.isEmpty()) {
                for (Student friend : student.studentStatistics.getFriendsInSchool()) {
                    if (friend != student) {
                        candidates.add(friend);
                    }
                }
            }
            return candidates;
        }
    }
    
    /**
     * Simple idle action node.
     */
    private static class IdleActionNode extends behavior.leaf.ActionNode {
        public IdleActionNode() {
            super("Idle", 1);
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student != null && student.getEntityState() != null) {
                student.getEntityState().setCurrentActivity(entity.ActivityType.IDLE);
            }
            return BehaviorStatus.SUCCESS;
        }
    }
    
    /**
     * Whisper action node that requires the target to be in an adjacent seat.
     * Prefers adjacent friends, but can whisper to any adjacent student.
     */
    private static class WhisperActionNode extends behavior.leaf.ActionNode {
        public WhisperActionNode() {
            super("Whisper", 1);
        }
        
        @Override
        public boolean canExecute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return false;
            }
            
            // Must have at least one adjacent student to whisper to
            java.util.List<Student> adjacent = getAdjacentStudents(student, context);
            return !adjacent.isEmpty();
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            // Get adjacent students and select a target (prefer friends)
            java.util.List<Student> adjacent = getAdjacentStudents(student, context);
            if (adjacent.isEmpty()) {
                return BehaviorStatus.FAILURE;
            }
            
            Student target = TargetSelector.selectTarget(student, adjacent);
            if (target == null) {
                return BehaviorStatus.FAILURE;
            }
            
            // Register the interaction with the manager for conflict resolution
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, entity.ActivityType.WHISPERING);
            }
            
            // Tentatively set activity (may be reverted during resolution)
            student.getEntityState().setCurrentActivity(entity.ActivityType.WHISPERING);
            
            // Store target in context
            context.setVariable("interaction_target", target);
            
            // Drain empathy slightly from social interaction
            student.studentStatistics.drainSecondaryStat("empathy",
                    constants.SimConstants.STAT_DRAIN_WHISPER_EMPATHY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
            
            // Boost entertainment (whispering relieves boredom)
            student.getEntityState().setEntertainment(student.getEntityState().getEntertainment() + 3);
            
            // Socializing is positive - slight allostatic recovery
            student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                    constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_SOCIALIZING);
            
            return BehaviorStatus.SUCCESS;
        }
        
        /**
         * Gets students adjacent to the given student in the current seating arrangement.
         */
        private java.util.List<Student> getAdjacentStudents(Student student, BehaviorContext context) {
            entity.EntityState state = student.getEntityState();
            entity.Rooms.Room room = state.getCurrentRoom();
            
            if (room == null || context.getTime() == null) {
                return java.util.Collections.emptyList();
            }
            
            int period = context.getTime().getCurrentPeriod();
            return room.getAdjacentStudentsFor(student, period);
        }
        
    }
    
    /**
     * Text action node: sends a text message to another student via cell phone.
     * Lower benefit than talking but generally lower catch chance.
     * Catch chance depends on phone size (smaller = easier to hide) and whether
     * the phone has a physical keyboard (faster texting = less time exposed).
     * Requires the student to own a phone with SMS capability.
     */
    private static class TextActionNode extends behavior.leaf.ActionNode {
        
        public TextActionNode() {
            super("Text", 1);
        }
        
        @Override
        public boolean canExecute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
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
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }

            Town town = context.getTown();
            if (town == null || !town.hasPhone(student)) {
                return BehaviorStatus.FAILURE;
            }
            CellPhone phone = town.getStudentPhone(student);
            if (phone == null || !phone.hasSms()) {
                return BehaviorStatus.FAILURE;
            }
            if (!phone.useText()) {
                return BehaviorStatus.FAILURE;
            }

            entity.EntityState state = student.getEntityState();
            boolean inClass = state.isInClass() && hasTeacherPresent(state.getCurrentRoom());
            
            // Select a target using the tiered preference system
            Student target = TargetSelector.selectTarget(student, getClassmates(student, state));
            if (target == null) {
                return BehaviorStatus.FAILURE;
            }
            
            InteractionManager manager = context.getInteractionManager();
            if (manager != null) {
                manager.registerInteraction(student, target, entity.ActivityType.TEXTING);
            }
            
            state.setCurrentActivity(entity.ActivityType.TEXTING);
            context.setVariable("interaction_target", target);
            
            if (inClass) {
                student.studentStatistics.drainSecondaryStat("empathy",
                        constants.SimConstants.STAT_DRAIN_TEXT_EMPATHY,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY);
                student.studentStatistics.drainSecondaryStat("responsibility",
                        constants.SimConstants.STAT_DRAIN_TEXT_RESPONSIBILITY,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);
                
                int catchChance = computeCatchChance(phone, student);
                
                if (GameRandom.nextDouble(100) < catchChance) {
                    context.setVariable("was_caught", true);
                    context.setVariable("catch_type", "texting");
                    student.studentStatistics.drainSecondaryStat("resilience",
                            constants.SimConstants.STAT_DRAIN_CAUGHT_RESILIENCE,
                            constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
                    student.studentStatistics.drainSecondaryStat("adaptability",
                            constants.SimConstants.STAT_DRAIN_CAUGHT_ADAPTABILITY,
                            constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);
                    // Treat being caught as a completed outcome so the selector
                    // does not immediately replace this with another action.
                    return BehaviorStatus.SUCCESS;
                }
                
                state.setEntertainment(state.getEntertainment()
                        + constants.SimConstants.TEXT_ENTERTAINMENT_BOOST_IN_CLASS);
                student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                        constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_TEXTING);
            } else {
                state.setEntertainment(state.getEntertainment()
                        + constants.SimConstants.TEXT_ENTERTAINMENT_BOOST_OUT_OF_CLASS);
                student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                        constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_TEXTING);
                student.studentStatistics.drainSecondaryStat("empathy", 1,
                        constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_EMPATHY * 0.3);
            }
            
            context.setVariable("friendship_gained", 2);
            return BehaviorStatus.SUCCESS;
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
        
        private java.util.List<Student> getClassmates(Student student, entity.EntityState state) {
            java.util.List<Student> classmates = new ArrayList<>();
            entity.Rooms.Room room = state.getCurrentRoom();
            if (room != null && room.getStudents() != null) {
                for (Student s : room.getStudents()) {
                    if (s != null && s != student) {
                        classmates.add(s);
                    }
                }
            }
            if (classmates.isEmpty()) {
                for (Student friend : student.studentStatistics.getFriendsInSchool()) {
                    if (friend != student) {
                        classmates.add(friend);
                    }
                }
            }
            return classmates;
        }
    }
    
    /**
     * Take notes action node with secondary stat drain.
     */
    private static class TakeNotesActionNode extends behavior.leaf.ActionNode {
        public TakeNotesActionNode() {
            super("TakeNotes", 1);
        }
        
        @Override
        public boolean canExecute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return false;
            }
            return student.getEntityState().isInClass();
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            student.getEntityState().setCurrentActivity(entity.ActivityType.TAKING_NOTES);
            
            // Small entertainment drain (note-taking is tedious)
            student.getEntityState().setEntertainment(student.getEntityState().getEntertainment() - 1);
            
            // Drain creativity and initiative from note-taking effort
            student.studentStatistics.drainSecondaryStat("creativity",
                    constants.SimConstants.STAT_DRAIN_TAKE_NOTES_CREATIVITY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_CREATIVITY);
            student.studentStatistics.drainSecondaryStat("initiative",
                    constants.SimConstants.STAT_DRAIN_TAKE_NOTES_INITIATIVE,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_INITIATIVE);
            
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Hang out at locker: mild entertainment boost, small allostatic recovery.
     * Represents a student killing time at their locker between classes.
     */
    private static class HangOutAtLockerActionNode extends behavior.leaf.ActionNode {
        public HangOutAtLockerActionNode() {
            super("HangOutAtLocker", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            student.getEntityState().setCurrentActivity(entity.ActivityType.AT_LOCKER);

            student.getEntityState().setEntertainment(student.getEntityState().getEntertainment() + 2);
            student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                    constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_DAYDREAMING);
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Out-of-class daydreaming: stronger entertainment boost than in-class
     * daydreaming, with no risk of being caught.
     */
    private static class OutOfClassDaydreamActionNode extends behavior.leaf.ActionNode {
        public OutOfClassDaydreamActionNode() {
            super("OutOfClassDaydream", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            student.getEntityState().setCurrentActivity(entity.ActivityType.DAYDREAMING);

            student.getEntityState().setEntertainment(student.getEntityState().getEntertainment() + 6);
            student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                    constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_DAYDREAMING);
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Stand around: the student isn't doing anything in particular.
     * Provides minimal recovery. Always succeeds -- used as a soft
     * fallback so the student is never truly idle outside of class.
     */
    private static class StandAroundActionNode extends behavior.leaf.ActionNode {
        public StandAroundActionNode() {
            super("StandAround", 1);
        }

        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            student.getEntityState().setCurrentActivity(entity.ActivityType.SOCIALIZING);

            student.studentStatistics.getAllostaticLoad().applyRelaxationRecovery(
                    constants.SimConstants.ALLOSTATIC_RELAXATION_RECOVERY_DAYDREAMING * 0.5);
            return BehaviorStatus.SUCCESS;
        }
    }

    /**
     * Checks whether a room has at least one teacher assigned.
     * Students can only be "caught" misbehaving when a teacher is
     * present and class is in session.
     */
    private static boolean hasTeacherPresent(entity.Rooms.Room room) {
        return room != null
                && room.getAssignedStaff() != null
                && !room.getAssignedStaff().isEmpty();
    }
}
