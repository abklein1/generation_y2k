package utility;

import entity.RomanticStatus;
import entity.Student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static constants.SimConstants.ROMANCE_CRUSH_MIN_SCORE;
import static constants.SimConstants.ROMANCE_FLEETING_CRUSH_CHANCE;
import static constants.SimConstants.ROMANCE_FLEETING_CRUSH_EXTRA_MAX;
import static constants.SimConstants.ROMANCE_MAX_SIMULTANEOUS_CRUSHES;
import static constants.SimConstants.ROMANCE_MUTUAL_MIN_SCORE;
import static constants.SimConstants.ROMANCE_PULSE_FRIENDSHIP_CRUSH_CHANCE;
import static constants.SimConstants.ROMANCE_STANDOUT_MAX_SHARE;
import static constants.SimConstants.ROMANCE_STANDOUT_SD_MULTIPLIER;

/**
 * Creates brand-new crushes while the simulation runs, complementing the
 * generation-time {@link RomanceAssigner} (which seeds the initial romance
 * state) and {@link RomanceUpdater} (which only evolves or dissolves
 * existing records). Two formation paths:
 *
 * <ul>
 * <li><b>Friendship-grown</b> ({@link #pulseFriendshipCrushes}): each period
 * pulse, a student may realize they have feelings for an existing warm
 * friend. Candidates need an outgoing score at friend tier or better, must
 * match the student's (presented) attraction, and warmer links are
 * likelier picks. These crushes sit on an already-strong link, so they are
 * durable.</li>
 * <li><b>Fleeting</b> ({@link #maybeDevelopFleetingCrush}): interacting
 * with one of the school's rare stat standouts (intelligence, charisma, or
 * strength roughly two standard deviations above the school mean, hard
 * capped at {@code ROMANCE_STANDOUT_MAX_SHARE} of the student body) can
 * spark a shallow crush on the spot. The crush link is seeded barely above
 * the crush floor, so without reinforcement daily decay erases it within
 * days via {@link RomanceUpdater#endOfDayMaintenance}.</li>
 * </ul>
 *
 * <p>Both paths respect the simultaneous-crush cap, the sibling exclusion,
 * and the attraction gate (closeted students present as straight, so
 * neither path can accidentally out them with a visible same-gender
 * crush). New crushes feed the existing act/reject/jealousy machinery like
 * any generation-time crush.</p>
 */
public final class CrushDeveloper {

    private CrushDeveloper() {
    }

    /** Current stat standouts, refreshed once per romance pulse. */
    private static final Set<Student> STANDOUTS = new HashSet<>();

    /** The stat that qualified each standout, for event flavor text. */
    private static final HashMap<Student, String> STANDOUT_TRAITS = new HashMap<>();

    /**
     * Recomputes the standout registry from the current population. A
     * student qualifies when intelligence, charisma, or strength is at
     * least {@code ROMANCE_STANDOUT_SD_MULTIPLIER} standard deviations
     * above the school-wide mean for that stat; the pool is then trimmed to
     * the top {@code ROMANCE_STANDOUT_MAX_SHARE} of the student body by
     * z-score so overlapping stat tails can never inflate it.
     *
     * @param students the enrolled student population
     */
    public static void refreshStandouts(Collection<Student> students) {
        STANDOUTS.clear();
        STANDOUT_TRAITS.clear();
        if (students == null || students.size() < 2) {
            return;
        }

        double[][] stats = {
                statsOf(students, StatKind.INTELLIGENCE),
                statsOf(students, StatKind.CHARISMA),
                statsOf(students, StatKind.STRENGTH)};
        String[] traits = {"brilliance", "charm", "athleticism"};
        double[] means = new double[stats.length];
        double[] sds = new double[stats.length];
        for (int s = 0; s < stats.length; s++) {
            means[s] = mean(stats[s]);
            sds[s] = standardDeviation(stats[s], means[s]);
        }

        List<Candidate> qualifying = new ArrayList<>();
        int i = 0;
        for (Student student : students) {
            double bestZ = Double.NEGATIVE_INFINITY;
            String bestTrait = null;
            for (int s = 0; s < stats.length; s++) {
                // A flat stat (sd 0) can crown no standouts
                if (sds[s] <= 0) {
                    continue;
                }
                double z = (stats[s][i] - means[s]) / sds[s];
                if (z > bestZ) {
                    bestZ = z;
                    bestTrait = traits[s];
                }
            }
            if (bestZ >= ROMANCE_STANDOUT_SD_MULTIPLIER) {
                qualifying.add(new Candidate(student, bestZ, bestTrait));
            }
            i++;
        }

        // Hard cap: keep only the most exceptional students when the three
        // stat tails overlap into more than the allowed share.
        int cap = Math.max(1, (int) Math.floor(students.size() * ROMANCE_STANDOUT_MAX_SHARE));
        qualifying.sort((a, b) -> Double.compare(b.zScore, a.zScore));
        for (Candidate candidate : qualifying.subList(0, Math.min(cap, qualifying.size()))) {
            STANDOUTS.add(candidate.student);
            STANDOUT_TRAITS.put(candidate.student, candidate.trait);
        }
    }

    /**
     * Whether the student currently qualifies as a stat standout (capable
     * of sparking fleeting crushes in students they interact with).
     *
     * @param student the student to check
     * @return true if the student is in the standout registry
     */
    public static boolean isStatStandout(Student student) {
        return student != null && STANDOUTS.contains(student);
    }

    /**
     * Friendship-grown crush pass, run once per romance pulse: each student
     * below the crush cap rolls a small chance to develop a crush on one of
     * their warm friends (outgoing score at least
     * {@code ROMANCE_MUTUAL_MIN_SCORE}), weighted toward the warmest links.
     *
     * @param students  the student population
     * @param connector the social link connector holding graph and romance data
     * @return true if any new crush formed
     */
    public static boolean pulseFriendshipCrushes(HashMap<Integer, Student> students,
            SocialLinkConnector connector) {
        if (students == null || students.isEmpty() || connector == null) {
            return false;
        }
        boolean changed = false;
        for (Student student : new ArrayList<>(students.values())) {
            if (GameRandom.nextDouble() >= ROMANCE_PULSE_FRIENDSHIP_CRUSH_CHANCE) {
                continue;
            }
            if (RomanceAssigner.countOutgoingCrushes(student, connector)
                    >= ROMANCE_MAX_SIMULTANEOUS_CRUSHES) {
                continue;
            }
            Student friend = pickWarmFriend(student, connector);
            if (friend == null) {
                continue;
            }
            connector.setRomanticStatus(student, friend, RomanticStatus.CRUSH);
            RomanceUpdater.recordExternalEvent(name(student)
                    + " realized they have feelings for their friend " + name(friend) + ".");
            changed = true;
        }
        return changed;
    }

    /**
     * Fleeting-crush roll, invoked when a confirmed social interaction puts
     * the observer in the presence of {@code other}. Only fires when the
     * other party is a stat standout; on success the observer gains a weak
     * crush whose link sits barely above the crush floor, so it fades
     * within days unless reinforced.
     *
     * @param observer  the student who might be dazzled
     * @param other     the interaction partner
     * @param connector the social link connector holding graph and romance data
     * @return true if a fleeting crush formed
     */
    public static boolean maybeDevelopFleetingCrush(Student observer, Student other,
            SocialLinkConnector connector) {
        if (observer == null || other == null || observer.equals(other) || connector == null) {
            return false;
        }
        if (!isStatStandout(other)) {
            return false;
        }
        if (GameRandom.nextDouble() >= ROMANCE_FLEETING_CRUSH_CHANCE) {
            return false;
        }
        if (RomanceAssigner.countOutgoingCrushes(observer, connector)
                >= ROMANCE_MAX_SIMULTANEOUS_CRUSHES) {
            return false;
        }
        if (!RomanceAssigner.attractedTo(observer, other)
                || observer.studentStatistics.getSiblingsInSchool().contains(other)
                || connector.getRomanticStatus(observer, other) != RomanticStatus.NONE) {
            return false;
        }

        connector.setRomanticStatus(observer, other, RomanticStatus.CRUSH);
        // Seed a deliberately weak link: just above the crush floor, so the
        // crush starves out in a few days of decay unless something feeds it.
        double score = connector.getSocialScore(observer, other);
        double desired = ROMANCE_CRUSH_MIN_SCORE
                + GameRandom.nextDouble() * ROMANCE_FLEETING_CRUSH_EXTRA_MAX;
        if (score < desired) {
            connector.modifySocialScore(observer, other, desired - score);
        }
        String trait = STANDOUT_TRAITS.getOrDefault(other, "presence");
        RomanceUpdater.recordExternalEvent(name(observer) + " was dazzled by "
                + name(other) + "'s " + trait + " and developed a fleeting crush.");
        return true;
    }

    /**
     * Weighted pick of a crush-eligible warm friend: outgoing score at
     * least {@code ROMANCE_MUTUAL_MIN_SCORE}, attraction-compatible, not a
     * sibling, and no existing romance record toward them. Warmer links
     * carry proportionally more weight.
     */
    private static Student pickWarmFriend(Student student, SocialLinkConnector connector) {
        List<Student> candidates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;
        for (Student other : connector.getPositiveConnections(student)) {
            double score = connector.getSocialScore(student, other);
            if (score < ROMANCE_MUTUAL_MIN_SCORE) {
                continue;
            }
            if (!RomanceAssigner.attractedTo(student, other)
                    || student.studentStatistics.getSiblingsInSchool().contains(other)
                    || connector.getRomanticStatus(student, other) != RomanticStatus.NONE) {
                continue;
            }
            candidates.add(other);
            weights.add(score);
            totalWeight += score;
        }
        if (candidates.isEmpty() || totalWeight <= 0) {
            return null;
        }
        double roll = GameRandom.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return candidates.get(i);
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private enum StatKind { INTELLIGENCE, CHARISMA, STRENGTH }

    /**
     * Snapshots one stat across the population into an array whose order
     * matches the population's iteration order (so index i in every stat
     * array refers to the same student).
     */
    private static double[] statsOf(Collection<Student> students, StatKind kind) {
        double[] values = new double[students.size()];
        int i = 0;
        for (Student student : students) {
            values[i++] = switch (kind) {
                case INTELLIGENCE -> student.studentStatistics.getIntelligence();
                case CHARISMA -> student.studentStatistics.getCharisma();
                case STRENGTH -> student.studentStatistics.getStrength();
            };
        }
        return values;
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    /** Population standard deviation around a precomputed mean. */
    private static double standardDeviation(double[] values, double mean) {
        double variance = 0;
        for (double value : values) {
            variance += (value - mean) * (value - mean);
        }
        return Math.sqrt(variance / values.length);
    }

    private record Candidate(Student student, double zScore, String trait) {
    }

    private static String name(Student student) {
        return student.studentName.getFullName();
    }
}
