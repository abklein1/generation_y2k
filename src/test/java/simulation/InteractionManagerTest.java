package simulation;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.GameRandom;
import utility.RomanceUpdater;
import utility.SocialLinkConnector;

import java.util.HashMap;

import static constants.SimConstants.BADMOUTH_BACKFIRE_CHANCE;
import static constants.SimConstants.BADMOUTH_BACKFIRE_PENALTY;
import static constants.SimConstants.IMPRESS_CHARISMA_DIVISOR;
import static constants.SimConstants.IMPRESS_FLOP_CHANCE;
import static constants.SimConstants.IMPRESS_FLOP_PENALTY;
import static constants.SimConstants.IMPRESS_TARGET_GAIN_BASE;
import static constants.SimConstants.SOCIAL_LINK_DRAIN_BADMOUTH;
import static constants.SimConstants.SOCIAL_LINK_GAIN_BADMOUTH;
import static constants.SimConstants.SOCIAL_LINK_GAIN_IMPRESS;
import static constants.SimConstants.SOCIAL_LINK_GAIN_TALKING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the per-activity interaction resolution: the jealousy-driven
 * BADMOUTHING and IMPRESSING activities have asymmetric score outcomes,
 * while ordinary social activities keep their mutual positive gains.
 */
@DisplayName("InteractionManager jealousy activities")
class InteractionManagerTest {

    @Test
    @DisplayName("Ordinary talking still applies the mutual positive gain")
    void testTalkingStaysMutuallyPositive() {
        GameRandom.reset();
        GameRandom.initialize(1L);
        Fixture fixture = new Fixture();

        fixture.manager.registerInteraction(fixture.initiator, fixture.target,
                ActivityType.TALKING);
        fixture.manager.resolveInteractions();

        assertEquals(SOCIAL_LINK_GAIN_TALKING,
                fixture.connector.getSocialScore(fixture.initiator, fixture.target), 1e-9);
        assertEquals(SOCIAL_LINK_GAIN_TALKING,
                fixture.connector.getSocialScore(fixture.target, fixture.initiator), 1e-9);
    }

    @Test
    @DisplayName("Badmouthing a rival to a non-loyal listener drains their opinion of the rival")
    void testBadmouthDrainsRival() {
        GameRandom.reset();
        GameRandom.initialize(31337L);
        Fixture fixture = new Fixture();
        RomanceUpdater.drainDaysEvents();

        // Listener's opinion of the rival sits below the loyalty threshold
        fixture.connector.modifySocialScore(fixture.target, fixture.subject, 30);

        fixture.manager.registerInteraction(fixture.initiator, fixture.target,
                ActivityType.BADMOUTHING, fixture.subject);
        fixture.manager.resolveInteractions();

        assertEquals(30 - SOCIAL_LINK_DRAIN_BADMOUTH,
                fixture.connector.getSocialScore(fixture.target, fixture.subject), 1e-9,
                "The listener should sour on the badmouthed rival");
        assertEquals(SOCIAL_LINK_GAIN_BADMOUTH,
                fixture.connector.getSocialScore(fixture.initiator, fixture.target), 1e-9,
                "The badmouther gets a small conspiratorial gain");
        assertEquals(ActivityType.TALKING,
                fixture.target.getEntityState().getCurrentActivity(),
                "The listener mirrors a plain conversation");
        assertTrue(RomanceUpdater.drainDaysEvents().stream()
                        .anyMatch(event -> event.contains("talked trash")),
                "Badmouthing should be recorded as a romance event");
    }

    @Test
    @DisplayName("A loyal listener can snap back at the badmouther instead")
    void testBadmouthCanBackfireOnLoyalListener() {
        GameRandom.reset();
        GameRandom.initialize(97531L);
        Fixture fixture = new Fixture();
        // Loyal listener: opinion of the rival is above the loyalty threshold
        fixture.connector.modifySocialScore(fixture.target, fixture.subject, 80);

        // The fixture setup consumes random draws of its own, so sample the
        // exact roll the resolution will consume only now, then rewind the
        // seed so the assertion can adapt to whichever branch it selects.
        GameRandom.reset();
        GameRandom.initialize(97531L);
        double roll = GameRandom.nextDouble();
        GameRandom.reset();
        GameRandom.initialize(97531L);

        fixture.manager.registerInteraction(fixture.initiator, fixture.target,
                ActivityType.BADMOUTHING, fixture.subject);
        fixture.manager.resolveInteractions();

        if (roll < BADMOUTH_BACKFIRE_CHANCE) {
            assertEquals(-BADMOUTH_BACKFIRE_PENALTY,
                    fixture.connector.getSocialScore(fixture.target, fixture.initiator), 1e-9,
                    "Backfire: the loyal listener snaps at the badmouther");
            assertEquals(80.0,
                    fixture.connector.getSocialScore(fixture.target, fixture.subject), 1e-9,
                    "Backfire: the rival's standing is defended");
        } else {
            assertEquals(80 - SOCIAL_LINK_DRAIN_BADMOUTH,
                    fixture.connector.getSocialScore(fixture.target, fixture.subject), 1e-9,
                    "No backfire: the dirt lands even on a loyal listener");
        }
    }

    @Test
    @DisplayName("Showing off warms the crush by a charisma-scaled amount")
    void testImpressGainScalesWithCharisma() {
        GameRandom.reset();
        GameRandom.initialize(2L);
        Fixture fixture = new Fixture();
        // High charisma: above the flop threshold, so the outcome is deterministic
        fixture.initiator.studentStatistics.setCharisma(80);

        fixture.manager.registerInteraction(fixture.initiator, fixture.target,
                ActivityType.IMPRESSING);
        fixture.manager.resolveInteractions();

        assertEquals(SOCIAL_LINK_GAIN_IMPRESS,
                fixture.connector.getSocialScore(fixture.initiator, fixture.target), 1e-9,
                "The initiator invests warmth in the crush");
        assertEquals(IMPRESS_TARGET_GAIN_BASE + 80 / IMPRESS_CHARISMA_DIVISOR,
                fixture.connector.getSocialScore(fixture.target, fixture.initiator), 1e-9,
                "The crush warms by the charisma-scaled amount");
    }

    @Test
    @DisplayName("A low-charisma attempt can flop and embarrass the initiator")
    void testImpressCanFlopWithLowCharisma() {
        GameRandom.reset();
        GameRandom.initialize(8642L);
        Fixture fixture = new Fixture();
        fixture.initiator.studentStatistics.setCharisma(20);

        // Sample the roll the resolution will consume after fixture setup,
        // then rewind so the assertion adapts to the branch it selects.
        GameRandom.reset();
        GameRandom.initialize(8642L);
        double roll = GameRandom.nextDouble();
        GameRandom.reset();
        GameRandom.initialize(8642L);

        fixture.manager.registerInteraction(fixture.initiator, fixture.target,
                ActivityType.IMPRESSING);
        fixture.manager.resolveInteractions();

        if (roll < IMPRESS_FLOP_CHANCE) {
            assertEquals(-IMPRESS_FLOP_PENALTY,
                    fixture.connector.getSocialScore(fixture.target, fixture.initiator), 1e-9,
                    "Flop: the crush is unimpressed");
        } else {
            assertEquals(IMPRESS_TARGET_GAIN_BASE + 20 / IMPRESS_CHARISMA_DIVISOR,
                    fixture.connector.getSocialScore(fixture.target, fixture.initiator), 1e-9,
                    "No flop: the crush still warms a little");
        }
    }

    /**
     * Minimal three-student world: an initiator, an interaction target, and
     * a third-party subject, wired to a connector and a fresh manager. The
     * target carries the entity state and behavior context required for a
     * confirmed interaction.
     */
    private static final class Fixture {
        final Student initiator = namedStudent(0, "Male");
        final Student target = namedStudent(1, "Female");
        final Student subject = namedStudent(2, "Male");
        final SocialLinkConnector connector = new SocialLinkConnector();
        final InteractionManager manager = new InteractionManager();

        Fixture() {
            HashMap<Integer, Student> students = new HashMap<>();
            students.put(0, initiator);
            students.put(1, target);
            students.put(2, subject);
            connector.restoreFromSnapshot(students, null);
            manager.setSocialLinkConnector(connector);
            manager.clearTick();

            initiator.setEntityState(new EntityState());
            initiator.setBehaviorContext(new BehaviorContext());
            target.setEntityState(new EntityState());
            target.setBehaviorContext(new BehaviorContext());
        }

        private static Student namedStudent(int id, String gender) {
            Student student = new Student();
            student.studentName.setFirstName("Student" + id);
            student.studentName.setLastName("Interaction");
            student.studentStatistics.setGradeLevel("Junior");
            student.studentStatistics.setGender(gender);
            student.studentStatistics.setDetermination(50);
            student.studentStatistics.setCharisma(50);
            student.studentStatistics.setEmpathy(60);
            student.studentStatistics.setLuck(50);
            student.studentStatistics.setMainClique("NoLife");
            student.studentStatistics.setNeighborhoodName("Oak Hills");
            return student;
        }
    }
}
