package utility.music;

import entity.Radio.MusicGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CliqueMusicPreferenceLoader")
class CliqueMusicPreferenceLoaderTest {

    @BeforeEach
    void reset() {
        CliqueMusicPreferenceLoader.resetForTests();
    }

    @Test
    @DisplayName("Parses liked and disliked genre weights for a clique")
    void testLikesAndDislikes() {
        MusicPreference country =
                CliqueMusicPreferenceLoader.getPreference("Country");
        assertTrue(country.weightFor(MusicGenre.COUNTRY) > 0.5,
                "Country clique should strongly like COUNTRY");
        assertTrue(country.weightFor(MusicGenre.HIP_HOP) < 0.0,
                "Country clique should dislike HIP_HOP");
    }

    @Test
    @DisplayName("Unlisted genres are neutral (0.0)")
    void testUnlistedGenreNeutral() {
        MusicPreference country =
                CliqueMusicPreferenceLoader.getPreference("Country");
        assertEquals(0.0, country.weightFor(MusicGenre.JAZZ), 1e-9);
    }

    @Test
    @DisplayName("openness is parsed within range")
    void testOpenness() {
        double openness = CliqueMusicPreferenceLoader
                .getPreference("Metal").getOpenness();
        assertTrue(openness >= 0.0 && openness <= 1.0,
                "openness out of range: " + openness);
    }

    @Test
    @DisplayName("Unknown clique falls back to the _default preference")
    void testDefaultFallback() {
        assertFalse(CliqueMusicPreferenceLoader.hasPreference("NotAClique"));
        MusicPreference fallback =
                CliqueMusicPreferenceLoader.getPreference("NotAClique");
        MusicPreference def = CliqueMusicPreferenceLoader.getDefaultPreference();
        assertEquals(def.getOpenness(), fallback.getOpenness(), 1e-9);
        assertTrue(fallback.weightFor(MusicGenre.POP) > 0.0,
                "default should have some positive baseline taste");
    }

    @Test
    @DisplayName("All listed cliques parse with at least one genre weight")
    void testKnownCliquesPresent() {
        String[] cliques = {"Emo", "Bling", "Latino", "Electronic", "Jock"};
        for (String clique : cliques) {
            assertTrue(CliqueMusicPreferenceLoader.hasPreference(clique),
                    "missing preference entry for " + clique);
            assertFalse(CliqueMusicPreferenceLoader.getPreference(clique)
                            .getGenreWeights().isEmpty(),
                    clique + " should have genre weights");
        }
    }
}
