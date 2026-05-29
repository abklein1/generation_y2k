package utility.music;

import entity.Radio.MusicGenre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenreCollapser")
class GenreCollapserTest {

    @Test
    @DisplayName("Genre-defining markers win over generic 'pop'")
    void testPriorityOrdering() {
        assertEquals(MusicGenre.PUNK, GenreCollapser.collapse("pop punk"));
        assertEquals(MusicGenre.HIP_HOP, GenreCollapser.collapse("pop rap"));
        assertEquals(MusicGenre.POP, GenreCollapser.collapse("dance pop"));
        assertEquals(MusicGenre.POP, GenreCollapser.collapse("electropop"));
    }

    @Test
    @DisplayName("Representative tags collapse to the expected canonical genre")
    void testRepresentativeTags() {
        assertEquals(MusicGenre.METAL, GenreCollapser.collapse("death metal"));
        assertEquals(MusicGenre.ELECTRONIC, GenreCollapser.collapse("deep house"));
        assertEquals(MusicGenre.COUNTRY,
                GenreCollapser.collapse("contemporary country"));
        assertEquals(MusicGenre.RNB, GenreCollapser.collapse("blues"));
        assertEquals(MusicGenre.PUNK, GenreCollapser.collapse("ska"));
        assertEquals(MusicGenre.JAZZ, GenreCollapser.collapse("bebop"));
        assertEquals(MusicGenre.LATIN, GenreCollapser.collapse("reggaeton"));
    }

    @Test
    @DisplayName("Soft/yacht/adult 'rock' collapses to POP, not ROCK")
    void testSoftRockRoutesToPop() {
        assertEquals(MusicGenre.POP, GenreCollapser.collapse("soft rock"));
        assertEquals(MusicGenre.POP, GenreCollapser.collapse("yacht rock"));
        assertEquals(MusicGenre.POP,
                GenreCollapser.collapse("adult contemporary"));
        assertEquals(MusicGenre.POP, GenreCollapser.collapse("mellow gold"));
    }

    @Test
    @DisplayName("Hard/classic/album rock still collapses to ROCK")
    void testHardRockStaysRock() {
        assertEquals(MusicGenre.ROCK, GenreCollapser.collapse("rock"));
        assertEquals(MusicGenre.ROCK, GenreCollapser.collapse("classic rock"));
        assertEquals(MusicGenre.ROCK, GenreCollapser.collapse("hard rock"));
        assertEquals(MusicGenre.ROCK, GenreCollapser.collapse("album rock"));
    }

    @Test
    @DisplayName("Unrecognized and blank tags fall through to OTHER")
    void testOtherFallback() {
        assertEquals(MusicGenre.OTHER, GenreCollapser.collapse("escape room"));
        assertEquals(MusicGenre.OTHER, GenreCollapser.collapse(""));
        assertEquals(MusicGenre.OTHER, GenreCollapser.collapse(null));
    }

    @Test
    @DisplayName("collapseAll unions distinct genres; empty input yields {OTHER}")
    void testCollapseAll() {
        Set<MusicGenre> mixed =
                GenreCollapser.collapseAll(List.of("dance pop", "pop rap"));
        assertTrue(mixed.contains(MusicGenre.POP));
        assertTrue(mixed.contains(MusicGenre.HIP_HOP));

        assertEquals(Set.of(MusicGenre.OTHER),
                GenreCollapser.collapseAll(List.of()));
        assertEquals(Set.of(MusicGenre.OTHER),
                GenreCollapser.collapseAll(List.of("escape room")));
    }
}
