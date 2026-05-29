package utility.music;

import entity.Radio.MusicGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MusicGenreLoader")
class MusicGenreLoaderTest {

    @BeforeEach
    void reset() {
        MusicGenreLoader.resetForTests();
    }

    @Test
    @DisplayName("Resolves a known solo artist via the artist-level map")
    void testArtistMatch() {
        Set<MusicGenre> genres =
                MusicGenreLoader.genresFor("Confessions Part II", "Usher");
        assertFalse(genres.isEmpty(), "Usher should resolve to genres");
        assertTrue(genres.contains(MusicGenre.RNB)
                        || genres.contains(MusicGenre.POP),
                "Expected pop/R&B for Usher; got " + genres);
    }

    @Test
    @DisplayName("Collaboration credits union the matched artists' genres")
    void testCollaborationUnion() {
        Set<MusicGenre> solo =
                MusicGenreLoader.genresFor("Confessions Part II", "Usher");
        Set<MusicGenre> collab = MusicGenreLoader.genresFor(
                "Yeah!", "Usher Featuring Lil Jon & Ludacris");
        assertFalse(collab.isEmpty(), "collaboration should resolve genres");
        assertTrue(collab.containsAll(solo),
                "collaboration genres should include the lead artist's genres");
    }

    @Test
    @DisplayName("Unknown performer yields an empty, immutable set")
    void testUnknownPerformer() {
        Set<MusicGenre> genres = MusicGenreLoader.genresFor(
                "Totally Made Up Song", "Zzzqqq Nonexistent Artist 9999");
        assertTrue(genres.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> genres.add(MusicGenre.POP));
    }

    @Test
    @DisplayName("Returned genre sets are immutable")
    void testImmutableResult() {
        Set<MusicGenre> genres =
                MusicGenreLoader.genresFor("Confessions Part II", "Usher");
        assertThrows(UnsupportedOperationException.class,
                () -> genres.add(MusicGenre.METAL));
    }
}
