package utility;

import behavior.TargetSelector;
import entity.CellPhone;
import entity.OrientationDisclosure;
import entity.SexualOrientation;
import entity.StandardSchool;
import entity.Student;
import entity.Town;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import save.SocialLinkSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static constants.SimConstants.PHONE_CONTACT_ACQUAINTANCE_PROBABILITY;
import static constants.SimConstants.PHONE_CONTACT_CLOSE_FRIEND_PROBABILITY;
import static constants.SimConstants.SOCIAL_LINK_SAME_GENDER_ACQUAINTANCE_WEIGHT;
import static constants.SimConstants.SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT;
import static constants.SimConstants.SOCIAL_LINK_SM_FEMALE_SAME_GENDER_MULTIPLIER;
import static constants.SimConstants.SOCIAL_LINK_SM_MALE_SAME_GENDER_ACQUAINTANCE_WEIGHT;
import static constants.SimConstants.SOCIAL_LINK_SM_MALE_SAME_GENDER_CLOSE_WEIGHT;
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
    @DisplayName("Should total incoming scores per student for popularity rankings")
    void testIncomingScoreTotals() {
        Student beloved = namedStudent(0, "Female");
        Student admirer = namedStudent(1, "Male");
        Student hater = namedStudent(2, "Female");

        SocialLinkConnector connector = new SocialLinkConnector();
        connector.modifySocialScore(admirer, beloved, 60);
        connector.modifySocialScore(hater, beloved, -20);
        connector.modifySocialScore(beloved, admirer, 10);

        var totals = connector.computeIncomingScoreTotals();
        assertEquals(40.0, totals.get(beloved), 0.001,
                "Popularity should sum all incoming directed scores");
        assertEquals(10.0, totals.get(admirer), 0.001);
        assertEquals(0.0, totals.get(hater), 0.001,
                "Students nobody has rated should total zero");
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

    @Test
    @DisplayName("Should adjust the same-gender friendship weight by orientation and disclosure")
    void testOrientationAwareSameGenderWeights() {
        Student straightMale = namedStudent(0, "Male");
        assertEquals(SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT,
                SocialLinkConnector.sameGenderWeightFor(straightMale, true),
                "Straight students use the base close-friend weight");
        assertEquals(SOCIAL_LINK_SAME_GENDER_ACQUAINTANCE_WEIGHT,
                SocialLinkConnector.sameGenderWeightFor(straightMale, false),
                "Straight students use the base acquaintance weight");

        Student openGayMale = namedStudent(1, "Male");
        openGayMale.studentStatistics.setSexualOrientation(SexualOrientation.GAY);
        openGayMale.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);
        assertEquals(SOCIAL_LINK_SM_MALE_SAME_GENDER_CLOSE_WEIGHT,
                SocialLinkConnector.sameGenderWeightFor(openGayMale, true),
                "Openly sexual-minority males should prefer cross-gender close friends");
        assertEquals(SOCIAL_LINK_SM_MALE_SAME_GENDER_ACQUAINTANCE_WEIGHT,
                SocialLinkConnector.sameGenderWeightFor(openGayMale, false));

        Student openBiFemale = namedStudent(2, "Female");
        openBiFemale.studentStatistics.setSexualOrientation(SexualOrientation.BISEXUAL);
        openBiFemale.studentStatistics.setOrientationDisclosure(OrientationDisclosure.OPEN);
        assertEquals(SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT * SOCIAL_LINK_SM_FEMALE_SAME_GENDER_MULTIPLIER,
                SocialLinkConnector.sameGenderWeightFor(openBiFemale, true),
                "Openly sexual-minority females should amplify the same-gender preference");

        Student closetedGayMale = namedStudent(3, "Male");
        closetedGayMale.studentStatistics.setSexualOrientation(SexualOrientation.GAY);
        closetedGayMale.studentStatistics.setOrientationDisclosure(OrientationDisclosure.CLOSETED);
        assertEquals(SOCIAL_LINK_SAME_GENDER_CLOSE_WEIGHT,
                SocialLinkConnector.sameGenderWeightFor(closetedGayMale, true),
                "Closeted students mirror heterosexual friendship tendencies");
    }

    @Test
    @DisplayName("Should bias generated perception by the target's clique standing")
    void testCliquePerceptionBias() {
        Student inGroupStudent = namedStudent(0, "Female");
        inGroupStudent.studentStatistics.setMainClique(CliqueLoader.getInGroups().iterator().next());
        assertEquals(constants.SimConstants.SOCIAL_LINK_IN_GROUP_PERCEPTION_BONUS,
                SocialLinkConnector.cliquePerceptionBias(inGroupStudent),
                "In-group members enjoy a warmer collective perception");

        Student outGroupStudent = namedStudent(1, "Male");
        outGroupStudent.studentStatistics.setMainClique(CliqueLoader.getOutGroups().iterator().next());
        assertEquals(constants.SimConstants.SOCIAL_LINK_OUT_GROUP_PERCEPTION_PENALTY,
                SocialLinkConnector.cliquePerceptionBias(outGroupStudent),
                "Out-group members are viewed a bit cooler");

        Student neutralStudent = namedStudent(2, "Female");
        neutralStudent.studentStatistics.setMainClique("NoLife");
        assertEquals(0.0, SocialLinkConnector.cliquePerceptionBias(neutralStudent),
                "Neutral cliques carry no perception bias");

        Student cliquelessStudent = namedStudent(3, "Male");
        cliquelessStudent.studentStatistics.setMainClique(null);
        assertEquals(0.0, SocialLinkConnector.cliquePerceptionBias(cliquelessStudent),
                "Students without a clique carry no perception bias");
    }

    @Test
    @DisplayName("Should make in-group students more popular than out-group students on average")
    void testInGroupStudentsDominatePopularity() {
        GameRandom.reset();
        GameRandom.initialize(9182736L);
        String inClique = CliqueLoader.getInGroups().iterator().next();
        String outClique = CliqueLoader.getOutGroups().iterator().next();

        HashMap<Integer, Student> students = createPeerMap(40, 90, 85, 80);
        for (int i = 0; i < students.size(); i++) {
            students.get(i).studentStatistics.setMainClique(i < 20 ? inClique : outClique);
        }
        StandardSchool school = gradeSchool(students);
        SocialLinkConnector connector = new SocialLinkConnector(students, school);

        var totals = connector.computeIncomingScoreTotals();
        double inGroupMean = 0;
        double outGroupMean = 0;
        for (int i = 0; i < students.size(); i++) {
            if (i < 20) {
                inGroupMean += totals.get(students.get(i)) / 20;
            } else {
                outGroupMean += totals.get(students.get(i)) / 20;
            }
        }
        assertTrue(inGroupMean > outGroupMean,
                "The clique halo should lift in-group popularity above out-group; was "
                        + inGroupMean + " vs " + outGroupMean);
    }

    @Test
    @DisplayName("Should keep friendships mostly within a social stratum with limited in/out crossover")
    void testFriendshipsPreferOwnStratum() {
        GameRandom.reset();
        GameRandom.initialize(4455667L);
        List<String> inCliques = new ArrayList<>(CliqueLoader.getInGroups());
        List<String> outCliques = new ArrayList<>(CliqueLoader.getOutGroups());

        HashMap<Integer, Student> students = createPeerMap(100, 90, 85, 80);
        for (int i = 0; i < students.size(); i++) {
            // Half the school in in-group cliques, half in out-group cliques
            Student student = students.get(i);
            student.studentStatistics.setMainClique(i % 2 == 0
                    ? inCliques.get(i % inCliques.size())
                    : outCliques.get(i % outCliques.size()));
        }
        StandardSchool school = gradeSchool(students);
        new SocialLinkConnector(students, school);

        int withinStratum = 0;
        int crossStratum = 0;
        for (Student student : students.values()) {
            String myCategory = CliqueLoader.getGroupCategory(
                    student.studentStatistics.getMainClique());
            for (Student friend : student.studentStatistics.getFriendsInSchool()) {
                String theirCategory = CliqueLoader.getGroupCategory(
                        friend.studentStatistics.getMainClique());
                if (myCategory.equals(theirCategory)) {
                    withinStratum++;
                } else {
                    crossStratum++;
                }
            }
        }
        assertTrue(withinStratum > crossStratum * 2,
                "Friendships should stay mostly within the in/out stratum; was "
                        + withinStratum + " within vs " + crossStratum + " across");
    }

    @Test
    @DisplayName("Should keep never-enrolled siblings and stale roster entries out of the graph")
    void testGhostStudentsExcludedFromGraph() {
        GameRandom.reset();
        GameRandom.initialize(1357L);
        HashMap<Integer, Student> students = createPeerMap(12, 90, 85, 80);

        // Town generation creates siblings before enrollment is decided, so an
        // enrolled student's sibling list can reference someone who never
        // enrolled. That ghost must not become a graph vertex.
        Student ghostSibling = namedStudent(98, "Female");
        ghostSibling.studentName.setFirstName("Ghost");
        ghostSibling.studentName.setLastName("Sibling");
        Student enrolledStudent = students.get(0);
        enrolledStudent.studentStatistics.addSiblingsInSchool(ghostSibling);
        ghostSibling.studentStatistics.addSiblingsInSchool(enrolledStudent);

        // A grade roster that drifted out of sync with the enrolled map (e.g.
        // a student culled by scheduling) must not leak into candidate pools.
        Student ghostRosterEntry = namedStudent(99, "Male");
        ghostRosterEntry.studentName.setFirstName("Ghost");
        ghostRosterEntry.studentName.setLastName("Roster");
        HashMap<Integer, Student> staleRoster = new HashMap<>(students);
        staleRoster.put(999, ghostRosterEntry);
        StandardSchool school = gradeSchool(staleRoster);

        SocialLinkConnector connector = new SocialLinkConnector(students, school);

        var totals = connector.computeIncomingScoreTotals();
        assertFalse(totals.containsKey(ghostSibling),
                "Never-enrolled siblings must not appear in the social graph");
        assertFalse(totals.containsKey(ghostRosterEntry),
                "Stale grade-roster entries must not appear in the social graph");
        assertEquals(students.size(), totals.size(),
                "Graph should contain exactly the enrolled students");
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
