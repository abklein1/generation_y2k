package utility.music;

import entity.Radio.MusicGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MusicTaste blending")
class MusicTasteTest {

    @BeforeEach
    void reset() {
        CliqueMusicPreferenceLoader.resetForTests();
    }

    @Test
    @DisplayName("No secondary clique returns the primary preference unchanged")
    void testPrimaryOnly() {
        MusicPreference emoOnly = MusicTaste.forCliques("Emo", null);
        MusicPreference emo = CliqueMusicPreferenceLoader.getPreference("Emo");
        assertEquals(emo.weightFor(MusicGenre.EMO),
                emoOnly.weightFor(MusicGenre.EMO), 1e-9);
        assertEquals(emo.getOpenness(), emoOnly.getOpenness(), 1e-9);
    }

    @Test
    @DisplayName("Secondary clique lifts an aligned genre's weight")
    void testSecondaryLiftsAlignedGenre() {
        double emoPunk = MusicTaste.forCliques("Emo", "Punk")
                .weightFor(MusicGenre.PUNK);
        double emoOnly = MusicTaste.forCliques("Emo", null)
                .weightFor(MusicGenre.PUNK);
        assertTrue(emoPunk >= emoOnly,
                "Punk secondary should not lower PUNK weight: "
                        + emoPunk + " vs " + emoOnly);
        assertTrue(emoPunk > 0.0, "PUNK should remain liked");
    }

    @Test
    @DisplayName("Blended weights stay clamped to [-1, 1]")
    void testWeightsClamped() {
        MusicPreference blended = MusicTaste.forCliques("Metal", "Punk");
        for (Double weight : blended.getGenreWeights().values()) {
            assertTrue(weight >= -1.0 && weight <= 1.0,
                    "weight out of range: " + weight);
        }
    }

    @Test
    @DisplayName("Openness is taken from the primary clique")
    void testOpennessFromPrimary() {
        MusicPreference primary =
                CliqueMusicPreferenceLoader.getPreference("Country");
        MusicPreference blended = MusicTaste.forCliques("Country", "Jock");
        assertEquals(primary.getOpenness(), blended.getOpenness(), 1e-9);
    }
}
