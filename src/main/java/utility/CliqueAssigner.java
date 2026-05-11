package utility;

import entity.Student;
import view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static constants.SimConstants.*;

/**
 * Assigns a main clique, subgroup label, and optional secondary clique
 * to every student in the population based on the distribution weights
 * defined in {@code SimConstants} and the group categories loaded by
 * {@link CliqueLoader}.
 *
 * <p>Certain cliques have demographic biases that make them more likely
 * to draw students matching particular traits (race, income, gender,
 * physical stats). The target distribution is still honoured; biases
 * only influence <em>which</em> students fill each clique's slots.</p>
 */
public final class CliqueAssigner {

    private CliqueAssigner() {
    }

    /**
     * Assigns cliques to all students in the provided map.
     * Students are shuffled, then each is assigned a clique via
     * weighted random selection that combines the remaining target
     * capacity with a per-student demographic suitability multiplier.
     *
     * @param studentHashMap the student population
     * @param view           the game view (for logging consistency)
     */
    public static void assignCliques(
            HashMap<Integer, Student> studentHashMap, GameView view) {

        List<Student> students = new ArrayList<>(studentHashMap.values());
        if (students.isEmpty()) {
            return;
        }

        Map<String, Integer> remaining = new LinkedHashMap<>(
                buildTargetCounts(students.size()));

        GameRandom.shuffle(students);

        Map<String, Integer> actualCounts = new LinkedHashMap<>();
        for (Student student : students) {
            List<String> available = new ArrayList<>();
            List<Double> weights = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
                if (entry.getValue() > 0) {
                    available.add(entry.getKey());
                    double base = entry.getValue();
                    double suit = getCliqueSuitability(
                            student, entry.getKey());
                    weights.add(base * suit);
                }
            }

            double[] wArr = new double[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                wArr[i] = weights.get(i);
            }

            String clique = weightedPick(available, wArr);
            remaining.merge(clique, -1, Integer::sum);

            student.studentStatistics.setMainClique(clique);
            assignSubgroup(student, clique);
            assignSecondaryClique(student, clique);
            actualCounts.merge(clique, 1, Integer::sum);
        }

        logSummary(students.size(), actualCounts);
    }

    /**
     * Computes how many students each clique should receive, based on
     * the weight constants and group categories from CliqueLoader.
     */
    static Map<String, Integer> buildTargetCounts(int totalStudents) {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (String clique : CliqueLoader.getAllCliques()) {
            if ("NoLife".equals(clique)) {
                weights.put(clique, CLIQUE_WEIGHT_NOLIFE);
            } else {
                String category = CliqueLoader.getGroupCategory(clique);
                switch (category) {
                    case "in-group" ->
                        weights.put(clique, CLIQUE_WEIGHT_IN_GROUP);
                    case "out-group" ->
                        weights.put(clique, CLIQUE_WEIGHT_OUT_GROUP);
                    default ->
                        weights.put(clique, CLIQUE_WEIGHT_NEUTRAL);
                }
            }
        }

        double totalWeight = weights.values().stream()
                .mapToDouble(Double::doubleValue).sum();

        Map<String, Integer> targets = new LinkedHashMap<>();
        int assigned = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            int count = (int) Math.round(
                    entry.getValue() / totalWeight * totalStudents);
            count = Math.max(count, 1);
            targets.put(entry.getKey(), count);
            assigned += count;
        }

        int diff = totalStudents - assigned;
        targets.merge("NoLife", diff, Integer::sum);

        return targets;
    }

    /**
     * Returns a demographic suitability multiplier for assigning the
     * given student to the given clique. Values above 1.0 make the
     * clique more likely; below 1.0 make it less likely.
     */
    private static double getCliqueSuitability(
            Student student, String clique) {
        String race = student.studentStatistics.getRace();
        String income = student.studentStatistics.getIncomeLevel();
        String gender = student.studentStatistics.getGender();
        int strength = student.studentStatistics.getStrength();
        int agility = student.studentStatistics.getAgility();

        return switch (clique) {
            case "Latino" -> {
                if ("hispanic".equals(race)) {
                    yield 3.0;
                }
                if ("2prace".equals(race)) {
                    yield 2.0;
                }
                yield 0.3;
            }
            case "Trap" -> {
                double mult = 1.0;
                if ("black".equals(race)) {
                    mult *= 2.5;
                }
                if ("low".equals(income)) {
                    mult *= 2.0;
                }
                yield mult;
            }
            case "Outcast" -> {
                if ("Male".equals(gender)) {
                    yield 2.0;
                }
                yield 0.5;
            }
            case "Bling" -> {
                if ("white".equals(race)) {
                    yield 0.2;
                }
                yield 1.5;
            }
            case "Jock" -> {
                boolean strongEnough = strength > 65;
                boolean agileEnough = agility > 65;
                if (strongEnough && agileEnough) {
                    yield 3.0;
                }
                if (strongEnough || agileEnough) {
                    yield 2.0;
                }
                if (strength < 40 && agility < 40) {
                    yield 0.3;
                }
                yield 1.0;
            }
            default -> 1.0;
        };
    }

    private static void assignSubgroup(Student student, String clique) {
        List<String> subgroups = CliqueLoader.getSubgroups(clique);
        if (subgroups.isEmpty()) {
            student.studentStatistics.setSubgroup("Standard");
            return;
        }

        double[] subWeights = new double[subgroups.size()];
        for (int i = 0; i < subgroups.size(); i++) {
            String sg = subgroups.get(i);
            if (CliqueLoader.isDecliningSubgroup(clique, sg)) {
                subWeights[i] = CLIQUE_DECLINING_SUBGROUP_WEIGHT;
            } else if (CliqueLoader.isRisingSubgroup(clique, sg)) {
                subWeights[i] = CLIQUE_RISING_SUBGROUP_WEIGHT;
            } else {
                subWeights[i] = 1.0;
            }
        }

        student.studentStatistics.setSubgroup(
                weightedPick(subgroups, subWeights));
    }

    private static void assignSecondaryClique(
            Student student, String clique) {
        if ("NoLife".equals(clique)) {
            return;
        }
        if (GameRandom.nextDouble() >= CLIQUE_SECONDARY_CHANCE) {
            return;
        }
        List<String> aligns = CliqueLoader.getAligns(clique);
        if (aligns.isEmpty()) {
            return;
        }
        String secondary = aligns.get(
                GameRandom.nextInt(aligns.size()));
        student.studentStatistics.setSecondaryClique(secondary);
    }

    /**
     * Weighted random pick from a parallel list/array.
     */
    static String weightedPick(List<String> items, double[] weights) {
        double total = 0;
        for (double w : weights) {
            total += w;
        }
        double roll = GameRandom.nextDouble() * total;
        double cumulative = 0;
        for (int i = 0; i < items.size(); i++) {
            cumulative += weights[i];
            if (roll < cumulative) {
                return items.get(i);
            }
        }
        return items.get(items.size() - 1);
    }

    private static void logSummary(
            int totalStudents, Map<String, Integer> counts) {
        GameLogger.logGeneration(
                "=== Clique Assignment Summary ===");
        GameLogger.logGeneration(
                "Total students: " + totalStudents);

        List<Map.Entry<String, Integer>> sorted =
                new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        for (Map.Entry<String, Integer> entry : sorted) {
            double pct = 100.0 * entry.getValue() / totalStudents;
            GameLogger.logGeneration(String.format(
                    "  %-15s %4d  (%5.1f%%)",
                    entry.getKey(), entry.getValue(), pct));
        }
    }
}
