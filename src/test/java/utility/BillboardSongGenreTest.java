package utility;

import entity.Radio.Song;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("BillboardSongLoader genre enrichment")
class BillboardSongGenreTest {

    @Test
    @DisplayName("Songs are enriched with genres at parse time")
    void testTopSongEnriched() {
        LocalDate week = LocalDate.of(2004, 7, 24);
        List<Song> chart = BillboardSongLoader.getChart(week);
        assertEquals(100, chart.size());

        Song top = chart.get(0); // "Confessions Part II" by Usher
        assertEquals("Usher", top.getPerformer());
        assertFalse(top.getGenres().isEmpty(),
                "Expected the #1 song to be genre-tagged");
    }

    @Test
    @DisplayName("getGenres never returns null, even for unmatched songs")
    void testGenresNeverNull() {
        LocalDate week = LocalDate.of(1958, 8, 4);
        List<Song> chart = BillboardSongLoader.getChart(week);
        assertFalse(chart.isEmpty());
        for (Song s : chart) {
            assertNotNull(s.getGenres(),
                    "getGenres must be non-null for " + s);
        }
    }
}
