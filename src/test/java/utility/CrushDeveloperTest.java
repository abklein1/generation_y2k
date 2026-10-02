package utility;

import entity.RomanticStatus;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static constants.SimConstants.ROMANCE_CRUSH_MIN_SCORE;
import static constants.SimConstants.ROMANCE_FLEETING_CRUSH_EXTRA_MAX;
import static constants.SimConstants.ROMANCE_STANDOUT_MAX_SHARE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CrushDeveloper")
class CrushDeveloperTest {

    @Test
    @DisplayName("Standout registry admits only extreme-stat students and respects the 5% cap")
    void testStandoutRegistry() {
        HashMap<Integer, Student> students = averagePopulation(100);
        // One genuine standout per stat, far above the pack
        students.get(0).studentStatistics.setIntelligence(100);
        students.get(1).studentStatistics.setCharisma(100);
        students.get(2).studentStatistics.setStrength(100);

        CrushDeveloper.refreshStandouts(students.values());

        assertTrue(CrushDeveloper.isStatStandout(students.get(0)),
                "Extreme intelligence should qualify");
        assertTrue(CrushDeveloper.isStatStandout(students.get(1)),
                "Extreme charisma should qualify");
        assertTrue(CrushDeveloper.isStatStandout(students.get(2)),
                "Extreme strength should qualify");
        assertFalse(CrushDeveloper.isStatStandout(students.get(50)),
                "An average student should never qualify");

        int cap = Math.max(1, (int) Math.floor(students.size() * ROMANCE_STANDOUT_MAX_SHARE));
        int standouts = 0;
        for (Student student : students.values()) {
            if (CrushDeveloper.isStatStandout(student)) {
                standouts++;
            }
        }
        assertTrue(standouts <= cap,
                "Standouts must stay within the cap (" + cap + "); found " + standouts);
    }

    @Test
    @DisplayName("Flat stats crown no standouts")
    void testFlatStatsProduceNoStandouts() {
        HashMap<Integer, Student> students = averagePopulation(50);
        CrushDeveloper.refreshStandouts(students.values());
        for (Student student : students.values()) {
            assertFalse(CrushDeveloper.isStatStandout(student),
                    "With zero stat spread, nobody stands out");
        }
    }

    @Test
    @DisplayName("Warm attraction-compatible friendships can grow into crushes over pulses")
    void testFriendshipGrownCrush() {
        GameRandom.reset();
        GameRandom.initialize(4242L);

        HashMap<Integer, Student> students = new HashMap<>();
        Student him = namedStudent(0, "Male");
        Student her = namedStudent(1, "Female");
        Student buddy = namedStudent(2, "Male"); // same gender: never a target for him
        students.put(0, him);
        students.put(1, her);
        students.put(2, buddy);

        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);
        connector.modifySocialScore(him, her, 55);
        connector.modifySocialScore(her, him, 50);
        connector.modifySocialScore(him, buddy, 70);

        boolean crushFormed = false;
        for (int pulse = 0; pulse < 50_000 && !crushFormed; pulse++) {
            CrushDeveloper.pulseFriendshipCrushes(students, connector);
            crushFormed = connector.getRomanticStatus(him, her) == RomanticStatus.CRUSH;
            // The same-gender friend must never become a crush for a
            // straight student, no matter how warm the friendship.
            assertEquals(RomanticStatus.NONE, connector.getRomanticStatus(him, buddy),
                    "Attraction gate must hold for friendship-grown crushes");
        }
        assertTrue(crushFormed,
                "A warm cross-gender friendship should eventually grow into a crush");
    }

    @Test
    @DisplayName("Standout presence can spark a weak fleeting crush; gates hold")
    void testFleetingCrush() {
        GameRandom.reset();
        GameRandom.initialize(777L);

        HashMap<Integer, Student> students = averagePopulation(60);
        Student observer = students.get(10); // Female (even index)
        Student dazzlingMale = students.get(11);
        dazzlingMale.studentStatistics.setCharisma(100);
        Student plainMale = students.get(13);
        CrushDeveloper.refreshStandouts(students.values());
        assertTrue(CrushDeveloper.isStatStandout(dazzlingMale));
        assertFalse(CrushDeveloper.isStatStandout(plainMale));

        SocialLinkConnector connector = new SocialLinkConnector();
        connector.restoreFromSnapshot(students, null);

        // A non-standout never sparks a fleeting crush
        for (int i = 0; i < 2_000; i++) {
            assertFalse(CrushDeveloper.maybeDevelopFleetingCrush(observer, plainMale, connector));
        }
        // A same-gender standout never sparks one in a straight observer
        Student dazzlingFemale = students.get(12);
        dazzlingFemale.studentStatistics.setIntelligence(100);
        CrushDeveloper.refreshStandouts(students.values());
        for (int i = 0; i < 2_000; i++) {
            assertFalse(CrushDeveloper.maybeDevelopFleetingCrush(observer, dazzlingFemale, connector));
        }

        // The compatible standout eventually dazzles the observer
        boolean sparked = false;
        for (int i = 0; i < 10_000 && !sparked; i++) {
            sparked = CrushDeveloper.maybeDevelopFleetingCrush(observer, dazzlingMale, connector);
        }
        assertTrue(sparked, "A compatible standout should eventually spark a fleeting crush");
        assertEquals(RomanticStatus.CRUSH, connector.getRomanticStatus(observer, dazzlingMale));

        // The seeded link is weak: barely above the crush floor, so it will
        // starve out within days unless reinforced.
        double score = connector.getSocialScore(observer, dazzlingMale);
        assertTrue(score >= ROMANCE_CRUSH_MIN_SCORE
                        && score <= ROMANCE_CRUSH_MIN_SCORE + ROMANCE_FLEETING_CRUSH_EXTRA_MAX,
                "Fleeting crush link should sit just above the crush floor; was " + score);

        // Once sparked, the existing record blocks a duplicate
        assertFalse(CrushDeveloper.maybeDevelopFleetingCrush(observer, dazzlingMale, connector),
                "An existing romance record must block a repeat spark");
    }

    private Student namedStudent(int id, String gender) {
        Student student = new Student();
        student.studentName.setFirstName("Student" + id);
        student.studentName.setLastName("Develop");
        student.studentStatistics.setGradeLevel("Junior");
        student.studentStatistics.setGender(gender);
        student.studentStatistics.setCharisma(60);
        student.studentStatistics.setIntelligence(60);
        student.studentStatistics.setStrength(60);
        return student;
    }

    /**
     * A population with mild, identical baseline stats (no spread beyond
     * what tests add explicitly). Even ids are Female, odd ids Male.
     */
    private HashMap<Integer, Student> averagePopulation(int count) {
        HashMap<Integer, Student> students = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Student student = new Student();
            student.studentName.setFirstName("Peer" + i);
            student.studentName.setLastName("Standout");
            student.studentStatistics.setGradeLevel("Junior");
            student.studentStatistics.setGender(i % 2 == 0 ? "Female" : "Male");
            student.studentStatistics.setIntelligence(50);
            student.studentStatistics.setCharisma(50);
            student.studentStatistics.setStrength(50);
            students.put(i, student);
        }
        return students;
    }
}
