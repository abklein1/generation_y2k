package utility;

import entity.OrientationDisclosure;
import entity.SexualOrientation;
import entity.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static constants.SimConstants.ORIENTATION_ASEXUAL_WEIGHT;
import static constants.SimConstants.ORIENTATION_BISEXUAL_WEIGHT;
import static constants.SimConstants.ORIENTATION_CLOSETED_NON_HETERO_RATE;
import static constants.SimConstants.ORIENTATION_GAY_WEIGHT;
import static constants.SimConstants.ORIENTATION_OPEN_NON_HETERO_RATE;
import static constants.SimConstants.ORIENTATION_OUT_GROUP_SELECTION_WEIGHT;

/**
 * Assigns a sexual orientation and disclosure state to every student using
 * cohort target counts derived from the 2004-era simulation parameters in
 * {@code SimConstants}.
 *
 * <p>The school-wide totals are fixed up front (~1% openly non-heterosexual,
 * ~5% closeted) rather than rolled per student, so a generated school always
 * lands on the configured demographic mix. Which students fill the
 * non-heterosexual cohort is a weighted sample without replacement:
 * members of out-group cliques (loaded via {@link CliqueLoader}) carry a
 * higher selection weight, concentrating the cohort there without changing
 * the totals.</p>
 *
 * <p>Must run <i>after</i> {@link CliqueAssigner#assignCliques} so the
 * out-group weighting can see each student's main clique. Orientation does
 * not influence platonic friendship generation; it exists as the
 * demographic foundation for future romantic relationship mechanics.</p>
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
        for (int i = 0; i < cohort.size(); i++) {
            Student student = cohort.get(i);
            student.studentStatistics.setSexualOrientation(pickNonHeteroOrientation());
            if (i < openTarget) {
                student.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);
                openAssigned++;
            } else {
                student.studentStatistics.setOrientationDisclosure(OrientationDisclosure.CLOSETED);
            }
        }

        logSummary(total, openAssigned, cohort.size() - openAssigned);
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
