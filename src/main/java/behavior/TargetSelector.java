package behavior;

import entity.Student;
import utility.CliqueLoader;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility for selecting a social-interaction target from a pool of
 * candidates using a tiered preference cascade.
 *
 * <p>Priority order (first non-empty tier wins):
 * <ol>
 *   <li><b>Friends</b> &ndash; students in {@code friendsInSchool}</li>
 *   <li><b>Same clique</b> &ndash; same main clique</li>
 *   <li><b>Aligned cliques</b> &ndash; {@code CliqueLoader.getRelationship} returns {@code "Aligns"}</li>
 *   <li><b>Positive cliques</b> &ndash; relationship {@code "Positive"}</li>
 *   <li><b>Neutral cliques</b> &ndash; relationship {@code "Neutral"}</li>
 *   <li><b>Same neighborhood</b> &ndash; matching {@code neighborhoodName}</li>
 *   <li><b>Negative / Hate</b> &ndash; last resort</li>
 * </ol>
 *
 * <p>Within a tier a random candidate is chosen.
 */
public final class TargetSelector {

    private TargetSelector() {
    }

    /**
     * Selects a target from {@code candidates} using the tiered preference
     * cascade relative to {@code student}.
     *
     * @param student    the initiating student
     * @param candidates other students available for interaction (self excluded)
     * @return a chosen target, or {@code null} if {@code candidates} is empty
     */
    public static Student selectTarget(Student student, List<Student> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        ArrayList<Student> friends = student.studentStatistics.getFriendsInSchool();
        String myClique = student.studentStatistics.getMainClique();
        String myNeighborhood = student.studentStatistics.getNeighborhoodName();

        // Tier 1: friends present in the candidate pool
        List<Student> tier = filterFriends(friends, candidates);
        if (!tier.isEmpty()) {
            return pick(tier);
        }

        // Tier 2: same clique
        tier = filterByClique(myClique, candidates, null);
        if (!tier.isEmpty()) {
            return pick(tier);
        }

        // Tier 3: aligned cliques
        tier = filterByRelationship(myClique, candidates, "Aligns");
        if (!tier.isEmpty()) {
            return pick(tier);
        }

        // Tier 4: positive cliques
        tier = filterByRelationship(myClique, candidates, "Positive");
        if (!tier.isEmpty()) {
            return pick(tier);
        }

        // Tier 5: neutral cliques
        tier = filterByRelationship(myClique, candidates, "Neutral");
        if (!tier.isEmpty()) {
            return pick(tier);
        }

        // Tier 6: same neighborhood
        tier = filterByNeighborhood(myNeighborhood, candidates);
        if (!tier.isEmpty()) {
            return pick(tier);
        }

        // Tier 7: negative / hate (last resort -- anyone remaining)
        return pick(candidates);
    }

    private static List<Student> filterFriends(List<Student> friends, List<Student> candidates) {
        List<Student> result = new ArrayList<>();
        for (Student c : candidates) {
            if (friends.contains(c)) {
                result.add(c);
            }
        }
        return result;
    }

    private static List<Student> filterByClique(String myClique, List<Student> candidates,
                                                 @SuppressWarnings("unused") Void ignored) {
        if (myClique == null) {
            return List.of();
        }
        List<Student> result = new ArrayList<>();
        for (Student c : candidates) {
            if (myClique.equals(c.studentStatistics.getMainClique())) {
                result.add(c);
            }
        }
        return result;
    }

    private static List<Student> filterByRelationship(String myClique, List<Student> candidates,
                                                       String relationship) {
        if (myClique == null) {
            return List.of();
        }
        List<Student> result = new ArrayList<>();
        for (Student c : candidates) {
            String theirClique = c.studentStatistics.getMainClique();
            if (theirClique != null && !theirClique.equals(myClique)
                    && relationship.equals(CliqueLoader.getRelationship(myClique, theirClique))) {
                result.add(c);
            }
        }
        return result;
    }

    private static List<Student> filterByNeighborhood(String myNeighborhood, List<Student> candidates) {
        if (myNeighborhood == null) {
            return List.of();
        }
        List<Student> result = new ArrayList<>();
        for (Student c : candidates) {
            if (myNeighborhood.equals(c.studentStatistics.getNeighborhoodName())) {
                result.add(c);
            }
        }
        return result;
    }

    private static Student pick(List<Student> pool) {
        return pool.get(GameRandom.nextInt(pool.size()));
    }
}
