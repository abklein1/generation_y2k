package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EntityState temperature drift")
class EntityStateTemperatureTest {

    private static final double DRIFT_PER_UNIT = 0.05;

    @Test
    @DisplayName("A matched outfit holds temperature at the comfortable 50")
    void testNoMismatchHoldsSteady() {
        EntityState state = new EntityState();
        assertEquals(50.0, state.getTemperature());

        for (int i = 0; i < 500; i++) {
            state.tickTemperature(0, DRIFT_PER_UNIT);
        }
        assertEquals(50.0, state.getTemperature());
    }

    @Test
    @DisplayName("Overdressed students heat toward 100, clamped at the cap")
    void testOverdressedHeatsUp() {
        EntityState state = new EntityState();

        state.tickTemperature(2, DRIFT_PER_UNIT);
        assertEquals(50.1, state.getTemperature(), 1e-9);

        for (int i = 0; i < 5000; i++) {
            state.tickTemperature(2, DRIFT_PER_UNIT);
        }
        assertEquals(100.0, state.getTemperature(),
                "Temperature must clamp at 100");
    }

    @Test
    @DisplayName("Underdressed students cool toward 0")
    void testUnderdressedCoolsDown() {
        EntityState state = new EntityState();

        for (int i = 0; i < 100; i++) {
            state.tickTemperature(-3, DRIFT_PER_UNIT);
        }
        assertTrue(state.getTemperature() < 50.0);
        assertEquals(50.0 - 100 * 3 * DRIFT_PER_UNIT,
                state.getTemperature(), 1e-9);
    }

    @Test
    @DisplayName("resetNeeds restores temperature to 50 at day end")
    void testResetRestoresTemperature() {
        EntityState state = new EntityState();
        for (int i = 0; i < 200; i++) {
            state.tickTemperature(2, DRIFT_PER_UNIT);
        }
        assertTrue(state.getTemperature() > 50.0);

        state.resetNeeds();
        assertEquals(50.0, state.getTemperature());
    }
}
