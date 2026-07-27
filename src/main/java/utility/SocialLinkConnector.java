package utility;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import save.SocialLinkSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import javax.swing.*;

import entity.StandardSchool;
import static constants.SimConstants.*;

public class SocialLinkConnector {

    // Directed graph to maintain distinct edges for each direction
    Graph<Student, DefaultWeightedEdge> socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    private JSlider zoomSlider;
    private mxGraph graph;
    private mxGraphComponent graphComponent;
    private HashMap<Student, Object> vertexToCellMap = new HashMap<>();
    private final HashMap<Student, Integer> studentIds = new HashMap<>();
    private int nextFallbackStudentId;

    // Catalyst records: keyed by a canonical pair identifier, storing the catalyst
    // text.
    // A catalyst is a mutual event that solidifies a best-friend bond. Both parties
    // share
    // the same catalyst entry. Without a catalyst, scores cannot cross the
    // best-friend threshold.
    private final HashMap<String, String> catalystRecords = new HashMap<>();

    // Template actions for generating initial catalyst text (pre-existing best
    // friendships)
    private static final String[] CATALYST_ACTIONS = {
            "stayed up all night talking at a sleepover",
            "survived a scary movie marathon together",
            "had an epic snowball fight",
            "stood up for each other against a bully",
            "got lost on a class field trip together",
            "worked on a group project all weekend",
            "sat together on the first day of school",
            "traded snacks at lunch every single day",
            "were lab partners in science class",
            "spent a whole summer hanging out at the pool",
            "were on the same sports team",
            "discovered they had the same taste in music",
            "went to their first concert together",
            "bonded over a shared hobby after school",
            "helped each other through a really tough time",
            "got stuck in detention together",
            "pulled off an epic prank on a teacher",
            "rode the same bus since middle school",
            "lived on the same street growing up",
            "were partners at a school dance"
    };

    // Template time descriptions for catalyst events (relative to the 2004 school
    // year)
    private static final String[] CATALYST_TIMES = {
            "a few weeks ago",
            "a few months ago",
            "about a year ago",
            "two years ago",
            "back in middle school",
            "since elementary school",
            "over the summer",
            "last semester",
            "at the beginning of the school year",
            "before high school started"
    };

    /**
     * Constructor to initialize social links.
     *
     * @param studentHashMap HashMap of students.
     * @param standardSchool The standard school entity.
     */
    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool) {
        this(); // Call the default constructor to initialize graphComponent
        initializeSocialLinks(studentHashMap, standardSchool);
    }

    /**
     * Default constructor initializing graph components.
     */
    public SocialLinkConnector() {
        graph = new mxGraph();
        graphComponent = new mxGraphComponent(graph);
        zoomSlider = new JSlider();

        // Existing initialization code
        graphComponent.zoomActual();
        zoomSlider.setValue(100);
    }

    /**
     * Initializes social links between students in eight phases:
     * <ol>
     * <li>Add all students as vertices in the social graph</li>
     * <li>Calculate social capacity (maxBestFriends and the wider
     * maxSocialConnections) for each student based on personality</li>
     * <li>Create sibling relationships with variable weights (can be positive or
     * negative)</li>
     * <li>Generate close-friend relationships using a bell curve distribution
     * with a soft same-gender preference</li>
     * <li>Generate rival/negative relationships for some students</li>
     * <li>Widen each student's network with weak casual/acquaintance links up
     * to their overall connection capacity</li>
     * <li>Record catalysts for pre-existing mutual best friendships</li>
     * <li>Synchronize the friendsInSchool compatibility cache from the graph</li>
     * </ol>
     *
     * <p>
     * Social link weights use the -100 to 100 scale described in the README.
     * Relationships are directed: A's feeling about B can differ from B's feeling
     * about A. Acquaintance reciprocals are rolled independently, so one-sided
     * admiration, indifference, and outright dislike all occur naturally.
     * </p>
     *
     * <p>
     * Best-friend bonds (mutual scores &ge; 75) require a catalyst event. During
     * initial
     * generation, any friendships that qualify are assumed to have already
     * experienced a
     * catalyst and receive generated placeholder text.
     * </p>
     *
     * @param studentHashMap HashMap of students.
     * @param standardSchool The standard school entity.
     */
    public void initializeSocialLinks(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool) {

        if (studentHashMap == null || standardSchool == null) {
            throw new IllegalArgumentException("Student hash map and standard school cannot be null.");
        }
        registerStudentIds(studentHashMap);

        // Phase 1: Add all students as vertices in the social graph
        for (Student student : studentHashMap.values()) {
            socialGraph.addVertex(student);
        }

        // Phase 2: Calculate social capacity (maxBestFriends + overall
        // connection capacity) for each student
        for (Student student : studentHashMap.values()) {
            setMaxBestFriends(student);
        }

        // Phase 3: Create sibling relationships with meaningful, variable weights
        // Sibling relationships are stored bidirectionally in siblingsInSchool lists,
        // so iterating all students will naturally create both directions.
        for (Student student : studentHashMap.values()) {
            ArrayList<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();
            for (Student sibling : siblingsInSchool) {
                if (sibling == null) {
                    continue;
                }

                // Ensure sibling is a vertex (they may not be in the main studentHashMap)
                socialGraph.addVertex(sibling);

                // Add directed edge: student -> sibling (independent weight per direction)
                if (!socialGraph.containsEdge(student, sibling)) {
                    DefaultWeightedEdge edge = socialGraph.addEdge(student, sibling);
                    if (edge != null) {
                        socialGraph.setEdgeWeight(edge, assignSiblingWeight());
                    }
                }
            }
        }

        // Phase 4: Generate close-friend relationships using bell curve
        // distribution. The number of friends follows a normal distribution
        // centered on a personality-adjusted mean. Same-gender friends are
        // preferred (soft weight, not a hard filter) to reflect typical high
        // school social patterns.
        for (Student student : studentHashMap.values()) {
            int targetFriendCount = generateFriendCount(student);
            // Account for any friends already added via reciprocal links from earlier
            // students
            int friendsAdded = student.studentStatistics.getFriendsInSchool().size();
            int attempts = 0;
            int maxAttempts = Math.max(targetFriendCount * SOCIAL_LINK_FRIEND_MAX_ATTEMPTS_MULTIPLIER, 5);

            while (friendsAdded < targetFriendCount && attempts < maxAttempts) {
                Student potentialFriend = findPotentialFriend(
                        student, standardSchool, false, SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT);
                attempts++;

                if (potentialFriend == null) {
                    continue;
                }
                if (student.equals(potentialFriend)) {
                    continue;
                }
                if (student.studentStatistics.getFriendsInSchool().contains(potentialFriend)) {
                    continue;
                }
                // Don't create a separate friend edge for siblings (they already have sibling
                // edges)
                if (student.studentStatistics.getSiblingsInSchool().contains(potentialFriend)) {
                    continue;
                }
                // Don't duplicate edges in the same direction
                if (socialGraph.containsEdge(student, potentialFriend)) {
                    continue;
                }

                // Add to this student's friend list
                student.studentStatistics.addFriendInSchool(potentialFriend);

                // Ensure potential friend is a vertex (they may come from grade class lists
                // that include students not in the main studentHashMap)
                socialGraph.addVertex(potentialFriend);

                // Add directed edge: student -> friend (positive weight)
                double friendWeight = assignFriendWeight();
                DefaultWeightedEdge edge = socialGraph.addEdge(student, potentialFriend);
                if (edge != null) {
                    socialGraph.setEdgeWeight(edge, friendWeight);
                }

                // Add reciprocal edge: friend -> student (independent weight, may differ)
                // Per the README, relationships are not bidirectional in score:
                // each party can feel differently about the other.
                if (!socialGraph.containsEdge(potentialFriend, student)) {
                    double reciprocalWeight = assignReciprocalWeight();
                    DefaultWeightedEdge reciprocalEdge = socialGraph.addEdge(potentialFriend, student);
                    if (reciprocalEdge != null) {
                        socialGraph.setEdgeWeight(reciprocalEdge, reciprocalWeight);

                        // If the reciprocal weight is positive enough, they also consider this a
                        // friendship
                        if (reciprocalWeight >= SOCIAL_LINK_RECIPROCAL_FRIEND_THRESHOLD
                                && !potentialFriend.studentStatistics.getFriendsInSchool().contains(student)) {
                            potentialFriend.studentStatistics.addFriendInSchool(student);
                        }
                    }
                }

                friendsAdded++;
            }
        }

        // Phase 5: Generate rival/negative relationships
        // Some students actively dislike others. This creates asymmetric negative edges
        // that do not affect the target's friendsInSchool list.
        for (Student student : studentHashMap.values()) {
            int numRivals = generateRivalCount();
            int rivalsAdded = 0;
            int attempts = 0;
            int maxAttempts = Math.max(numRivals * SOCIAL_LINK_FRIEND_MAX_ATTEMPTS_MULTIPLIER, 3);

            while (rivalsAdded < numRivals && attempts < maxAttempts) {
                Student rival = findPotentialFriend(
                        student, standardSchool, true, SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT);
                attempts++;

                if (rival == null) {
                    continue;
                }
                if (student.equals(rival)) {
                    continue;
                }
                // Don't overwrite an existing edge (friend, sibling, or previous rival)
                if (socialGraph.containsEdge(student, rival)) {
                    continue;
                }
                // Don't make siblings rivals through this path (sibling bonds are handled
                // separately)
                if (student.studentStatistics.getSiblingsInSchool().contains(rival)) {
                    continue;
                }

                // Ensure rival is a vertex (they may come from grade class lists)
                socialGraph.addVertex(rival);

                DefaultWeightedEdge edge = socialGraph.addEdge(student, rival);
                if (edge != null) {
                    socialGraph.setEdgeWeight(edge, assignRivalWeight());
                }

                rivalsAdded++;
            }
        }

        // Phase 6: Widen each student's network with weak casual/acquaintance
        // links. Real contact circles extend far past close friends: loose
        // acquaintances, classmates, neighborhood kids. The overall network
        // size follows a bell curve centered on a ratio of the student's
        // connection capacity (itself derived from the same base-stat formula
        // as close-friend capacity). Reciprocal opinions are rolled
        // independently and may be neutral or negative, so some students end
        // up wanting to talk to people who don't really like them back.
        for (Student student : studentHashMap.values()) {
            int targetConnections = generateConnectionCount(student);
            int connections = socialGraph.outDegreeOf(student);
            int attempts = 0;
            int maxAttempts = Math.max(
                    (targetConnections - connections) * SOCIAL_LINK_FRIEND_MAX_ATTEMPTS_MULTIPLIER, 5);

            while (connections < targetConnections && attempts < maxAttempts) {
                Student acquaintance = findPotentialFriend(
                        student, standardSchool, false, SOCIAL_LINK_SAME_GENDER_ACQUAINTANCE_WEIGHT);
                attempts++;

                if (acquaintance == null || student.equals(acquaintance)) {
                    continue;
                }
                // Existing relationships (sibling, friend, rival, or reciprocal
                // acquaintance) already occupy this direction
                if (socialGraph.containsEdge(student, acquaintance)) {
                    continue;
                }
                if (student.studentStatistics.getSiblingsInSchool().contains(acquaintance)) {
                    continue;
                }

                socialGraph.addVertex(acquaintance);

                DefaultWeightedEdge edge = socialGraph.addEdge(student, acquaintance);
                if (edge != null) {
                    socialGraph.setEdgeWeight(edge, assignAcquaintanceWeight());
                }

                // Independent reciprocal: the other party may barely register
                // this student, or even quietly dislike them (asymmetry per
                // the README examples)
                if (!socialGraph.containsEdge(acquaintance, student)) {
                    DefaultWeightedEdge reciprocalEdge = socialGraph.addEdge(acquaintance, student);
                    if (reciprocalEdge != null) {
                        socialGraph.setEdgeWeight(reciprocalEdge, assignAcquaintanceReciprocalWeight());
                    }
                }

                connections++;
            }
        }

        // Phase 7: Record catalysts for pre-existing best friendships
        // Any mutual friendship where both scores >= BEST_FRIEND_THRESHOLD is assumed
        // to have
        // already experienced a catalyst event (the school existed before the sim
        // starts).
        generateInitialCatalysts(studentHashMap);

        // Phase 8: Synchronize the friendsInSchool compatibility cache so it
        // reflects exactly the friend-or-stronger outgoing links in the graph
        refreshFriendCaches();

        // After initializing all social links, visualize the graph
        schoolSocialLinkVisualizer();
    }

    // ---- Catalyst System ----

    /**
     * Scans all friend pairs and records catalyst text for any pre-existing mutual
     * best friendships (both directions at or above the best-friend threshold).
     * Since the school has existed before the simulation begins, we assume these
     * bonds were already solidified by a shared event.
     */
    private void generateInitialCatalysts(HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            for (Student friend : student.studentStatistics.getFriendsInSchool()) {
                // Only process each pair once using stable student identifiers.
                if (getStableStudentId(student) >= getStableStudentId(friend)) {
                    continue;
                }

                // Check if both directions exist and both meet the best-friend threshold
                if (socialGraph.containsEdge(student, friend) && socialGraph.containsEdge(friend, student)) {
                    double scoreAB = socialGraph.getEdgeWeight(socialGraph.getEdge(student, friend));
                    double scoreBA = socialGraph.getEdgeWeight(socialGraph.getEdge(friend, student));

                    if (scoreAB >= SOCIAL_LINK_BEST_FRIEND_THRESHOLD
                            && scoreBA >= SOCIAL_LINK_BEST_FRIEND_THRESHOLD) {
                        String key = makePairKey(student, friend);
                        if (!catalystRecords.containsKey(key)) {
                            catalystRecords.put(key, generateCatalystText(student, friend));
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a catalyst text string for a pre-existing best friendship.
     * Format: "[Person A] and [Person B] became best friends when they [action]
     * [time]."
     *
     * @param studentA First student in the pair.
     * @param studentB Second student in the pair.
     * @return The generated catalyst text.
     */
    private String generateCatalystText(Student studentA, Student studentB) {
        String nameA = studentA.studentName.getFirstName() + " " + studentA.studentName.getLastName();
        String nameB = studentB.studentName.getFirstName() + " " + studentB.studentName.getLastName();
        String action = CATALYST_ACTIONS[GameRandom.nextInt(CATALYST_ACTIONS.length)];
        String time = CATALYST_TIMES[GameRandom.nextInt(CATALYST_TIMES.length)];

        return nameA + " and " + nameB + " became best friends when they " + action + " " + time + ".";
    }

    private void registerStudentIds(HashMap<Integer, Student> studentHashMap) {
        studentIds.clear();
        int maxId = -1;
        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            studentIds.put(entry.getValue(), entry.getKey());
            maxId = Math.max(maxId, entry.getKey());
        }
        nextFallbackStudentId = maxId + 1;
    }

    private int getStableStudentId(Student student) {
        Integer existingId = studentIds.get(student);
        if (existingId != null) {
            return existingId;
        }
        int fallbackId = nextFallbackStudentId++;
        studentIds.put(student, fallbackId);
        return fallbackId;
    }

    /**
     * Creates a canonical key for an unordered student pair.
     * Both (A, B) and (B, A) produce the same key, since catalysts are mutual.
     *
     * @param a First student.
     * @param b Second student.
     * @return A canonical string key for the pair.
     */
    private String makePairKey(Student a, Student b) {
        int idA = getStableStudentId(a);
        int idB = getStableStudentId(b);
        if (idA <= idB) {
            return idA + ":" + idB;
        }
        return idB + ":" + idA;
    }

    /**
     * Checks whether a catalyst exists for a student pair.
     * A catalyst is required for a friendship to cross the best-friend threshold.
     *
     * @param a First student.
     * @param b Second student.
     * @return true if a catalyst has been recorded for this pair.
     */
    public boolean hasCatalyst(Student a, Student b) {
        return catalystRecords.containsKey(makePairKey(a, b));
    }

    /**
     * Registers a catalyst event between two students, allowing their scores
     * to cross the best-friend threshold. The catalyst must be mutual.
     *
     * @param a    First student.
     * @param b    Second student.
     * @param text Description of the catalyst event.
     */
    public void registerCatalyst(Student a, Student b, String text) {
        catalystRecords.put(makePairKey(a, b), text);
    }

    /**
     * Gets the catalyst text for a student pair, or null if none exists.
     *
     * @param a First student.
     * @param b Second student.
     * @return The catalyst text, or null.
     */
    public String getCatalyst(Student a, Student b) {
        return catalystRecords.get(makePairKey(a, b));
    }

    /**
     * Gets all recorded catalyst entries. Useful for inspection/debugging.
     *
     * @return Unmodifiable view of catalyst records.
     */
    public HashMap<String, String> getAllCatalysts() {
        return new HashMap<>(catalystRecords);
    }

    public SocialLinkSnapshot createSnapshot() {
        SocialLinkSnapshot snapshot = new SocialLinkSnapshot();
        for (DefaultWeightedEdge edge : socialGraph.edgeSet()) {
            Student source = socialGraph.getEdgeSource(edge);
            Student target = socialGraph.getEdgeTarget(edge);
            snapshot.addEdge(getStableStudentId(source), getStableStudentId(target),
                    socialGraph.getEdgeWeight(edge));
        }
        snapshot.putCatalysts(catalystRecords);
        return snapshot;
    }

    public void restoreFromSnapshot(HashMap<Integer, Student> studentHashMap,
            SocialLinkSnapshot snapshot) {
        registerStudentIds(studentHashMap);
        socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        catalystRecords.clear();
        vertexToCellMap.clear();

        if (studentHashMap != null) {
            for (Student student : studentHashMap.values()) {
                socialGraph.addVertex(student);
            }
        }
        if (snapshot == null) {
            return;
        }
        HashMap<Integer, Student> byId = new HashMap<>();
        if (studentHashMap != null) {
            byId.putAll(studentHashMap);
        }
        for (SocialLinkSnapshot.EdgeSnapshot edgeSnapshot : snapshot.getEdges()) {
            Student source = byId.get(edgeSnapshot.getSourceStudentId());
            Student target = byId.get(edgeSnapshot.getTargetStudentId());
            if (source == null || target == null) {
                continue;
            }
            socialGraph.addVertex(source);
            socialGraph.addVertex(target);
            DefaultWeightedEdge edge = socialGraph.addEdge(source, target);
            if (edge != null) {
                socialGraph.setEdgeWeight(edge, edgeSnapshot.getWeight());
            }
        }
        catalystRecords.putAll(snapshot.getCatalysts());

        // Re-align every student's friendsInSchool cache with the restored
        // graph so the compatibility list and edge weights cannot drift.
        refreshFriendCaches();
    }

    // ---- Relationship Decay ----

    /**
     * Applies daily decay to all social link scores, drifting them toward neutral
     * (0).
     * This should be called once per simulated day (typically at end of day).
     *
     * <p>
     * Decay rates vary by relationship type to reflect real social dynamics:
     * <ul>
     * <li><b>Standard</b> (acquaintances, rivals): fastest decay at
     * {@value constants.SimConstants#SOCIAL_LINK_DECAY_STANDARD}/day.
     * A score of 50 reaches neutral in ~100 school days without reinforcement.</li>
     * <li><b>Best friends</b> (with catalyst): slower decay at
     * {@value constants.SimConstants#SOCIAL_LINK_DECAY_BEST_FRIEND}/day.
     * These bonds are hard-won and persist longer.</li>
     * <li><b>Family/siblings</b>: slowest decay at
     * {@value constants.SimConstants#SOCIAL_LINK_DECAY_FAMILY}/day.
     * Family bonds are the most resilient over time.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Positive scores decrease toward 0; negative scores increase toward 0.
     * Scores that reach the neutral threshold are snapped to exactly 0.
     * This incentivizes NPCs and players to maintain relationships through
     * interaction.
     * </p>
     */
    public void applyDailyDecay() {
        for (DefaultWeightedEdge edge : socialGraph.edgeSet()) {
            double currentWeight = socialGraph.getEdgeWeight(edge);

            // Skip edges already at neutral
            if (Math.abs(currentWeight) < SOCIAL_LINK_DECAY_NEUTRAL_THRESHOLD) {
                continue;
            }

            Student source = socialGraph.getEdgeSource(edge);
            Student target = socialGraph.getEdgeTarget(edge);

            // Determine decay rate based on relationship type (slowest applicable rate
            // wins)
            double decayRate = SOCIAL_LINK_DECAY_STANDARD;

            if (source.studentStatistics.getSiblingsInSchool().contains(target)) {
                // Family bonds are the most resilient
                decayRate = SOCIAL_LINK_DECAY_FAMILY;
            } else if (hasCatalyst(source, target)) {
                // Best-friend bonds (with catalyst) decay slower than standard
                decayRate = SOCIAL_LINK_DECAY_BEST_FRIEND;
            }

            // Apply decay toward 0 (neutral)
            double newWeight;
            if (currentWeight > 0) {
                newWeight = currentWeight - decayRate;
                // Don't overshoot past neutral
                if (newWeight < 0) {
                    newWeight = 0;
                }
            } else {
                newWeight = currentWeight + decayRate;
                // Don't overshoot past neutral
                if (newWeight > 0) {
                    newWeight = 0;
                }
            }

            socialGraph.setEdgeWeight(edge, newWeight);
        }

        // Friendships that decayed below the friend tier fall out of the
        // compatibility cache; anything still qualifying is retained.
        refreshFriendCaches();
    }

    // ---- Social Score Modification ----

    /**
     * Modifies the social score between two students by a given amount.
     *
     * <p>
     * Scores can increase freely through social actions up to the best-friend
     * soft cap ({@value constants.SimConstants#SOCIAL_LINK_BEST_FRIEND_SOFT_CAP}).
     * Crossing the best-friend threshold
     * ({@value constants.SimConstants#SOCIAL_LINK_BEST_FRIEND_THRESHOLD})
     * requires a mutual catalyst to have been registered for the pair. Without a
     * catalyst,
     * the score is capped at the soft cap.
     * </p>
     *
     * <p>
     * If no edge exists between the students, a new edge is created with the
     * given amount as its initial weight.
     * </p>
     *
     * @param source The student whose score toward the target is being modified.
     * @param target The other student.
     * @param amount The amount to add (positive = warmer, negative = colder).
     */
    public void modifySocialScore(Student source, Student target, double amount) {
        if (source == null || target == null || source.equals(target)) {
            return;
        }

        // Ensure both vertices exist
        socialGraph.addVertex(source);
        socialGraph.addVertex(target);

        if (!socialGraph.containsEdge(source, target)) {
            // No existing relationship - create a new edge
            DefaultWeightedEdge edge = socialGraph.addEdge(source, target);
            if (edge != null) {
                double weight = Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_SCORE_MAX, amount));
                socialGraph.setEdgeWeight(edge, weight);
                updateFriendCacheEntry(source, target, weight);
            }
            return;
        }

        DefaultWeightedEdge edge = socialGraph.getEdge(source, target);
        double currentWeight = socialGraph.getEdgeWeight(edge);
        double newWeight = currentWeight + amount;

        // Enforce the best-friend soft cap: cannot cross the best-friend threshold
        // without a mutual catalyst event having been recorded
        if (currentWeight < SOCIAL_LINK_BEST_FRIEND_THRESHOLD
                && newWeight >= SOCIAL_LINK_BEST_FRIEND_THRESHOLD
                && !hasCatalyst(source, target)) {
            newWeight = Math.min(newWeight, SOCIAL_LINK_BEST_FRIEND_SOFT_CAP);
        }

        // Clamp to valid range
        newWeight = Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_SCORE_MAX, newWeight));
        socialGraph.setEdgeWeight(edge, newWeight);
        updateFriendCacheEntry(source, target, newWeight);
    }

    /**
     * Keeps the source's {@code friendsInSchool} cache aligned with a single
     * directed score after it changes: targets crossing the friend-tier
     * threshold are added, targets falling below it are removed. Siblings
     * never enter the friend cache (they have their own lists).
     */
    private void updateFriendCacheEntry(Student source, Student target, double newWeight) {
        if (source.studentStatistics.getSiblingsInSchool().contains(target)) {
            return;
        }
        if (newWeight >= SOCIAL_LINK_TIER_FRIEND_THRESHOLD) {
            source.studentStatistics.addFriendInSchool(target);
        } else {
            source.studentStatistics.removeFriendInSchool(target);
        }
    }

    /**
     * Gets the current social score from source toward target, or 0 if no edge
     * exists.
     *
     * @param source The source student.
     * @param target The target student.
     * @return The social score, or 0.0 if no relationship exists.
     */
    public double getSocialScore(Student source, Student target) {
        if (socialGraph.containsEdge(source, target)) {
            return socialGraph.getEdgeWeight(socialGraph.getEdge(source, target));
        }
        return 0.0;
    }

    // ---- Friend Count Generation (Bell Curve) ----

    /**
     * Generates the actual number of friends a student will attempt to form using a
     * normal distribution. The mean is derived from their social capacity
     * (maxBestFriends)
     * scaled by a ratio, producing a bell curve of relationship counts across the
     * school.
     *
     * @param student The student.
     * @return Target number of friends to generate, clamped to [0, maxBestFriends].
     */
    private int generateFriendCount(Student student) {
        int maxFriends = student.studentStatistics.getMaxBestFriends();
        double mean = maxFriends * SOCIAL_LINK_FRIEND_COUNT_MEAN_RATIO;
        double count = GameRandom.nextGaussian() * SOCIAL_LINK_FRIEND_COUNT_STD_DEV + mean;
        return (int) Math.round(Math.max(0, Math.min(maxFriends, count)));
    }

    /**
     * Generates the number of rival/negative relationships for a student.
     * Most students will have 0-1 rivals; a few may have 2-3.
     *
     * @return Number of rivals to generate.
     */
    private int generateRivalCount() {
        double count = GameRandom.nextGaussian() * SOCIAL_LINK_RIVAL_COUNT_STD_DEV + SOCIAL_LINK_RIVAL_COUNT_MEAN;
        return (int) Math.round(Math.max(0, Math.min(SOCIAL_LINK_RIVAL_MAXIMUM, count)));
    }

    /**
     * Generates the total number of directed connections (close friends,
     * casual links, acquaintances, and rivals combined) a student's network
     * should reach. Uses a normal distribution centered at a ratio of the
     * student's overall connection capacity, which derives from the same
     * charisma/empathy/luck formula as close-friend capacity.
     *
     * @param student The student.
     * @return Target overall network size, clamped to [0, maxSocialConnections].
     */
    private int generateConnectionCount(Student student) {
        int maxConnections = student.studentStatistics.getMaxSocialConnections();
        double mean = maxConnections * SOCIAL_LINK_CONNECTION_COUNT_MEAN_RATIO;
        double count = GameRandom.nextGaussian() * SOCIAL_LINK_CONNECTION_COUNT_STD_DEV + mean;
        return (int) Math.round(Math.max(0, Math.min(maxConnections, count)));
    }

    // ---- Social Capacity Calculation ----

    /**
     * Sets the maximum number of best friends for a student based on personality
     * attributes.
     * Uses charisma (primary), empathy, and luck to calculate social capacity on a
     * 0 to
     * SOCIAL_LINK_FRIEND_MAXIMUM scale. A variability factor prevents deterministic
     * outcomes.
     * Every student is guaranteed at least SOCIAL_LINK_FRIEND_MINIMUM capacity.
     *
     * @param student The student.
     */
    private void setMaxBestFriends(Student student) {
        int charisma = student.studentStatistics.getCharisma();
        int luck = student.studentStatistics.getLuck();
        int empathy = student.studentStatistics.getEmpathy();

        // Calculate a composite score based on attributes and their modifiers
        double compositeScore = (charisma * SOCIAL_LINK_FRIEND_CHARISMA_MODIFIER)
                + (empathy * SOCIAL_LINK_FRIEND_EMPATHY_MODIFIER)
                + (luck * SOCIAL_LINK_FRIEND_LUCK_MODIFIER);

        // Introduce variability to avoid deterministic outcomes
        double variabilityFactor = 1 + (GameRandom.nextDouble() * SOCIAL_LINK_FRIEND_VARIABILITY_RANGE)
                - (SOCIAL_LINK_FRIEND_VARIABILITY_RANGE / 2);

        // Apply variability to the composite score
        double variedScore = compositeScore * variabilityFactor;

        // Normalize to 0-1 range using a scaling factor calibrated for typical stat
        // ranges
        // (composite typically ranges ~10-115 based on stat distributions)
        double normalizedScore = Math.max(0, Math.min(1, variedScore / SOCIAL_LINK_FRIEND_SCALING_FACTOR));

        // Map the normalized score to max friends range
        int maxBestFriends = (int) Math.round(SOCIAL_LINK_FRIEND_MAXIMUM * normalizedScore);

        // Ensure every student can have at least some social connections
        maxBestFriends = Math.max(SOCIAL_LINK_FRIEND_MINIMUM, maxBestFriends);

        student.studentStatistics.setMaxBestFriends(maxBestFriends);
        // The wider network capacity scales off the same base-stat formula
        student.studentStatistics.setMaxSocialConnections(
                StudentStatistics.deriveMaxSocialConnections(maxBestFriends));
    }

    // ---- Potential Friend Selection ----

    /**
     * Finds a potential friend for a student based on grade level and gender
     * preference.
     * <p>
     * Grade preference hierarchy:
     * <ul>
     * <li>90% chance: same grade classmates</li>
     * <li>~7.5% chance: adjacent grades (e.g., Freshman-Sophomore,
     * Junior-Senior)</li>
     * <li>~2.5% chance: other grades</li>
     * </ul>
     * </p>
     * <p>
     * Same-gender preference is a soft candidate-weight multiplier rather
     * than a hard filter: same-gender candidates are favoured by
     * {@code sameGenderWeight} but mixed-gender links remain possible.
     * Close friendships pass a strong weight (~70% same-gender in a balanced
     * pool); casual acquaintances a mild one.
     * </p>
     *
     * @param student          The student seeking friends.
     * @param standardSchool   The standard school entity.
     * @param forRival         If true, uses rival affinity weights (prefers
     *                         Hate/Negative cliques); otherwise uses friend
     *                         affinity weights (prefers Same/Aligns cliques).
     * @param sameGenderWeight Candidate-weight multiplier applied to
     *                         same-gender candidates (1.0 = no preference).
     * @return A potential friend/rival or null if none found.
     */
    private Student findPotentialFriend(Student student,
            StandardSchool standardSchool, boolean forRival, double sameGenderWeight) {
        String gradeLevel = student.studentStatistics.getGradeLevel();
        ArrayList<Student> potentialFriends = new ArrayList<>();

        if (GameRandom.nextInt(
                SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_THRESHOLD) {
            HashMap<Integer, Student> gradeClassmates = standardSchool.getStudentGradeClass(gradeLevel);
            if (gradeClassmates != null) {
                for (Student otherStudent : gradeClassmates.values()) {
                    if (!otherStudent.equals(student)) {
                        potentialFriends.add(otherStudent);
                    }
                }
            }
        } else {
            if (GameRandom.nextInt(
                    SOCIAL_LINK_FRIEND_ADJACENT_GRADE_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_ADJACENT_GRADE_THRESHOLD) {
                String[] adjacentGrades = getAdjacentGrades(gradeLevel);
                for (String grade : adjacentGrades) {
                    HashMap<Integer, Student> adjacentGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    if (adjacentGradeClassmates != null) {
                        potentialFriends.addAll(adjacentGradeClassmates.values());
                    }
                }
            } else {
                String[] otherGrades = getOtherGrades(gradeLevel);
                for (String grade : otherGrades) {
                    HashMap<Integer, Student> otherGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    if (otherGradeClassmates != null) {
                        potentialFriends.addAll(otherGradeClassmates.values());
                    }
                }
            }
        }

        if (potentialFriends.isEmpty()) {
            return null;
        }

        return weightedCliqueSelect(potentialFriends, student, forRival, sameGenderWeight);
    }

    /**
     * Selects a student from the candidate list using weighted random
     * selection based on clique affinity and a soft same-gender preference.
     * Friend mode favours Same and Aligns cliques; rival mode favours Hate
     * and Negative cliques. Same-gender candidates have their weight
     * multiplied by {@code sameGenderWeight}.
     */
    private Student weightedCliqueSelect(List<Student> candidates,
            Student student, boolean forRival, double sameGenderWeight) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        String myClique = student.studentStatistics.getMainClique();
        String myGender = student.studentStatistics.getGender();
        String myNeighborhood = student.studentStatistics.getNeighborhoodName();
        double[] weights = new double[candidates.size()];
        double total = 0;

        for (int i = 0; i < candidates.size(); i++) {
            Student candidate = candidates.get(i);
            String theirClique = candidate.studentStatistics.getMainClique();
            double weight = getCliqueWeight(myClique, theirClique, forRival);
            String theirGender = candidate.studentStatistics.getGender();
            if (myGender != null && theirGender != null
                    && myGender.equalsIgnoreCase(theirGender)) {
                weight *= sameGenderWeight;
            }
            String theirNeighborhood = candidate.studentStatistics.getNeighborhoodName();
            if (myNeighborhood != null && myNeighborhood.equals(theirNeighborhood)) {
                weight *= SOCIAL_LINK_SAME_NEIGHBORHOOD_WEIGHT;
            }
            weights[i] = weight;
            total += weight;
        }

        double roll = GameRandom.nextDouble() * total;
        double cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights[i];
            if (roll < cumulative) {
                return candidates.get(i);
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private double getCliqueWeight(String myClique, String theirClique,
            boolean forRival) {
        if (myClique == null || theirClique == null) {
            return forRival
                    ? CLIQUE_RIVAL_AFFINITY_NEUTRAL
                    : CLIQUE_AFFINITY_NEUTRAL;
        }
        if (myClique.equals(theirClique)) {
            return forRival
                    ? CLIQUE_RIVAL_AFFINITY_SAME
                    : CLIQUE_AFFINITY_SAME;
        }
        String rel = CliqueLoader.getRelationship(myClique, theirClique);
        if (forRival) {
            return switch (rel) {
                case "Aligns" -> CLIQUE_RIVAL_AFFINITY_ALIGNS;
                case "Positive" -> CLIQUE_RIVAL_AFFINITY_POSITIVE;
                case "Negative" -> CLIQUE_RIVAL_AFFINITY_NEGATIVE;
                case "Hate" -> CLIQUE_RIVAL_AFFINITY_HATE;
                default -> CLIQUE_RIVAL_AFFINITY_NEUTRAL;
            };
        }
        return switch (rel) {
            case "Aligns" -> CLIQUE_AFFINITY_ALIGNS;
            case "Positive" -> CLIQUE_AFFINITY_POSITIVE;
            case "Negative" -> CLIQUE_AFFINITY_NEGATIVE;
            case "Hate" -> CLIQUE_AFFINITY_HATE;
            default -> CLIQUE_AFFINITY_NEUTRAL;
        };
    }

    // ---- Grade Helper Methods ----

    /**
     * Returns adjacent grade levels for a given grade.
     * Sophomore and Junior now correctly include their adjacent grades.
     */
    private String[] getAdjacentGrades(String gradeLevel) {
        return switch (gradeLevel) {
            case "Freshman" -> new String[] { "Sophomore" };
            case "Sophomore" -> new String[] { "Freshman", "Junior" };
            case "Junior" -> new String[] { "Sophomore", "Senior" };
            case "Senior" -> new String[] { "Junior" };
            default -> new String[] {};
        };
    }

    /**
     * Returns non-adjacent, non-self grade levels for a given grade.
     * These are the "distant" grades that a student is least likely to befriend.
     */
    private String[] getOtherGrades(String gradeLevel) {
        return switch (gradeLevel) {
            case "Freshman" -> new String[] { "Junior", "Senior" };
            case "Sophomore" -> new String[] { "Senior" };
            case "Junior" -> new String[] { "Freshman" };
            case "Senior" -> new String[] { "Freshman", "Sophomore" };
            default -> new String[] {};
        };
    }

    // ---- Weight Assignment Methods ----
    // All weights use the -100 to 100 scale per the README.

    /**
     * Assigns a positive weight for a friend relationship.
     * Friends always have at least a mildly positive score (floor of 10).
     * Gaussian distribution centered around 50 with std dev 20.
     *
     * @return A weight in the range [FLOOR, 100].
     */
    private double assignFriendWeight() {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_FRIEND_WEIGHT_STD_DEV
                + SOCIAL_LINK_FRIEND_WEIGHT_MEAN;
        return Math.max(SOCIAL_LINK_FRIEND_WEIGHT_FLOOR, Math.min(SOCIAL_LINK_SCORE_MAX, weight));
    }

    /**
     * Assigns a reciprocal weight for when a student is befriended by someone else.
     * The feeling is not guaranteed to be mutual: the reciprocal score may be
     * positive, near-zero, or even slightly negative.
     * Gaussian distribution centered around 30 with std dev 25.
     *
     * @return A weight in the range [-100, 100].
     */
    private double assignReciprocalWeight() {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_RECIPROCAL_WEIGHT_STD_DEV
                + SOCIAL_LINK_RECIPROCAL_WEIGHT_MEAN;
        return Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_SCORE_MAX, weight));
    }

    /**
     * Assigns a weight for a sibling relationship.
     * Sibling relationships are highly variable: some siblings are close allies,
     * others actively resent each other. Mean is slightly positive (25) with
     * a wide std dev (35) to produce the full range of sibling dynamics.
     *
     * @return A weight in the range [-100, 100].
     */
    private double assignSiblingWeight() {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_SIBLING_WEIGHT_STD_DEV
                + SOCIAL_LINK_SIBLING_WEIGHT_MEAN;
        return Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_SCORE_MAX, weight));
    }

    /**
     * Assigns a negative weight for a rival/enemy relationship.
     * Rivals always have a negative score (ceiling of -5).
     * Gaussian distribution centered around -40 with std dev 20.
     *
     * @return A weight in the range [-100, -5].
     */
    private double assignRivalWeight() {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_RIVAL_WEIGHT_STD_DEV
                + SOCIAL_LINK_RIVAL_WEIGHT_MEAN;
        return Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_RIVAL_WEIGHT_CEILING, weight));
    }

    /**
     * Assigns a mildly positive weight for a casual/acquaintance link
     * (the initiating direction). Gaussian around 15 with a floor of 1,
     * so acquaintances are always at least faintly positive from the
     * initiator's side.
     *
     * @return A weight in the range [FLOOR, 100].
     */
    private double assignAcquaintanceWeight() {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_ACQUAINTANCE_WEIGHT_STD_DEV
                + SOCIAL_LINK_ACQUAINTANCE_WEIGHT_MEAN;
        return Math.max(SOCIAL_LINK_ACQUAINTANCE_WEIGHT_FLOOR, Math.min(SOCIAL_LINK_SCORE_MAX, weight));
    }

    /**
     * Assigns the independent reciprocal weight for an acquaintance link.
     * Centered lower and with a wide spread, so the other party may be
     * mildly positive, indifferent, or negative -- producing the one-sided
     * relationships described in the README.
     *
     * @return A weight in the range [-100, 100].
     */
    private double assignAcquaintanceReciprocalWeight() {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_ACQUAINTANCE_RECIPROCAL_STD_DEV
                + SOCIAL_LINK_ACQUAINTANCE_RECIPROCAL_MEAN;
        return Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_SCORE_MAX, weight));
    }

    // ---- Relationship Classification ----

    /**
     * Directed relationship tier derived from a single social-link score.
     * Tiers are computed on demand from the score rather than persisted,
     * so they always reflect the current state of the graph.
     */
    public enum RelationshipTier {
        BEST_FRIEND,
        FRIEND,
        ACQUAINTANCE,
        NEUTRAL,
        DISLIKE,
        ENEMY;
    }

    /**
     * Reciprocity state of an unordered student pair, derived from both
     * directed scores. Combined with sibling/catalyst context this covers
     * the README's asymmetric relationship examples (unrequited admiration,
     * deceitful friend, one-sided jealousy, frenemies) without persisting
     * speculative labels.
     */
    public enum Reciprocity {
        MUTUAL_POSITIVE,
        ONE_SIDED_POSITIVE,
        OPPOSED,
        ONE_SIDED_NEGATIVE,
        MUTUAL_NEGATIVE,
        NEUTRAL;
    }

    /**
     * Classifies a raw directed score into a relationship tier.
     *
     * @param score the directed social-link score (-100 to 100)
     * @return the tier for that score
     */
    public static RelationshipTier classifyScore(double score) {
        if (score >= SOCIAL_LINK_BEST_FRIEND_THRESHOLD) {
            return RelationshipTier.BEST_FRIEND;
        }
        if (score >= SOCIAL_LINK_TIER_FRIEND_THRESHOLD) {
            return RelationshipTier.FRIEND;
        }
        if (score >= SOCIAL_LINK_TIER_ACQUAINTANCE_THRESHOLD) {
            return RelationshipTier.ACQUAINTANCE;
        }
        if (score > SOCIAL_LINK_TIER_DISLIKE_THRESHOLD) {
            return RelationshipTier.NEUTRAL;
        }
        if (score > SOCIAL_LINK_TIER_ENEMY_THRESHOLD) {
            return RelationshipTier.DISLIKE;
        }
        return RelationshipTier.ENEMY;
    }

    /**
     * Returns the directed relationship tier from source toward target.
     *
     * @param source the student whose feelings are being classified
     * @param target the other student
     * @return the tier of source's outgoing link (NEUTRAL when no edge exists)
     */
    public RelationshipTier getRelationshipTier(Student source, Student target) {
        return classifyScore(getSocialScore(source, target));
    }

    /**
     * Derives the reciprocity state of a pair from both directed scores.
     *
     * @param a one student
     * @param b the other student
     * @return the pair's reciprocity state
     */
    public Reciprocity getReciprocity(Student a, Student b) {
        double scoreAb = getSocialScore(a, b);
        double scoreBa = getSocialScore(b, a);
        boolean abPositive = scoreAb >= SOCIAL_LINK_TIER_ACQUAINTANCE_THRESHOLD;
        boolean baPositive = scoreBa >= SOCIAL_LINK_TIER_ACQUAINTANCE_THRESHOLD;
        boolean abNegative = scoreAb <= SOCIAL_LINK_TIER_DISLIKE_THRESHOLD;
        boolean baNegative = scoreBa <= SOCIAL_LINK_TIER_DISLIKE_THRESHOLD;

        if (abPositive && baPositive) {
            return Reciprocity.MUTUAL_POSITIVE;
        }
        if (abNegative && baNegative) {
            return Reciprocity.MUTUAL_NEGATIVE;
        }
        if ((abPositive && baNegative) || (abNegative && baPositive)) {
            return Reciprocity.OPPOSED;
        }
        if (abPositive || baPositive) {
            return Reciprocity.ONE_SIDED_POSITIVE;
        }
        if (abNegative || baNegative) {
            return Reciprocity.ONE_SIDED_NEGATIVE;
        }
        return Reciprocity.NEUTRAL;
    }

    /**
     * Returns every student this student holds a positive outgoing link
     * toward (score above 0), regardless of tier. Used by behavior systems
     * that want the full known-and-liked circle, not just close friends.
     *
     * @param student the student whose outgoing links to inspect
     * @return list of positively-linked targets (never null)
     */
    public List<Student> getPositiveConnections(Student student) {
        List<Student> result = new ArrayList<>();
        if (student == null || !socialGraph.containsVertex(student)) {
            return result;
        }
        for (DefaultWeightedEdge edge : socialGraph.outgoingEdgesOf(student)) {
            if (socialGraph.getEdgeWeight(edge) > 0) {
                result.add(socialGraph.getEdgeTarget(edge));
            }
        }
        return result;
    }

    // ---- Friend Cache Synchronization ----

    /**
     * Rebuilds every student's {@code friendsInSchool} compatibility cache
     * from the graph: the cache holds exactly the non-sibling targets whose
     * outgoing score is at friend tier or stronger
     * ({@value constants.SimConstants#SOCIAL_LINK_TIER_FRIEND_THRESHOLD}+).
     * Weak acquaintances live only in the graph.
     */
    public void refreshFriendCaches() {
        for (Student student : socialGraph.vertexSet()) {
            refreshFriendCache(student);
        }
    }

    /**
     * Synchronizes a single student's friend cache with their outgoing
     * graph edges (see {@link #refreshFriendCaches()}).
     */
    private void refreshFriendCache(Student student) {
        if (student == null || !socialGraph.containsVertex(student)) {
            return;
        }
        ArrayList<Student> friends = student.studentStatistics.getFriendsInSchool();
        // Drop entries that no longer qualify (decayed, or stale duplicates)
        friends.removeIf(friend -> getSocialScore(student, friend) < SOCIAL_LINK_TIER_FRIEND_THRESHOLD);
        // Add qualifying non-sibling targets (addFriendInSchool is dupe-safe)
        for (DefaultWeightedEdge edge : socialGraph.outgoingEdgesOf(student)) {
            if (socialGraph.getEdgeWeight(edge) < SOCIAL_LINK_TIER_FRIEND_THRESHOLD) {
                continue;
            }
            Student target = socialGraph.getEdgeTarget(edge);
            if (student.studentStatistics.getSiblingsInSchool().contains(target)) {
                continue;
            }
            student.studentStatistics.addFriendInSchool(target);
        }
    }

    // ---- Visualization Methods ----

    /**
     * Visualizes the social graph using mxGraph. Weak links (absolute score
     * below {@value constants.SimConstants#SOCIAL_LINK_VISUALIZER_MIN_ABS_WEIGHT})
     * are hidden so the much denser acquaintance-widened graph stays legible;
     * the underlying graph keeps every edge.
     */
    public void schoolSocialLinkVisualizer() {
        // Use JGraphXAdapter to adapt JGraphT graph to JGraphX
        JGraphXAdapter<Student, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(socialGraph);
        graphAdapter.setAllowDanglingEdges(false);

        graph = new mxGraph();
        graph.getModel().beginUpdate();
        try {
            Object parent = graph.getDefaultParent();

            // Insert all vertices and map them to mxCells
            for (Student student : socialGraph.vertexSet()) {
                Object cell = graph.insertVertex(parent, null, student.toString(), 0, 0, 30, 30);
                vertexToCellMap.put(student, cell);
            }

            // Insert all significant edges using the mapped mxCells.
            // Weak acquaintance edges are omitted from the school-wide view.
            for (DefaultWeightedEdge edge : socialGraph.edgeSet()) {
                if (Math.abs(socialGraph.getEdgeWeight(edge)) < SOCIAL_LINK_VISUALIZER_MIN_ABS_WEIGHT) {
                    continue;
                }
                Student source = socialGraph.getEdgeSource(edge);
                Student target = socialGraph.getEdgeTarget(edge);
                Object sourceCell = vertexToCellMap.get(source);
                Object targetCell = vertexToCellMap.get(target);

                // Ensure that both source and target cells exist
                if (sourceCell != null && targetCell != null) {
                    graph.insertEdge(parent, null, "", sourceCell, targetCell);
                } else {
                    GameLogger.logDebug("Source or Target cell is null for edge: " + edge);
                }
            }

        } finally {
            graph.getModel().endUpdate();
        }

        // Apply layout (mxCircleLayout for evenly spaced nodes)
        mxCircleLayout layout = new mxCircleLayout(graph);
        layout.setRadius(150); // Adjust radius as needed
        layout.execute(graph.getDefaultParent());

        // Update the graph in the component
        graphComponent.setGraph(graph);
        graphComponent.refresh();

        // Additional visualization settings can be applied here
    }

    public void studentVisualizer(Student student) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();

        // Create a subgraph for the specific student and their connections
        Graph<Student, DefaultEdge> subGraph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        subGraph.addVertex(student);

        for (DefaultWeightedEdge edge : socialGraph.edgesOf(student)) {
            Student source = socialGraph.getEdgeSource(edge);
            Student target = socialGraph.getEdgeTarget(edge);
            subGraph.addVertex(source);
            subGraph.addVertex(target);
            subGraph.addEdge(source, target);
            subGraph.setEdgeWeight(source, target, socialGraph.getEdgeWeight(edge));
        }

        JGraphXAdapter<Student, DefaultEdge> graphAdapter = new JGraphXAdapter<>(subGraph);
        JFrame frame = new JFrame(studentName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        graphComponent.getGraphControl().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                if (cell != null) {
                    Object vertex = graphAdapter.getCellToVertexMap().get(cell);
                    if (vertex instanceof Student clicked) {
                        LinkSupport.navigate(clicked);
                    }
                }
            }
        });
        frame.add(graphComponent);
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        frame.pack();
        frame.setVisible(true);
    }
}
