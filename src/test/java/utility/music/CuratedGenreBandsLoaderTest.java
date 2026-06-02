package utility.music;

import entity.Radio.MusicGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CuratedGenreBandsLoader")
class CuratedGenreBandsLoaderTest {

    @BeforeEach
    void reset() {
        CuratedGenreBandsLoader.resetForTests();
    }

    @Test
    @DisplayName("Parses the curated EMO scene bands")
    void testEmoCurated() {
        List<String> emo =
                CuratedGenreBandsLoader.getBandsByGenre().get(MusicGenre.EMO);
        assertFalse(emo == null || emo.isEmpty(),
                "expected curated EMO bands to load");
        assertTrue(emo.contains("My Chemical Romance"),
                "curated EMO list should include My Chemical Romance");
        assertTrue(emo.contains("Taking Back Sunday"),
                "curated EMO list should include Taking Back Sunday");
    }

    @Test
    @DisplayName("Comment keys are ignored, never exposed as a genre")
    void testNoCommentLeak() {
        // OTHER must never carry curated bands (the "_comment" key is skipped).
        assertTrue(CuratedGenreBandsLoader.getBandsByGenre()
                        .get(MusicGenre.OTHER) == null,
                "OTHER should not appear in the curated map");
    }
}
