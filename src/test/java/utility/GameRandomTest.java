package utility;

import entity.StandardSchool;
import entity.Student;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import save.SocialLinkSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("GameRandom")
class GameRandomTest {

    @Test
    @DisplayName("Should shuffle reproducibly for the same seed")
    void testShuffleUsesCurrentSeed() {
        List<Integer> first = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        List<Integer> second = new ArrayList<>(first);
        List<Integer> differentSeed = new ArrayList<>(first);

        GameRandom.reset();
        GameRandom.initialize(12345L);
        GameRandom.shuffle(first);

        GameRandom.reset();
        GameRandom.initialize(12345L);
        GameRandom.shuffle(second);

        GameRandom.reset();
        GameRandom.initialize(54321L);
        GameRandom.shuffle(differentSeed);

        assertEquals(first, second, "Same seed should reproduce the same shuffle order");
        assertNotEquals(first, differentSeed, "Different seeds should produce a different shuffle order");
    }

    @Test
    @DisplayName("Should reproduce mixed random sequences for the same seed")
    void testSeedReproducesMixedRandomSequence() {
        List<String> first = captureMixedRandomSequence(987654321L);
        List<String> second = captureMixedRandomSequence(987654321L);
        List<String> differentSeed = captureMixedRandomSequence(123456789L);

        assertEquals(first, second, "Same seed should reproduce every GameRandom API call in order");
        assertNotEquals(first, differentSeed, "Different seeds should produce a different mixed sequence");
    }

    @Test
    @DisplayName("Randomizer should delegate to the seeded GameRandom stream")
    void testRandomizerUsesGameRandomSeed() {
        GameRandom.reset();
        GameRandom.initialize(20260511L);
        int fromRandomizer = Randomizer.setRandom(10, 20);

        GameRandom.reset();
        GameRandom.initialize(20260511L);
        int fromGameRandom = GameRandom.nextInt(10, 20);

        assertEquals(fromGameRandom, fromRandomizer,
                "Randomizer should consume the same seeded stream as GameRandom");
    }

    @Test
    @DisplayName("Should generate reproducible social links for the same seed")
    void testSocialLinksUseCurrentSeed() {
        String first = buildSocialLinkFingerprint(8675309L);
        String second = buildSocialLinkFingerprint(8675309L);

        assertEquals(first, second, "Same seed should reproduce social-link relationships and catalysts");
    }

    @Test
    @DisplayName("Should restore random stream position from captured state")
    void testRandomStateRestoresStreamPosition() {
        GameRandom.reset();
        GameRandom.initialize(1234L);
        GameRandom.nextInt(100);
        GameRandom.RandomState state = GameRandom.captureState();
        int expected = GameRandom.nextInt(100);

        GameRandom.nextInt(100);
        GameRandom.restoreState(state);

        assertEquals(expected, GameRandom.nextInt(100),
                "Restored state should continue from the captured stream position");
    }

    @Test
    @DisplayName("Should restore social links from a snapshot")
    void testSocialLinkSnapshotRestoresRelationships() {
        GameRandom.reset();
        GameRandom.initialize(2468L);
        HashMap<Integer, Student> students = createStudentMap();
        StandardSchool school = new StandardSchool();
        school.setStudentGradeClass(students, null);
        SocialLinkConnector connector = new SocialLinkConnector(students, school);
        SocialLinkSnapshot snapshot = connector.createSnapshot();
        String expected = socialLinkFingerprint(connector);

        SocialLinkConnector restored = new SocialLinkConnector();
        restored.restoreFromSnapshot(students, snapshot);

        assertEquals(expected, socialLinkFingerprint(restored),
                "Social-link snapshot should restore graph weights and catalysts");
    }

    private String buildSocialLinkFingerprint(long seed) {
        GameRandom.reset();
        GameRandom.initialize(seed);

        HashMap<Integer, Student> students = createStudentMap();
        StandardSchool school = new StandardSchool();
        school.setStudentGradeClass(students, null);

        SocialLinkConnector connector = new SocialLinkConnector(students, school);
        return socialLinkFingerprint(connector);
    }

    private List<String> captureMixedRandomSequence(long seed) {
        GameRandom.reset();
        GameRandom.initialize(seed);
        List<Integer> shuffled = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        GameRandom.shuffle(shuffled);

        List<String> sequence = new ArrayList<>();
        sequence.add(String.valueOf(GameRandom.nextInt(100)));
        sequence.add(String.valueOf(GameRandom.nextInt(50, 75)));
        sequence.add(String.valueOf(GameRandom.nextLong(1000L, 5000L)));
        sequence.add(String.format(Locale.ROOT, "%.12f", GameRandom.nextDouble()));
        sequence.add(String.format(Locale.ROOT, "%.12f", GameRandom.nextDouble(25.0)));
        sequence.add(String.format(Locale.ROOT, "%.12f", GameRandom.nextGaussian()));
        sequence.add(String.valueOf(GameRandom.nextBoolean()));
        sequence.add(shuffled.toString());
        return sequence;
    }

    private String socialLinkFingerprint(SocialLinkConnector connector) {
        List<String> edgeFingerprints = new ArrayList<>();
        for (DefaultWeightedEdge edge : connector.socialGraph.edgeSet()) {
            Student source = connector.socialGraph.getEdgeSource(edge);
            Student target = connector.socialGraph.getEdgeTarget(edge);
            edgeFingerprints.add(source.studentName.getFullName()
                    + "->"
                    + target.studentName.getFullName()
                    + ":"
                    + String.format(Locale.ROOT, "%.4f", connector.socialGraph.getEdgeWeight(edge)));
        }
        edgeFingerprints.sort(String::compareTo);

        return String.join("|", edgeFingerprints)
                + "::"
                + new TreeMap<>(connector.getAllCatalysts());
    }

    private HashMap<Integer, Student> createStudentMap() {
        HashMap<Integer, Student> students = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            Student student = new Student();
            student.studentName.setFirstName("Student" + i);
            student.studentName.setLastName("Seed");
            student.studentStatistics.setGradeLevel(i < 4 ? "Freshman" : "Sophomore");
            student.studentStatistics.setGender(i % 2 == 0 ? "Female" : "Male");
            student.studentStatistics.setCharisma(90);
            student.studentStatistics.setEmpathy(85);
            student.studentStatistics.setLuck(80);
            student.studentStatistics.setMainClique(i % 2 == 0 ? "Jocks" : "Nerds");
            students.put(i, student);
        }
        return students;
    }
}
