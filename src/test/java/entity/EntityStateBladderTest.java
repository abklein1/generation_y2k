package entity;

import constants.SimConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.GameRandom;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EntityState bladder variance")
class EntityStateBladderTest {

    @Test
    @DisplayName("New entities do not share one synchronized bladder value")
    void testNewEntitiesStartWithVariedBladderLevels() {
        GameRandom.initialize(316L);

        Set<Integer> roundedBladderLevels = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            EntityState state = new EntityState();
            roundedBladderLevels.add((int) Math.round(state.getBladder()));
            assertTrue(state.getBladder() >= SimConstants.NEED_BLADDER_START_MIN);
            assertTrue(state.getBladder() <= SimConstants.NEED_BLADDER_START_MAX);
        }

        assertTrue(roundedBladderLevels.size() > 1,
                "Students should not all start with the same bladder meter");
    }

    @Test
    @DisplayName("Baseline bladder decay is varied and slow enough for no-drink days")
    void testBaselineBladderDecayVariesWithoutForcingEveryStudentCritical() {
        GameRandom.initialize(316L);

        int criticalCount = 0;
        Set<Integer> roundedBladderLevels = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            EntityState state = new EntityState();
            for (int tick = 0; tick < 480; tick++) {
                state.tickNeeds(0, 0,
                        SimConstants.NEED_BLADDER_DECAY_PER_TICK,
                        SimConstants.NEED_BLADDER_POST_MEAL_DECAY_PER_TICK,
                        0, 0, 0);
            }
            roundedBladderLevels.add((int) Math.round(state.getBladder()));
            if (state.getBladder() < SimConstants.NEED_CRITICAL_THRESHOLD) {
                criticalCount++;
            }
        }

        assertTrue(criticalCount > 0,
                "Some students should still need the bathroom naturally");
        assertTrue(criticalCount < 50,
                "Not every student should need the bathroom without eating or drinking");
        assertTrue(roundedBladderLevels.size() > 1,
                "Bladder values should remain spread out over the day");
    }

    @Test
    @DisplayName("Bathroom relief does not reset every entity to the same value")
    void testBathroomReliefDoesNotHardResetEveryoneToOneHundred() {
        GameRandom.initialize(316L);

        Set<Integer> reliefLevels = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            EntityState state = new EntityState();
            state.setBladder(5.0);
            state.relieveBladder();
            reliefLevels.add((int) Math.round(state.getBladder()));
            assertTrue(state.getBladder() >= SimConstants.NEED_BLADDER_RELIEF_MIN);
            assertTrue(state.getBladder() <= SimConstants.NEED_BLADDER_RELIEF_MAX);
        }

        assertTrue(reliefLevels.size() > 1,
                "Bathroom relief should preserve per-student variance");
    }
}
