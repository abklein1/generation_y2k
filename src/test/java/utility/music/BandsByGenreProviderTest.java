package utility.music;

import entity.Radio.MusicGenre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BandsByGenreProvider")
class BandsByGenreProviderTest {

    @Test
    @DisplayName("Mainstream genres resolve to a non-empty era-appropriate roster")
    void testPopulatedGenres() {
        for (MusicGenre genre : new MusicGenre[]{
                MusicGenre.ROCK, MusicGenre.POP, MusicGenre.COUNTRY,
                MusicGenre.HIP_HOP}) {
            List<String> bands = BandsByGenreProvider.bandsFor(genre);
            assertFalse(bands.isEmpty(),
                    "expected at least one performer for " + genre);
        }
    }

    @Test
    @DisplayName("Performer names are de-duplicated and free of featuring credits")
    void testNamesCleaned() {
        List<String> rock = BandsByGenreProvider.bandsFor(MusicGenre.ROCK);
        long distinct = rock.stream().distinct().count();
        assertTrue(distinct == rock.size(), "roster should be de-duplicated");
        for (String name : rock) {
            assertFalse(name.toLowerCase().contains(" featuring "),
                    "name should not contain a featuring credit: " + name);
            assertFalse(name.isBlank(), "name should not be blank");
        }
    }

    @Test
    @DisplayName("OTHER is never indexed as a band genre")
    void testOtherEmpty() {
        assertTrue(BandsByGenreProvider.bandsFor(MusicGenre.OTHER).isEmpty(),
                "OTHER should carry no band roster");
    }

    @Test
    @DisplayName("pickBand returns a performer from the genre roster")
    void testPickBandWithinRoster() {
        List<String> rock = BandsByGenreProvider.bandsFor(MusicGenre.ROCK);
        for (int i = 0; i < 50; i++) {
            String picked = BandsByGenreProvider.pickBand(MusicGenre.ROCK);
            assertTrue(rock.contains(picked),
                    "picked band should be in the ROCK roster: " + picked);
        }
    }

    @Test
    @DisplayName("pickBand returns null for an empty genre roster")
    void testPickBandEmptyGenre() {
        assertTrue(BandsByGenreProvider.pickBand(MusicGenre.OTHER) == null,
                "OTHER has no roster, so pickBand should return null");
    }

    @Test
    @DisplayName("pickAnyBand returns a real performer from any genre")
    void testPickAnyBand() {
        for (int i = 0; i < 50; i++) {
            String picked = BandsByGenreProvider.pickAnyBand();
            assertTrue(picked != null && !picked.isBlank(),
                    "pickAnyBand should return a real performer");
        }
    }

    @Test
    @DisplayName("pickFutureBand draws acts charting after the reference date")
    void testPickFutureBand() {
        // From Aug 2004 the future window is Nov 2004 - Apr 2005, well within
        // the Billboard dataset, so a mainstream genre should yield picks.
        java.time.LocalDate now = java.time.LocalDate.of(2004, 8, 23);
        boolean gotOne = false;
        for (int i = 0; i < 50; i++) {
            String picked = BandsByGenreProvider.pickFutureBand(MusicGenre.POP, now);
            if (picked != null && !picked.isBlank()) {
                gotOne = true;
            }
        }
        assertTrue(gotOne, "expected at least one future POP act in the window");
    }

    @Test
    @DisplayName("pickFutureBand is null with a null reference date")
    void testPickFutureBandNullDate() {
        assertTrue(BandsByGenreProvider.pickFutureBand(MusicGenre.POP, null) == null,
                "null reference date should yield no future band");
    }

    @Test
    @DisplayName("Curated scene bands surface in their genre even when uncharted")
    void testCuratedBandsInjected() {
        // My Chemical Romance never charted the Hot 100 by the 2004 cutoff, so
        // it can only appear via the curated supplement.
        List<String> emo = BandsByGenreProvider.bandsFor(MusicGenre.EMO);
        assertTrue(emo.contains("My Chemical Romance"),
                "curated EMO band should be in the roster: " + emo);
        assertTrue(emo.contains("The Used"),
                "curated EMO band should be in the roster");
    }

    @Test
    @DisplayName("Curated bands are weighted on par with top charting acts")
    void testCuratedBandsHeavilyWeighted() {
        // Sized to the genre's top charting act, a curated band should land in
        // the upper portion of the weight-ordered roster, not the long tail.
        List<String> emo = BandsByGenreProvider.bandsFor(MusicGenre.EMO);
        int idx = emo.indexOf("My Chemical Romance");
        assertTrue(idx >= 0, "curated band should exist in the roster");
        assertTrue(idx < Math.max(1, emo.size() / 2),
                "curated band should rank in the top half of EMO by weight, "
                        + "was index " + idx + " of " + emo.size());
    }

    @Test
    @DisplayName("Weighting favors recent, chart-topping acts over the long tail")
    void testWeightingFavorsRecentTopActs() {
        // Across many weighted draws, picks should concentrate near the top of
        // the (weight-ordered) roster far more than uniform sampling would.
        List<String> rock = BandsByGenreProvider.bandsFor(MusicGenre.ROCK);
        int rosterSize = rock.size();
        int topCount = 0;
        int draws = 2000;
        int topBucket = Math.max(1, rosterSize / 10); // top 10% by weight
        for (int i = 0; i < draws; i++) {
            String picked = BandsByGenreProvider.pickBand(MusicGenre.ROCK);
            if (rock.indexOf(picked) < topBucket) {
                topCount++;
            }
        }
        double uniformShare = (double) topBucket / rosterSize;
        double observedShare = (double) topCount / draws;
        assertTrue(observedShare > uniformShare,
                "weighted picks should favor the top of the roster: observed "
                        + observedShare + " vs uniform " + uniformShare);
    }
}
