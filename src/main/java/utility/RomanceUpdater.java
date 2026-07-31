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
import static constants.SimConstants.ROMANCE_MUTUAL_MIN_SCORE;
import static constants.SimConstants.ROMANCE_PULSE_ASYM_BREAKUP_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_ASYM_CONVERGE_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_CRUSH_ACT_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_FLING_FIZZLE_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_FLING_OFFICIAL_CHANCE;
import static constants.SimConstants.ROMANCE_PULSE_STEADY_BREAKUP_CHANCE;
import static constants.SimConstants.ROMANCE_REJECTION_SCORE_PENALTY;
import static constants.SimConstants.ROMANCE_STEADY_UNHEALTHY_BREAKUP_MULTIPLIER;

/**
 * Evolves romantic relationships after generation. Two entry points:
 *
 * <ul>
 * <li>{@link #periodPulse}: rolled at every period transition (~7 per school
 * day), so escalations and breakups land throughout the day rather than only
 * at midnight. Crushes get acted on (or shot down), mutual hookups can become
 * official, one-sided situationships resolve, and couples break up.</li>
 * <li>{@link #endOfDayMaintenance}: deterministic housekeeping run after
 * daily score decay. Statuses whose underlying friendship scores have
 * decayed below their entry thresholds quietly dissolve (crushes fade,
 * hookups drift apart, starving relationships end).</li>
 * </ul>
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
        return forEachPair(students, connector, RomanceUpdater::pulsePair);
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
            return pulseSteady(a, b, ab, ba, connector);
        }
        // Mutual hookup: may become official or fizzle
        if (ab == RomanticStatus.FLING && ba == RomanticStatus.FLING) {
            return pulseMutualFling(a, b, connector);
        }
        // One-sided hookup: holder either makes it mutual or gives up
        if (ab == RomanticStatus.FLING || ba == RomanticStatus.FLING) {
            Student holder = ab == RomanticStatus.FLING ? a : b;
            Student target = holder == a ? b : a;
            return pulseOneSidedFling(holder, target, connector);
        }
        // Crushes (one-sided or, defensively, mutual)
        if (ab == RomanticStatus.CRUSH && ba == RomanticStatus.CRUSH) {
            // Mutual crushes always escalate once either notices
            if (GameRandom.nextDouble() < ROMANCE_PULSE_CRUSH_ACT_CHANCE * 2) {
                startHookingUp(a, b, connector, " (mutual crushes finally acted on)");
                return true;
            }
            return false;
        }
        if (ab == RomanticStatus.CRUSH || ba == RomanticStatus.CRUSH) {
            Student holder = ab == RomanticStatus.CRUSH ? a : b;
            Student target = holder == a ? b : a;
            return pulseCrush(holder, target, connector);
        }
        return false;
    }

    /**
     * A crush holder may act on it. Success requires the target to be
     * attracted back, warm enough toward the holder, and romantically
     * unattached; failure removes the crush and stings. Hidden same-gender
     * crushes held by closeted students are never acted on.
     */
    private static boolean pulseCrush(Student holder, Student target,
            SocialLinkConnector connector) {
        if (isSecret(holder, target)) {
            return false;
        }
        if (GameRandom.nextDouble() >= ROMANCE_PULSE_CRUSH_ACT_CHANCE) {
            return false;
        }
        boolean accepted = RomanceAssigner.attractedTo(target, holder)
                && connector.getSocialScore(target, holder) >= ROMANCE_MUTUAL_MIN_SCORE
                && !connector.hasMutualRomance(target);
        if (accepted) {
            startHookingUp(holder, target, connector, " (a crush paid off)");
        } else {
            connector.setRomanticStatus(holder, target, RomanticStatus.NONE);
            connector.modifySocialScore(holder, target, -ROMANCE_REJECTION_SCORE_PENALTY);
            recordEvent(name(holder) + " got shot down by " + name(target) + ".");
        }
        return true;
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
                    && connector.getSocialScore(target, holder) >= ROMANCE_MUTUAL_MIN_SCORE
                    && !connector.hasMutualRomance(target);
            if (accepted) {
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
    }

    /**
     * Hidden same-gender crushes held by closeted students are never acted
     * on: doing so would out them.
     */
    private static boolean isSecret(Student holder, Student target) {
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
