package behavior;

import entity.Student;
import utility.CliqueLoader;
import utility.GameRandom;
import utility.SocialLinkConnector;

import java.util.ArrayList;
import java.util.List;

import static constants.SimConstants.SOCIAL_LINK_TIER_ACQUAINTANCE_THRESHOLD;
import static constants.SimConstants.SOCIAL_LINK_TIER_DISLIKE_THRESHOLD;

/**
 * Shared utility for selecting a social-interaction target from a pool of
 * candidates.
 *
 * <p>When a {@link SocialLinkConnector} is available, selection is driven by
 * the initiator's <i>outgoing</i> social scores (the directed graph):
 * <ol>
 *   <li><b>Positive known contacts</b> &ndash; candidates the initiator
 *       likes (score at or above the acquaintance threshold), picked with
 *       score-proportional weights so close friends are favoured but a
 *       casual acquaintance can still win</li>
 *   <li><b>Unknown/neutral peers</b> &ndash; fall back to the clique and
 *       neighborhood cascade below</li>
 *   <li><b>Disliked peers</b> &ndash; last resort only</li>
 * </ol>
 * The <i>reciprocal</i> score is deliberately ignored: a student happily
 * seeks out someone who does not particularly like them back, producing the
 * asymmetric interactions described in the README.
 *
 * <p>Without a connector (legacy callers, tests), the original tiered
 * preference cascade is used:
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
 * <p>Within a cascade tier a random candidate is chosen.
 */
public final class TargetSelector {

    private TargetSelector() {
    }

    /**
     * Legacy entry point without a social graph. Uses the clique cascade.
     *
     * @param student    the initiating student
     * @param candidates other students available for interaction (self excluded)
     * @return a chosen target, or {@code null} if {@code candidates} is empty
     */
    public static Student selectTarget(Student student, List<Student> candidates) {
        return selectTarget(student, candidates, null);
    }

    /**
     * Selects a target from {@code candidates} relative to {@code student},
     * preferring the initiator's positive outgoing social links when a
     * connector is available.
     *
     * @param student    the initiating student
     * @param candidates other students available for interaction (self excluded)
     * @param connector  the social graph, or {@code null} to use the
     *                   clique cascade only
     * @return a chosen target, or {@code null} if {@code candidates} is empty
     */
    public static Student selectTarget(Student student, List<Student> candidates,
                                       SocialLinkConnector connector) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        if (connector == null) {
            return cascadeSelect(student, candidates);
        }

        List<Student> liked = new ArrayList<>();
        List<Double> likedWeights = new ArrayList<>();
        List<Student> neutral = new ArrayList<>();
        for (Student candidate : candidates) {
            double score = connector.getSocialScore(student, candidate);
            if (score >= SOCIAL_LINK_TIER_ACQUAINTANCE_THRESHOLD) {
                liked.add(candidate);
                likedWeights.add(score);
            } else if (score > SOCIAL_LINK_TIER_DISLIKE_THRESHOLD) {
                neutral.add(candidate);
            }
        }

        // Tier 1: someone the initiator actively likes, weighted by how much
        if (!liked.isEmpty()) {
            return weightedPick(liked, likedWeights);
        }

        // Tier 2: unknown/neutral peers via the clique & neighborhood cascade
        if (!neutral.isEmpty()) {
            return cascadeSelect(student, neutral);
        }

        // Tier 3: only disliked peers remain -- last resort
        return pick(candidates);
    }

    /**
     * The clique/neighborhood preference cascade (see class docs).
     */
    private static Student cascadeSelect(Student student, List<Student> candidates) {
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

    /**
     * Weighted random pick where each candidate's chance is proportional to
     * its (positive) weight.
     */
    private static Student weightedPick(List<Student> pool, List<Double> weights) {
        double total = 0;
        for (double w : weights) {
            total += w;
        }
        double roll = GameRandom.nextDouble() * total;
        double cumulative = 0;
        for (int i = 0; i < pool.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return pool.get(i);
            }
        }
        return pool.get(pool.size() - 1);
    }
}
