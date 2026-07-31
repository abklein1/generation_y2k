package utility;

import entity.OrientationDisclosure;
import entity.SexualOrientation;
import entity.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static constants.SimConstants.ORIENTATION_ASEXUAL_WEIGHT;
import static constants.SimConstants.ORIENTATION_BISEXUAL_WEIGHT;
import static constants.SimConstants.ORIENTATION_CLOSETED_CHANCE_IN_GROUP;
import static constants.SimConstants.ORIENTATION_CLOSETED_CHANCE_NEUTRAL;
import static constants.SimConstants.ORIENTATION_CLOSETED_CHANCE_OUT_GROUP;
import static constants.SimConstants.ORIENTATION_CLOSETED_NON_HETERO_RATE;
import static constants.SimConstants.ORIENTATION_GAY_WEIGHT;
import static constants.SimConstants.ORIENTATION_OPEN_NON_HETERO_RATE;
import static constants.SimConstants.ORIENTATION_OUT_GROUP_SELECTION_WEIGHT;

/**
 * Assigns a sexual orientation and disclosure state to every student using
 * cohort target counts derived from the 2004-era simulation parameters in
 * {@code SimConstants}.
 *
 * <p>The school-wide cohort size is fixed up front (~6% non-heterosexual)
 * rather than rolled per student, so a generated school always lands on the
 * configured demographic mix. Which students fill the non-heterosexual
 * cohort is a weighted sample without replacement: members of out-group
 * cliques (loaded via {@link CliqueLoader}) carry a higher selection
 * weight, concentrating the cohort there without changing the total.</p>
 *
 * <p>Disclosure (open vs. closeted) is conditioned on where each cohort
 * member landed socially. In-group cliques are conservative and less
 * accepting of sexual-minority behavior, so members there almost always
 * stay closeted; out-group cliques tolerate openness far more. Students are
 * never relocated between cliques for this -- only the disclosure state
 * adapts to placement, so the school-wide open/closeted split drifts
 * slightly around the historical ~1%/~5% targets depending on where the
 * cohort ended up.</p>
 *
 * <p>Must run <i>after</i> {@link CliqueAssigner#assignCliques} so the
 * out-group weighting and disclosure rolls can see each student's main
 * clique, and <i>before</i> {@link SocialLinkConnector} generation, which
 * uses orientation to adjust same-gender friendship preferences.</p>
 */
public final class OrientationAssigner {

    private OrientationAssigner() {
    }

    /**
     * Assigns orientation and disclosure to every student in the map.
     *
     * @param studentHashMap the student population (post-clique-assignment)
     */
    public static void assignOrientations(HashMap<Integer, Student> studentHashMap) {
        if (studentHashMap == null || studentHashMap.isEmpty()) {
            return;
        }
        List<Student> students = new ArrayList<>(studentHashMap.values());
        int total = students.size();

        // Default: everyone straight and (trivially) open.
        for (Student student : students) {
            student.studentStatistics.setSexualOrientation(SexualOrientation.STRAIGHT);
            student.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);
        }

        int openTarget = (int) Math.round(total * ORIENTATION_OPEN_NON_HETERO_RATE);
        int closetedTarget = (int) Math.round(total * ORIENTATION_CLOSETED_NON_HETERO_RATE);
        int cohortSize = Math.min(openTarget + closetedTarget, total);
        if (cohortSize <= 0) {
            logSummary(total, 0, 0);
            return;
        }

        List<Student> cohort = sampleCohort(students, cohortSize);

        int openAssigned = 0;
        for (Student student : cohort) {
            student.studentStatistics.setSexualOrientation(pickNonHeteroOrientation());
            if (GameRandom.nextDouble() < closetedChanceFor(student)) {
                student.studentStatistics.setOrientationDisclosure(OrientationDisclosure.CLOSETED);
            } else {
                student.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);
                openAssigned++;
            }
        }

        logSummary(total, openAssigned, cohort.size() - openAssigned);
    }

    /**
     * Probability that a non-heterosexual student stays closeted, based on
     * their main clique's group category. Conservative in-groups suppress
     * openness almost entirely; out-groups are the most accepting.
     */
    private static double closetedChanceFor(Student student) {
        String clique = student.studentStatistics.getMainClique();
        String category = clique != null ? CliqueLoader.getGroupCategory(clique) : null;
        if ("in-group".equals(category)) {
            return ORIENTATION_CLOSETED_CHANCE_IN_GROUP;
        }
        if ("out-group".equals(category)) {
            return ORIENTATION_CLOSETED_CHANCE_OUT_GROUP;
        }
        return ORIENTATION_CLOSETED_CHANCE_NEUTRAL;
    }

    /**
     * Weighted sample without replacement. Out-group clique members carry
     * {@code ORIENTATION_OUT_GROUP_SELECTION_WEIGHT}; everyone else 1.0.
     * The returned list is in selection order (already random).
     */
    private static List<Student> sampleCohort(List<Student> students, int cohortSize) {
        List<Student> pool = new ArrayList<>(students);
        List<Double> weights = new ArrayList<>(pool.size());
        double totalWeight = 0;
        for (Student student : pool) {
            double weight = selectionWeight(student);
            weights.add(weight);
            totalWeight += weight;
        }

        List<Student> selected = new ArrayList<>(cohortSize);
        while (selected.size() < cohortSize && !pool.isEmpty()) {
            double roll = GameRandom.nextDouble() * totalWeight;
            double cumulative = 0;
            int pickIndex = pool.size() - 1;
            for (int i = 0; i < pool.size(); i++) {
                cumulative += weights.get(i);
                if (roll < cumulative) {
                    pickIndex = i;
                    break;
                }
            }
            selected.add(pool.remove(pickIndex));
            totalWeight -= weights.remove(pickIndex);
        }
        return selected;
    }

    private static double selectionWeight(Student student) {
        String clique = student.studentStatistics.getMainClique();
        if (clique != null && "out-group".equals(CliqueLoader.getGroupCategory(clique))) {
            return ORIENTATION_OUT_GROUP_SELECTION_WEIGHT;
        }
        return 1.0;
    }

    private static SexualOrientation pickNonHeteroOrientation() {
        double total = ORIENTATION_GAY_WEIGHT + ORIENTATION_BISEXUAL_WEIGHT + ORIENTATION_ASEXUAL_WEIGHT;
        double roll = GameRandom.nextDouble() * total;
        if (roll < ORIENTATION_GAY_WEIGHT) {
            return SexualOrientation.GAY;
        }
        if (roll < ORIENTATION_GAY_WEIGHT + ORIENTATION_BISEXUAL_WEIGHT) {
            return SexualOrientation.BISEXUAL;
        }
        return SexualOrientation.ASEXUAL;
    }

    private static void logSummary(int total, int open, int closeted) {
        GameLogger.logGeneration("=== Orientation Assignment Summary ===");
        GameLogger.logGeneration(String.format(
                "Total students: %d | openly non-heterosexual: %d | closeted: %d | straight: %d",
                total, open, closeted, total - open - closeted));
    }
}
