package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ColorSchemeLoader")
class ColorSchemeLoaderTest {

    @Test
    @DisplayName("Loads named schemes")
    void testHasSchemes() {
        assertTrue(ColorSchemeLoader.hasSchemes());
    }

    @Test
    @DisplayName("Dark scheme is populated and heavily weighted toward black")
    void testDarkSchemeIsMostlyBlack() {
        List<String> dark = ColorSchemeLoader.getSchemeColors("dark");
        assertFalse(dark.isEmpty());
        long blackCount = dark.stream().filter("black"::equals).count();
        assertTrue(blackCount >= dark.size() - blackCount,
                "dark scheme should be at least half black");
    }

    @Test
    @DisplayName("Emo and Goth are locked to the dark scheme")
    void testDarkCliquesLockedToDark() {
        assertEquals(List.of("dark"),
                ColorSchemeLoader.getSchemesForClique("Emo"));
        assertEquals(List.of("dark"),
                ColorSchemeLoader.getSchemesForClique("Goth"));
    }

    @Test
    @DisplayName("Unmapped cliques fall back to the default scheme set")
    void testUnmappedCliqueFallsBackToDefault() {
        List<String> fallback =
                ColorSchemeLoader.getSchemesForClique("NotARealClique");
        assertEquals(ColorSchemeLoader.getDefaultSchemes(), fallback);
        assertFalse(fallback.isEmpty());
    }

    @Test
    @DisplayName("Unknown scheme name returns an empty color list")
    void testUnknownSchemeReturnsEmpty() {
        assertTrue(ColorSchemeLoader.getSchemeColors("nope").isEmpty());
        assertTrue(ColorSchemeLoader.getSchemeColors(null).isEmpty());
    }
}
