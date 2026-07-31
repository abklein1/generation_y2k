package utility;

import entity.OrientationDisclosure;
import entity.RomanticStatus;
import entity.SexualOrientation;
import entity.StandardSchool;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import save.SocialLinkSnapshot;

import java.util.HashMap;

import static constants.SimConstants.SOCIAL_LINK_DECAY_STANDARD;
import static constants.SimConstants.SOCIAL_LINK_DECAY_STEADY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RomanceAssigner")
class RomanceAssignerTest {

    @Test
    @DisplayName("Should promote friendships within orientation constraints at roughly the target rate")
    void testGenerationInvariantsAndParticipation() {
        GameRandom.reset();
        GameRandom.initialize(13579L);
        HashMap<Integer, Student> students = createPeerMap(80);
        StandardSchool school = gradeSchool(students);
        SocialLinkConnector connector = new SocialLinkConnector(students, school);
        OrientationAssigner.assignOrientations(students);
        RomanceAssigner.assignRomanticRelationships(students, connector);

        int involved = 0;
        for (Student student : students.values()) {
            boolean hasOutgoing = !connector.getRomanticInterests(student).isEmpty();
            boolean hasIncomingMutual = connector.hasMutualRomance(student);
            if (hasOutgoing || hasIncomingMutual) {
                involved++;
            }

            for (Student other : connector.getRomanticInterests(student)) {
                RomanticStatus outgoing = connector.getRomanticStatus(student, other);
                assertNotEquals(student, other, "No self-romance");
                assertFalse(student.studentStatistics.getSiblingsInSchool().contains(other),
                        "Siblings can never be romance targets");
                assertTrue(connector.getSocialScore(student, other) > 0,
                        "Romance must sit on a positive outgoing link");

                SexualOrientation orientation = student.studentStatistics.getSexualOrientation();
                OrientationDisclosure disclosure = student.studentStatistics.getOrientationDisclosure();
                boolean sameGender = student.studentStatistics.getGender()
                        .equalsIgnoreCase(other.studentStatistics.getGender());

                if (orientation == SexualOrientation.ASEXUAL) {
                    assertNotEquals(RomanticStatus.FLING, outgoing,
                            "Asexual students never have flings");
                }
                if (orientation == SexualOrientation.STRAIGHT) {
                    assertFalse(sameGender, "Straight students pursue cross-gender romance only");
                }
                if (orientation.isNonHeterosexual()
                        && disclosure == OrientationDisclosure.CLOSETED && sameGender) {
                    assertEquals(RomanticStatus.CRUSH, outgoing,
                            "Closeted same-gender romance can only be a hidden crush");
                    assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(other, student),
                            "Hidden crushes are never reciprocated");
                }
                if (orientation == SexualOrientation.GAY
                        && disclosure == OrientationDisclosure.OPEN) {
                    assertTrue(sameGender, "Openly gay students pursue same-gender romance only");
                }
            }
        }

        double participation = (double) involved / students.size();
        assertTrue(participation >= 0.25 && participation <= 0.60,
                "Participation should land near the ~50% target; was " + participation);
    }

    @Test
    @DisplayName("Should preserve romance records across snapshot save/restore")
    void testSnapshotRoundTrip() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student a = namedStudent(0, "Female");
        Student b = namedStudent(1, "Male");
        Student c = namedStudent(2, "Male");
        students.put(0, a);
        students.put(1, b);
        students.put(2, c);

        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        connector.modifySocialScore(a, b, 60);
        connector.modifySocialScore(b, a, 55);
        connector.modifySocialScore(c, a, 20);
        connector.setRomanticStatus(a, b, RomanticStatus.STEADY);
        connector.setRomanticStatus(b, a, RomanticStatus.FLING);
        connector.setRomanticStatus(c, a, RomanticStatus.CRUSH);

        SocialLinkSnapshot snapshot = connector.createSnapshot();
        SocialLinkConnector restored = new SocialLinkConnector();
        restored.restoreFromSnapshot(students, snapshot);

        assertEquals(RomanticStatus.STEADY, restored.getRomanticStatus(a, b));
        assertEquals(RomanticStatus.FLING, restored.getRomanticStatus(b, a));
        assertEquals(RomanticStatus.CRUSH, restored.getRomanticStatus(c, a));
        assertEquals(RomanticStatus.NONE, restored.getRomanticStatus(a, c));
    }

    @Test
    @DisplayName("Should decay steady-partner links slower than standard links")
    void testSteadyPartnerDecay() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student a = namedStudent(0, "Female");
        Student b = namedStudent(1, "Male");
        Student c = namedStudent(2, "Male");
        students.put(0, a);
        students.put(1, b);
        students.put(2, c);

        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        connector.modifySocialScore(a, b, 60);
        connector.modifySocialScore(a, c, 60);
        connector.setRomanticStatus(a, b, RomanticStatus.STEADY);

        connector.applyDailyDecay();

        assertEquals(60 - SOCIAL_LINK_DECAY_STEADY, connector.getSocialScore(a, b), 1e-9,
                "Steady partners decay at the reduced rate");
        assertEquals(60 - SOCIAL_LINK_DECAY_STANDARD, connector.getSocialScore(a, c), 1e-9,
                "Non-romantic links decay at the standard rate");
    }

    @Test
    @DisplayName("Should describe asymmetric perception in the relationship summary")
    void testRelationshipSummaryAsymmetry() {
        HashMap<Integer, Student> students = new HashMap<>();
        Student a = namedStudent(0, "Female");
        Student b = namedStudent(1, "Male");
        students.put(0, a);
        students.put(1, b);

        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        connector.modifySocialScore(a, b, 50);
        connector.modifySocialScore(b, a, 45);

        assertEquals("No romantic relationship.", connector.getRelationshipSummary(a, b));

        connector.setRomanticStatus(a, b, RomanticStatus.STEADY);
        connector.setRomanticStatus(b, a, RomanticStatus.FLING);
        String asymmetric = connector.getRelationshipSummary(a, b);
        assertTrue(asymmetric.contains("going out") && asymmetric.contains("hooking up"),
                "Summary should surface both perceptions; was: " + asymmetric);

        // Agreement upgrades the label: both seeing it as serious is "official"
        connector.setRomanticStatus(b, a, RomanticStatus.STEADY);
        String mutual = connector.getRelationshipSummary(a, b);
        assertTrue(mutual.startsWith("Mutual") && mutual.contains("official"),
                "Mutual steady should read as official; was: " + mutual);

        // Both seeing it as casual is "FWB"
        connector.setRomanticStatus(a, b, RomanticStatus.FLING);
        connector.setRomanticStatus(b, a, RomanticStatus.FLING);
        String fwb = connector.getRelationshipSummary(a, b);
        assertTrue(fwb.contains("FWB"),
                "Mutual fling should read as FWB; was: " + fwb);

        connector.setRomanticStatus(a, b, RomanticStatus.NONE);
        connector.setRomanticStatus(b, a, RomanticStatus.CRUSH);
        String crush = connector.getRelationshipSummary(a, b);
        assertTrue(crush.contains("crush") && crush.contains("unaware"),
                "One-sided crushes should note the other party is unaware; was: " + crush);
    }

    @Test
    @DisplayName("Should let friendless students hold crushes (no prior link required)")
    void testCrushesDoNotRequireExistingFriendships() {
        GameRandom.reset();
        GameRandom.initialize(24680L);
        HashMap<Integer, Student> students = createPeerMap(60);
        // Bare connector: vertices only, no friendships or acquaintances at all
        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        OrientationAssigner.assignOrientations(students);

        RomanceAssigner.assignRomanticRelationships(students, connector);

        int crushes = 0;
        for (Student student : students.values()) {
            for (Student other : connector.getRomanticInterests(student)) {
                assertEquals(RomanticStatus.CRUSH, connector.getRomanticStatus(student, other),
                        "With no friendships to promote, only crushes can form");
                assertTrue(connector.getSocialScore(student, other)
                                >= constants.SimConstants.ROMANCE_CRUSH_MIN_SCORE,
                        "A crush should create/raise the outgoing link to crush level");
                crushes++;
            }
        }
        assertTrue(crushes > 0,
                "Students with no social links should still develop crushes from afar");
    }

    @Test
    @DisplayName("Should rate popular in-group students as far more crush-worthy")
    void testDesirabilityFavorsPopularInGroupStudents() {
        String inClique = CliqueLoader.getInGroups().iterator().next();
        String outClique = CliqueLoader.getOutGroups().iterator().next();

        Student queenBee = namedStudent(0, "Female");
        queenBee.studentStatistics.setMainClique(inClique);
        Student wallflower = namedStudent(1, "Female");
        wallflower.studentStatistics.setMainClique("NoLife");
        Student outcast = namedStudent(2, "Female");
        outcast.studentStatistics.setMainClique(outClique);

        // Popularity percentiles (rank in the school-wide ordering, 0..1)
        HashMap<Student, Double> percentiles = new HashMap<>();
        percentiles.put(queenBee, 0.98);
        percentiles.put(wallflower, 0.50);
        percentiles.put(outcast, 0.50);

        double queenDesirability = RomanceAssigner.desirability(queenBee, percentiles);
        double wallflowerDesirability = RomanceAssigner.desirability(wallflower, percentiles);
        double outcastDesirability = RomanceAssigner.desirability(outcast, percentiles);

        assertTrue(queenDesirability > wallflowerDesirability * 2,
                "Popularity plus in-group standing should multiply crush appeal");
        assertTrue(wallflowerDesirability > outcastDesirability,
                "Out-group standing should reduce crush appeal at equal popularity");
    }

    @Test
    @DisplayName("Should keep crush clique affinity flatter than friendship affinity (crossover stays alive)")
    void testCrushCliqueAffinity() {
        // Find a real Hate pair across the in/out divide from the loaded matrix
        String hater = null;
        String hated = null;
        outer:
        for (String out : CliqueLoader.getOutGroups()) {
            for (String in : CliqueLoader.getInGroups()) {
                if ("Hate".equals(CliqueLoader.getRelationship(out, in))) {
                    hater = out;
                    hated = in;
                    break outer;
                }
            }
        }
        assertTrue(hater != null && hated != null,
                "Clique matrix should contain at least one out->in Hate pair");

        Student holder = namedStudent(0, "Female");
        holder.studentStatistics.setMainClique(hater);
        Student crossCrush = namedStudent(1, "Male");
        crossCrush.studentStatistics.setMainClique(hated);
        Student ownClique = namedStudent(2, "Male");
        ownClique.studentStatistics.setMainClique(hater);

        assertEquals(constants.SimConstants.ROMANCE_CRUSH_CLIQUE_SAME,
                RomanceAssigner.crushCliqueAffinity(holder, ownClique),
                "Own clique should be the most crush-friendly");
        assertEquals(constants.SimConstants.ROMANCE_CRUSH_CLIQUE_HATE,
                RomanceAssigner.crushCliqueAffinity(holder, crossCrush));
        assertTrue(constants.SimConstants.ROMANCE_CRUSH_CLIQUE_HATE > 0,
                "Cross-strata crushes must stay possible");
        assertTrue(constants.SimConstants.ROMANCE_CRUSH_CLIQUE_HATE
                        > constants.SimConstants.CLIQUE_AFFINITY_HATE,
                "Romance should penalize hated cliques less than friendship does");
    }

    @Test
    @DisplayName("Should gate attraction by presented orientation (closeted presents as straight)")
    void testAttractionCompatibility() {
        Student straightMale = namedStudent(0, "Male");
        Student straightFemale = namedStudent(1, "Female");
        Student openGayMale = namedStudent(2, "Male");
        openGayMale.studentStatistics.setSexualOrientation(SexualOrientation.GAY);
        openGayMale.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);
        Student closetedGayMale = namedStudent(3, "Male");
        closetedGayMale.studentStatistics.setSexualOrientation(SexualOrientation.GAY);
        closetedGayMale.studentStatistics.setOrientationDisclosure(OrientationDisclosure.CLOSETED);
        Student openBiFemale = namedStudent(4, "Female");
        openBiFemale.studentStatistics.setSexualOrientation(SexualOrientation.BISEXUAL);
        openBiFemale.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);

        assertTrue(RomanceAssigner.attractedTo(straightMale, straightFemale));
        assertFalse(RomanceAssigner.attractedTo(straightMale, openGayMale));
        assertTrue(RomanceAssigner.attractedTo(openGayMale, straightMale));
        assertFalse(RomanceAssigner.attractedTo(openGayMale, straightFemale));
        // Closeted gay male presents as straight: cover relationships are
        // cross-gender, same-gender pursuit is suppressed
        assertTrue(RomanceAssigner.attractedTo(closetedGayMale, straightFemale));
        assertFalse(RomanceAssigner.attractedTo(closetedGayMale, openGayMale));
        // Bisexual students are compatible with both genders
        assertTrue(RomanceAssigner.attractedTo(openBiFemale, straightMale));
        assertTrue(RomanceAssigner.attractedTo(openBiFemale, straightFemale));
    }

    private Student namedStudent(int id, String gender) {
        Student student = new Student();
        student.studentName.setFirstName("Student" + id);
        student.studentName.setLastName("Romance");
        student.studentStatistics.setGradeLevel("Junior");
        student.studentStatistics.setGender(gender);
        student.studentStatistics.setCharisma(80);
        student.studentStatistics.setEmpathy(75);
        student.studentStatistics.setLuck(70);
        student.studentStatistics.setMainClique("NoLife");
        student.studentStatistics.setNeighborhoodName("Oak Hills");
        return student;
    }

    private HashMap<Integer, Student> createPeerMap(int count) {
        HashMap<Integer, Student> students = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Student student = new Student();
            student.studentName.setFirstName("Peer" + i);
            student.studentName.setLastName("Heart");
            student.studentStatistics.setGradeLevel(i % 2 == 0 ? "Freshman" : "Sophomore");
            student.studentStatistics.setGender(i % 2 == 0 ? "Female" : "Male");
            student.studentStatistics.setCharisma(90);
            student.studentStatistics.setEmpathy(85);
            student.studentStatistics.setLuck(80);
            student.studentStatistics.setMainClique(i % 3 == 0 ? "Jock" : (i % 3 == 1 ? "Emo" : "NoLife"));
            student.studentStatistics.setNeighborhoodName(i % 2 == 0 ? "Oak Hills" : "River Bend");
            students.put(i, student);
        }
        return students;
    }

    private StandardSchool gradeSchool(HashMap<Integer, Student> students) {
        StandardSchool school = new StandardSchool();
        school.setStudentGradeClass(students, null);
        return school;
    }
}
