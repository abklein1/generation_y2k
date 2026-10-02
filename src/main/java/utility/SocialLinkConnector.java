package utility;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import save.SocialLinkSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import javax.swing.*;

import entity.OrientationDisclosure;
import entity.RomanticStatus;
import entity.SexualOrientation;
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

    // Romance records: keyed by a *directed* pair identifier ("sourceId>targetId"),
    // storing the source's perception of the relationship. Each direction is
    // stored independently, so the two parties can disagree about what (if
    // anything) is going on between them -- one may believe they are going
    // steady while the other considers it a fling, and crushes are entirely
    // one-directional.
    private final HashMap<String, RomanticStatus> romanceRecords = new HashMap<>();

    // Couple-knowledge records: keyed by "observerId>idA:idB" (directed
    // observer over an unordered couple pair). Each entry means the observer
    // knows students A and B are an item. Stored per observer so a future
    // gossip system can copy records between students; today they are only
    // created by the perception-gated notice pass in RomanceUpdater.
    private final HashSet<String> coupleKnowledgeRecords = new HashSet<>();

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
        this(studentHashMap, standardSchool, null);
    }

    /**
     * Constructor to initialize social links with progress reporting.
     *
     * @param studentHashMap HashMap of students.
     * @param standardSchool The standard school entity.
     * @param progress       optional callback receiving human-readable phase
     *                       updates during the (long) generation pass; may be null
     */
    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool,
            Consumer<String> progress) {
        this(); // Call the default constructor to initialize graphComponent
        initializeSocialLinks(studentHashMap, standardSchool, progress);
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
        initializeSocialLinks(studentHashMap, standardSchool, null);
    }

    /**
     * Initializes social links with optional progress reporting (see
     * {@link #initializeSocialLinks(HashMap, StandardSchool)} for the phase
     * breakdown). With ~1200 students and acquaintance widening this pass is
     * the longest part of school generation, so callers with a loading UI
     * should pass a progress consumer.
     *
     * @param studentHashMap HashMap of students.
     * @param standardSchool The standard school entity.
     * @param progress       optional callback for phase/percentage updates; may be null
     */
    public void initializeSocialLinks(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool,
            Consumer<String> progress) {

        if (studentHashMap == null || standardSchool == null) {
            throw new IllegalArgumentException("Student hash map and standard school cannot be null.");
        }
        registerStudentIds(studentHashMap);

        // Only enrolled students may enter the graph. Town generation creates
        // extra-pool students and "not in school" siblings that are still
        // referenced from enrolled students' sibling lists; linking them would
        // create ghost vertices that pollute rankings and waste link capacity.
        HashSet<Student> enrolled = new HashSet<>(studentHashMap.values());

        // Grade rosters are static during generation; snapshot them once so
        // candidate selection doesn't rebuild a full grade list per attempt.
        HashMap<String, ArrayList<Student>> gradePools = buildGradePools(standardSchool, enrolled);
        int totalStudents = studentHashMap.size();

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
        report(progress, "Social links: linking siblings...");
        for (Student student : studentHashMap.values()) {
            ArrayList<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();
            for (Student sibling : siblingsInSchool) {
                // Sibling lists are populated during town generation, before
                // enrollment is decided, so they can reference students who
                // never enrolled. Skip those instead of adding ghost vertices.
                if (sibling == null || !enrolled.contains(sibling)) {
                    continue;
                }

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
        report(progress, "Social links: forming close friendships...");
        int friendPassProcessed = 0;
        for (Student student : studentHashMap.values()) {
            friendPassProcessed++;
            if (progress != null && friendPassProcessed % PROGRESS_REPORT_INTERVAL == 0) {
                progress.accept("Social links: forming close friendships ("
                        + friendPassProcessed + "/" + totalStudents + ")...");
            }
            int targetFriendCount = generateFriendCount(student);
            // Account for any friends already added via reciprocal links from earlier
            // students
            int friendsAdded = student.studentStatistics.getFriendsInSchool().size();
            int attempts = 0;
            int maxAttempts = Math.max(targetFriendCount * SOCIAL_LINK_FRIEND_MAX_ATTEMPTS_MULTIPLIER, 5);
            // Candidate weights are constant per (student, pool, mode), so they
            // are computed once here and reused across this student's attempts.
            HashMap<String, WeightedPool> poolCache = new HashMap<>();

            while (friendsAdded < targetFriendCount && attempts < maxAttempts) {
                Student potentialFriend = findPotentialFriend(
                        student, gradePools, poolCache, false, sameGenderWeightFor(student, true));
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

                socialGraph.addVertex(potentialFriend);

                // Add directed edge: student -> friend (positive weight, with
                // the clique halo bias toward the target)
                double friendWeight = clampScore(
                        assignFriendWeight(student) + cliquePerceptionBias(potentialFriend));
                DefaultWeightedEdge edge = socialGraph.addEdge(student, potentialFriend);
                if (edge != null) {
                    socialGraph.setEdgeWeight(edge, friendWeight);
                }

                // Add reciprocal edge: friend -> student (independent weight, may differ)
                // Per the README, relationships are not bidirectional in score:
                // each party can feel differently about the other.
                if (!socialGraph.containsEdge(potentialFriend, student)) {
                    double reciprocalWeight = clampScore(
                            assignReciprocalWeight() + cliquePerceptionBias(student));
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
        report(progress, "Social links: seeding rivalries...");
        for (Student student : studentHashMap.values()) {
            int numRivals = generateRivalCount();
            int rivalsAdded = 0;
            int attempts = 0;
            int maxAttempts = Math.max(numRivals * SOCIAL_LINK_FRIEND_MAX_ATTEMPTS_MULTIPLIER, 3);
            HashMap<String, WeightedPool> poolCache = new HashMap<>();

            while (rivalsAdded < numRivals && attempts < maxAttempts) {
                Student rival = findPotentialFriend(
                        student, gradePools, poolCache, true, SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT);
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
        report(progress, "Social links: weaving acquaintance circles...");
        int acquaintancePassProcessed = 0;
        for (Student student : studentHashMap.values()) {
            acquaintancePassProcessed++;
            if (progress != null && acquaintancePassProcessed % PROGRESS_REPORT_INTERVAL == 0) {
                progress.accept("Social links: weaving acquaintance circles ("
                        + acquaintancePassProcessed + "/" + totalStudents + ")...");
            }
            int targetConnections = generateConnectionCount(student);
            int connections = socialGraph.outDegreeOf(student);
            int attempts = 0;
            int maxAttempts = Math.max(
                    (targetConnections - connections) * SOCIAL_LINK_FRIEND_MAX_ATTEMPTS_MULTIPLIER, 5);
            HashMap<String, WeightedPool> poolCache = new HashMap<>();

            while (connections < targetConnections && attempts < maxAttempts) {
                Student acquaintance = findPotentialFriend(
                        student, gradePools, poolCache, false, sameGenderWeightFor(student, false));
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
                    socialGraph.setEdgeWeight(edge, clampScore(
                            assignAcquaintanceWeight() + cliquePerceptionBias(acquaintance)));
                }

                // Independent reciprocal: the other party may barely register
                // this student, or even quietly dislike them (asymmetry per
                // the README examples)
                if (!socialGraph.containsEdge(acquaintance, student)) {
                    DefaultWeightedEdge reciprocalEdge = socialGraph.addEdge(acquaintance, student);
                    if (reciprocalEdge != null) {
                        socialGraph.setEdgeWeight(reciprocalEdge, clampScore(
                                assignAcquaintanceReciprocalWeight() + cliquePerceptionBias(student)));
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
        report(progress, "Social links: recording best-friend catalysts...");
        generateInitialCatalysts(studentHashMap);

        // Phase 8: Synchronize the friendsInSchool compatibility cache so it
        // reflects exactly the friend-or-stronger outgoing links in the graph
        report(progress, "Social links: syncing friend lists...");
        refreshFriendCaches();

        // The whole-school visualization is intentionally NOT built here:
        // adapting ~1200 vertices and tens of thousands of directed edges into
        // mxGraph cells is expensive, and the UI rebuilds it on demand via
        // schoolSocialLinkVisualizer() anyway.
    }

    private static void report(Consumer<String> progress, String message) {
        if (progress != null) {
            progress.accept(message);
        }
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

    // ---- Romance Registry ----

    /**
     * Creates a directed key for a source-target pair. Unlike catalyst pair
     * keys, (A, B) and (B, A) produce different keys: romantic perception is
     * per direction.
     */
    private String makeDirectedKey(Student source, Student target) {
        return getStableStudentId(source) + ">" + getStableStudentId(target);
    }

    /**
     * Returns the source student's perception of their romantic relationship
     * with the target ({@link RomanticStatus#NONE} when no record exists).
     * The reverse direction is stored independently and may differ.
     *
     * @param source the student whose perception is queried
     * @param target the other student
     * @return the source's romantic status toward the target
     */
    public RomanticStatus getRomanticStatus(Student source, Student target) {
        if (source == null || target == null) {
            return RomanticStatus.NONE;
        }
        return romanceRecords.getOrDefault(makeDirectedKey(source, target), RomanticStatus.NONE);
    }

    /**
     * Records the source student's perception of their romantic relationship
     * with the target. Passing {@link RomanticStatus#NONE} (or null) clears
     * the record. The reverse direction is untouched.
     *
     * @param source the student whose perception is being set
     * @param target the other student
     * @param status the source's perceived relationship, or NONE to clear
     */
    public void setRomanticStatus(Student source, Student target, RomanticStatus status) {
        if (source == null || target == null || source.equals(target)) {
            return;
        }
        String key = makeDirectedKey(source, target);
        if (status == null || status == RomanticStatus.NONE) {
            romanceRecords.remove(key);
        } else {
            romanceRecords.put(key, status);
        }
    }

    /**
     * Returns every student toward whom this student holds any romantic
     * perception (crush, fling, or steady). Romance always sits on top of an
     * existing social edge, so outgoing edges are the complete search space.
     *
     * @param source the student whose romantic interests to list
     * @return list of romance targets (never null)
     */
    public List<Student> getRomanticInterests(Student source) {
        List<Student> result = new ArrayList<>();
        if (source == null || !socialGraph.containsVertex(source)) {
            return result;
        }
        for (DefaultWeightedEdge edge : socialGraph.outgoingEdgesOf(source)) {
            Student target = socialGraph.getEdgeTarget(edge);
            if (getRomanticStatus(source, target) != RomanticStatus.NONE) {
                result.add(target);
            }
        }
        return result;
    }

    /**
     * Checks whether this student is involved in a mutual romantic
     * relationship (both directions fling-or-stronger with someone).
     *
     * @param student the student to check
     * @return true if any mutual fling/steady pairing exists
     */
    public boolean hasMutualRomance(Student student) {
        for (Student other : getRomanticInterests(student)) {
            RomanticStatus outgoing = getRomanticStatus(student, other);
            RomanticStatus incoming = getRomanticStatus(other, student);
            if (outgoing.ordinal() >= RomanticStatus.FLING.ordinal()
                    && incoming.ordinal() >= RomanticStatus.FLING.ordinal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a human-readable classification of the pair's romantic state
     * from both directed perceptions (mutual steady, asymmetric perception,
     * one-sided crush, etc.).
     *
     * @param a one student
     * @param b the other student
     * @return a short description of the pair's romantic relationship
     */
    public String getRelationshipSummary(Student a, Student b) {
        RomanticStatus ab = getRomanticStatus(a, b);
        RomanticStatus ba = getRomanticStatus(b, a);
        String nameA = a.studentName.getFirstName();
        String nameB = b.studentName.getFirstName();
        if (ab == RomanticStatus.NONE && ba == RomanticStatus.NONE) {
            return "No romantic relationship.";
        }
        if (ab == ba) {
            return "Mutual: both consider it " + ab.mutualLabel() + ".";
        }
        if (ab == RomanticStatus.CRUSH && ba == RomanticStatus.NONE) {
            return nameA + " has a crush on " + nameB + " (" + nameB + " is unaware).";
        }
        if (ba == RomanticStatus.CRUSH && ab == RomanticStatus.NONE) {
            return nameB + " has a crush on " + nameA + " (" + nameA + " is unaware).";
        }
        return "Asymmetric: " + nameA + " considers it " + ab.label()
                + ", while " + nameB + " considers it " + ba.label() + ".";
    }

    /**
     * Gets all recorded romance entries keyed by directed pair id. Useful
     * for inspection/debugging and generation summaries.
     *
     * @return copy of the romance records
     */
    public HashMap<String, RomanticStatus> getAllRomanceRecords() {
        return new HashMap<>(romanceRecords);
    }

    // ---- Couple Knowledge (who knows who's dating) ----

    /**
     * Whether the pair reads as a couple to observant peers: both directions
     * report fling-or-stronger. One-sided flings and crushes are invisible
     * from the outside -- nothing couple-like happens in public.
     *
     * @param a one student
     * @param b the other student
     * @return true when both perceptions are FLING or STEADY
     */
    public boolean isObservableCouple(Student a, Student b) {
        RomanticStatus ab = getRomanticStatus(a, b);
        RomanticStatus ba = getRomanticStatus(b, a);
        return (ab == RomanticStatus.FLING || ab == RomanticStatus.STEADY)
                && (ba == RomanticStatus.FLING || ba == RomanticStatus.STEADY);
    }

    /**
     * Key for one observer's knowledge of an unordered couple:
     * "observerId&gt;idA:idB" (the pair portion reuses the catalyst pair key,
     * so the pair's digits-and-colon format can never collide with the
     * observer prefix).
     */
    private String makeKnowledgeKey(Student observer, Student a, Student b) {
        return getStableStudentId(observer) + ">" + makePairKey(a, b);
    }

    /**
     * Records that the observer now knows students {@code a} and {@code b}
     * are a couple.
     *
     * @param observer the student who learned about the couple
     * @param a        one member of the couple
     * @param b        the other member of the couple
     */
    public void recordCoupleKnowledge(Student observer, Student a, Student b) {
        if (observer == null || a == null || b == null) {
            return;
        }
        coupleKnowledgeRecords.add(makeKnowledgeKey(observer, a, b));
    }

    /**
     * Whether the observer knows students {@code a} and {@code b} are a
     * couple.
     *
     * @param observer the potential knower
     * @param a        one member of the couple
     * @param b        the other member of the couple
     * @return true if the observer holds a knowledge record for the pair
     */
    public boolean knowsAboutCouple(Student observer, Student a, Student b) {
        if (observer == null || a == null || b == null) {
            return false;
        }
        return coupleKnowledgeRecords.contains(makeKnowledgeKey(observer, a, b));
    }

    /**
     * Purges every observer's knowledge of the (a, b) couple. Called when
     * the couple dissolves so stale jealousy does not linger; if the pair
     * gets back together, peers must notice all over again.
     *
     * @param a one member of the former couple
     * @param b the other member of the former couple
     */
    public void clearCoupleKnowledge(Student a, Student b) {
        if (a == null || b == null) {
            return;
        }
        String suffix = ">" + makePairKey(a, b);
        coupleKnowledgeRecords.removeIf(key -> key.endsWith(suffix));
    }

    /**
     * Lists the partners of {@code crush} that the observer both can see
     * (still an observable couple) and actually knows about. The jealousy
     * behavior branch uses this to pick a rival.
     *
     * @param observer the jealous student
     * @param crush    the student whose partners are being looked up
     * @return known, still-observable partners (never null)
     */
    public List<Student> getKnownPartnersOf(Student observer, Student crush) {
        List<Student> result = new ArrayList<>();
        if (observer == null || crush == null) {
            return result;
        }
        for (Student partner : getRomanticInterests(crush)) {
            if (partner.equals(observer)) {
                continue;
            }
            if (isObservableCouple(crush, partner)
                    && knowsAboutCouple(observer, crush, partner)) {
                result.add(partner);
            }
        }
        return result;
    }

    /**
     * Gets all couple-knowledge records ("observerId&gt;idA:idB"). Useful for
     * snapshots, tests, and inspection.
     *
     * @return copy of the knowledge records
     */
    public HashSet<String> getAllCoupleKnowledge() {
        return new HashSet<>(coupleKnowledgeRecords);
    }

    /**
     * Computes each student's reputation total: the sum of every other
     * student's directed score toward them. This is the popularity metric
     * used by the social rankings — a student many people like scores high,
     * a student many people dislike scores negative, and asymmetries are
     * captured because only incoming edges count.
     *
     * @return map of every student in the graph to their incoming score sum
     */
    public HashMap<Student, Double> computeIncomingScoreTotals() {
        HashMap<Student, Double> totals = new HashMap<>();
        for (Student student : socialGraph.vertexSet()) {
            totals.put(student, 0.0);
        }
        for (DefaultWeightedEdge edge : socialGraph.edgeSet()) {
            totals.merge(socialGraph.getEdgeTarget(edge), socialGraph.getEdgeWeight(edge), Double::sum);
        }
        return totals;
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
        HashMap<String, String> romanceByName = new HashMap<>();
        for (Map.Entry<String, RomanticStatus> entry : romanceRecords.entrySet()) {
            romanceByName.put(entry.getKey(), entry.getValue().name());
        }
        snapshot.putRomance(romanceByName);
        snapshot.putCoupleKnowledge(coupleKnowledgeRecords);
        return snapshot;
    }

    public void restoreFromSnapshot(HashMap<Integer, Student> studentHashMap,
            SocialLinkSnapshot snapshot) {
        registerStudentIds(studentHashMap);
        socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        catalystRecords.clear();
        romanceRecords.clear();
        coupleKnowledgeRecords.clear();
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
        for (Map.Entry<String, String> entry : snapshot.getRomance().entrySet()) {
            try {
                romanceRecords.put(entry.getKey(), RomanticStatus.valueOf(entry.getValue()));
            } catch (IllegalArgumentException ignored) {
                // Unknown status name from a newer/older save: skip the record
            }
        }
        coupleKnowledgeRecords.addAll(snapshot.getCoupleKnowledge());

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
     * <li><b>Steady romantic partners</b>: slower still at
     * {@value constants.SimConstants#SOCIAL_LINK_DECAY_STEADY}/day while the
     * source considers the pair to be going steady.</li>
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
            } else if (getRomanticStatus(source, target) == RomanticStatus.STEADY) {
                // Steady partners see each other constantly; the bond erodes
                // slower than a catalyst best friendship
                decayRate = SOCIAL_LINK_DECAY_STEADY;
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

    // ---- Orientation-Aware Gender Preference ----

    /**
     * Checks whether a student is openly non-heterosexual. Closeted students
     * never qualify: they deliberately mirror heterosexual social patterns
     * to blend in.
     *
     * @param student the student to check
     * @return true if the student is non-heterosexual and open about it
     */
    static boolean isOpenSexualMinority(Student student) {
        SexualOrientation orientation = student.studentStatistics.getSexualOrientation();
        OrientationDisclosure disclosure = student.studentStatistics.getOrientationDisclosure();
        return orientation != null && orientation.isNonHeterosexual()
                && disclosure != OrientationDisclosure.CLOSETED;
    }

    private static boolean isOpenSexualMinorityMale(Student student) {
        return isOpenSexualMinority(student)
                && "male".equalsIgnoreCase(student.studentStatistics.getGender());
    }

    /**
     * Resolves the same-gender candidate-weight multiplier a student uses
     * when forming friendships, adjusted for sexual orientation per studies
     * of sexual-minority youth friendship networks:
     * <ul>
     * <li>Straight and closeted students use the base weight (closeted
     * students intentionally fit heterosexual friendship tendencies)</li>
     * <li>Openly non-heterosexual females amplify the same-gender preference
     * (heightened participation in close same-gender friendships)</li>
     * <li>Openly non-heterosexual males invert it (more cross-gender than
     * same-gender friends)</li>
     * </ul>
     *
     * @param student   the student seeking friends
     * @param closeTier true for close-friend formation, false for the milder
     *                  acquaintance-widening pass
     * @return the same-gender candidate-weight multiplier for this student
     */
    static double sameGenderWeightFor(Student student, boolean closeTier) {
        double base = closeTier
                ? SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT
                : SOCIAL_LINK_SAME_GENDER_ACQUAINTANCE_WEIGHT;
        if (!isOpenSexualMinority(student)) {
            return base;
        }
        String gender = student.studentStatistics.getGender();
        if ("female".equalsIgnoreCase(gender)) {
            return base * SOCIAL_LINK_SM_FEMALE_SAME_GENDER_MULTIPLIER;
        }
        if ("male".equalsIgnoreCase(gender)) {
            return closeTier
                    ? SOCIAL_LINK_SM_MALE_SAME_GENDER_CLOSE_WEIGHT
                    : SOCIAL_LINK_SM_MALE_SAME_GENDER_ACQUAINTANCE_WEIGHT;
        }
        return base;
    }

    // ---- Potential Friend Selection ----

    /**
     * Precomputed weighted candidate pool for one (student, grade pool, mode)
     * combination. Candidate weights depend only on static attributes
     * (clique, gender, neighborhood), so a pool built once can serve every
     * selection attempt a student makes during a generation phase, replacing
     * an O(gradeSize) rebuild-and-rescore per attempt with an O(log n) draw.
     */
    private static final class WeightedPool {
        final ArrayList<Student> candidates;
        final double[] cumulativeWeights;
        final double totalWeight;

        WeightedPool(ArrayList<Student> candidates, double[] cumulativeWeights, double totalWeight) {
            this.candidates = candidates;
            this.cumulativeWeights = cumulativeWeights;
            this.totalWeight = totalWeight;
        }
    }

    /** Pool cache keys for the three grade-preference buckets. */
    private static final String POOL_SAME_GRADE = "same";
    private static final String POOL_ADJACENT_GRADES = "adjacent";
    private static final String POOL_OTHER_GRADES = "other";

    private static final String[] GRADE_LEVELS = { "Freshman", "Sophomore", "Junior", "Senior" };

    /** How often (in students processed) the long phases report progress. */
    private static final int PROGRESS_REPORT_INTERVAL = 200;

    /**
     * Snapshots each grade's roster into a plain list once per generation
     * pass. Rosters do not change while social links are being generated.
     */
    private HashMap<String, ArrayList<Student>> buildGradePools(StandardSchool standardSchool,
            HashSet<Student> enrolled) {
        HashMap<String, ArrayList<Student>> pools = new HashMap<>();
        for (String grade : GRADE_LEVELS) {
            HashMap<Integer, Student> roster = standardSchool.getStudentGradeClass(grade);
            ArrayList<Student> pool = new ArrayList<>();
            if (roster != null) {
                // Guard against grade rosters that drifted out of sync with the
                // enrolled map (e.g. scheduling culls); only enrolled students
                // may become link candidates.
                for (Student candidate : roster.values()) {
                    if (enrolled.contains(candidate)) {
                        pool.add(candidate);
                    }
                }
            }
            pools.put(grade, pool);
        }
        return pools;
    }

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
     * @param gradePools       Per-grade rosters snapshotted for this pass.
     * @param poolCache        Per-student cache of weighted pools; must be
     *                         scoped to a single (student, mode) loop.
     * @param forRival         If true, uses rival affinity weights (prefers
     *                         Hate/Negative cliques); otherwise uses friend
     *                         affinity weights (prefers Same/Aligns cliques).
     * @param sameGenderWeight Candidate-weight multiplier applied to
     *                         same-gender candidates (1.0 = no preference).
     * @return A potential friend/rival or null if none found.
     */
    private Student findPotentialFriend(Student student,
            HashMap<String, ArrayList<Student>> gradePools,
            HashMap<String, WeightedPool> poolCache,
            boolean forRival, double sameGenderWeight) {
        String poolKey;
        if (GameRandom.nextInt(
                SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_THRESHOLD) {
            poolKey = POOL_SAME_GRADE;
        } else if (GameRandom.nextInt(
                SOCIAL_LINK_FRIEND_ADJACENT_GRADE_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_ADJACENT_GRADE_THRESHOLD) {
            poolKey = POOL_ADJACENT_GRADES;
        } else {
            poolKey = POOL_OTHER_GRADES;
        }

        WeightedPool pool = poolCache.get(poolKey);
        if (pool == null) {
            pool = buildWeightedPool(student,
                    collectGradeCandidates(poolKey, student.studentStatistics.getGradeLevel(), gradePools),
                    forRival, sameGenderWeight);
            poolCache.put(poolKey, pool);
        }
        return pickFromPool(pool);
    }

    /**
     * Gathers the raw candidate list for one grade-preference bucket, in the
     * same grade order the pre-cache implementation used.
     */
    private List<Student> collectGradeCandidates(String poolKey, String gradeLevel,
            HashMap<String, ArrayList<Student>> gradePools) {
        String[] grades = switch (poolKey) {
            case POOL_SAME_GRADE -> new String[] { gradeLevel };
            case POOL_ADJACENT_GRADES -> getAdjacentGrades(gradeLevel);
            default -> getOtherGrades(gradeLevel);
        };
        ArrayList<Student> result = new ArrayList<>();
        for (String grade : grades) {
            ArrayList<Student> pool = gradePools.get(grade);
            if (pool != null) {
                result.addAll(pool);
            }
        }
        return result;
    }

    /**
     * Builds a weighted pool from raw candidates (self excluded). Weights use
     * clique affinity (friend mode favours Same/Aligns; rival mode favours
     * Hate/Negative), a soft same-gender multiplier, and a soft
     * same-neighborhood multiplier.
     */
    private WeightedPool buildWeightedPool(Student student, List<Student> rawCandidates,
            boolean forRival, double sameGenderWeight) {
        ArrayList<Student> candidates = new ArrayList<>(rawCandidates.size());
        for (Student candidate : rawCandidates) {
            if (!candidate.equals(student)) {
                candidates.add(candidate);
            }
        }

        String myClique = student.studentStatistics.getMainClique();
        String myGender = student.studentStatistics.getGender();
        String myNeighborhood = student.studentStatistics.getNeighborhoodName();
        double[] cumulativeWeights = new double[candidates.size()];
        double total = 0;

        for (int i = 0; i < candidates.size(); i++) {
            Student candidate = candidates.get(i);
            double weight = getCliqueWeight(myClique,
                    candidate.studentStatistics.getMainClique(), forRival);
            String theirGender = candidate.studentStatistics.getGender();
            if (myGender != null && theirGender != null
                    && myGender.equalsIgnoreCase(theirGender)) {
                weight *= sameGenderWeight;
            }
            String theirNeighborhood = candidate.studentStatistics.getNeighborhoodName();
            if (myNeighborhood != null && myNeighborhood.equals(theirNeighborhood)) {
                weight *= SOCIAL_LINK_SAME_NEIGHBORHOOD_WEIGHT;
            }
            total += weight;
            cumulativeWeights[i] = total;
        }
        return new WeightedPool(candidates, cumulativeWeights, total);
    }

    /**
     * Draws one candidate from a weighted pool: binary search over the
     * cumulative weight array for the first entry exceeding the roll.
     * Mirrors the previous linear scan exactly (including the single-candidate
     * fast path that consumes no randomness and the last-candidate fallback).
     */
    private Student pickFromPool(WeightedPool pool) {
        if (pool.candidates.isEmpty()) {
            return null;
        }
        if (pool.candidates.size() == 1) {
            return pool.candidates.get(0);
        }

        double roll = GameRandom.nextDouble() * pool.totalWeight;
        int lo = 0;
        int hi = pool.cumulativeWeights.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (roll < pool.cumulativeWeights[mid]) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return pool.candidates.get(lo);
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
     * Openly sexual-minority males receive a flat bonus on their outgoing
     * close-friend scores (heightened best-friend attachment per
     * sexual-minority youth friendship studies).
     *
     * @param initiator the student whose outgoing friend score this is
     * @return A weight in the range [FLOOR, 100].
     */
    private double assignFriendWeight(Student initiator) {
        double weight = GameRandom.nextGaussian() * SOCIAL_LINK_FRIEND_WEIGHT_STD_DEV
                + SOCIAL_LINK_FRIEND_WEIGHT_MEAN;
        if (isOpenSexualMinorityMale(initiator)) {
            weight += SOCIAL_LINK_SM_MALE_FRIEND_WEIGHT_BONUS;
        }
        return Math.max(SOCIAL_LINK_FRIEND_WEIGHT_FLOOR, Math.min(SOCIAL_LINK_SCORE_MAX, weight));
    }

    /**
     * The clique halo effect: a flat perception bias applied to incoming
     * friend/acquaintance/reciprocal weights during generation based on the
     * <i>target's</i> clique standing. Members of "in" cliques are viewed a
     * few points warmer by everyone, "out" clique members a bit cooler, and
     * neutral cliques (or students without a clique) are unaffected. Sibling
     * and rival edges skip the bias: family feelings and grudges don't care
     * about social standing.
     *
     * @param target the student being perceived
     * @return the score bias to add to an incoming generated edge weight
     */
    static double cliquePerceptionBias(Student target) {
        String clique = target.studentStatistics.getMainClique();
        return switch (CliqueLoader.getGroupCategory(clique)) {
            case "in-group" -> SOCIAL_LINK_IN_GROUP_PERCEPTION_BONUS;
            case "out-group" -> SOCIAL_LINK_OUT_GROUP_PERCEPTION_PENALTY;
            default -> 0.0;
        };
    }

    private static double clampScore(double value) {
        return Math.max(SOCIAL_LINK_SCORE_MIN, Math.min(SOCIAL_LINK_SCORE_MAX, value));
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

    // ---- Individual Student Graph Styling ----

    /**
     * How a peer relates to the graph's subject, in legend/sort order.
     * Romance and sibling ties trump the plain score tier because they carry
     * the most context; everything else derives from the subject's outgoing
     * score via {@link #classifyScore(double)}.
     */
    private enum PeerCategory {
        ROMANCE("Romance", "#F8BBD0"),
        SIBLING("Sibling", "#E1BEE7"),
        BEST_FRIEND("Best friend", "#FFE082"),
        FRIEND("Friend", "#C8E6C9"),
        ACQUAINTANCE("Acquaintance", "#BBDEFB"),
        NEUTRAL("Neutral", "#ECEFF1"),
        DISLIKE("Dislike", "#FFE0B2"),
        ENEMY("Enemy", "#FFCDD2");

        final String label;
        final String fillColor;

        PeerCategory(String label, String fillColor) {
            this.label = label;
            this.fillColor = fillColor;
        }

        /** Close relations sit on the inner ring, everyone else outside. */
        boolean isInnerRing() {
            return this == ROMANCE || this == SIBLING || this == BEST_FRIEND || this == FRIEND;
        }
    }

    private static final String VIZ_SUBJECT_FILL = "#FFF59D";
    private static final int VIZ_NODE_WIDTH = 180;
    private static final int VIZ_NODE_HEIGHT = 62;
    /** Blank canvas border kept around the outer ring. */
    private static final int VIZ_MARGIN = 80;
    private static final double VIZ_MIN_INNER_RADIUS = 260;
    private static final double VIZ_RING_GAP = 200;
    /** Circumference allowance per node so ring neighbours don't overlap. */
    private static final double VIZ_ARC_PER_NODE = 210;

    /**
     * Opens a cleaned-up relationship graph for one student. The subject
     * sits in the center; peers are placed on two rings (close relations
     * inside, casual/negative outside), color-coded by their relationship
     * to the subject, and labeled with both directed scores
     * (&rarr; how the subject feels, &larr; how the peer feels back) plus a
     * tag for sibling/romance/best-friend context. A legend along the
     * bottom explains the colors. Clicking a peer navigates to them.
     *
     * @param student the subject of the graph
     */
    public void studentVisualizer(Student student) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();
        StudentGraphWindow window = new StudentGraphWindow(student);
        mxGraphComponent component = window.component;

        component.setConnectable(false);
        component.getGraphControl().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Object cell = component.getCellAt(e.getX(), e.getY());
                Student clicked = cell != null ? window.cellToStudent.get(cell) : null;
                if (clicked != null && !clicked.equals(student)) {
                    LinkSupport.navigate(clicked);
                }
            }
        });

        // Ctrl + mouse wheel zooms; plain wheel scrolls vertically and
        // Shift + wheel scrolls horizontally (handled manually because the
        // scroll pane's default handler would fight the zoom gesture)
        component.setWheelScrollingEnabled(false);
        component.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                if (e.getWheelRotation() < 0) {
                    component.zoomIn();
                } else {
                    component.zoomOut();
                }
            } else {
                JScrollBar bar = e.isShiftDown()
                        ? component.getHorizontalScrollBar()
                        : component.getVerticalScrollBar();
                bar.setValue(bar.getValue() + e.getWheelRotation() * 60);
            }
        });

        JFrame frame = new JFrame(studentName + " - Social Links");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new java.awt.BorderLayout());
        frame.add(buildGraphToolbar(window), java.awt.BorderLayout.NORTH);
        frame.add(component, java.awt.BorderLayout.CENTER);
        frame.add(buildLegendPanel(), java.awt.BorderLayout.SOUTH);
        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);

        // Track the window so end-of-day refreshes reach it; stop when closed
        openStudentGraphs.add(window);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                openStudentGraphs.remove(window);
            }
        });

        frame.setVisible(true);

        // Start at full size, centered on the subject (after layout settles)
        SwingUtilities.invokeLater(window::centerOnSubject);
    }

    /**
     * Live handle for an open per-student graph window so the display can be
     * rebuilt when the underlying social data changes.
     */
    private final class StudentGraphWindow {
        private final Student subject;
        private final HashMap<Object, Student> cellToStudent = new HashMap<>();
        private final mxGraphComponent component;

        private StudentGraphWindow(Student subject) {
            this.subject = subject;
            this.component = new mxGraphComponent(buildStudentPeerGraph(subject, cellToStudent));
        }

        private Object subjectCell() {
            for (Map.Entry<Object, Student> entry : cellToStudent.entrySet()) {
                if (entry.getValue().equals(subject)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        private void centerOnSubject() {
            Object cell = subjectCell();
            if (cell != null) {
                component.scrollCellToVisible(cell, true);
            }
        }

        /**
         * Rebuilds the graph from the current social data, preserving the
         * user's zoom level and scroll position. Must run on the EDT.
         */
        private void rebuild() {
            double scale = component.getGraph().getView().getScale();
            java.awt.Point viewPosition = component.getViewport().getViewPosition();
            cellToStudent.clear();
            mxGraph fresh = buildStudentPeerGraph(subject, cellToStudent);
            fresh.getView().setScale(scale);
            component.setGraph(fresh);
            component.refresh();
            SwingUtilities.invokeLater(() -> component.getViewport().setViewPosition(viewPosition));
        }
    }

    /** Open student graph windows that receive end-of-day refreshes. */
    private final java.util.List<StudentGraphWindow> openStudentGraphs =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Redraws any open per-student graph windows from the current social
     * data, keeping each window's zoom and scroll position. Invoked once per
     * simulated day (right after {@link #applyDailyDecay()}) rather than per
     * tick or per period: scores drift slowly, so daily refreshes capture
     * every visible change while avoiding constant Swing model churn.
     */
    public void refreshOpenStudentGraphs() {
        if (openStudentGraphs.isEmpty()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            for (StudentGraphWindow window : openStudentGraphs) {
                window.rebuild();
            }
        });
    }

    /** Zoom controls for the student graph window. */
    private JPanel buildGraphToolbar(StudentGraphWindow window) {
        mxGraphComponent component = window.component;
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.LIGHT_GRAY));

        JButton zoomIn = new JButton("Zoom In");
        zoomIn.addActionListener(e -> component.zoomIn());
        toolbar.add(zoomIn);

        JButton zoomOut = new JButton("Zoom Out");
        zoomOut.addActionListener(e -> component.zoomOut());
        toolbar.add(zoomOut);

        JButton actual = new JButton("100%");
        actual.addActionListener(e -> {
            component.zoomActual();
            SwingUtilities.invokeLater(window::centerOnSubject);
        });
        toolbar.add(actual);

        JButton fit = new JButton("Fit Window");
        fit.addActionListener(e -> {
            com.mxgraph.util.mxRectangle bounds = component.getGraph().getGraphBounds();
            java.awt.Dimension viewport = component.getViewport().getSize();
            double scale = Math.min(1.0, Math.min(
                    viewport.getWidth() / Math.max(1.0, bounds.getWidth() + 2 * VIZ_MARGIN),
                    viewport.getHeight() / Math.max(1.0, bounds.getHeight() + 2 * VIZ_MARGIN)));
            component.zoomTo(scale, true);
        });
        toolbar.add(fit);

        JLabel hint = new JLabel("Ctrl + mouse wheel to zoom \u00b7 wheel/Shift+wheel to scroll \u00b7 click a student to open them");
        hint.setFont(hint.getFont().deriveFont(java.awt.Font.ITALIC, 11f));
        toolbar.add(hint);
        return toolbar;
    }

    /**
     * Builds the mxGraph model for one student's relationship view: subject
     * centered, peers on two rings (close relations inside, casual/negative
     * outside), sorted so same-colored categories group together. Extracted
     * from {@link #studentVisualizer(Student)} so it can be rendered
     * off-screen (tests, image export) without opening a window.
     *
     * @param student       the subject of the graph
     * @param cellToStudent out-parameter mapping created vertex cells to the
     *                      peer they represent (for click navigation)
     * @return the populated graph model
     */
    mxGraph buildStudentPeerGraph(Student student, HashMap<Object, Student> cellToStudent) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();

        // Collect each connected peer once (union of both edge directions)
        java.util.LinkedHashSet<Student> peerSet = new java.util.LinkedHashSet<>();
        for (DefaultWeightedEdge edge : socialGraph.edgesOf(student)) {
            Student source = socialGraph.getEdgeSource(edge);
            Student target = socialGraph.getEdgeTarget(edge);
            peerSet.add(source.equals(student) ? target : source);
        }

        // Classify and sort so colors group together around each ring
        List<Student> inner = new ArrayList<>();
        List<Student> outer = new ArrayList<>();
        HashMap<Student, PeerCategory> categories = new HashMap<>();
        for (Student peer : peerSet) {
            PeerCategory category = classifyPeer(student, peer);
            categories.put(peer, category);
            (category.isInnerRing() ? inner : outer).add(peer);
        }
        java.util.Comparator<Student> byCategoryThenScore = java.util.Comparator
                .comparing((Student p) -> categories.get(p).ordinal())
                .thenComparing(p -> -getSocialScore(student, p));
        inner.sort(byCategoryThenScore);
        outer.sort(byCategoryThenScore);

        mxGraph peerGraph = new mxGraph();
        peerGraph.setHtmlLabels(true);
        peerGraph.setCellsEditable(false);
        peerGraph.setCellsResizable(false);
        peerGraph.setCellsDisconnectable(false);
        peerGraph.setAllowDanglingEdges(false);
        peerGraph.setEdgeLabelsMovable(false);
        Object parent = peerGraph.getDefaultParent();

        // Size the rings first so the whole graph can be laid out in
        // positive coordinates (negative coordinates get clipped past the
        // top-left corner of the scroll pane and become unreachable)
        double innerRadius = Math.max(VIZ_MIN_INNER_RADIUS,
                inner.size() * VIZ_ARC_PER_NODE / (2 * Math.PI));
        double outerRadius = outer.isEmpty() ? innerRadius
                : Math.max(innerRadius + VIZ_RING_GAP,
                        outer.size() * VIZ_ARC_PER_NODE / (2 * Math.PI));
        double centerX = VIZ_MARGIN + VIZ_NODE_WIDTH / 2.0 + outerRadius;
        double centerY = VIZ_MARGIN + VIZ_NODE_HEIGHT / 2.0 + outerRadius;

        peerGraph.getModel().beginUpdate();
        try {
            String subjectStyle = "rounded=1;whiteSpace=wrap;fontSize=15;fontStyle=1;"
                    + "fillColor=" + VIZ_SUBJECT_FILL + ";strokeColor=#5D4037;strokeWidth=2";
            Object subjectCell = peerGraph.insertVertex(parent, null,
                    "<b>" + escapeHtml(studentName) + "</b>",
                    centerX - VIZ_NODE_WIDTH / 2.0, centerY - VIZ_NODE_HEIGHT / 2.0,
                    VIZ_NODE_WIDTH, VIZ_NODE_HEIGHT, subjectStyle);
            cellToStudent.put(subjectCell, student);

            placeRing(peerGraph, parent, subjectCell, cellToStudent,
                    student, inner, categories, innerRadius, centerX, centerY);
            placeRing(peerGraph, parent, subjectCell, cellToStudent,
                    student, outer, categories, outerRadius, centerX, centerY);
        } finally {
            peerGraph.getModel().endUpdate();
        }
        return peerGraph;
    }

    /**
     * Places one ring of peers around the subject: evenly spaced vertices
     * color-coded by category and labeled with both directed scores, each
     * connected to the subject by a thin edge tinted by the subject's
     * outgoing feeling (green positive, red negative, gray neutral).
     */
    private void placeRing(mxGraph peerGraph, Object parent, Object subjectCell,
            HashMap<Object, Student> cellToStudent, Student student,
            List<Student> peers, HashMap<Student, PeerCategory> categories, double radius,
            double centerX, double centerY) {
        for (int i = 0; i < peers.size(); i++) {
            Student peer = peers.get(i);
            PeerCategory category = categories.get(peer);
            double angle = 2 * Math.PI * i / peers.size() - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle) - VIZ_NODE_WIDTH / 2.0;
            double y = centerY + radius * Math.sin(angle) - VIZ_NODE_HEIGHT / 2.0;

            double outgoing = getSocialScore(student, peer);
            double incoming = getSocialScore(peer, student);
            String vertexStyle = "rounded=1;whiteSpace=wrap;fontSize=12;"
                    + "fillColor=" + category.fillColor + ";strokeColor=#607D8B";
            Object cell = peerGraph.insertVertex(parent, null,
                    buildPeerLabel(student, peer, outgoing, incoming, category),
                    x, y, VIZ_NODE_WIDTH, VIZ_NODE_HEIGHT, vertexStyle);
            cellToStudent.put(cell, peer);

            double signal = outgoing != 0 ? outgoing : incoming;
            String strokeColor = signal > 0 ? "#66BB6A" : (signal < 0 ? "#EF5350" : "#B0BEC5");
            double strokeWidth = 1 + Math.min(2.5, Math.abs(signal) / 40.0);
            String edgeStyle = "endArrow=none;strokeColor=" + strokeColor
                    + ";strokeWidth=" + strokeWidth + ";opacity=55";
            peerGraph.insertEdge(parent, null, "", subjectCell, cell, edgeStyle);
        }
    }

    /**
     * Builds the HTML vertex label for a peer: name, both directed scores
     * (&rarr; subject's feeling toward the peer, &larr; the peer's feeling
     * back), and an optional context tag (sibling / best friend / romance).
     */
    private String buildPeerLabel(Student student, Student peer,
            double outgoing, double incoming, PeerCategory category) {
        StringBuilder sb = new StringBuilder("<b>")
                .append(escapeHtml(peer.studentName.getFullName()))
                .append("</b><br/>&rarr; ").append(Math.round(outgoing))
                .append(" &nbsp; &larr; ").append(Math.round(incoming));
        String tag = peerTag(student, peer, category);
        if (tag != null) {
            sb.append("<br/><i>").append(tag).append("</i>");
        }
        return sb.toString();
    }

    /** Short context tag shown under the scores, or null for plain tiers. */
    private String peerTag(Student student, Student peer, PeerCategory category) {
        if (category == PeerCategory.ROMANCE) {
            RomanticStatus outgoing = getRomanticStatus(student, peer);
            RomanticStatus incoming = getRomanticStatus(peer, student);
            if (outgoing == incoming) {
                return switch (outgoing) {
                    case CRUSH -> "Mutual crush";
                    case FLING -> "FWB";
                    case STEADY -> "Official";
                    default -> null;
                };
            }
            if (incoming == RomanticStatus.NONE) {
                return outgoing == RomanticStatus.CRUSH ? "Their crush"
                        : "Sees it as " + outgoing.label();
            }
            if (outgoing == RomanticStatus.NONE) {
                return incoming == RomanticStatus.CRUSH ? "Crushing on them"
                        : "Peer sees " + incoming.label();
            }
            return "Sees " + outgoing.label() + " / peer sees " + incoming.label();
        }
        if (category == PeerCategory.SIBLING) {
            return "Sibling";
        }
        if (category == PeerCategory.BEST_FRIEND) {
            return "Best friend";
        }
        return null;
    }

    /**
     * Classifies a peer relative to the subject. Any romantic perception in
     * either direction wins, then sibling ties, then the tier of the
     * subject's outgoing score.
     */
    private PeerCategory classifyPeer(Student student, Student peer) {
        if (getRomanticStatus(student, peer) != RomanticStatus.NONE
                || getRomanticStatus(peer, student) != RomanticStatus.NONE) {
            return PeerCategory.ROMANCE;
        }
        if (student.studentStatistics.getSiblingsInSchool().contains(peer)) {
            return PeerCategory.SIBLING;
        }
        return switch (classifyScore(getSocialScore(student, peer))) {
            case BEST_FRIEND -> PeerCategory.BEST_FRIEND;
            case FRIEND -> PeerCategory.FRIEND;
            case ACQUAINTANCE -> PeerCategory.ACQUAINTANCE;
            case NEUTRAL -> PeerCategory.NEUTRAL;
            case DISLIKE -> PeerCategory.DISLIKE;
            case ENEMY -> PeerCategory.ENEMY;
        };
    }

    /** Legend strip explaining the vertex colors and score arrows. */
    private JPanel buildLegendPanel() {
        JPanel legend = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 4));
        legend.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.LIGHT_GRAY));
        for (PeerCategory category : PeerCategory.values()) {
            legend.add(legendEntry(category.label, java.awt.Color.decode(category.fillColor)));
        }
        JLabel arrows = new JLabel("\u2192 their feeling toward peer   \u2190 peer's feeling back");
        arrows.setFont(arrows.getFont().deriveFont(java.awt.Font.ITALIC, 11f));
        legend.add(arrows);
        return legend;
    }

    private JPanel legendEntry(String text, java.awt.Color color) {
        JPanel entry = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new java.awt.Dimension(14, 14));
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));
        entry.add(swatch);
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(11f));
        entry.add(label);
        return entry;
    }

    private static String escapeHtml(String text) {
        return text == null ? "" : text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
