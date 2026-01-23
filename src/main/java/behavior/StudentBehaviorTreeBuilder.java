package behavior;

import behavior.composite.Selector;
import behavior.composite.Sequence;
import behavior.composite.RandomSelector;
import behavior.decorator.Inverter;
import behavior.leaf.student.*;
import entity.Student;

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
        
        // Priority 3: Default idle action
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
     * Builds a sequence for distracted/social behavior.
     */
    private static BehaviorNode buildDistractedSequence() {
        Sequence distracted = new Sequence("DistractedBehavior");
        distracted.addChild(new HasFriendNearbyCondition());
        
        // Random choice between passing note and whispering
        RandomSelector socialChoice = new RandomSelector("SocialChoice");
        socialChoice.addChild(new PassNoteActionNode());
        socialChoice.addChild(new WhisperActionNode());
        
        distracted.addChild(socialChoice);
        return distracted;
    }
    
    /**
     * Builds a sequence for social behavior.
     */
    private static BehaviorNode buildSocialSequence() {
        Sequence social = new Sequence("SocialBehavior");
        social.addChild(new HasFriendNearbyCondition());
        social.addChild(new PassNoteActionNode());
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
            
            // Done, clear needs
            state.setNeedsBathroom(false);
            state.setHasPermissionToLeave(false);
            state.setCurrentActivity(entity.ActivityType.IDLE);
            return BehaviorStatus.SUCCESS;
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
     * Simple whisper action node.
     */
    private static class WhisperActionNode extends behavior.leaf.ActionNode {
        public WhisperActionNode() {
            super("Whisper", 1);
        }
        
        @Override
        public boolean canExecute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null) {
                return false;
            }
            return !student.studentStatistics.getFriendsInSchool().isEmpty();
        }
        
        @Override
        public BehaviorStatus execute(BehaviorContext context) {
            Student student = context.getStudent();
            if (student == null || student.getEntityState() == null) {
                return BehaviorStatus.FAILURE;
            }
            
            student.getEntityState().setCurrentActivity(entity.ActivityType.WHISPERING);
            
            // Decrease boredom
            int boredom = student.studentStatistics.getBoredom();
            student.studentStatistics.setBoredom(Math.max(0, boredom - 3));
            
            return BehaviorStatus.SUCCESS;
        }
    }
    
    /**
     * Simple take notes action node.
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
            
            // Small boredom increase
            int boredom = student.studentStatistics.getBoredom();
            student.studentStatistics.setBoredom(Math.min(100, boredom + 1));
            
            return BehaviorStatus.SUCCESS;
        }
    }
}
