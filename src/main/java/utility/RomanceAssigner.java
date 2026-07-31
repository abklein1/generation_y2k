package utility;

import entity.OrientationDisclosure;
import entity.RomanticStatus;
import entity.SexualOrientation;
import entity.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static constants.SimConstants.ROMANCE_ASEXUAL_PARTICIPATION_MULTIPLIER;
import static constants.SimConstants.ROMANCE_CLOSETED_HIDDEN_CRUSH_CHANCE;
import static constants.SimConstants.ROMANCE_CRUSH_ADJACENT_GRADE_FACTOR;
import static constants.SimConstants.ROMANCE_CRUSH_CLIQUE_ALIGNS;
import static constants.SimConstants.ROMANCE_CRUSH_CLIQUE_HATE;
import static constants.SimConstants.ROMANCE_CRUSH_CLIQUE_NEGATIVE;
import static constants.SimConstants.ROMANCE_CRUSH_CLIQUE_POSITIVE;
import static constants.SimConstants.ROMANCE_CRUSH_CLIQUE_SAME;
import static constants.SimConstants.ROMANCE_CRUSH_DESIRABILITY_EXPONENT;
import static constants.SimConstants.ROMANCE_CRUSH_DISTANT_GRADE_FACTOR;
import static constants.SimConstants.ROMANCE_CRUSH_FAMILIARITY_DIVISOR;
import static constants.SimConstants.ROMANCE_CRUSH_IN_GROUP_DESIRABILITY;
import static constants.SimConstants.ROMANCE_CRUSH_MIN_SCORE;
import static constants.SimConstants.ROMANCE_CRUSH_MAGNET_CURVE;
import static constants.SimConstants.ROMANCE_CRUSH_MAGNET_RANGE;
import static constants.SimConstants.ROMANCE_CRUSH_NEW_LINK_EXTRA_MAX;
import static constants.SimConstants.ROMANCE_CRUSH_OUT_GROUP_DESIRABILITY;
import static constants.SimConstants.ROMANCE_GRADE_FACTOR_FRESHMAN;
import static constants.SimConstants.ROMANCE_GRADE_FACTOR_JUNIOR;
import static constants.SimConstants.ROMANCE_GRADE_FACTOR_SENIOR;
import static constants.SimConstants.ROMANCE_GRADE_FACTOR_SOPHOMORE;
import static constants.SimConstants.ROMANCE_MALE_CRUSH_SHARE;
import static constants.SimConstants.ROMANCE_MUTUAL_MIN_SCORE;
import static constants.SimConstants.ROMANCE_PARTICIPATION_RATE;
import static constants.SimConstants.ROMANCE_PERCEPTION_MISMATCH_CHANCE;
import static constants.SimConstants.ROMANCE_STEADY_SCORE_BONUS;
import static constants.SimConstants.ROMANCE_STEADY_SCORE_BONUS_SM_MALE;
import static constants.SimConstants.ROMANCE_TYPE_CRUSH_WEIGHT;
import static constants.SimConstants.ROMANCE_TYPE_FLING_WEIGHT;
import static constants.SimConstants.ROMANCE_TYPE_STEADY_WEIGHT;

/**
 * Generates romantic relationships during school generation. Mutual forms
 * (flings and steady pairs) promote existing friendships: both directions
 * must already hold friend-level links. Crushes are different: they are
 * picked from the whole orientation-compatible student body, weighted by
 * grade proximity, familiarity (an existing positive link helps but is not
 * required), and the target's <i>desirability</i> (school-wide popularity
 * and clique standing, raised to an exponent). Desirable students therefore
 * collect many admirers ("crush magnets") while a student with no friends
 * at all can still quietly pine for someone. Holding a crush creates or
 * raises the outgoing social link -- you like whoever you pine for -- so
 * crush decay and escalation mechanics work even for near-strangers.
 *
 * <p>Roughly {@code ROMANCE_PARTICIPATION_RATE} of the student body ends up
 * holding some form of romance record (crush, fling, or steady), scaled
 * mildly by grade so seniors date more than freshmen. Perception is stored
 * per direction: a configurable share of fling/steady pairings are
 * asymmetric, with the partner perceiving something weaker (steady seen as
 * a fling, a fling seen as nothing), matching the asymmetry surveys found
 * when both parties of adolescent couples were asked separately.</p>
 *
 * <p>Sexual orientation gates candidate gender. Closeted non-heterosexual
 * students present as straight and can hold opposite-gender "cover"
 * relationships; a small share additionally hold a hidden one-directional
 * same-gender crush that is never mutual and never acted on. Crush-holding
 * skews male ({@code ROMANCE_MALE_CRUSH_SHARE}) because most cross-gender
 * friendships initiated by male students carry a hope of romance. Openly
 * sexual-minority males apply a smaller score bump toward steady partners
 * (lower romantic attachment than heterosexual males).</p>
 *
 * <p>Must run <i>after</i> the {@link SocialLinkConnector} generation pass
 * so friendships exist to be promoted.</p>
 */
public final class RomanceAssigner {

    private RomanceAssigner() {
    }

    /**
     * Assigns generation-time romantic relationships across the student body.
     *
     * @param studentHashMap the student population (post-social-link generation)
     * @param connector      the social link connector holding the friendship graph
     */
    public static void assignRomanticRelationships(HashMap<Integer, Student> studentHashMap,
            SocialLinkConnector connector) {
        if (studentHashMap == null || studentHashMap.isEmpty() || connector == null) {
            return;
        }
        List<Student> students = new ArrayList<>(studentHashMap.values());
        GameRandom.shuffle(students);

        // Popularity percentile snapshot used for crush-target desirability.
        // Taken once: crush links created below shouldn't feed back into
        // later picks. Rank-based so the magnet curve is independent of the
        // generation's absolute score scale.
        HashMap<Student, Double> popularityPercentiles =
                computePopularityPercentiles(connector);

        Set<Student> inRomance = new HashSet<>();
        int target = (int) Math.round(students.size() * ROMANCE_PARTICIPATION_RATE);

        int crushes = 0;
        int flings = 0;
        int steadies = 0;
        int asymmetric = 0;
        int coverRelationships = 0;

        for (Student student : students) {
            if (inRomance.size() >= target) {
                break;
            }
            if (inRomance.contains(student)) {
                continue;
            }
            if (GameRandom.nextDouble() >= attemptChance(student)) {
                continue;
            }

            RomanticStatus type = rollType(student);
            if (type == RomanticStatus.CRUSH) {
                Student crushTarget = pickCrushTarget(student, students, connector,
                        popularityPercentiles, false);
                if (crushTarget == null) {
                    continue;
                }
                connector.setRomanticStatus(student, crushTarget, RomanticStatus.CRUSH);
                ensureCrushLink(student, crushTarget, connector);
                inRomance.add(student);
                crushes++;
                continue;
            }

            Student partner = pickCandidate(student, connector,
                    ROMANCE_MUTUAL_MIN_SCORE, inRomance);
            if (partner == null) {
                continue;
            }

            connector.setRomanticStatus(student, partner, type);
            RomanticStatus perceived = type;
            if (GameRandom.nextDouble() < ROMANCE_PERCEPTION_MISMATCH_CHANCE) {
                perceived = downgrade(type);
                asymmetric++;
            }
            connector.setRomanticStatus(partner, student, perceived);

            inRomance.add(student);
            if (perceived != RomanticStatus.NONE) {
                inRomance.add(partner);
            }
            if (type == RomanticStatus.STEADY) {
                steadies++;
                // Steady partners feel closer to each other; openly
                // sexual-minority males are less attached to romantic
                // partners and apply a smaller bump.
                connector.modifySocialScore(student, partner, steadyBonusFor(student));
                if (perceived == RomanticStatus.STEADY) {
                    connector.modifySocialScore(partner, student, steadyBonusFor(partner));
                }
            } else {
                flings++;
            }
            if (isClosetedNonHetero(student) || isClosetedNonHetero(partner)) {
                coverRelationships++;
            }
        }

        // Hidden same-gender crush pass: closeted gay/bisexual students may
        // quietly hold an unrequited same-gender crush they never act on,
        // regardless of any cover relationship above.
        int hiddenCrushes = 0;
        for (Student student : students) {
            if (!isClosetedNonHetero(student)
                    || student.studentStatistics.getSexualOrientation() == SexualOrientation.ASEXUAL) {
                continue;
            }
            if (GameRandom.nextDouble() >= ROMANCE_CLOSETED_HIDDEN_CRUSH_CHANCE) {
                continue;
            }
            Student crushTarget = pickCrushTarget(student, students, connector,
                    popularityPercentiles, true);
            if (crushTarget == null) {
                continue;
            }
            connector.setRomanticStatus(student, crushTarget, RomanticStatus.CRUSH);
            ensureCrushLink(student, crushTarget, connector);
            hiddenCrushes++;
        }

        logSummary(students.size(), inRomance.size(), crushes, flings, steadies,
                asymmetric, coverRelationships, hiddenCrushes);
    }

    /**
     * Probability that a student attempts to form a romance when their turn
     * comes up. The overall participation level is capped by the school-wide
     * target; this chance shapes *who* participates (grade skew, asexual
     * students opting out far more often).
     */
    private static double attemptChance(Student student) {
        double chance = switch (student.studentStatistics.getGradeLevel() == null
                ? "" : student.studentStatistics.getGradeLevel()) {
            case "Freshman" -> ROMANCE_GRADE_FACTOR_FRESHMAN;
            case "Sophomore" -> ROMANCE_GRADE_FACTOR_SOPHOMORE;
            case "Junior" -> ROMANCE_GRADE_FACTOR_JUNIOR;
            case "Senior" -> ROMANCE_GRADE_FACTOR_SENIOR;
            default -> 1.0;
        };
        if (student.studentStatistics.getSexualOrientation() == SexualOrientation.ASEXUAL) {
            chance *= ROMANCE_ASEXUAL_PARTICIPATION_MULTIPLIER;
        }
        return Math.min(1.0, chance);
    }

    /**
     * Rolls the romance form for one student. The crush weight is biased by
     * gender ({@code ROMANCE_MALE_CRUSH_SHARE}): most cross-gender
     * friendships initiated by males carry a hope of romance, so males hold
     * proportionally more crushes than females. Asexual students never roll
     * flings.
     */
    private static RomanticStatus rollType(Student student) {
        double crushWeight = ROMANCE_TYPE_CRUSH_WEIGHT;
        String gender = student.studentStatistics.getGender();
        if ("male".equalsIgnoreCase(gender)) {
            crushWeight *= 2 * ROMANCE_MALE_CRUSH_SHARE;
        } else if ("female".equalsIgnoreCase(gender)) {
            crushWeight *= 2 * (1 - ROMANCE_MALE_CRUSH_SHARE);
        }
        double flingWeight = ROMANCE_TYPE_FLING_WEIGHT;
        if (student.studentStatistics.getSexualOrientation() == SexualOrientation.ASEXUAL) {
            flingWeight = 0;
        }
        double total = crushWeight + flingWeight + ROMANCE_TYPE_STEADY_WEIGHT;
        double roll = GameRandom.nextDouble() * total;
        if (roll < crushWeight) {
            return RomanticStatus.CRUSH;
        }
        if (roll < crushWeight + flingWeight) {
            return RomanticStatus.FLING;
        }
        return RomanticStatus.STEADY;
    }

    /**
     * Picks a mutual-romance candidate (fling/steady) from the student's
     * existing positive outgoing links, weighted by score (warmer
     * friendships are likelier promotions). Both directions must be
     * romantically compatible and at least {@code minScore}, and the
     * candidate must be free of any existing romance involvement.
     */
    private static Student pickCandidate(Student student, SocialLinkConnector connector,
            double minScore, Set<Student> inRomance) {
        List<Student> candidates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;
        for (Student other : connector.getPositiveConnections(student)) {
            double score = connector.getSocialScore(student, other);
            if (score < minScore) {
                continue;
            }
            if (student.studentStatistics.getSiblingsInSchool().contains(other)) {
                continue;
            }
            if (!attractedTo(student, other)) {
                continue;
            }
            if (inRomance.contains(other) || !attractedTo(other, student)) {
                continue;
            }
            if (connector.getSocialScore(other, student) < minScore) {
                continue;
            }
            candidates.add(other);
            weights.add(score);
            totalWeight += score;
        }
        return weightedPick(candidates, weights, totalWeight);
    }

    /**
     * Picks a crush target from the whole student body -- crushes do not
     * require an existing friendship, so even friendless students can pine
     * for someone. Candidate weight is
     * {@code gradeProximity * familiarity * cliqueAffinity * desirability^exponent}:
     * familiarity favors people the holder already likes, clique affinity
     * softly favors the holder's own social stratum (while leaving
     * cross-strata romance alive), and the desirability exponent
     * concentrates crushes on popular, in-clique students (crush magnets).
     * Candidates the holder actively dislikes (negative outgoing score) are
     * excluded.
     *
     * @param hiddenSameGender when true, targets are gated by gender
     *                         equality using the closeted holder's true
     *                         orientation instead of the presented
     *                         {@link #attractedTo} gate
     */
    private static Student pickCrushTarget(Student student, List<Student> population,
            SocialLinkConnector connector, HashMap<Student, Double> popularityPercentiles,
            boolean hiddenSameGender) {
        String myGender = student.studentStatistics.getGender();
        List<Student> candidates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;
        for (Student other : population) {
            if (other.equals(student)) {
                continue;
            }
            if (student.studentStatistics.getSiblingsInSchool().contains(other)) {
                continue;
            }
            if (hiddenSameGender) {
                String theirGender = other.studentStatistics.getGender();
                if (myGender == null || theirGender == null
                        || !myGender.equalsIgnoreCase(theirGender)) {
                    continue;
                }
            } else if (!attractedTo(student, other)) {
                continue;
            }
            if (connector.getRomanticStatus(student, other) != RomanticStatus.NONE) {
                continue;
            }
            double score = connector.getSocialScore(student, other);
            if (score < 0) {
                continue;
            }
            double familiarity = 1 + score / ROMANCE_CRUSH_FAMILIARITY_DIVISOR;
            double weight = gradeProximityFactor(student, other) * familiarity
                    * crushCliqueAffinity(student, other)
                    * Math.pow(desirability(other, popularityPercentiles),
                            ROMANCE_CRUSH_DESIRABILITY_EXPONENT);
            if (weight <= 0) {
                continue;
            }
            candidates.add(other);
            weights.add(weight);
            totalWeight += weight;
        }
        return weightedPick(candidates, weights, totalWeight);
    }

    /**
     * How crush-worthy a target looks to the school at large: grows steeply
     * with the target's popularity <i>percentile</i> (rank in the school-wide
     * incoming-score ordering, 0..1) and is scaled by clique standing
     * (in-group members are seen as bigger catches, out-group members
     * smaller ones). The steep percentile curve keeps almost all of the
     * bonus in the top decile, producing a handful of crush magnets per
     * generation regardless of how compressed raw scores are.
     */
    static double desirability(Student target, HashMap<Student, Double> popularityPercentiles) {
        double percentile = popularityPercentiles.getOrDefault(target, 0.0);
        double value = 1 + ROMANCE_CRUSH_MAGNET_RANGE
                * Math.pow(percentile, ROMANCE_CRUSH_MAGNET_CURVE);
        String category = CliqueLoader.getGroupCategory(
                target.studentStatistics.getMainClique());
        if ("in-group".equals(category)) {
            value *= ROMANCE_CRUSH_IN_GROUP_DESIRABILITY;
        } else if ("out-group".equals(category)) {
            value *= ROMANCE_CRUSH_OUT_GROUP_DESIRABILITY;
        }
        return value;
    }

    /**
     * Ranks every student by total incoming social score and maps them to a
     * percentile in [0, 1] (most popular = 1). Ties keep list order; with
     * continuous scores that's inconsequential.
     */
    static HashMap<Student, Double> computePopularityPercentiles(SocialLinkConnector connector) {
        HashMap<Student, Double> totals = connector.computeIncomingScoreTotals();
        List<Student> ranked = new ArrayList<>(totals.keySet());
        ranked.sort((a, b) -> Double.compare(totals.get(a), totals.get(b)));
        HashMap<Student, Double> percentiles = new HashMap<>();
        int n = ranked.size();
        for (int i = 0; i < n; i++) {
            percentiles.put(ranked.get(i), n <= 1 ? 1.0 : (double) i / (n - 1));
        }
        return percentiles;
    }

    /**
     * How the holder's clique feels about the target's clique, as a soft
     * multiplier on crush weight. Deliberately much flatter than the
     * friendship affinity so cross-strata romance (in-group falling for
     * out-group and vice versa) stays a real possibility.
     */
    static double crushCliqueAffinity(Student holder, Student target) {
        String mine = holder.studentStatistics.getMainClique();
        String theirs = target.studentStatistics.getMainClique();
        if (mine == null || theirs == null) {
            return 1.0;
        }
        if (mine.equals(theirs)) {
            return ROMANCE_CRUSH_CLIQUE_SAME;
        }
        return switch (CliqueLoader.getRelationship(mine, theirs)) {
            case "Aligns" -> ROMANCE_CRUSH_CLIQUE_ALIGNS;
            case "Positive" -> ROMANCE_CRUSH_CLIQUE_POSITIVE;
            case "Negative" -> ROMANCE_CRUSH_CLIQUE_NEGATIVE;
            case "Hate" -> ROMANCE_CRUSH_CLIQUE_HATE;
            default -> 1.0;
        };
    }

    /** Crushes skew heavily toward the holder's own grade. */
    private static double gradeProximityFactor(Student a, Student b) {
        int gradeA = gradeIndex(a.studentStatistics.getGradeLevel());
        int gradeB = gradeIndex(b.studentStatistics.getGradeLevel());
        if (gradeA < 0 || gradeB < 0) {
            return 1.0;
        }
        int distance = Math.abs(gradeA - gradeB);
        if (distance == 0) {
            return 1.0;
        }
        return distance == 1 ? ROMANCE_CRUSH_ADJACENT_GRADE_FACTOR
                : ROMANCE_CRUSH_DISTANT_GRADE_FACTOR;
    }

    private static int gradeIndex(String grade) {
        return switch (grade == null ? "" : grade) {
            case "Freshman" -> 0;
            case "Sophomore" -> 1;
            case "Junior" -> 2;
            case "Senior" -> 3;
            default -> -1;
        };
    }

    /**
     * Holding a crush means liking the target: if the outgoing link is below
     * crush level (or absent entirely, for a crush from afar), raise it to
     * the crush threshold plus a small random extra so persistence, decay,
     * and act-on-crush mechanics work on a real link.
     */
    private static void ensureCrushLink(Student student, Student target,
            SocialLinkConnector connector) {
        double score = connector.getSocialScore(student, target);
        double desired = ROMANCE_CRUSH_MIN_SCORE
                + GameRandom.nextDouble() * ROMANCE_CRUSH_NEW_LINK_EXTRA_MAX;
        if (score < desired) {
            connector.modifySocialScore(student, target, desired - score);
        }
    }

    private static Student weightedPick(List<Student> candidates, List<Double> weights,
            double totalWeight) {
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

    /**
     * Whether the source could plausibly pursue romance with the target,
     * based on the source's <i>presented</i> orientation: closeted
     * non-heterosexual students present as straight, which is exactly what
     * makes opposite-gender cover relationships possible. Asexual students
     * have no gendered attraction gate in this model (their participation is
     * throttled separately).
     */
    static boolean attractedTo(Student source, Student target) {
        String sourceGender = source.studentStatistics.getGender();
        String targetGender = target.studentStatistics.getGender();
        if (sourceGender == null || targetGender == null) {
            return false;
        }
        boolean sameGender = sourceGender.equalsIgnoreCase(targetGender);
        SexualOrientation orientation = source.studentStatistics.getSexualOrientation();
        if (orientation == null) {
            orientation = SexualOrientation.STRAIGHT;
        }
        if (orientation.isNonHeterosexual() && isClosetedNonHetero(source)
                && orientation != SexualOrientation.ASEXUAL) {
            // Closeted students present as heterosexual (cover relationships)
            orientation = SexualOrientation.STRAIGHT;
        }
        return switch (orientation) {
            case STRAIGHT -> !sameGender;
            case GAY -> sameGender;
            case BISEXUAL, ASEXUAL -> true;
        };
    }

    /**
     * The weaker perception a partner holds when the pair disagrees: a
     * steady relationship is seen as just a fling, and a fling is not
     * considered a relationship at all.
     */
    private static RomanticStatus downgrade(RomanticStatus type) {
        return type == RomanticStatus.STEADY ? RomanticStatus.FLING : RomanticStatus.NONE;
    }

    static boolean isClosetedNonHetero(Student student) {
        SexualOrientation orientation = student.studentStatistics.getSexualOrientation();
        return orientation != null && orientation.isNonHeterosexual()
                && student.studentStatistics.getOrientationDisclosure() == OrientationDisclosure.CLOSETED;
    }

    static double steadyBonusFor(Student student) {
        if (SocialLinkConnector.isOpenSexualMinority(student)
                && "male".equalsIgnoreCase(student.studentStatistics.getGender())) {
            return ROMANCE_STEADY_SCORE_BONUS_SM_MALE;
        }
        return ROMANCE_STEADY_SCORE_BONUS;
    }

    private static void logSummary(int total, int involved, int crushes, int flings,
            int steadies, int asymmetric, int coverRelationships, int hiddenCrushes) {
        GameLogger.logGeneration("=== Romance Assignment Summary ===");
        GameLogger.logGeneration(String.format(
                "Total students: %d | involved in romance: %d (%.1f%%)",
                total, involved, total > 0 ? 100.0 * involved / total : 0));
        GameLogger.logGeneration(String.format(
                "Crushes: %d | hooking up/FWB: %d | going out/official: %d | asymmetric perception: %d"
                        + " | cover relationships: %d | hidden crushes: %d",
                crushes, flings, steadies, asymmetric, coverRelationships, hiddenCrushes));
    }
}
