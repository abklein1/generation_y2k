package simulation;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import utility.CrushDeveloper;
import utility.GameRandom;
import utility.RomanceUpdater;
import utility.SocialLinkConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static constants.SimConstants.*;

/**
 * Manages social interaction conflicts between students during a single
 * simulation tick.
 *
 * <p>
 * When multiple students attempt to interact with the same student in the same
 * tick,
 * the InteractionManager resolves the conflict: the student with the highest
 * combined
 * Determination + Charisma is granted the interaction, and all others are
 * denied.
 * </p>
 *
 * <p>
 * When an interaction is confirmed, both the initiator and target are placed
 * into the same social activity (e.g. both end up TALKING), the target's
 * behavior context is updated so its action log entry references the
 * initiator, and both students' decision cooldowns are reset so neither party
 * immediately drops back out of the interaction. Getting caught is handled
 * afterwards by the {@link ClassroomDisciplineService}: the supervising
 * teacher's behavior tree perceives reported misbehavior and pulls both
 * confirmed participants into the incident.
 * </p>
 *
 * <p>
 * Score gains depend on the activity type (talking &gt; passing notes &gt;
 * whispering &gt; generic socializing) and are subject to the best-friend
 * soft cap enforced by {@link SocialLinkConnector#modifySocialScore}.
 * </p>
 *
 * <p>
 * Usage per tick:
 * <ol>
 * <li>{@link #clearTick()} at the start of each tick</li>
 * <li>Behavior tree action nodes call {@link #registerInteraction} to record
 * intended social actions</li>
 * <li>{@link #resolveInteractions()} after all behavior trees have been
 * ticked. The returned set lists the targets that were drawn into a confirmed
 * interaction this tick — callers should log those students even if their
 * own tree did not run.</li>
 * </ol>
 * </p>
 */
public class InteractionManager {

    private final List<PendingInteraction> pendingInteractions;
    private SocialLinkConnector socialLinkConnector;
    private static final int TARGET_COOLDOWN_TICKS = 5;

    /**
     * Activities that intrinsically can't be interrupted by a peer initiating
     * a social action — physically separate (off-campus lunch), in motion
     * (commuting/transitioning), private (in the bathroom), or mid-meal
     * (eating lunch, which must not be cut short or hunger never refills).
     * Targets in these states stay in their current activity even if an
     * interaction is granted.
     */
    private static final Set<ActivityType> NON_INTERRUPTIBLE_ACTIVITIES = Collections.unmodifiableSet(new HashSet<>(List.of(
            ActivityType.IN_BATHROOM,
            ActivityType.TRANSITIONING,
            ActivityType.COMMUTING_WALK,
            ActivityType.COMMUTING_BUS,
            ActivityType.COMMUTING_DRIVE,
            ActivityType.COMMUTING_CARPOOL,
            ActivityType.EATING_LUNCH,
            ActivityType.EATING_LUNCH_OFF_CAMPUS)));

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
     * Returns the social link connector, or null if not set. Behavior nodes
     * use it to drive score-weighted target selection.
     *
     * @return the social link connector, or null
     */
    public SocialLinkConnector getSocialLinkConnector() {
        return socialLinkConnector;
    }

    /**
     * Clears all pending interactions for a new tick.
     * Must be called at the start of each simulation tick before behavior trees are
     * processed.
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
     * @param intendedActivity the type of social activity (e.g. PASSING_NOTE,
     *                         WHISPERING)
     */
    public void registerInteraction(Student initiator, Student target, ActivityType intendedActivity) {
        registerInteraction(initiator, target, intendedActivity, null);
    }

    /**
     * Registers a pending social interaction that is <i>about</i> a third
     * student (e.g. BADMOUTHING a romantic rival to a crush). The subject is
     * not a participant: they are the one being discussed, and per-activity
     * resolution may adjust the target's opinion of them.
     *
     * @param initiator        the student initiating the interaction
     * @param target           the student being interacted with
     * @param intendedActivity the type of social activity
     * @param subject          the third student the interaction is about, or
     *                         null when the activity has no subject
     */
    public void registerInteraction(Student initiator, Student target, ActivityType intendedActivity,
            Student subject) {
        if (initiator == null || target == null || initiator == target) {
            return;
        }

        int determination = initiator.studentStatistics.getDetermination();
        int charisma = initiator.studentStatistics.getCharisma();
        int priorityScore = determination + charisma;

        pendingInteractions.add(new PendingInteraction(initiator, target, intendedActivity,
                priorityScore, subject));
    }

    /**
     * Resolves all pending interactions for this tick.
     *
     * <p>
     * Interactions are sorted by priority (DET + CHR) in descending order.
     * The highest-priority student gets their interaction confirmed first.
     * Any subsequent interaction involving an already-occupied student (as either
     * initiator or target) is denied.
     * </p>
     *
     * <p>
     * If an initiator is also a target of a higher-priority confirmed
     * interaction, the lower-priority registration they made is silently
     * dropped — their activity is <i>not</i> reset to IDLE, since they're now
     * legitimately engaged in someone else's conversation.
     * </p>
     *
     * @return the set of students that were confirmed as targets this tick.
     *         Callers should ensure these students get an action-log entry
     *         even if their own behavior tree did not tick.
     */
    public Set<Student> resolveInteractions() {
        Set<Student> confirmedTargets = new HashSet<>();
        if (pendingInteractions.isEmpty()) {
            return confirmedTargets;
        }

        // Sort by priority descending (highest DET + CHR first)
        pendingInteractions.sort(Comparator.comparingInt(PendingInteraction::getPriorityScore).reversed());

        // Track which students are occupied (either as initiator or target)
        Set<Student> occupied = new HashSet<>();
        List<PendingInteraction> deferredDenials = new ArrayList<>();

        // First pass: confirm interactions in priority order.
        for (PendingInteraction pending : pendingInteractions) {
            Student initiator = pending.getInitiator();
            Student target = pending.getTarget();

            if (occupied.contains(initiator) || occupied.contains(target)) {
                deferredDenials.add(pending);
                continue;
            }

            // Always reserve the initiator so they can't be re-considered for
            // another interaction this tick, even if the target turns out to
            // be unreachable and the interaction gets downgraded to a denial.
            occupied.add(initiator);
            if (confirmInteraction(pending)) {
                occupied.add(target);
                confirmedTargets.add(target);
            }
        }

        // Second pass: deny the rest, skipping any initiator that was already
        // confirmed as a target — those students are legitimately engaged and
        // their activity should not be flipped back to IDLE.
        for (PendingInteraction pending : deferredDenials) {
            Student initiator = pending.getInitiator();
            if (initiator != null && confirmedTargets.contains(initiator)) {
                continue;
            }
            denyInteraction(pending);
        }

        return confirmedTargets;
    }

    /**
     * Confirms a granted interaction. The initiator's activity is already set
     * tentatively by the action node; this method makes the target a real
     * participant in the same activity:
     *
     * <ul>
     *   <li>Mirrors the specific social activity onto the target (e.g. both
     *       students end up in {@code TALKING}, not a generic
     *       {@code SOCIALIZING}). Targets in non-interruptible states
     *       (bathroom, transitioning, off-campus) keep their current activity.</li>
     *   <li>Sets {@code interaction_target} on the target's
     *       {@link BehaviorContext} so its log line reads "with &lt;initiator&gt;".</li>
     *   <li>Resets the target's decision cooldown so they remain engaged for
     *       the same duration as the initiator instead of immediately
     *       picking a new action.</li>
     *   <li>Bumps the social link in both directions, subject to the
     *       best-friend soft cap.</li>
     * </ul>
     *
     * <p>
     * If the target has no {@link BehaviorContext} yet — meaning their
     * behavior tree has not run today (e.g. the student is still at home
     * before school) — they are not a valid interaction partner and the
     * confirmation is downgraded to a denial of the initiator. This guards
     * against stale candidate lists silently linking a present student to
     * one who is not yet in the simulation.
     * </p>
     *
     * @param interaction the confirmed interaction
     * @return true if the interaction was actually applied (target is a real
     *         participant), false if it was downgraded to a denial because
     *         the target was not reachable
     */
    private boolean confirmInteraction(PendingInteraction interaction) {
        Student initiator = interaction.getInitiator();
        Student target = interaction.getTarget();
        ActivityType activity = interaction.getIntendedActivity();

        if (target == null || target.getEntityState() == null
                || target.getBehaviorContext() == null) {
            // Target is not actually present in the simulation right now
            // (no behavior context means their tree has not run today, e.g.
            // they are still at home pre-school). Treat as a denial of the
            // initiator rather than silently mutating a phantom partner.
            denyInteraction(interaction);
            return false;
        }

        EntityState targetState = target.getEntityState();
        ActivityType currentActivity = targetState.getCurrentActivity();

        // Mirror the initiator's activity onto the target unless the
        // target is in a state that intrinsically can't be interrupted.
        // Directed activities (badmouthing, showing off) mirror as plain
        // talking: the target is just being talked to.
        ActivityType mirrored = mirroredActivityFor(activity);
        if (mirrored != null && !NON_INTERRUPTIBLE_ACTIVITIES.contains(currentActivity)) {
            targetState.setCurrentActivity(mirrored);
        }

        // Reset the target's decision cooldown so they stay engaged in the
        // interaction for the same window the initiator does.
        targetState.resetDecisionCooldown(TARGET_COOLDOWN_TICKS);

        // Surface the interaction in the target's behavior context so the
        // logger picks up the "with <initiator>" suffix. Caught state is no
        // longer propagated here: the ClassroomDisciplineService runs after
        // resolution and flags both confirmed participants itself.
        BehaviorContext targetContext = target.getBehaviorContext();
        if (initiator != null) {
            targetContext.setVariable("interaction_target", initiator);
        }

        // Apply the social score effects for the confirmed interaction.
        // Most activities are mutual positives; directed activities
        // (badmouthing, showing off) resolve with their own asymmetric rules.
        if (socialLinkConnector != null && initiator != null) {
            applyInteractionEffects(interaction);
        }
        return true;
    }

    /**
     * Dispatches the confirmed interaction to its score resolution. Plain
     * social activities bump both directions by the activity's friendship
     * gain; the jealousy-driven activities have asymmetric outcomes.
     *
     * @param interaction the confirmed interaction
     */
    private void applyInteractionEffects(PendingInteraction interaction) {
        Student initiator = interaction.getInitiator();
        Student target = interaction.getTarget();
        ActivityType activity = interaction.getIntendedActivity();

        if (activity == ActivityType.BADMOUTHING) {
            resolveBadmouthing(initiator, target, interaction.getSubject());
            return;
        }
        if (activity == ActivityType.IMPRESSING) {
            resolveImpressing(initiator, target);
            return;
        }
        double gain = getFriendshipGain(activity);
        socialLinkConnector.modifySocialScore(initiator, target, gain);
        socialLinkConnector.modifySocialScore(target, initiator, gain);

        // Being in the presence of one of the school's rare stat standouts
        // can spark a fleeting crush in either participant (no-op unless the
        // other party is a standout; see CrushDeveloper).
        CrushDeveloper.maybeDevelopFleetingCrush(initiator, target, socialLinkConnector);
        CrushDeveloper.maybeDevelopFleetingCrush(target, initiator, socialLinkConnector);
    }

    /**
     * Resolves a badmouthing attempt: the initiator talks trash about the
     * subject (their romantic rival) to the target (their crush).
     *
     * <p>A loyal target -- one whose outgoing score toward the subject is at
     * least {@code BADMOUTH_BACKFIRE_LOYALTY_THRESHOLD} -- may snap back at
     * the initiator instead of absorbing the dirt. Otherwise the target's
     * opinion of the subject drops and the conspiratorial chat slightly
     * warms the target toward the initiator.</p>
     *
     * @param initiator the badmouther
     * @param target    the student being talked to
     * @param subject   the student being trashed (null degrades to a chat)
     */
    private void resolveBadmouthing(Student initiator, Student target, Student subject) {
        if (subject == null) {
            // No third party to trash: degrade to a generic chat.
            socialLinkConnector.modifySocialScore(initiator, target, SOCIAL_LINK_GAIN_SOCIALIZING);
            socialLinkConnector.modifySocialScore(target, initiator, SOCIAL_LINK_GAIN_SOCIALIZING);
            return;
        }
        boolean loyal = socialLinkConnector.getSocialScore(target, subject)
                >= BADMOUTH_BACKFIRE_LOYALTY_THRESHOLD;
        if (loyal && GameRandom.nextDouble() < BADMOUTH_BACKFIRE_CHANCE) {
            socialLinkConnector.modifySocialScore(target, initiator, -BADMOUTH_BACKFIRE_PENALTY);
            RomanceUpdater.recordExternalEvent(name(target) + " defended " + name(subject)
                    + " when " + name(initiator) + " talked trash.");
            return;
        }
        socialLinkConnector.modifySocialScore(target, subject, -SOCIAL_LINK_DRAIN_BADMOUTH);
        socialLinkConnector.modifySocialScore(initiator, target, SOCIAL_LINK_GAIN_BADMOUTH);
        RomanceUpdater.recordExternalEvent(name(initiator) + " talked trash about "
                + name(subject) + " to " + name(target) + ".");
    }

    /**
     * Resolves an attention-seeking attempt: the initiator shows off for the
     * target (their crush). The warmth the target gains scales with the
     * initiator's charisma; a low-charisma attempt can flop and embarrass
     * the initiator instead.
     *
     * @param initiator the student showing off
     * @param target    the student whose attention is sought
     */
    private void resolveImpressing(Student initiator, Student target) {
        socialLinkConnector.modifySocialScore(initiator, target, SOCIAL_LINK_GAIN_IMPRESS);

        int charisma = initiator.studentStatistics.getCharisma();
        if (charisma < IMPRESS_FLOP_CHARISMA_THRESHOLD
                && GameRandom.nextDouble() < IMPRESS_FLOP_CHANCE) {
            socialLinkConnector.modifySocialScore(target, initiator, -IMPRESS_FLOP_PENALTY);
            RomanceUpdater.recordExternalEvent(name(initiator) + " tried to impress "
                    + name(target) + " and flopped.");
            return;
        }
        double gain = IMPRESS_TARGET_GAIN_BASE + charisma / IMPRESS_CHARISMA_DIVISOR;
        socialLinkConnector.modifySocialScore(target, initiator, gain);
    }

    /**
     * The activity mirrored onto a confirmed target. Directed activities
     * (badmouthing, showing off) belong to the initiator only; the target
     * simply experiences a conversation.
     */
    private static ActivityType mirroredActivityFor(ActivityType activity) {
        if (activity == ActivityType.BADMOUTHING || activity == ActivityType.IMPRESSING) {
            return ActivityType.TALKING;
        }
        return activity;
    }

    private static String name(Student student) {
        return student.studentName.getFullName();
    }

    /**
     * Returns the friendship score gain for a given social activity type.
     * Different activities carry different social weight:
     * passing notes is a deliberate personal gesture (highest gain),
     * talking is significant, whispering is quick, and generic socializing is
     * minimal.
     *
     * @param activity the type of social activity
     * @return the friendship score gain
     */
    private double getFriendshipGain(ActivityType activity) {
        if (activity == null) {
            return SOCIAL_LINK_GAIN_SOCIALIZING;
        }
        return switch (activity) {
            case TALKING -> SOCIAL_LINK_GAIN_TALKING;
            case WHISPERING -> SOCIAL_LINK_GAIN_WHISPERING;
            case PASSING_NOTE -> SOCIAL_LINK_GAIN_PASSING_NOTE;
            case TEXTING -> SOCIAL_LINK_GAIN_TEXTING;
            default -> SOCIAL_LINK_GAIN_SOCIALIZING;
        };
    }

    /**
     * Denies an interaction because the target (or initiator) is already occupied.
     * The initiator is reverted to IDLE since their intended social action
     * cannot proceed, and the stale interaction-related context variables are
     * cleared so the logger does not show a partner for an action that
     * effectively did not happen. (The discipline service also skips
     * misbehavior reports whose action fizzled like this.)
     *
     * @param interaction the denied interaction
     */
    private void denyInteraction(PendingInteraction interaction) {
        Student initiator = interaction.getInitiator();
        if (initiator == null) {
            return;
        }
        if (initiator.getEntityState() != null) {
            initiator.getEntityState().setCurrentActivity(ActivityType.IDLE);
        }
        BehaviorContext context = initiator.getBehaviorContext();
        if (context != null) {
            context.removeVariable("interaction_target");
            context.removeVariable("friendship_gained");
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
     * Checks if a specific student has already registered an interaction as
     * initiator this tick.
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
        private final Student subject;

        /**
         * Creates a new pending interaction.
         *
         * @param initiator        the student initiating the interaction
         * @param target           the student being interacted with
         * @param intendedActivity the type of social activity
         * @param priorityScore    the initiator's combined DET + CHR
         * @param subject          the third student the interaction is about
         *                         (e.g. the rival being badmouthed), or null
         */
        public PendingInteraction(Student initiator, Student target,
                ActivityType intendedActivity, int priorityScore, Student subject) {
            this.initiator = initiator;
            this.target = target;
            this.intendedActivity = intendedActivity;
            this.priorityScore = priorityScore;
            this.subject = subject;
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

        public Student getSubject() {
            return subject;
        }

        @Override
        public String toString() {
            return String.format("PendingInteraction{%s -> %s, activity=%s, priority=%d}",
                    initiator, target, intendedActivity, priorityScore);
        }
    }
}
