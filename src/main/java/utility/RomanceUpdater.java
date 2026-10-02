package utility;

import entity.RomanticStatus;
import entity.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static constants.SimConstants.ROMANCE_BREAKUP_SCORE_PENALTY;
import static constants.SimConstants.ROMANCE_CRUSH_MIN_SCORE;
import static constants.SimConstants.ROMANCE_FLING_OFFICIAL_MIN_SCORE;
import static constants.SimConstants.ROMANCE_JEALOUSY_DISCOVERY_STING;
import static constants.SimConstants.ROMANCE_JEALOUSY_DRIP;
import static constants.SimConstants.ROMANCE_MUTUAL_MIN_SCORE;
import static constants.SimConstants.ROMANCE_NOTICE_BASE_CHANCE;
import static constants.SimConstants.ROMANCE_NOTICE_CHANCE_MAX;
import static constants.SimConstants.ROMANCE_NOTICE_FLING_VISIBILITY;
import static constants.SimConstants.ROMANCE_PARTNERED_ESCALATE_CHANCE_MAX;
import static constants.SimConstants.ROMANCE_PARTNERED_ESCALATE_CHANCE_MIN;
import static constants.SimConstants.ROMANCE_PULSE_ASYM_BREAKUP_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_ASYM_CONVERGE_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_CRUSH_ACT_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_FLING_FIZZLE_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_FLING_OFFICIAL_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_STEADY_BREAKUP_CHANCE;
import static constants.SimConstants.ROMANCE_REJECTION_SCORE_PENALTY;
import static constants.SimConstants.ROMANCE_SECOND_RELATIONSHIP_PARTNER_DRAIN;
import static constants.SimConstants.ROMANCE_SECOND_RELATIONSHIP_PARTNER_ECHO;
import static constants.SimConstants.ROMANCE_SPLIT_ATTENTION_DRAIN;
import static constants.SimConstants.ROMANCE_SPLIT_ATTENTION_ECHO;
import static constants.SimConstants.ROMANCE_STEADY_UNHEALTHY_BREAKUP_MULTIPLIER;

/**
 * Evolves romantic relationships after generation. Two entry points:
 *
 * <ul>
 * <li>{@link #periodPulse}: rolled at every period transition (~7 per school
 * day), so escalations and breakups land throughout the day rather than only
 * at midnight. New crushes can grow out of warm friendships (via
 * {@link CrushDeveloper}), crushes get acted on (or shot down), mutual
 * hookups can become official, one-sided situationships resolve, and couples
 * break up.</li>
 * <li>{@link #endOfDayMaintenance}: deterministic housekeeping run after
 * daily score decay. Statuses whose underlying friendship scores have
 * decayed below their entry thresholds quietly dissolve (crushes fade,
 * hookups drift apart, starving relationships end).</li>
 * </ul>
 *
 * <p>Students have a finite pool of romantic feelings: a side crush or
 * second relationship cools every other fling/steady they hold (score drain
 * on those bonds). Partnered students may escalate a <i>mutual</i> crush
 * into a concurrent relationship when the crush is warm enough relative to
 * the existing bond; one-sided partnered crushes stay latent but still
 * drain attention from the first relationship.</p>
 *
 * <p>Every change is recorded as a human-readable event (also logged via
 * {@link GameLogger#logSocialLinks}) and queued for the day; a future daily
 * summary feature can drain the queue with {@link #drainDaysEvents()}.</p>
 */
public final class RomanceUpdater {

    private RomanceUpdater() {
    }

    /** Human-readable romance events accumulated over the current day. */
    private static final List<String> DAYS_EVENTS = new ArrayList<>();

    /**
     * Rolls in-day romance transitions across the student body. Intended to
     * be called once per period transition.
     *
     * @param students  the student population
     * @param connector the social link connector holding graph and romance data
     * @return true if any relationship changed (callers may refresh UI)
     */
    public static boolean periodPulse(HashMap<Integer, Student> students,
            SocialLinkConnector connector) {
        // Keep the stat-standout registry current so fleeting-crush rolls in
        // the InteractionManager (which fire between pulses) see fresh data.
        if (students != null) {
            CrushDeveloper.refreshStandouts(students.values());
        }
        boolean changed = CrushDeveloper.pulseFriendshipCrushes(students, connector);
        changed |= noticeAndEnvyPass(students, connector);
        changed |= forEachPair(students, connector, RomanceUpdater::pulsePair);
        return changed;
    }

    /**
     * Deterministic end-of-day cleanup, run after {@code applyDailyDecay}:
     * removes statuses whose underlying scores no longer support them.
     *
     * @param students  the student population
     * @param connector the social link connector holding graph and romance data
     * @return true if any relationship changed
     */
    public static boolean endOfDayMaintenance(HashMap<Integer, Student> students,
            SocialLinkConnector connector) {
        return forEachPair(students, connector, RomanceUpdater::maintainPair);
    }

    /**
     * Returns and clears the day's accumulated romance events. Intended for
     * the (future) end-of-day summary; safe to call at any time.
     *
     * @return the events recorded since the last drain, oldest first
     */
    public static List<String> drainDaysEvents() {
        List<String> drained = new ArrayList<>(DAYS_EVENTS);
        DAYS_EVENTS.clear();
        return drained;
    }

    /**
     * Records a romance-adjacent event raised outside this class (e.g. the
     * InteractionManager resolving a badmouthing attempt) so it lands in the
     * same daily digest and social-link log as native romance events.
     *
     * @param event human-readable event text
     */
    public static void recordExternalEvent(String event) {
        recordEvent(event);
    }

    // ---- Jealousy: couple discovery and envy toward the rival ----

    /**
     * Perception-gated discovery of couples plus the passive jealousy that
     * follows. For every non-hidden crush whose target is in an observable
     * couple with someone else, the crush holder either rolls to notice the
     * couple (recording knowledge, taking an immediate dislike to the rival)
     * or -- once they know -- quietly resents the rival a little more each
     * pulse. Long unrequited crushes therefore grind the holder's opinion of
     * the rival toward dislike/enemy territory on their own.
     *
     * @param students  the student population
     * @param connector the social link connector holding graph and romance data
     * @return true if any couple was newly discovered
     */
    private static boolean noticeAndEnvyPass(HashMap<Integer, Student> students,
            SocialLinkConnector connector) {
        if (students == null || students.isEmpty() || connector == null) {
            return false;
        }
        boolean changed = false;
        for (Student holder : new ArrayList<>(students.values())) {
            for (Student crush : connector.getRomanticInterests(holder)) {
                if (connector.getRomanticStatus(holder, crush) != RomanticStatus.CRUSH
                        || isSecretCrush(holder, crush)) {
                    continue;
                }
                for (Student rival : connector.getRomanticInterests(crush)) {
                    if (rival.equals(holder) || !connector.isObservableCouple(crush, rival)) {
                        continue;
                    }
                    if (connector.knowsAboutCouple(holder, crush, rival)) {
                        dripJealousy(holder, crush, rival, connector);
                    } else if (GameRandom.nextDouble() < noticeChance(holder, crush, rival, connector)) {
                        connector.recordCoupleKnowledge(holder, crush, rival);
                        connector.modifySocialScore(holder, rival,
                                -ROMANCE_JEALOUSY_DISCOVERY_STING);
                        recordEvent(name(holder) + " noticed " + name(crush) + " is with "
                                + name(rival) + " and felt a pang of jealousy.");
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Chance per pulse that the holder notices the (crush, rival) couple:
     * base chance scaled by how visible the couple is (steady pairs act
     * couple-y in public, fling-level pairs are sneakier) and by the
     * holder's perception, capped so even eagle-eyed students take a few
     * pulses on average.
     */
    private static double noticeChance(Student holder, Student crush, Student rival,
            SocialLinkConnector connector) {
        boolean steady = connector.getRomanticStatus(crush, rival) == RomanticStatus.STEADY
                || connector.getRomanticStatus(rival, crush) == RomanticStatus.STEADY;
        double visibility = steady ? 1.0 : ROMANCE_NOTICE_FLING_VISIBILITY;
        double perceptionFactor = 0.5 + holder.studentStatistics.getPerception() / 100.0;
        return Math.min(ROMANCE_NOTICE_CHANCE_MAX,
                ROMANCE_NOTICE_BASE_CHANCE * visibility * perceptionFactor);
    }

    /**
     * Per-pulse resentment drip from a jealous crush holder toward the
     * rival, scaled by how warm the holder still is on the crush: stronger
     * feelings breed stronger resentment of whoever is in the way.
     */
    private static void dripJealousy(Student holder, Student crush, Student rival,
            SocialLinkConnector connector) {
        double crushWarmth = Math.max(0, connector.getSocialScore(holder, crush));
        double drip = ROMANCE_JEALOUSY_DRIP * (0.5 + crushWarmth / 100.0);
        connector.modifySocialScore(holder, rival, -drip);
    }

    /**
     * Cools every fling/steady {@code student} holds other than {@code except}
     * by {@code drain} on the outgoing score and {@code echo} on the
     * reciprocal. Models a finite romantic-feelings pool: giving to one bond
     * takes from the others.
     */
    static void drainOtherPartnerships(Student student, Student except,
            SocialLinkConnector connector, double drain, double echo) {
        for (Student partner : connector.getRomanticInterests(student)) {
            if (partner.equals(except)) {
                continue;
            }
            RomanticStatus status = connector.getRomanticStatus(student, partner);
            if (status != RomanticStatus.FLING && status != RomanticStatus.STEADY) {
                continue;
            }
            connector.modifySocialScore(student, partner, -drain);
            if (echo > 0) {
                connector.modifySocialScore(partner, student, -echo);
            }
        }
    }

    /**
     * Visits every unordered pair with a romance record exactly once.
     * Records are directed and possibly one-sided, so pairs are deduped by
     * unordered pair identity rather than by visited student.
     */
    private static boolean forEachPair(HashMap<Integer, Student> students,
            SocialLinkConnector connector, PairProcessor processor) {
        if (students == null || students.isEmpty() || connector == null) {
            return false;
        }
        boolean changed = false;
        Set<Set<Student>> processed = new HashSet<>();
        for (Student student : new ArrayList<>(students.values())) {
            for (Student other : connector.getRomanticInterests(student)) {
                Set<Student> pairKey = new HashSet<>(List.of(student, other));
                if (!processed.add(pairKey)) {
                    continue;
                }
                changed |= processor.process(student, other, connector);
            }
        }
        return changed;
    }

    @FunctionalInterface
    private interface PairProcessor {
        boolean process(Student a, Student b, SocialLinkConnector connector);
    }

    // ---- In-day pulse transitions ----

    /** Dispatches one pair to the matching pulse rule. Returns true on change. */
    private static boolean pulsePair(Student a, Student b, SocialLinkConnector connector) {
        RomanticStatus ab = connector.getRomanticStatus(a, b);
        RomanticStatus ba = connector.getRomanticStatus(b, a);

        // Serious on at least one side: convergence/breakup dynamics
        if (ab == RomanticStatus.STEADY || ba == RomanticStatus.STEADY) {
            dripSplitAttention(a, b, connector);
            return pulseSteady(a, b, ab, ba, connector);
        }
        // Mutual hookup: may become official or fizzle
        if (ab == RomanticStatus.FLING && ba == RomanticStatus.FLING) {
            dripSplitAttention(a, b, connector);
            return pulseMutualFling(a, b, connector);
        }
        // One-sided hookup: holder either makes it mutual or gives up
        if (ab == RomanticStatus.FLING || ba == RomanticStatus.FLING) {
            Student holder = ab == RomanticStatus.FLING ? a : b;
            Student target = holder == a ? b : a;
            dripSplitAttention(holder, target, connector);
            return pulseOneSidedFling(holder, target, connector);
        }
        // Crushes (one-sided or, defensively, mutual)
        if (ab == RomanticStatus.CRUSH && ba == RomanticStatus.CRUSH) {
            return pulseMutualCrush(a, b, connector);
        }
        if (ab == RomanticStatus.CRUSH || ba == RomanticStatus.CRUSH) {
            Student holder = ab == RomanticStatus.CRUSH ? a : b;
            Student target = holder == a ? b : a;
            return pulseCrush(holder, target, connector);
        }
        return false;
    }

    /**
     * Mutual crushes may escalate into a hookup. When either party already
     * has another partnership, the roll is scaled by crush warmth versus
     * existing-bond warmth, and success drains those other bonds.
     */
    private static boolean pulseMutualCrush(Student a, Student b,
            SocialLinkConnector connector) {
        boolean aPartnered = hasOtherPartnership(a, b, connector);
        boolean bPartnered = hasOtherPartnership(b, a, connector);
        if (aPartnered) {
            drainOtherPartnerships(a, b, connector,
                    ROMANCE_SPLIT_ATTENTION_DRAIN, ROMANCE_SPLIT_ATTENTION_ECHO);
        }
        if (bPartnered) {
            drainOtherPartnerships(b, a, connector,
                    ROMANCE_SPLIT_ATTENTION_DRAIN, ROMANCE_SPLIT_ATTENTION_ECHO);
        }

        double chance = ROMANCE_PULSE_CRUSH_ACT_CHANCE * 2;
        if (aPartnered || bPartnered) {
            chance *= partneredEscalateFactor(a, b, connector);
        }
        if (GameRandom.nextDouble() >= chance) {
            return false;
        }
        if (aPartnered) {
            drainOtherPartnerships(a, b, connector,
                    ROMANCE_SECOND_RELATIONSHIP_PARTNER_DRAIN,
                    ROMANCE_SECOND_RELATIONSHIP_PARTNER_ECHO);
        }
        if (bPartnered) {
            drainOtherPartnerships(b, a, connector,
                    ROMANCE_SECOND_RELATIONSHIP_PARTNER_DRAIN,
                    ROMANCE_SECOND_RELATIONSHIP_PARTNER_ECHO);
        }
        String flavor = (aPartnered || bPartnered)
                ? " (mutual crushes won out over an existing bond)"
                : " (mutual crushes finally acted on)";
        startHookingUp(a, b, connector, flavor);
        return true;
    }

    /**
     * A crush holder may act on it. Success requires the target to be
     * attracted back and warm enough toward the holder; failure removes the
     * crush and stings. Hidden same-gender crushes held by closeted students
     * are never acted on. Partnered holders do not escalate one-sided
     * crushes (only mutual crushes can become a second relationship) but
     * still drip attention away from their existing bond.
     */
    private static boolean pulseCrush(Student holder, Student target,
            SocialLinkConnector connector) {
        if (isSecretCrush(holder, target)) {
            return false;
        }
        if (hasOtherPartnership(holder, target, connector)) {
            drainOtherPartnerships(holder, target, connector,
                    ROMANCE_SPLIT_ATTENTION_DRAIN, ROMANCE_SPLIT_ATTENTION_ECHO);
            return false;
        }
        if (GameRandom.nextDouble() >= ROMANCE_PULSE_CRUSH_ACT_CHANCE) {
            return false;
        }
        boolean accepted = RomanceAssigner.attractedTo(target, holder)
                && connector.getSocialScore(target, holder) >= ROMANCE_MUTUAL_MIN_SCORE;
        if (accepted) {
            if (hasOtherPartnership(target, holder, connector)) {
                // Target is leaving bandwidth for this new bond
                drainOtherPartnerships(target, holder, connector,
                        ROMANCE_SECOND_RELATIONSHIP_PARTNER_DRAIN,
                        ROMANCE_SECOND_RELATIONSHIP_PARTNER_ECHO);
                startHookingUp(holder, target, connector,
                        " (a crush paid off despite an existing bond)");
            } else {
                startHookingUp(holder, target, connector, " (a crush paid off)");
            }
        } else {
            connector.setRomanticStatus(holder, target, RomanticStatus.NONE);
            connector.modifySocialScore(holder, target, -ROMANCE_REJECTION_SCORE_PENALTY);
            recordEvent(name(holder) + " got shot down by " + name(target) + ".");
        }
        return true;
    }

    /**
     * Scales mutual-crush escalation chance when at least one party is
     * already partnered: hotter mutual crush vs cooler existing bond raises
     * the factor toward {@link constants.SimConstants#ROMANCE_PARTNERED_ESCALATE_CHANCE_MAX}.
     */
    private static double partneredEscalateFactor(Student a, Student b,
            SocialLinkConnector connector) {
        double crushWarmth = (connector.getSocialScore(a, b)
                + connector.getSocialScore(b, a)) / 2.0;
        double partnerWarmth = Math.max(
                strongestOtherPartnershipWarmth(a, b, connector),
                strongestOtherPartnershipWarmth(b, a, connector));
        double crushShare = crushWarmth / (crushWarmth + Math.max(partnerWarmth, 1.0));
        return ROMANCE_PARTNERED_ESCALATE_CHANCE_MIN
                + (ROMANCE_PARTNERED_ESCALATE_CHANCE_MAX - ROMANCE_PARTNERED_ESCALATE_CHANCE_MIN)
                * crushShare;
    }

    /**
     * Average mutual warmth of {@code student}'s strongest fling/steady bond
     * other than {@code except}, or 0 if none.
     */
    private static double strongestOtherPartnershipWarmth(Student student, Student except,
            SocialLinkConnector connector) {
        double best = 0;
        for (Student partner : connector.getRomanticInterests(student)) {
            if (partner.equals(except)) {
                continue;
            }
            RomanticStatus status = connector.getRomanticStatus(student, partner);
            if (status != RomanticStatus.FLING && status != RomanticStatus.STEADY) {
                continue;
            }
            double warmth = (connector.getSocialScore(student, partner)
                    + connector.getSocialScore(partner, student)) / 2.0;
            if (warmth > best) {
                best = warmth;
            }
        }
        return best;
    }

    /**
     * Whether {@code student} reports a fling/steady with someone other than
     * {@code except}.
     */
    private static boolean hasOtherPartnership(Student student, Student except,
            SocialLinkConnector connector) {
        for (Student other : connector.getRomanticInterests(student)) {
            if (other.equals(except)) {
                continue;
            }
            RomanticStatus status = connector.getRomanticStatus(student, other);
            if (status == RomanticStatus.FLING || status == RomanticStatus.STEADY) {
                return true;
            }
        }
        return false;
    }

    /**
     * While a fling/steady pair is pulsed, each member who also holds another
     * partnership drips attention away from those other bonds.
     */
    private static void dripSplitAttention(Student a, Student b,
            SocialLinkConnector connector) {
        if (hasOtherPartnership(a, b, connector)) {
            drainOtherPartnerships(a, b, connector,
                    ROMANCE_SPLIT_ATTENTION_DRAIN, ROMANCE_SPLIT_ATTENTION_ECHO);
        }
        if (hasOtherPartnership(b, a, connector)) {
            drainOtherPartnerships(b, a, connector,
                    ROMANCE_SPLIT_ATTENTION_DRAIN, ROMANCE_SPLIT_ATTENTION_ECHO);
        }
    }

    /** Mutual FWB pair: chance to become official, or to fizzle out. */
    private static boolean pulseMutualFling(Student a, Student b,
            SocialLinkConnector connector) {
        boolean warmEnough = connector.getSocialScore(a, b) >= ROMANCE_FLING_OFFICIAL_MIN_SCORE
                && connector.getSocialScore(b, a) >= ROMANCE_FLING_OFFICIAL_MIN_SCORE;
        if (warmEnough && GameRandom.nextDouble() < ROMANCE_PULSE_FLING_OFFICIAL_CHANCE) {
            makeOfficial(a, b, connector, " -- their hookup got serious");
            return true;
        }
        if (GameRandom.nextDouble() < ROMANCE_PULSE_FLING_FIZZLE_CHANCE) {
            clearPair(a, b, connector);
            recordEvent(name(a) + " and " + name(b) + " stopped hooking up.");
            return true;
        }
        return false;
    }

    /**
     * One participant thinks they're hooking up, the other reports nothing.
     * The holder either succeeds in making it mutual (same gate as acting on
     * a crush) or gives up; one-sided hookups fizzle twice as readily as
     * mutual ones.
     */
    private static boolean pulseOneSidedFling(Student holder, Student target,
            SocialLinkConnector connector) {
        if (GameRandom.nextDouble() < ROMANCE_PULSE_CRUSH_ACT_CHANCE) {
            boolean accepted = RomanceAssigner.attractedTo(target, holder)
                    && connector.getSocialScore(target, holder) >= ROMANCE_MUTUAL_MIN_SCORE;
            if (accepted) {
                if (hasOtherPartnership(target, holder, connector)) {
                    drainOtherPartnerships(target, holder, connector,
                            ROMANCE_SECOND_RELATIONSHIP_PARTNER_DRAIN,
                            ROMANCE_SECOND_RELATIONSHIP_PARTNER_ECHO);
                }
                connector.setRomanticStatus(target, holder, RomanticStatus.FLING);
                recordEvent(name(holder) + " and " + name(target)
                        + " are now both calling it FWB.");
            } else {
                connector.setRomanticStatus(holder, target, RomanticStatus.NONE);
                connector.modifySocialScore(holder, target, -ROMANCE_REJECTION_SCORE_PENALTY);
                recordEvent(name(holder) + " realized nothing was going on with "
                        + name(target) + ".");
            }
            return true;
        }
        if (GameRandom.nextDouble() < ROMANCE_PULSE_FLING_FIZZLE_CHANCE * 2) {
            clearPair(holder, target, connector);
            recordEvent(name(holder) + " quietly gave up on " + name(target) + ".");
            return true;
        }
        return false;
    }

    /**
     * At least one side considers the pair serious. Mutual official couples
     * have a small baseline breakup chance (multiplied when the bond has
     * decayed below friend level); asymmetric pairs either converge to
     * official or blow up when the mismatch surfaces.
     */
    private static boolean pulseSteady(Student a, Student b, RomanticStatus ab,
            RomanticStatus ba, SocialLinkConnector connector) {
        if (ab == RomanticStatus.STEADY && ba == RomanticStatus.STEADY) {
            boolean unhealthy = connector.getSocialScore(a, b) < ROMANCE_MUTUAL_MIN_SCORE
                    || connector.getSocialScore(b, a) < ROMANCE_MUTUAL_MIN_SCORE;
            double chance = ROMANCE_PULSE_STEADY_BREAKUP_CHANCE
                    * (unhealthy ? ROMANCE_STEADY_UNHEALTHY_BREAKUP_MULTIPLIER : 1);
            if (GameRandom.nextDouble() < chance) {
                breakUp(a, b, connector, " broke up");
                return true;
            }
            return false;
        }

        // Asymmetric: identify who holds the serious view
        Student believer = ab == RomanticStatus.STEADY ? a : b;
        Student partner = believer == a ? b : a;
        RomanticStatus partnerView = connector.getRomanticStatus(partner, believer);

        if (partnerView == RomanticStatus.FLING
                && connector.getSocialScore(partner, believer) >= ROMANCE_MUTUAL_MIN_SCORE
                && GameRandom.nextDouble() < ROMANCE_PULSE_ASYM_CONVERGE_CHANCE) {
            makeOfficial(believer, partner, connector, " -- they finally agreed it's serious");
            return true;
        }
        if (GameRandom.nextDouble() < ROMANCE_PULSE_ASYM_BREAKUP_CHANCE) {
            breakUp(believer, partner, connector,
                    " split up over where things were going");
            return true;
        }
        return false;
    }

    // ---- End-of-day maintenance ----

    /**
     * Deterministic threshold checks after decay: statuses dissolve when the
     * friendship underneath can no longer support them. Returns true on change.
     */
    private static boolean maintainPair(Student a, Student b, SocialLinkConnector connector) {
        RomanticStatus ab = connector.getRomanticStatus(a, b);
        RomanticStatus ba = connector.getRomanticStatus(b, a);
        double scoreAb = connector.getSocialScore(a, b);
        double scoreBa = connector.getSocialScore(b, a);

        // Serious pairs starve out only when BOTH directions fall below the
        // acquaintance floor (the slow steady decay makes this rare)
        if (ab == RomanticStatus.STEADY || ba == RomanticStatus.STEADY) {
            if (scoreAb < ROMANCE_CRUSH_MIN_SCORE && scoreBa < ROMANCE_CRUSH_MIN_SCORE) {
                breakUp(a, b, connector, " drifted apart and broke up");
                return true;
            }
            return false;
        }
        // Hookups need both parties at least acquaintance-warm
        if (ab == RomanticStatus.FLING || ba == RomanticStatus.FLING) {
            if (scoreAb < ROMANCE_CRUSH_MIN_SCORE || scoreBa < ROMANCE_CRUSH_MIN_SCORE) {
                clearPair(a, b, connector);
                recordEvent(name(a) + " and " + name(b) + " drifted apart.");
                return true;
            }
            return false;
        }
        // Crushes fade once the holder's own warmth drops below entry level
        boolean changed = false;
        if (ab == RomanticStatus.CRUSH && scoreAb < ROMANCE_CRUSH_MIN_SCORE) {
            connector.setRomanticStatus(a, b, RomanticStatus.NONE);
            recordEvent(name(a) + "'s crush on " + name(b) + " faded.");
            changed = true;
        }
        if (ba == RomanticStatus.CRUSH && scoreBa < ROMANCE_CRUSH_MIN_SCORE) {
            connector.setRomanticStatus(b, a, RomanticStatus.NONE);
            recordEvent(name(b) + "'s crush on " + name(a) + " faded.");
            changed = true;
        }
        return changed;
    }

    // ---- Shared transition helpers ----

    /** Both directions become FLING: the pair starts hooking up. */
    private static void startHookingUp(Student a, Student b,
            SocialLinkConnector connector, String flavor) {
        connector.setRomanticStatus(a, b, RomanticStatus.FLING);
        connector.setRomanticStatus(b, a, RomanticStatus.FLING);
        recordEvent(name(a) + " and " + name(b) + " started hooking up" + flavor + ".");
    }

    /**
     * Both directions become STEADY and each side applies its steady score
     * bonus (smaller for openly sexual-minority males).
     */
    private static void makeOfficial(Student a, Student b,
            SocialLinkConnector connector, String flavor) {
        connector.setRomanticStatus(a, b, RomanticStatus.STEADY);
        connector.setRomanticStatus(b, a, RomanticStatus.STEADY);
        connector.modifySocialScore(a, b, RomanceAssigner.steadyBonusFor(a));
        connector.modifySocialScore(b, a, RomanceAssigner.steadyBonusFor(b));
        recordEvent(name(a) + " and " + name(b) + " are official" + flavor + ".");
    }

    /** Clears both directions and applies the mutual breakup score penalty. */
    private static void breakUp(Student a, Student b,
            SocialLinkConnector connector, String flavor) {
        clearPair(a, b, connector);
        connector.modifySocialScore(a, b, -ROMANCE_BREAKUP_SCORE_PENALTY);
        connector.modifySocialScore(b, a, -ROMANCE_BREAKUP_SCORE_PENALTY);
        recordEvent(name(a) + " and " + name(b) + flavor + ".");
    }

    private static void clearPair(Student a, Student b, SocialLinkConnector connector) {
        connector.setRomanticStatus(a, b, RomanticStatus.NONE);
        connector.setRomanticStatus(b, a, RomanticStatus.NONE);
        // The couple no longer exists, so peers' knowledge of it (and the
        // jealousy it fueled) dissolves with it.
        connector.clearCoupleKnowledge(a, b);
    }

    /**
     * Hidden same-gender crushes held by closeted students are never acted
     * on (or visibly reacted to): doing so would out them. Also used by the
     * jealousy behavior branch to keep closeted students from acting on
     * rivalries over a secret crush.
     *
     * @param holder the crush holder
     * @param target the crush target
     * @return true if the crush must stay hidden
     */
    public static boolean isSecretCrush(Student holder, Student target) {
        if (!RomanceAssigner.isClosetedNonHetero(holder)) {
            return false;
        }
        String holderGender = holder.studentStatistics.getGender();
        String targetGender = target.studentStatistics.getGender();
        return holderGender != null && holderGender.equalsIgnoreCase(targetGender);
    }

    private static void recordEvent(String event) {
        DAYS_EVENTS.add(event);
        GameLogger.logSocialLinks("[Romance] " + event);
    }

    private static String name(Student student) {
        return student.studentName.getFullName();
    }
}
