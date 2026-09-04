package behavior;

import behavior.leaf.student.BadmouthRivalActionNode;
import behavior.leaf.student.GetCrushAttentionActionNode;
import behavior.leaf.student.HasJealousRivalCondition;
import behavior.leaf.student.IsSabotageInclinedCondition;
import entity.ActivityType;
import entity.EntityState;
import entity.RomanticStatus;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simulation.InteractionManager;
import utility.GameRandom;
import utility.SocialLinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static constants.SimConstants.IMPRESS_CHARISMA_DIVISOR;
import static constants.SimConstants.IMPRESS_TARGET_GAIN_BASE;
import static constants.SimConstants.SOCIAL_LINK_DRAIN_BADMOUTH;
import static constants.SimConstants.SOCIAL_LINK_GAIN_IMPRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for the jealous-drama behavior branch: a student who
 * knows their crush is taken either badmouths the rival (low empathy and
 * responsibility) or vies for the crush's attention, and the registered
 * interaction resolves into the expected score changes.
 */
@DisplayName("Jealousy behavior branch")
class JealousyBehaviorTest {

    @Test
    @DisplayName("A jealous low-empathy student badmouths the rival to their crush")
    void testSabotagePathRegistersBadmouthing() {
        GameRandom.reset();
        GameRandom.initialize(1357L);
        World world = new World();
        // Sabotage disposition: low empathy and responsibility
        world.jealous.studentStatistics.setEmpathy(20);
        world.jealous.studentStatistics.setResponsibility(20);

        BehaviorContext context = world.contextForJealous();

        HasJealousRivalCondition condition = new HasJealousRivalCondition();
        boolean triggered = false;
        for (int i = 0; i < 300 && !triggered; i++) {
            triggered = condition.tick(context) == BehaviorStatus.SUCCESS;
        }
        assertTrue(triggered, "The act gate should pass within 300 attempts");
        assertSame(world.crush, context.getVariable("jealousy_crush"));
        assertSame(world.rival, context.getVariable("jealousy_rival"));

        assertEquals(BehaviorStatus.SUCCESS, new IsSabotageInclinedCondition().tick(context),
                "Low empathy + responsibility should choose sabotage");

        world.manager.clearTick();
        assertEquals(BehaviorStatus.SUCCESS, new BadmouthRivalActionNode().tick(context));
        assertEquals(ActivityType.BADMOUTHING,
                world.jealous.getEntityState().getCurrentActivity());

        double crushOpinionBefore = world.connector.getSocialScore(world.crush, world.rival);
        world.manager.resolveInteractions();
        assertEquals(crushOpinionBefore - SOCIAL_LINK_DRAIN_BADMOUTH,
                world.connector.getSocialScore(world.crush, world.rival), 1e-9,
                "Resolving the badmouthing should sour the crush on the rival");
    }

    @Test
    @DisplayName("A jealous high-empathy student vies for the crush's attention instead")
    void testPursuitPathRegistersImpressing() {
        GameRandom.reset();
        GameRandom.initialize(2468L);
        World world = new World();
        // Decent person, high charisma: pursue rather than sabotage
        world.jealous.studentStatistics.setEmpathy(80);
        world.jealous.studentStatistics.setResponsibility(80);
        world.jealous.studentStatistics.setCharisma(80);

        BehaviorContext context = world.contextForJealous();

        HasJealousRivalCondition condition = new HasJealousRivalCondition();
        boolean triggered = false;
        for (int i = 0; i < 300 && !triggered; i++) {
            triggered = condition.tick(context) == BehaviorStatus.SUCCESS;
        }
        assertTrue(triggered, "The act gate should pass within 300 attempts");

        assertEquals(BehaviorStatus.FAILURE, new IsSabotageInclinedCondition().tick(context),
                "High empathy should refuse sabotage");

        world.manager.clearTick();
        assertEquals(BehaviorStatus.SUCCESS, new GetCrushAttentionActionNode().tick(context));
        assertEquals(ActivityType.IMPRESSING,
                world.jealous.getEntityState().getCurrentActivity());

        double selfScoreBefore = world.connector.getSocialScore(world.jealous, world.crush);
        double crushScoreBefore = world.connector.getSocialScore(world.crush, world.jealous);
        world.manager.resolveInteractions();
        assertEquals(selfScoreBefore + SOCIAL_LINK_GAIN_IMPRESS,
                world.connector.getSocialScore(world.jealous, world.crush), 1e-9);
        assertEquals(crushScoreBefore + IMPRESS_TARGET_GAIN_BASE + 80 / IMPRESS_CHARISMA_DIVISOR,
                world.connector.getSocialScore(world.crush, world.jealous), 1e-9,
                "The crush should warm by the charisma-scaled amount");
    }

    /**
     * Three-student world: the jealous student commutes alongside their
     * crush (co-location via transit group), the crush is in an observable
     * steady couple with the rival, and the jealous student already knows.
     * The crush's opinion of the rival sits below the loyalty threshold so
     * badmouthing resolves deterministically.
     */
    private static final class World {
        final Student jealous = namedStudent(0, "Male");
        final Student crush = namedStudent(1, "Female");
        final Student rival = namedStudent(2, "Male");
        final SocialLinkConnector connector = new SocialLinkConnector();
        final InteractionManager manager = new InteractionManager();

        World() {
            HashMap<Integer, Student> students = new HashMap<>();
            students.put(0, jealous);
            students.put(1, crush);
            students.put(2, rival);
            connector.restoreFromSnapshot(students, null);
            manager.setSocialLinkConnector(connector);
            manager.clearTick();

            // The jealous student's crush, and the couple they resent
            connector.modifySocialScore(jealous, crush, 50);
            connector.setRomanticStatus(jealous, crush, RomanticStatus.CRUSH);
            connector.modifySocialScore(crush, rival, 30);
            connector.modifySocialScore(rival, crush, 30);
            connector.setRomanticStatus(crush, rival, RomanticStatus.STEADY);
            connector.setRomanticStatus(rival, crush, RomanticStatus.STEADY);
            connector.recordCoupleKnowledge(jealous, crush, rival);

            // Co-locate the jealous student with their crush via the commute
            List<Student> group = new ArrayList<>(List.of(jealous, crush));
            EntityState jealousState = new EntityState();
            jealousState.setInTransit(true);
            jealousState.setTransitGroup(group);
            jealous.setEntityState(jealousState);
            EntityState crushState = new EntityState();
            crushState.setInTransit(true);
            crushState.setTransitGroup(group);
            crush.setEntityState(crushState);
            crush.setBehaviorContext(new BehaviorContext());
        }

        BehaviorContext contextForJealous() {
            BehaviorContext context = new BehaviorContext(jealous, null, null);
            context.setInteractionManager(manager);
            jealous.setBehaviorContext(context);
            return context;
        }

        private static Student namedStudent(int id, String gender) {
            Student student = new Student();
            student.studentName.setFirstName("Student" + id);
            student.studentName.setLastName("Drama");
            student.studentStatistics.setGradeLevel("Junior");
            student.studentStatistics.setGender(gender);
            student.studentStatistics.setDetermination(50);
            student.studentStatistics.setCharisma(50);
            student.studentStatistics.setEmpathy(60);
            student.studentStatistics.setResponsibility(60);
            student.studentStatistics.setInitiative(100);
            student.studentStatistics.setPerception(50);
            student.studentStatistics.setLuck(50);
            student.studentStatistics.setMainClique("NoLife");
            student.studentStatistics.setNeighborhoodName("Oak Hills");
            return student;
        }
    }
}
