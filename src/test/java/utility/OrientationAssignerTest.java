package utility;

import entity.OrientationDisclosure;
import entity.SexualOrientation;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OrientationAssigner")
class OrientationAssignerTest {

    @Test
    @DisplayName("Should assign reproducible orientations for the same seed")
    void testOrientationAssignmentIsReproducible() {
        String first = fingerprint(424242L);
        String second = fingerprint(424242L);
        assertEquals(first, second, "Same seed should reproduce orientation assignments");
    }

    @Test
    @DisplayName("Should hit the ~6% non-heterosexual cohort target with a closeted majority")
    void testCohortTargetsMatchConfiguredRates() {
        GameRandom.reset();
        GameRandom.initialize(777001L);
        HashMap<Integer, Student> students = createStudents(200, false);
        OrientationAssigner.assignOrientations(students);

        int open = 0;
        int closeted = 0;
        int straight = 0;
        for (Student student : students.values()) {
            SexualOrientation orientation = student.studentStatistics.getSexualOrientation();
            OrientationDisclosure disclosure = student.studentStatistics.getOrientationDisclosure();
            if (!orientation.isNonHeterosexual()) {
                straight++;
                assertEquals(OrientationDisclosure.OPEN, disclosure);
            } else if (disclosure == OrientationDisclosure.OPEN) {
                open++;
            } else {
                closeted++;
            }
        }

        // Cohort size is fixed: round(200 * 0.01) + round(200 * 0.05) = 12.
        // The open/closeted split is now rolled per student from clique
        // category (all NoLife here -> neutral, 80% closeted), so only the
        // total and the closeted majority are guaranteed.
        assertEquals(188, straight);
        assertEquals(12, open + closeted);
        assertTrue(closeted > open,
                "Neutral-clique cohorts should stay predominantly closeted");
    }

    @Test
    @DisplayName("Should concentrate non-heterosexual students in out-group cliques")
    void testOutGroupConcentrationWithoutChangingTotals() {
        GameRandom.reset();
        GameRandom.initialize(909090L);
        HashMap<Integer, Student> students = createStudents(200, true);
        OrientationAssigner.assignOrientations(students);

        int nonHetero = 0;
        int outGroupNonHetero = 0;
        for (Student student : students.values()) {
            if (!student.studentStatistics.getSexualOrientation().isNonHeterosexual()) {
                continue;
            }
            nonHetero++;
            if ("out-group".equals(CliqueLoader.getGroupCategory(
                    student.studentStatistics.getMainClique()))) {
                outGroupNonHetero++;
            }
        }

        assertEquals(12, nonHetero, "School-wide non-heterosexual total must stay at cohort size");
        // With equal in/out populations and 3x out-group weight, out-groups should
        // hold a clear majority of the cohort (expected ~9 of 12).
        assertTrue(outGroupNonHetero > nonHetero / 2,
                "Out-group students should hold more than half of the non-heterosexual cohort");
    }

    @Test
    @DisplayName("Should keep in-group sexual minorities closeted far more often than out-group ones")
    void testDisclosureConditionedOnCliqueCategory() {
        // Aggregate over several seeds so per-seed rolls can't dominate.
        int inGroupOpen = 0;
        int inGroupTotal = 0;
        int outGroupOpen = 0;
        int outGroupTotal = 0;
        for (long seed = 5000L; seed < 5010L; seed++) {
            GameRandom.reset();
            GameRandom.initialize(seed);
            HashMap<Integer, Student> students = createStudents(200, true);
            OrientationAssigner.assignOrientations(students);
            for (Student student : students.values()) {
                if (!student.studentStatistics.getSexualOrientation().isNonHeterosexual()) {
                    continue;
                }
                boolean open = student.studentStatistics
                        .getOrientationDisclosure() == OrientationDisclosure.OPEN;
                String category = CliqueLoader.getGroupCategory(
                        student.studentStatistics.getMainClique());
                if ("in-group".equals(category)) {
                    inGroupTotal++;
                    if (open) {
                        inGroupOpen++;
                    }
                } else if ("out-group".equals(category)) {
                    outGroupTotal++;
                    if (open) {
                        outGroupOpen++;
                    }
                }
            }
        }

        assertTrue(inGroupTotal > 0 && outGroupTotal > 0,
                "Both clique categories should receive cohort members across seeds");
        double inGroupOpenRate = (double) inGroupOpen / inGroupTotal;
        double outGroupOpenRate = (double) outGroupOpen / outGroupTotal;
        assertTrue(outGroupOpenRate > inGroupOpenRate,
                "Out-group minorities should be open more often (in-group: " + inGroupOpenRate
                        + ", out-group: " + outGroupOpenRate + ")");
        assertTrue(inGroupOpenRate < 0.25,
                "Conservative in-groups should keep nearly all minorities closeted; open rate was "
                        + inGroupOpenRate);
    }

    @Test
    @DisplayName("Should default unset orientation fields to straight/open")
    void testLegacyNullDefaults() {
        Student student = new Student();
        assertEquals(SexualOrientation.STRAIGHT, student.studentStatistics.getSexualOrientation());
        assertEquals(OrientationDisclosure.OPEN, student.studentStatistics.getOrientationDisclosure());
    }

    private String fingerprint(long seed) {
        GameRandom.reset();
        GameRandom.initialize(seed);
        HashMap<Integer, Student> students = createStudents(40, true);
        OrientationAssigner.assignOrientations(students);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            Student student = students.get(i);
            sb.append(i).append(':')
                    .append(student.studentStatistics.getSexualOrientation()).append('/')
                    .append(student.studentStatistics.getOrientationDisclosure()).append('|');
        }
        return sb.toString();
    }

    private HashMap<Integer, Student> createStudents(int count, boolean splitCliques) {
        HashMap<Integer, Student> students = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Student student = new Student();
            student.studentName.setFirstName("Student" + i);
            student.studentName.setLastName("Orient");
            student.studentStatistics.setGradeLevel(i % 2 == 0 ? "Freshman" : "Sophomore");
            student.studentStatistics.setGender(i % 2 == 0 ? "Female" : "Male");
            if (splitCliques) {
                student.studentStatistics.setMainClique(i < count / 2 ? "Emo" : "Jock");
            } else {
                student.studentStatistics.setMainClique("NoLife");
            }
            students.put(i, student);
        }
        return students;
    }
}
