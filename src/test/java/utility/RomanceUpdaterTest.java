package utility;

import entity.OrientationDisclosure;
import entity.RomanticStatus;
import entity.SexualOrientation;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RomanceUpdater")
class RomanceUpdaterTest {

    @Test
    @DisplayName("End-of-day maintenance dissolves statuses whose scores decayed below entry thresholds")
    void testEndOfDayMaintenanceDissolvesStaleStatuses() {
        GameRandom.reset();
        GameRandom.initialize(777L);
        HashMap<Integer, Student> students = new HashMap<>();
        Student fadedHolder = namedStudent(0, "Female");
        Student fadedTarget = namedStudent(1, "Male");
        Student freshHolder = namedStudent(2, "Female");
        Student freshTarget = namedStudent(3, "Male");
        Student driftA = namedStudent(4, "Female");
        Student driftB = namedStudent(5, "Male");
        Student starveA = namedStudent(6, "Female");
        Student starveB = namedStudent(7, "Male");
        for (int i = 0; i < 8; i++) {
            students.put(i, List.of(fadedHolder, fadedTarget, freshHolder, freshTarget,
                    driftA, driftB, starveA, starveB).get(i));
        }
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        RomanceUpdater.drainDaysEvents();

        // Crush whose underlying warmth decayed below acquaintance level
        connector.modifySocialScore(fadedHolder, fadedTarget, 3);
        connector.setRomanticStatus(fadedHolder, fadedTarget, RomanticStatus.CRUSH);

        // Healthy crush stays
        connector.modifySocialScore(freshHolder, freshTarget, 40);
        connector.setRomanticStatus(freshHolder, freshTarget, RomanticStatus.CRUSH);

        // Hookup where one side went cold
        connector.modifySocialScore(driftA, driftB, 30);
        connector.modifySocialScore(driftB, driftA, 2);
        connector.setRomanticStatus(driftA, driftB, RomanticStatus.FLING);
        connector.setRomanticStatus(driftB, driftA, RomanticStatus.FLING);

        // Official couple starved out on both sides
        connector.modifySocialScore(starveA, starveB, 4);
        connector.modifySocialScore(starveB, starveA, 3);
        connector.setRomanticStatus(starveA, starveB, RomanticStatus.STEADY);
        connector.setRomanticStatus(starveB, starveA, RomanticStatus.STEADY);

        assertTrue(RomanceUpdater.endOfDayMaintenance(students, connector));

        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(fadedHolder, fadedTarget),
                "Cold crush should fade");
        assertEquals(RomanticStatus.CRUSH, connector.getRomanticStatus(freshHolder, freshTarget),
                "Warm crush should persist");
        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(driftA, driftB),
                "Hookup with a cold side should dissolve");
        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(driftB, driftA));
        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(starveA, starveB),
                "Starved official couple should break up");
        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(starveB, starveA));
        assertTrue(connector.getSocialScore(starveA, starveB) < 0,
                "Breakup should apply a score penalty");

        List<String> events = RomanceUpdater.drainDaysEvents();
        assertFalse(events.isEmpty(), "Maintenance changes should be recorded as events");
        assertTrue(RomanceUpdater.drainDaysEvents().isEmpty(),
                "Draining should clear the event queue");
    }

    @Test
    @DisplayName("Period pulses eventually resolve romances into symmetric end states")
    void testPulsesEventuallyResolveRomances() {
        GameRandom.reset();
        GameRandom.initialize(24680L);
        HashMap<Integer, Student> students = new HashMap<>();
        Student crushHolder = namedStudent(0, "Male");
        Student crushTarget = namedStudent(1, "Female");
        Student flingA = namedStudent(2, "Female");
        Student flingB = namedStudent(3, "Male");
        Student believer = namedStudent(4, "Female");
        Student partner = namedStudent(5, "Male");
        for (int i = 0; i < 6; i++) {
            students.put(i, List.of(crushHolder, crushTarget, flingA, flingB,
                    believer, partner).get(i));
        }
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        RomanceUpdater.drainDaysEvents();

        // Reciprocated warmth: acting on this crush will succeed
        connector.modifySocialScore(crushHolder, crushTarget, 50);
        connector.modifySocialScore(crushTarget, crushHolder, 45);
        connector.setRomanticStatus(crushHolder, crushTarget, RomanticStatus.CRUSH);

        // Warm mutual hookup: can become official or fizzle
        connector.modifySocialScore(flingA, flingB, 60);
        connector.modifySocialScore(flingB, flingA, 60);
        connector.setRomanticStatus(flingA, flingB, RomanticStatus.FLING);
        connector.setRomanticStatus(flingB, flingA, RomanticStatus.FLING);

        // Asymmetric serious pair: converges to official or splits
        connector.modifySocialScore(believer, partner, 55);
        connector.modifySocialScore(partner, believer, 50);
        connector.setRomanticStatus(believer, partner, RomanticStatus.STEADY);
        connector.setRomanticStatus(partner, believer, RomanticStatus.FLING);

        for (int i = 0; i < 600; i++) {
            RomanceUpdater.periodPulse(students, connector);
        }

        // The crush must have been acted on by now; acceptance makes the pair
        // symmetric and every later transition preserves symmetry
        assertNotEquals(RomanticStatus.CRUSH,
                connector.getRomanticStatus(crushHolder, crushTarget),
                "600 pulses at the act chance should always resolve a crush");
        assertEquals(connector.getRomanticStatus(crushHolder, crushTarget),
                connector.getRomanticStatus(crushTarget, crushHolder));

        assertEquals(connector.getRomanticStatus(flingA, flingB),
                connector.getRomanticStatus(flingB, flingA),
                "Mutual hookups stay symmetric (official, ongoing, or over)");

        assertEquals(connector.getRomanticStatus(believer, partner),
                connector.getRomanticStatus(partner, believer),
                "Asymmetric serious pairs must converge or break up");

        assertFalse(RomanceUpdater.drainDaysEvents().isEmpty(),
                "Transitions should have produced events");
    }

    @Test
    @DisplayName("Hidden same-gender crushes held by closeted students are never acted on")
    void testSecretCrushesNeverActedOn() {
        GameRandom.reset();
        GameRandom.initialize(999L);
        HashMap<Integer, Student> students = new HashMap<>();
        Student closeted = namedStudent(0, "Male");
        closeted.studentStatistics.setSexualOrientation(SexualOrientation.GAY);
        closeted.studentStatistics.setOrientationDisclosure(OrientationDisclosure.CLOSETED);
        Student target = namedStudent(1, "Male");
        students.put(0, closeted);
        students.put(1, target);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        RomanceUpdater.drainDaysEvents();

        connector.modifySocialScore(closeted, target, 60);
        connector.modifySocialScore(target, closeted, 60);
        connector.setRomanticStatus(closeted, target, RomanticStatus.CRUSH);

        for (int i = 0; i < 600; i++) {
            RomanceUpdater.periodPulse(students, connector);
        }

        assertEquals(RomanticStatus.CRUSH, connector.getRomanticStatus(closeted, target),
                "A hidden crush is never acted on and never rejected away");
        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(target, closeted),
                "The target never learns about a hidden crush");
    }

    private Student namedStudent(int id, String gender) {
        Student student = new Student();
        student.studentName.setFirstName("Student" + id);
        student.studentName.setLastName("Updater");
        student.studentStatistics.setGradeLevel("Junior");
        student.studentStatistics.setGender(gender);
        student.studentStatistics.setCharisma(80);
        student.studentStatistics.setEmpathy(75);
        student.studentStatistics.setLuck(70);
        student.studentStatistics.setMainClique("NoLife");
        student.studentStatistics.setNeighborhoodName("Oak Hills");
        return student;
    }
}
