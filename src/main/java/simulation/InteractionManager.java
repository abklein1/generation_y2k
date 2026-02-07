package simulation;

import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import utility.SocialLinkConnector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static constants.SimConstants.*;

/**
 * Manages social interaction conflicts between students during a single simulation tick.
 *
 * <p>When multiple students attempt to interact with the same student in the same tick,
 * the InteractionManager resolves the conflict: the student with the highest combined
 * Determination + Charisma is granted the interaction, and all others are denied.</p>
 *
 * <p>When an interaction is confirmed, both the initiator and target gain social score
 * toward each other. The gain amount depends on the activity type (talking &gt; passing
 * notes &gt; whispering &gt; generic socializing). Score increases are subject to the
 * best-friend soft cap: scores cannot cross the best-friend threshold without a
 * catalyst event.</p>
 *
 * <p>Usage per tick:
 * <ol>
 *   <li>{@link #clearTick()} at the start of each tick</li>
 *   <li>Behavior tree action nodes call {@link #registerInteraction} to record intended social actions</li>
 *   <li>{@link #resolveInteractions()} after all behavior trees have been ticked</li>
 * </ol>
 * </p>
 *
 * <p>Each student may only be involved in one social interaction per tick, whether as
 * initiator or target. The resolution sorts all pending interactions by priority
 * (highest DET + CHR first) and grants them in order, skipping any interaction where
 * either the initiator or target is already occupied.</p>
 */
public class InteractionManager {

    private final List<PendingInteraction> pendingInteractions;
    private SocialLinkConnector socialLinkConnector;

    /**
     * Creates a new InteractionManager.
     */
    public InteractionManager() {
        this.pendingInteractions = new ArrayList<>();
    }

    /**
     * Sets the social link connector used to update relationship scores
     * when interactions are confirmed.
     *
     * @param socialLinkConnector the social link connector
     */
    public void setSocialLinkConnector(SocialLinkConnector socialLinkConnector) {
        this.socialLinkConnector = socialLinkConnector;
    }

    /**
     * Clears all pending interactions for a new tick.
     * Must be called at the start of each simulation tick before behavior trees are processed.
     */
    public void clearTick() {
        pendingInteractions.clear();
    }

    /**
     * Registers a pending social interaction between an initiator and a target.
     * The interaction is not immediately confirmed; it will be resolved after
     * all behavior trees have been ticked.
     *
     * @param initiator        the student initiating the interaction
     * @param target           the student being interacted with
     * @param intendedActivity the type of social activity (e.g. PASSING_NOTE, WHISPERING)
     */
    public void registerInteraction(Student initiator, Student target, ActivityType intendedActivity) {
        if (initiator == null || target == null || initiator == target) {
            return;
        }

        int determination = initiator.studentStatistics.getDetermination();
        int charisma = initiator.studentStatistics.getCharisma();
        int priorityScore = determination + charisma;

        pendingInteractions.add(new PendingInteraction(initiator, target, intendedActivity, priorityScore));
    }

    /**
     * Resolves all pending interactions for this tick.
     *
     * <p>Interactions are sorted by priority (DET + CHR) in descending order.
     * The highest-priority student gets their interaction confirmed first.
     * Any subsequent interaction involving an already-occupied student (as either
     * initiator or target) is denied.</p>
     *
     * <p>Denied students have their activity set to IDLE, since the person they
     * wanted to interact with is now occupied.</p>
     */
    public void resolveInteractions() {
        if (pendingInteractions.isEmpty()) {
            return;
        }

        // Sort by priority descending (highest DET + CHR first)
        pendingInteractions.sort(Comparator.comparingInt(PendingInteraction::getPriorityScore).reversed());

        // Track which students are occupied (either as initiator or target)
        Set<Student> occupied = new HashSet<>();

        for (PendingInteraction pending : pendingInteractions) {
            Student initiator = pending.getInitiator();
            Student target = pending.getTarget();

            // Check if either party is already occupied this tick
            if (occupied.contains(initiator) || occupied.contains(target)) {
                // Denied: revert the initiator to idle since their target is occupied
                denyInteraction(pending);
                continue;
            }

            // Granted: mark both parties as occupied for this tick
            occupied.add(initiator);
            occupied.add(target);
            confirmInteraction(pending);
        }
    }

    /**
     * Confirms a granted interaction. The initiator's activity is already set
     * tentatively by the action node, so we just need to mark the target as
     * engaged in a social interaction.
     *
     * <p>Additionally, both the initiator and target gain social score toward
     * each other based on the activity type. All social actions are treated as
     * positive for NPC interactions. Score increases are subject to the
     * best-friend soft cap enforced by {@link SocialLinkConnector#modifySocialScore}.</p>
     *
     * @param interaction the confirmed interaction
     */
    private void confirmInteraction(PendingInteraction interaction) {
        Student initiator = interaction.getInitiator();
        Student target = interaction.getTarget();

        if (target != null && target.getEntityState() != null) {
            EntityState targetState = target.getEntityState();
            // Mark the target as being in a social interaction (they're the recipient)
            // Only change their activity if they're doing something interruptible
            ActivityType currentActivity = targetState.getCurrentActivity();
            if (currentActivity == ActivityType.ATTENDING_CLASS
                    || currentActivity == ActivityType.IDLE
                    || currentActivity == ActivityType.DAYDREAMING) {
                targetState.setCurrentActivity(ActivityType.SOCIALIZING);
            }
        }

        // Apply social score gains for the confirmed interaction.
        // Both parties gain score toward each other (all NPC actions are positive for now).
        if (socialLinkConnector != null && initiator != null && target != null) {
            double gain = getFriendshipGain(interaction.getIntendedActivity());
            socialLinkConnector.modifySocialScore(initiator, target, gain);
            socialLinkConnector.modifySocialScore(target, initiator, gain);
        }
    }

    /**
     * Returns the friendship score gain for a given social activity type.
     * Different activities carry different social weight:
     * passing notes is a deliberate personal gesture (highest gain),
     * talking is significant, whispering is quick, and generic socializing is minimal.
     *
     * @param activity the type of social activity
     * @return the friendship score gain
     */
    private double getFriendshipGain(ActivityType activity) {
        return switch (activity) {
            case TALKING -> SOCIAL_LINK_GAIN_TALKING;
            case WHISPERING -> SOCIAL_LINK_GAIN_WHISPERING;
            case PASSING_NOTE -> SOCIAL_LINK_GAIN_PASSING_NOTE;
            default -> SOCIAL_LINK_GAIN_SOCIALIZING;
        };
    }

    /**
     * Denies an interaction because the target (or initiator) is already occupied.
     * The initiator is reverted to IDLE since their intended social action cannot proceed.
     *
     * @param interaction the denied interaction
     */
    private void denyInteraction(PendingInteraction interaction) {
        Student initiator = interaction.getInitiator();
        if (initiator != null && initiator.getEntityState() != null) {
            // Revert the initiator to idle - the person they wanted is occupied
            initiator.getEntityState().setCurrentActivity(ActivityType.IDLE);
        }
    }

    /**
     * Gets the number of pending interactions this tick (before resolution).
     *
     * @return the number of pending interactions
     */
    public int getPendingCount() {
        return pendingInteractions.size();
    }

    /**
     * Checks if a specific student has already registered an interaction as initiator this tick.
     * Useful for preventing duplicate registrations.
     *
     * @param student the student to check
     * @return true if the student has already registered an interaction
     */
    public boolean hasRegisteredInteraction(Student student) {
        if (student == null) {
            return false;
        }
        for (PendingInteraction pending : pendingInteractions) {
            if (pending.getInitiator() == student) {
                return true;
            }
        }
        return false;
    }

    /**
     * Represents a pending social interaction between two students.
     * Stored during the behavior tree tick phase and resolved afterwards.
     */
    private static class PendingInteraction {

        private final Student initiator;
        private final Student target;
        private final ActivityType intendedActivity;
        private final int priorityScore;

        /**
         * Creates a new pending interaction.
         *
         * @param initiator        the student initiating the interaction
         * @param target           the student being interacted with
         * @param intendedActivity the type of social activity
         * @param priorityScore    the initiator's combined DET + CHR
         */
        public PendingInteraction(Student initiator, Student target,
                                  ActivityType intendedActivity, int priorityScore) {
            this.initiator = initiator;
            this.target = target;
            this.intendedActivity = intendedActivity;
            this.priorityScore = priorityScore;
        }

        public Student getInitiator() {
            return initiator;
        }

        public Student getTarget() {
            return target;
        }

        public ActivityType getIntendedActivity() {
            return intendedActivity;
        }

        public int getPriorityScore() {
            return priorityScore;
        }

        @Override
        public String toString() {
            return String.format("PendingInteraction{%s -> %s, activity=%s, priority=%d}",
                    initiator, target, intendedActivity, priorityScore);
        }
    }
}
