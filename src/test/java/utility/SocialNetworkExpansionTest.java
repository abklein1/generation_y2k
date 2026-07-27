package utility;

import behavior.TargetSelector;
import entity.CellPhone;
import entity.StandardSchool;
import entity.Student;
import entity.Town;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import save.SocialLinkSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static constants.SimConstants.PHONE_CONTACT_ACQUAINTANCE_PROBABILITY;
import static constants.SimConstants.PHONE_CONTACT_CLOSE_FRIEND_PROBABILITY;
import static constants.SimConstants.SOCIAL_LINK_TIER_FRIEND_THRESHOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Social network expansion")
class SocialNetworkExpansionTest {

    @Test
    @DisplayName("Should derive larger connection capacity from the same base-stat formula")
    void testConnectionCapacityScalesWithBaseStats() {
        Student low = statsStudent(20, 20, 20);
        Student high = statsStudent(100, 100, 100);

        GameRandom.reset();
        GameRandom.initialize(111L);
        HashMap<Integer, Student> lowMap = new HashMap<>();
        lowMap.put(0, low);
        StandardSchool school = gradeSchool(lowMap);
        new SocialLinkConnector(lowMap, school);

        GameRandom.reset();
        GameRandom.initialize(111L);
        HashMap<Integer, Student> highMap = new HashMap<>();
        highMap.put(0, high);
        // Need peers so initialize can run capacity alone; reuse helper map
        HashMap<Integer, Student> peers = createPeerMap(30, 90, 85, 80);
        peers.put(0, high);
        StandardSchool highSchool = gradeSchool(peers);
        new SocialLinkConnector(peers, highSchool);

        assertTrue(high.studentStatistics.getMaxBestFriends()
                        > low.studentStatistics.getMaxBestFriends(),
                "Higher charisma/empathy/luck should raise close-friend capacity");
        assertTrue(high.studentStatistics.getMaxSocialConnections()
                        > low.studentStatistics.getMaxSocialConnections(),
                "Higher stats should also raise overall connection capacity");
        assertEquals(
                StudentStatistics.deriveMaxSocialConnections(high.studentStatistics.getMaxBestFriends()),
                high.studentStatistics.getMaxSocialConnections());
        assertTrue(high.studentStatistics.getMaxSocialConnections()
                >= high.studentStatistics.getMaxBestFriends() * 2);
    }

    @Test
    @DisplayName("Should widen networks beyond close friends and include mixed-gender acquaintances")
    void testWiderNetworksIncludeMixedGenderAcquaintances() {
        GameRandom.reset();
        GameRandom.initialize(5551212L);
        HashMap<Integer, Student> students = createPeerMap(40, 95, 90, 85);
        StandardSchool school = gradeSchool(students);
        SocialLinkConnector connector = new SocialLinkConnector(students, school);

        Student focal = students.get(0);
        int outDegree = connector.socialGraph.outDegreeOf(focal);
        int closeFriends = focal.studentStatistics.getFriendsInSchool().size();

        assertTrue(outDegree > closeFriends,
                "Overall outgoing links should exceed the close-friend cache");
        assertTrue(outDegree >= 8,
                "A socially capable student should typically hold a wider network");

        boolean mixedGenderAcquaintance = false;
        String myGender = focal.studentStatistics.getGender();
        for (Student peer : connector.getPositiveConnections(focal)) {
            if (focal.studentStatistics.getFriendsInSchool().contains(peer)) {
                continue;
            }
            if (peer.studentStatistics.getGender() != null
                    && !peer.studentStatistics.getGender().equalsIgnoreCase(myGender)) {
                mixedGenderAcquaintance = true;
                break;
            }
        }
        assertTrue(mixedGenderAcquaintance,
                "Acquaintance generation should allow mixed-gender links");
    }

    @Test
    @DisplayName("Should classify one-sided relationships and prefer initiator-liked targets")
    void testAsymmetricClassificationAndTargeting() {
        Student admirer = namedStudent(0, "Female");
        Student popular = namedStudent(1, "Male");
        Student rival = namedStudent(2, "Female");
        Student stranger = namedStudent(3, "Male");

        SocialLinkConnector connector = new SocialLinkConnector();
        // Admirer likes popular; popular is indifferent (one-sided positive)
        connector.modifySocialScore(admirer, popular, 55);
        connector.modifySocialScore(popular, admirer, 0);
        // Admirer dislikes rival
        connector.modifySocialScore(admirer, rival, -40);

        assertEquals(SocialLinkConnector.Reciprocity.ONE_SIDED_POSITIVE,
                connector.getReciprocity(admirer, popular));
        assertEquals(SocialLinkConnector.RelationshipTier.FRIEND,
                connector.getRelationshipTier(admirer, popular));
        assertEquals(SocialLinkConnector.RelationshipTier.NEUTRAL,
                connector.getRelationshipTier(popular, admirer));

        // With a liked peer present, targeting should prefer them over strangers/rivals
        List<Student> candidates = List.of(popular, rival, stranger);
        GameRandom.reset();
        GameRandom.initialize(42L);
        int popularPicks = 0;
        for (int i = 0; i < 40; i++) {
            Student pick = TargetSelector.selectTarget(admirer, candidates, connector);
            if (pick == popular) {
                popularPicks++;
            }
        }
        assertEquals(40, popularPicks,
                "When a positively-linked peer is available, targeting should choose them");
    }

    @Test
    @DisplayName("Should keep friendsInSchool cache duplicate-safe and score-synced")
    void testFriendCacheSyncAndDedup() {
        Student a = namedStudent(0, "Female");
        Student b = namedStudent(1, "Male");
        SocialLinkConnector connector = new SocialLinkConnector();

        a.studentStatistics.addFriendInSchool(b);
        a.studentStatistics.addFriendInSchool(b);
        assertEquals(1, a.studentStatistics.getFriendsInSchool().size(),
                "addFriendInSchool should be duplicate-safe");

        connector.modifySocialScore(a, b, SOCIAL_LINK_TIER_FRIEND_THRESHOLD);
        assertTrue(a.studentStatistics.getFriendsInSchool().contains(b));

        connector.modifySocialScore(a, b, -SOCIAL_LINK_TIER_FRIEND_THRESHOLD);
        assertFalse(a.studentStatistics.getFriendsInSchool().contains(b),
                "Falling below the friend tier should remove the cache entry");
    }

    @Test
    @DisplayName("Should restore friend caches from a social-link snapshot")
    void testSnapshotRestoresFriendCache() {
        GameRandom.reset();
        GameRandom.initialize(2468L);
        HashMap<Integer, Student> students = createPeerMap(12, 90, 85, 80);
        StandardSchool school = gradeSchool(students);
        SocialLinkConnector connector = new SocialLinkConnector(students, school);
        SocialLinkSnapshot snapshot = connector.createSnapshot();

        // Clear caches, then restore from snapshot
        for (Student student : students.values()) {
            student.studentStatistics.getFriendsInSchool().clear();
        }
        SocialLinkConnector restored = new SocialLinkConnector();
        restored.restoreFromSnapshot(students, snapshot);

        for (Student student : students.values()) {
            for (Student friend : student.studentStatistics.getFriendsInSchool()) {
                assertTrue(restored.getSocialScore(student, friend) >= SOCIAL_LINK_TIER_FRIEND_THRESHOLD);
            }
        }
    }

    @Test
    @DisplayName("Should populate asymmetric phone contacts from directed relationship tiers")
    void testTieredPhoneContactsCanExceedTwenty() {
        Town town = new Town("Testville");
        HashMap<Integer, Student> students = createPeerMap(35, 100, 95, 90);
        SocialLinkConnector connector = new SocialLinkConnector();
        Student focal = students.get(0);

        // Deterministic wide network: focal likes many peers strongly enough
        // that the close-friend save rate (~92%) yields well over 20 contacts.
        // Peers do not necessarily like focal back (asymmetric contacts).
        for (int i = 1; i < students.size(); i++) {
            Student peer = students.get(i);
            connector.modifySocialScore(focal, peer, 55.0);
            if (i % 4 != 0) {
                connector.modifySocialScore(peer, focal, 2.0);
            }
        }

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            CellPhone phone = new CellPhone(
                    String.format(Locale.ROOT, "%03d-%04d", 200, i),
                    student.studentName.getFullName(),
                    "Nokia", "3310", "blue", 300, 100);
            phone.setSms(true);
            town.assignStudentPhone(student, phone);
        }

        // Seed chosen so probability rolls save enough of the 34 outgoing links
        GameRandom.reset();
        GameRandom.initialize(7L);
        CellPhoneAssignmentService.populatePhoneContacts(town, connector);

        int focalContacts = town.getStudentPhone(focal).getContactCount();
        assertTrue(focalContacts > 20,
                "A student with dozens of positive outgoing links should exceed 20 contacts; was "
                        + focalContacts);

        // Asymmetry: some peers focal saved should not have focal saved back
        // (those peers have no outgoing edge, so they never roll a save).
        boolean foundAsymmetry = false;
        CellPhone focalPhone = town.getStudentPhone(focal);
        for (int i = 1; i < students.size(); i++) {
            if (i % 4 == 0) {
                Student peer = students.get(i);
                CellPhone peerPhone = town.getStudentPhone(peer);
                if (focalPhone.hasContactNumber(peerPhone.getPhoneNumber())
                        && !peerPhone.hasContactNumber(focalPhone.getPhoneNumber())) {
                    foundAsymmetry = true;
                    break;
                }
            }
        }
        assertTrue(foundAsymmetry,
                "One-sided social links should produce asymmetric contact lists");
    }

    @Test
    @DisplayName("Should map contact save probability by outgoing score tier")
    void testContactProbabilityTiers() {
        assertEquals(PHONE_CONTACT_CLOSE_FRIEND_PROBABILITY,
                CellPhoneAssignmentService.contactProbabilityForScore(80));
        assertEquals(PHONE_CONTACT_ACQUAINTANCE_PROBABILITY,
                CellPhoneAssignmentService.contactProbabilityForScore(10));
        assertEquals(0.0, CellPhoneAssignmentService.contactProbabilityForScore(-20));
    }

    private Student statsStudent(int charisma, int empathy, int luck) {
        Student student = namedStudent(0, "Female");
        student.studentStatistics.setCharisma(charisma);
        student.studentStatistics.setEmpathy(empathy);
        student.studentStatistics.setLuck(luck);
        return student;
    }

    private Student namedStudent(int id, String gender) {
        Student student = new Student();
        student.studentName.setFirstName("Student" + id);
        student.studentName.setLastName("Network");
        student.studentStatistics.setGradeLevel("Freshman");
        student.studentStatistics.setGender(gender);
        student.studentStatistics.setCharisma(80);
        student.studentStatistics.setEmpathy(75);
        student.studentStatistics.setLuck(70);
        student.studentStatistics.setMainClique("NoLife");
        student.studentStatistics.setNeighborhoodName("Oak Hills");
        return student;
    }

    private HashMap<Integer, Student> createPeerMap(int count, int charisma, int empathy, int luck) {
        HashMap<Integer, Student> students = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Student student = new Student();
            student.studentName.setFirstName("Peer" + i);
            student.studentName.setLastName("Seed");
            student.studentStatistics.setGradeLevel(i % 2 == 0 ? "Freshman" : "Sophomore");
            student.studentStatistics.setGender(i % 2 == 0 ? "Female" : "Male");
            student.studentStatistics.setCharisma(charisma);
            student.studentStatistics.setEmpathy(empathy);
            student.studentStatistics.setLuck(luck);
            student.studentStatistics.setMainClique(i % 3 == 0 ? "Jock" : (i % 3 == 1 ? "Geek" : "NoLife"));
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
