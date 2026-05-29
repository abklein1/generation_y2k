package utility.music;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PerformerNormalizer")
class PerformerNormalizerTest {

    @Test
    @DisplayName("normalize lowercases, strips punctuation, drops leading 'the'")
    void testBasicNormalize() {
        assertEquals("beatles", PerformerNormalizer.normalize("The Beatles"));
        assertEquals("nsync", PerformerNormalizer.normalize("*NSYNC"));
        assertEquals("", PerformerNormalizer.normalize(null));
        assertEquals("", PerformerNormalizer.normalize("   "));
    }

    @Test
    @DisplayName("normalize strips diacritics and folds & to 'and'")
    void testDiacriticsAndAmpersand() {
        assertEquals("beyonce",
                PerformerNormalizer.normalize("Beyonc\u00e9"));
        assertEquals("earth wind and fire",
                PerformerNormalizer.normalize("Earth, Wind & Fire"));
        assertEquals("hall and oates",
                PerformerNormalizer.normalize("Hall & Oates"));
    }

    @Test
    @DisplayName("splitCollaborators handles Featuring + & credits")
    void testSplitFeaturing() {
        List<String> parts = PerformerNormalizer.splitCollaborators(
                "Usher Featuring Lil Jon & Ludacris");
        assertEquals(List.of("Usher", "Lil Jon", "Ludacris"), parts);
    }

    @Test
    @DisplayName("splitCollaborators handles 'With' backing-group credits")
    void testSplitWith() {
        List<String> parts = PerformerNormalizer.splitCollaborators(
                "Elvis Presley With The Jordanaires");
        assertEquals(List.of("Elvis Presley", "The Jordanaires"), parts);
    }

    @Test
    @DisplayName("splitCollaborators leaves a solo artist intact")
    void testSplitSolo() {
        assertEquals(List.of("Jay-Z"),
                PerformerNormalizer.splitCollaborators("Jay-Z"));
    }

    @Test
    @DisplayName("songKey is stable across equivalent title/performer forms")
    void testSongKey() {
        String a = PerformerNormalizer.songKey("Yeah!",
                "Usher Featuring Lil Jon & Ludacris");
        String b = PerformerNormalizer.songKey("yeah",
                "Usher Featuring Lil Jon and Ludacris");
        assertEquals(a, b);
        assertTrue(a.contains("|"), "song key should join title and performer");
    }
}
