package utility;

import entity.StandardSchool;
import entity.Student;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Should generate reproducible social links for the same seed")
    void testSocialLinksUseCurrentSeed() {
        String first = buildSocialLinkFingerprint(8675309L);
        String second = buildSocialLinkFingerprint(8675309L);

        assertEquals(first, second, "Same seed should reproduce social-link relationships and catalysts");
    }

    private String buildSocialLinkFingerprint(long seed) {
        GameRandom.reset();
        GameRandom.initialize(seed);

        HashMap<Integer, Student> students = createStudentMap();
        StandardSchool school = new StandardSchool();
        school.setStudentGradeClass(students, null);

        SocialLinkConnector connector = new SocialLinkConnector(students, school);
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
