package entity.Radio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StationType")
class StationTypeTest {

    private static final LocalDate SIM_DATE = LocalDate.of(2004, 7, 24);

    @Test
    @DisplayName("broad() has no target genre; genre() targets GENRE_ROTATION")
    void testFactories() {
        StationType mix = StationType.broad("Mix", StationFormat.MIX);
        assertFalse(mix.isGenreStation());
        assertNull(mix.getTargetGenre());
        assertEquals(StationFormat.MIX, mix.getFormat());

        StationType rock = StationType.genre("Rock", MusicGenre.ROCK);
        assertTrue(rock.isGenreStation());
        assertEquals(MusicGenre.ROCK, rock.getTargetGenre());
        assertEquals(StationFormat.GENRE_ROTATION, rock.getFormat());
    }

    @Test
    @DisplayName("Catalog contains broad and genre station types")
    void testCatalog() {
        List<StationType> catalog = StationType.catalog();
        assertTrue(catalog.size() >= 10, "expected a sizable catalog");
        assertTrue(catalog.stream().anyMatch(StationType::isGenreStation),
                "catalog should contain genre stations");
        assertTrue(catalog.stream().anyMatch(t -> !t.isGenreStation()),
                "catalog should contain broad stations");
    }

    @Test
    @DisplayName("A genre station only plays songs tagged with its genre")
    void testGenreStationFiltersByGenre() {
        StationType rock = StationType.genre("Rock", MusicGenre.ROCK);
        RadioStation station =
                new RadioStation("WROK", "The Rock", 95.5, rock);
        Random rng = new Random(2004L);
        for (int i = 0; i < 40; i++) {
            station.resetSong();
            station.tick(SIM_DATE, rng);
            Song song = station.getCurrentSong();
            assertNotNull(song, "genre station should pick a song");
            assertTrue(song.getGenres().contains(MusicGenre.ROCK),
                    "Rock station played a non-rock song: " + song
                            + " genres=" + song.getGenres());
        }
    }

    @Test
    @DisplayName("decade() factory carries year bounds and no/optional genre")
    void testDecadeFactories() {
        StationType eighties = StationType.decade("All 80s", 1980, 1989);
        assertTrue(eighties.isDecadeStation());
        assertFalse(eighties.isGenreStation());
        assertEquals(1980, eighties.getStartYear());
        assertEquals(1989, eighties.getEndYear());
        assertEquals(StationFormat.DECADE, eighties.getFormat());

        StationType eightiesRock =
                StationType.decade("80s Rock", 1980, 1989, MusicGenre.ROCK);
        assertTrue(eightiesRock.isDecadeStation());
        assertTrue(eightiesRock.isGenreStation());
        assertEquals(MusicGenre.ROCK, eightiesRock.getTargetGenre());
    }

    @Test
    @DisplayName("A decade mix only plays songs from within its year window")
    void testDecadeMixStaysInWindow() {
        RadioStation station = new RadioStation("WEFM", "All 80s", 93.7,
                StationType.decade("All 80s", 1980, 1989));
        Random rng = new Random(1984L);
        for (int i = 0; i < 40; i++) {
            station.resetSong();
            station.tick(SIM_DATE, rng);
            Song song = station.getCurrentSong();
            assertNotNull(song, "decade station should pick a song");
            int year = song.getChartWeek().getYear();
            assertTrue(year >= 1980 && year <= 1989,
                    "80s station played a " + year + " song: " + song);
        }
    }

    @Test
    @DisplayName("A decade+genre station stays in window and on genre")
    void testDecadeGenreStation() {
        RadioStation station = new RadioStation("WCLS", "90s Rock", 97.3,
                StationType.decade("90s Rock", 1990, 1999, MusicGenre.ROCK));
        Random rng = new Random(1995L);
        for (int i = 0; i < 40; i++) {
            station.resetSong();
            station.tick(SIM_DATE, rng);
            Song song = station.getCurrentSong();
            assertNotNull(song);
            int year = song.getChartWeek().getYear();
            assertTrue(year >= 1990 && year <= 1999,
                    "90s Rock played a " + year + " song: " + song);
            assertTrue(song.getGenres().contains(MusicGenre.ROCK),
                    "90s Rock played a non-rock song: " + song
                            + " " + song.getGenres());
        }
    }

    @Test
    @DisplayName("A multi-genre station accepts songs in any of its genres")
    void testMultiGenreStationFiltersByAnyGenre() {
        StationType alt = StationType.multiGenre("Alternative",
                MusicGenre.ROCK, MusicGenre.PUNK, MusicGenre.EMO);
        assertTrue(alt.isGenreStation());
        Set<MusicGenre> targets = alt.getTargetGenres();
        assertTrue(targets.contains(MusicGenre.ROCK));
        assertTrue(targets.contains(MusicGenre.PUNK));
        assertTrue(targets.contains(MusicGenre.EMO));

        RadioStation station =
                new RadioStation("WALT", "The Edge", 100.3, alt);
        Random rng = new Random(2003L);
        for (int i = 0; i < 40; i++) {
            station.resetSong();
            station.tick(SIM_DATE, rng);
            Song song = station.getCurrentSong();
            assertNotNull(song, "alternative station should pick a song");
            boolean matches = song.getGenres().contains(MusicGenre.ROCK)
                    || song.getGenres().contains(MusicGenre.PUNK)
                    || song.getGenres().contains(MusicGenre.EMO);
            assertTrue(matches, "Alternative station played an off-genre song: "
                    + song + " genres=" + song.getGenres());
        }
    }

    @Test
    @DisplayName("Catalog includes the Alternative multi-genre station")
    void testCatalogHasAlternative() {
        StationType alt = StationType.catalog().stream()
                .filter(t -> "Alternative".equals(t.getLabel()))
                .findFirst()
                .orElse(null);
        assertNotNull(alt, "catalog should contain an Alternative station");
        assertTrue(alt.getTargetGenres().contains(MusicGenre.EMO),
                "Alternative station should include EMO");
        assertTrue(alt.getTargetGenres().contains(MusicGenre.PUNK),
                "Alternative station should include PUNK");
    }

    @Test
    @DisplayName("Different genre stations select different songs")
    void testDistinctGenreStations() {
        RadioStation country = new RadioStation("KCTY", "Country", 99.1,
                StationType.genre("Country", MusicGenre.COUNTRY));
        Random rng = new Random(7L);
        for (int i = 0; i < 20; i++) {
            country.resetSong();
            country.tick(SIM_DATE, rng);
            Song song = country.getCurrentSong();
            assertNotNull(song);
            assertTrue(song.getGenres().contains(MusicGenre.COUNTRY),
                    "Country station played: " + song);
        }
    }
}
