package utility;

import entity.RomanticStatus;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import save.SocialLinkSnapshot;

import java.util.HashMap;
import java.util.List;

import static constants.SimConstants.ROMANCE_JEALOUSY_DISCOVERY_STING;
import static constants.SimConstants.ROMANCE_JEALOUSY_DRIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jealousy secondary effects: the couple-knowledge store on
 * the social link connector, its snapshot persistence, and the
 * perception-gated discovery / envy drip passes in {@link RomanceUpdater}.
 */
@DisplayName("Jealousy: couple knowledge and rival effects")
class JealousyTest {

    @Test
    @DisplayName("Couple knowledge is stored per observer over an unordered couple")
    void testKnowledgeStoreBasics() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student observer = namedStudent(0, "Male");
        Student a = namedStudent(1, "Female");
        Student b = namedStudent(2, "Male");
        students.put(0, observer);
        students.put(1, a);
        students.put(2, b);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);

        assertFalse(connector.knowsAboutCouple(observer, a, b));

        connector.recordCoupleKnowledge(observer, a, b);
        assertTrue(connector.knowsAboutCouple(observer, a, b));
        assertTrue(connector.knowsAboutCouple(observer, b, a),
                "The couple pair is unordered");
        assertFalse(connector.knowsAboutCouple(a, observer, b),
                "Knowledge is per observer");

        connector.clearCoupleKnowledge(b, a);
        assertFalse(connector.knowsAboutCouple(observer, a, b),
                "Purging the couple removes every observer's record");
    }

    @Test
    @DisplayName("Only mutual fling/steady pairs read as observable couples")
    void testObservableCouple() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student a = namedStudent(0, "Female");
        Student b = namedStudent(1, "Male");
        students.put(0, a);
        students.put(1, b);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);

        assertFalse(connector.isObservableCouple(a, b), "No romance at all");

        connector.setRomanticStatus(a, b, RomanticStatus.FLING);
        assertFalse(connector.isObservableCouple(a, b), "One-sided flings are invisible");

        connector.setRomanticStatus(b, a, RomanticStatus.FLING);
        assertTrue(connector.isObservableCouple(a, b), "Mutual hookups are observable");

        connector.setRomanticStatus(a, b, RomanticStatus.STEADY);
        assertTrue(connector.isObservableCouple(a, b),
                "Asymmetric steady/fling pairs still act couple-y in public");

        connector.setRomanticStatus(a, b, RomanticStatus.CRUSH);
        connector.setRomanticStatus(b, a, RomanticStatus.CRUSH);
        assertFalse(connector.isObservableCouple(a, b), "Mutual crushes are not a couple yet");
    }

    @Test
    @DisplayName("getKnownPartnersOf lists only known, still-observable partners")
    void testGetKnownPartnersOf() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student observer = namedStudent(0, "Male");
        Student crush = namedStudent(1, "Female");
        Student partner = namedStudent(2, "Male");
        Student secretPartner = namedStudent(3, "Male");
        students.put(0, observer);
        students.put(1, crush);
        students.put(2, partner);
        students.put(3, secretPartner);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);

        // Romance sits on top of social edges, so the crush needs outgoing links
        connector.modifySocialScore(crush, partner, 40);
        connector.modifySocialScore(crush, secretPartner, 40);
        connector.setRomanticStatus(crush, partner, RomanticStatus.STEADY);
        connector.setRomanticStatus(partner, crush, RomanticStatus.STEADY);
        connector.setRomanticStatus(crush, secretPartner, RomanticStatus.FLING);
        connector.setRomanticStatus(secretPartner, crush, RomanticStatus.FLING);

        assertTrue(connector.getKnownPartnersOf(observer, crush).isEmpty(),
                "Nothing is known yet");

        connector.recordCoupleKnowledge(observer, crush, partner);
        List<Student> known = connector.getKnownPartnersOf(observer, crush);
        assertEquals(1, known.size(), "Only the discovered couple counts");
        assertTrue(known.contains(partner));
        assertFalse(known.contains(secretPartner), "The undiscovered fling stays unknown");

        // Once the couple dissolves, lingering knowledge no longer yields a rival
        connector.setRomanticStatus(crush, partner, RomanticStatus.NONE);
        connector.setRomanticStatus(partner, crush, RomanticStatus.NONE);
        assertTrue(connector.getKnownPartnersOf(observer, crush).isEmpty(),
                "A dissolved couple is no longer observable");
    }

    @Test
    @DisplayName("Couple knowledge survives a snapshot round-trip")
    void testSnapshotRoundTrip() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student observer = namedStudent(0, "Female");
        Student a = namedStudent(1, "Male");
        Student b = namedStudent(2, "Female");
        students.put(0, observer);
        students.put(1, a);
        students.put(2, b);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        connector.recordCoupleKnowledge(observer, a, b);

        SocialLinkSnapshot snapshot = connector.createSnapshot();
        SocialLinkConnector restored = new SocialLinkConnector();
        restored.restoreFromSnapshot(students, snapshot);

        assertTrue(restored.knowsAboutCouple(observer, a, b),
                "Knowledge records must persist through save/load");
        assertEquals(1, restored.getAllCoupleKnowledge().size());
    }

    @Test
    @DisplayName("A perceptive crusher notices the couple and takes a jealousy sting")
    void testDiscoverySting() {
        GameRandom.reset();
        GameRandom.initialize(1234L);
        HashMap<Integer, Student> students = new HashMap<>();
        Student crusher = namedStudent(0, "Male");
        crusher.studentStatistics.setPerception(100);
        Student crush = namedStudent(1, "Female");
        Student rival = namedStudent(2, "Male");
        students.put(0, crusher);
        students.put(1, crush);
        students.put(2, rival);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        RomanceUpdater.drainDaysEvents();

        connector.modifySocialScore(crusher, crush, 50);
        connector.setRomanticStatus(crusher, crush, RomanticStatus.CRUSH);

        connector.modifySocialScore(crush, rival, 60);
        connector.modifySocialScore(rival, crush, 60);
        connector.setRomanticStatus(crush, rival, RomanticStatus.STEADY);
        connector.setRomanticStatus(rival, crush, RomanticStatus.STEADY);

        boolean discovered = false;
        for (int i = 0; i < 400 && !discovered; i++) {
            RomanceUpdater.periodPulse(students, connector);
            discovered = connector.knowsAboutCouple(crusher, crush, rival);
        }

        assertTrue(discovered, "A high-perception crusher should notice within a few pulses");
        assertTrue(connector.getSocialScore(crusher, rival)
                        <= -ROMANCE_JEALOUSY_DISCOVERY_STING + 1e-9,
                "Discovery should apply the jealousy sting toward the rival");
        assertTrue(RomanceUpdater.drainDaysEvents().stream()
                        .anyMatch(event -> event.contains("noticed")),
                "Discovery should be recorded as an event");
    }

    @Test
    @DisplayName("Known rivals accrue a per-pulse jealousy drip while the crush lasts")
    void testJealousyDripStopsWithoutCrush() {
        GameRandom.reset();
        GameRandom.initialize(555L);
        HashMap<Integer, Student> students = new HashMap<>();
        Student crusher = namedStudent(0, "Male");
        Student crush = namedStudent(1, "Female");
        Student rival = namedStudent(2, "Male");
        students.put(0, crusher);
        students.put(1, crush);
        students.put(2, rival);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        RomanceUpdater.drainDaysEvents();

        connector.modifySocialScore(crusher, crush, 50);
        connector.setRomanticStatus(crusher, crush, RomanticStatus.CRUSH);
        connector.modifySocialScore(crusher, rival, 10);

        connector.modifySocialScore(crush, rival, 60);
        connector.modifySocialScore(rival, crush, 60);
        connector.setRomanticStatus(crush, rival, RomanticStatus.STEADY);
        connector.setRomanticStatus(rival, crush, RomanticStatus.STEADY);

        connector.recordCoupleKnowledge(crusher, crush, rival);

        // Crush warmth 50 -> drip = ROMANCE_JEALOUSY_DRIP * (0.5 + 50/100)
        double expectedDrip = ROMANCE_JEALOUSY_DRIP * (0.5 + 50.0 / 100.0);
        RomanceUpdater.periodPulse(students, connector);
        assertEquals(10.0 - expectedDrip, connector.getSocialScore(crusher, rival), 1e-9,
                "Each pulse should drip resentment toward the known rival");

        // Once the crush is gone, the resentment stops accruing
        connector.setRomanticStatus(crusher, crush, RomanticStatus.NONE);
        double frozen = connector.getSocialScore(crusher, rival);
        for (int i = 0; i < 3; i++) {
            RomanceUpdater.periodPulse(students, connector);
        }
        assertEquals(frozen, connector.getSocialScore(crusher, rival), 1e-9,
                "No crush means no jealousy drip, even with lingering knowledge");
    }

    @Test
    @DisplayName("Dissolving a couple purges everyone's knowledge of it")
    void testKnowledgePurgedOnDissolve() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student observer = namedStudent(0, "Male");
        Student a = namedStudent(1, "Female");
        Student b = namedStudent(2, "Male");
        students.put(0, observer);
        students.put(1, a);
        students.put(2, b);
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        RomanceUpdater.drainDaysEvents();

        // Starved official couple: end-of-day maintenance will break them up
        connector.modifySocialScore(a, b, 3);
        connector.modifySocialScore(b, a, 4);
        connector.setRomanticStatus(a, b, RomanticStatus.STEADY);
        connector.setRomanticStatus(b, a, RomanticStatus.STEADY);
        connector.recordCoupleKnowledge(observer, a, b);

        assertTrue(RomanceUpdater.endOfDayMaintenance(students, connector));

        assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(a, b));
        assertFalse(connector.knowsAboutCouple(observer, a, b),
                "Breaking up should purge observers' knowledge of the couple");
    }

    private Student namedStudent(int id, String gender) {
        Student student = new Student();
        student.studentName.setFirstName("Student" + id);
        student.studentName.setLastName("Jealousy");
        student.studentStatistics.setGradeLevel("Junior");
        student.studentStatistics.setGender(gender);
        student.studentStatistics.setCharisma(80);
        student.studentStatistics.setEmpathy(75);
        student.studentStatistics.setLuck(70);
        student.studentStatistics.setPerception(50);
        student.studentStatistics.setMainClique("NoLife");
        student.studentStatistics.setNeighborhoodName("Oak Hills");
        return student;
    }
}
