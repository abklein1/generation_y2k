package entity;

import constants.SimConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.GameRandom;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EntityState thirst variance")
class EntityStateThirstTest {

    @Test
    @DisplayName("New entities do not share one synchronized thirst value")
    void testNewEntitiesStartWithVariedThirstLevels() {
        GameRandom.initialize(316L);

        Set<Integer> roundedThirstLevels = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            EntityState state = new EntityState();
            roundedThirstLevels.add((int) Math.round(state.getThirst()));
            assertTrue(state.getThirst() >= SimConstants.NEED_THIRST_START_MIN);
            assertTrue(state.getThirst() <= SimConstants.NEED_THIRST_START_MAX);
        }

        assertTrue(roundedThirstLevels.size() > 1,
                "Students should not all start with the same thirst meter");
    }

    @Test
    @DisplayName("Baseline thirst decay is varied and slow enough for no-drink days")
    void testBaselineThirstDecayVariesWithoutForcingEveryStudentCritical() {
        GameRandom.initialize(316L);

        int criticalCount = 0;
        Set<Integer> roundedThirstLevels = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            EntityState state = new EntityState();
            for (int tick = 0; tick < 480; tick++) {
                state.tickNeeds(0,
                        SimConstants.NEED_THIRST_DECAY_PER_TICK,
                        0, 0, 0, 0, 0);
            }
            roundedThirstLevels.add((int) Math.round(state.getThirst()));
            if (state.getThirst() < SimConstants.NEED_CRITICAL_THRESHOLD) {
                criticalCount++;
            }
        }

        assertTrue(criticalCount > 0,
                "Some students should still become thirsty naturally");
        assertTrue(criticalCount < 50,
                "Not every student should become critical without drinking");
        assertTrue(roundedThirstLevels.size() > 1,
                "Thirst values should remain spread out over the day");
    }
}
