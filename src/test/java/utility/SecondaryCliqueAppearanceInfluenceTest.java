package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Secondary clique appearance influence")
class SecondaryCliqueAppearanceInfluenceTest {

    @Test
    @DisplayName("Secondary clique is recognized only when distinct")
    void testSecondaryCliqueMustBeDistinct() {
        assertTrue(StudentPopGenerator.hasSecondaryAppearanceClique("Emo", "Punk"));
        assertFalse(StudentPopGenerator.hasSecondaryAppearanceClique("Emo", "Emo"));
        assertFalse(StudentPopGenerator.hasSecondaryAppearanceClique("Emo", null));
    }

    @Test
    @DisplayName("Secondary clique can win the small appearance roll")
    void testSecondaryCliqueCanWinAppearanceRoll() {
        GameRandom.reset();
        GameRandom.initialize(4096L);

        String selected = StudentPopGenerator.pickSecondaryAppearanceClique(
                "Emo", "Punk", true);

        assertEquals("Punk", selected);
    }

    @Test
    @DisplayName("Main clique remains default when secondary data is unavailable")
    void testMainCliqueIsDefaultWithoutSecondaryData() {
        GameRandom.reset();
        GameRandom.initialize(4096L);

        String selected = StudentPopGenerator.pickSecondaryAppearanceClique(
                "Emo", "Punk", false);

        assertEquals("Emo", selected);
    }
}
