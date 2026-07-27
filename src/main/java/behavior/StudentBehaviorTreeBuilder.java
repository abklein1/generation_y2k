package behavior;

import behavior.composite.Selector;
import behavior.composite.Sequence;
import behavior.composite.RandomSelector;
import behavior.leaf.student.*;
import entity.CellPhone;
import entity.Student;
import entity.Town;
import simulation.InteractionManager;
import utility.AcademicProgressService;

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

        // Academic pressure gets first chance to compete with social relief.
        activitySelector.addChild(buildAcademicPressureSequence());
        
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
        
        // Final fallback: a student with nothing else to do in class sits and
        // pays attention instead of idling. Without this, a student whose
        // social options are blocked (e.g. the teacher just settled the class,
        // or they lost an interaction conflict) and who isn't determined or
        // bored enough for the other branches would fall through to the root
        // Idle node and re-tick every single minute, spamming "Idle in
        // classroom" log lines for the rest of the period.
        activitySelector.addChild(new PayAttentionActionNode());
        
        return activitySelector;
    }

    private static BehaviorNode buildAcademicPressureSequence() {
        Sequence academicPressure = new Sequence("AcademicPressure");
        academicPressure.addChild(new HasAcademicPressureCondition());

        RandomSelector academicChoice = new RandomSelector("AcademicPressureChoice");
        academicChoice.addChild(new TakeNotesActionNode());
        academicChoice.addChild(new PayAttentionActionNode());
        academicPressure.addChild(academicChoice);

        return academicPressure;
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
            state.relieveBladder();
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
     * Condition for students who have coursework pressure in the current class.
     */
    private static class HasAcademicPressureCondition extends behavior.leaf.ConditionNode {
        public HasAcademicPressureCondition() {
            super("HasAcademicPressure");
        }

        @Override
        public boolean check(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null
                    || !student.getEntityState().isInClass()) {
                return false;
            }

            String className = AcademicProgressService.getCurrentClassName(student, context.getTime());
            return AcademicProgressService.hasAcademicPressure(student, className);
        }
    }
    
    /**
     * Talk action node: context-aware social action.
     * <ul>
     *   <li><b>In class:</b> Can reach anyone in the room (voice carries).
     *       Loud and obvious — the misbehavior is reported to the supervising
     *       teacher, whose stats decide whether the student gets noticed.
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
            // Nobody dares talk right after the teacher settled the class.
            if (isClassCalmed(context, student.getEntityState())) {
                return false;
            }
            // Only succeed if there is at least one co-located peer to talk to.
            // During transit that means a co-traveler currently in transit; on
            // campus that means another student in the same room.
            return !getCandidates(student, student.getEntityState()).isEmpty();
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            entity.EntityState state = student.getEntityState();
            boolean inClass = state.isInClass() && hasTeacherPresent(state.getCurrentRoom());
            
            // Select a target using the initiator's outgoing social scores
            Student target = TargetSelector.selectTarget(student, getCandidates(student, state),
                    context.getSocialLinkConnector());
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
                
                // Report the (loud, obvious) misbehavior to the supervising
                // teacher. Charisma masks the chatting as participation and
                // perception times it to the teacher's back being turned.
                reportMisbehavior(context, student, state,
                        entity.ActivityType.TALKING,
                        student.studentStatistics.getCharisma() / 15
                                + student.studentStatistics.getPerception() / 20);
                
                // Entertainment boost and slight recovery (the teacher may
                // still interrupt this later in the tick)
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
            return collectCoLocatedPeers(student, state);
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
     * Quiet, but still reported to the supervising teacher — a perceptive
     * veteran can spot the leaning heads.
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
            // Nobody dares whisper right after the teacher settled the class.
            if (isClassCalmed(context, student.getEntityState())) {
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
            
            Student target = TargetSelector.selectTarget(student, adjacent,
                    context.getSocialLinkConnector());
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
            
            entity.EntityState state = student.getEntityState();
            if (state.isInClass() && hasTeacherPresent(state.getCurrentRoom())) {
                // Report the covert misbehavior to the supervising teacher.
                // Perceptive students pick their moment better.
                reportMisbehavior(context, student, state,
                        entity.ActivityType.WHISPERING,
                        student.studentStatistics.getPerception() / 15);
            }
            
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
            // Nobody dares text right after the teacher settled the class.
            if (isClassCalmed(context, student.getEntityState())) {
                return false;
            }
            Town town = context.getTown();
            if (town == null || !town.hasPhone(student)) {
                return false;
            }
            CellPhone phone = town.getStudentPhone(student);
            if (phone == null || !phone.hasSms() || phone.getTextsRemaining() <= 0) {
                return false;
            }
            // Need at least one co-located peer who is reachable by text:
            // they must own an SMS-capable phone AND their number must be in
            // this student's saved contact list.
            return !getTextableCandidates(student, student.getEntityState(), phone, town).isEmpty();
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

            entity.EntityState state = student.getEntityState();
            boolean inClass = state.isInClass() && hasTeacherPresent(state.getCurrentRoom());

            // Filter candidates to those we can actually text BEFORE consuming
            // a text from the monthly allowance.  Otherwise we'd burn a text
            // on a target lookup that fails, lying to the player about how
            // many texts they have left.
            java.util.List<Student> textable = getTextableCandidates(student, state, phone, town);
            Student target = TargetSelector.selectTarget(student, textable,
                    context.getSocialLinkConnector());
            if (target == null) {
                return BehaviorStatus.FAILURE;
            }
            if (!phone.useText()) {
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
                
                // Report the covert misbehavior to the supervising teacher.
                // Concealment: small phones hide better, physical keyboards
                // are faster, perceptive students pick their moment.
                reportMisbehavior(context, student, state,
                        entity.ActivityType.TEXTING,
                        computeConcealment(phone, student));
                
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
        
        /**
         * The student's concealment score for texting: percentage points
         * shaved off the teacher's notice chance. Phone hardware modifiers
         * (small = easier to hide, large = harder, keyboard = faster) are
         * inverted from their old catch-chance sign, plus a perception bonus.
         */
        private int computeConcealment(CellPhone phone, Student student) {
            int concealment = student.studentStatistics.getPerception() / 20;
            
            String size = phone.getSize();
            if (size != null) {
                switch (size.toLowerCase()) {
                    case "small" -> concealment -= constants.SimConstants.TEXT_PHONE_SIZE_MODIFIER_SMALL;
                    case "large" -> concealment -= constants.SimConstants.TEXT_PHONE_SIZE_MODIFIER_LARGE;
                    default -> concealment -= constants.SimConstants.TEXT_PHONE_SIZE_MODIFIER_MEDIUM;
                }
            }
            
            if (phone.hasKeyboard()) {
                concealment -= constants.SimConstants.TEXT_KEYBOARD_SPEED_MODIFIER;
            }
            
            return concealment;
        }
        
        /**
         * Returns the co-located peers that this student can actually text
         * right now: each must own an SMS-capable phone and be saved as a
         * contact on the student's own phone.  Empty list if the student is
         * out of range of a textable peer.
         */
        private java.util.List<Student> getTextableCandidates(Student student,
                                                              entity.EntityState state,
                                                              CellPhone studentPhone,
                                                              Town town) {
            java.util.List<Student> coLocated = collectCoLocatedPeers(student, state);
            return utility.CellPhoneAssignmentService.filterTextableCandidates(
                    student, studentPhone, town, coLocated);
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

            double learning = AcademicProgressService.recordCurrentClassLearning(
                    student, context.getTime(), 7, entity.ActivityType.TAKING_NOTES);
            context.setVariable("learning_gained", learning);
            
            // Small entertainment drain (note-taking is tedious)
            student.getEntityState().setEntertainment(student.getEntityState().getEntertainment() - 1);
            
            // Drain creativity from note-taking effort; academic service drains
            // initiative and responsibility for sustained class work.
            student.studentStatistics.drainSecondaryStat("creativity",
                    constants.SimConstants.STAT_DRAIN_TAKE_NOTES_CREATIVITY,
                    constants.SimConstants.ALLOSTATIC_STRESS_FACTOR_CREATIVITY);
            
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

    /**
     * Whether the student's classroom is inside the calm window after the
     * teacher settled the class. Students do not dare start new in-class
     * misbehavior while it lasts (out of class this is never true).
     */
    private static boolean isClassCalmed(BehaviorContext context, entity.EntityState state) {
        if (state == null || !state.isInClass()) {
            return false;
        }
        simulation.ClassroomDisciplineService discipline = context.getDisciplineService();
        return discipline != null && discipline.isRoomCalmed(state.getCurrentRoom());
    }

    /**
     * Reports an in-class misbehavior attempt to the discipline service so
     * the supervising teacher can (potentially) notice it this tick. The
     * concealment score is the student's side of the detection contest.
     */
    private static void reportMisbehavior(BehaviorContext context, Student student,
            entity.EntityState state, entity.ActivityType type, int concealment) {
        simulation.ClassroomDisciplineService discipline = context.getDisciplineService();
        if (discipline != null) {
            discipline.reportMisbehavior(student, state.getCurrentRoom(), type, concealment);
        }
    }

    /**
     * Returns the set of peers that the given student can plausibly initiate
     * a social interaction with right now, scoped to who is actually
     * co-located.
     *
     * <ul>
     *   <li><b>In transit:</b> co-travelers from the student's transit group
     *       who are also currently in transit. A friend who is still at home,
     *       has already arrived at school, or is in a different transit group
     *       cannot be reached during the commute.</li>
     *   <li><b>On campus:</b> other students currently in the same room.
     *       Notes/talk/text are room-bounded, so an unrelated friend across
     *       the building is not a candidate.</li>
     * </ul>
     *
     * The previous implementation fell back to {@code getFriendsInSchool()}
     * any time the room was unavailable, which let students initiate
     * "interactions" with friends who were not even in the simulation yet
     * (still at home before school). That manifested as Ryan Carnell
     * "Walking to school with Robert Nguyen" while Robert had no log entry
     * at all — Robert had not departed yet, so his behavior tree never ran
     * and he could not be reached.
     *
     * @param student the initiating student
     * @param state   the initiator's entity state (must be non-null)
     * @return a freshly-allocated, mutable list of candidate peers
     */
    static java.util.List<Student> collectCoLocatedPeers(Student student,
                                                          entity.EntityState state) {
        java.util.List<Student> candidates = new ArrayList<>();
        if (student == null || state == null) {
            return candidates;
        }

        if (state.isInTransit()) {
            java.util.List<Student> group = state.getTransitGroup();
            if (group == null) {
                return candidates;
            }
            for (Student peer : group) {
                if (peer == null || peer == student) {
                    continue;
                }
                entity.EntityState peerState = peer.getEntityState();
                if (peerState != null && peerState.isInTransit()) {
                    candidates.add(peer);
                }
            }
            return candidates;
        }

        entity.Rooms.Room room = state.getCurrentRoom();
        if (room != null && room.getStudents() != null) {
            for (Student s : room.getStudents()) {
                if (s != null && s != student) {
                    candidates.add(s);
                }
            }
        }
        return candidates;
    }
}
