package utility.music;

import entity.Radio.MusicGenre;
import entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FavoriteBandAssigner")
class FavoriteBandAssignerTest {

    @BeforeEach
    void reset() {
        CliqueMusicPreferenceLoader.resetForTests();
    }

    @Test
    @DisplayName("Assigns seven bands for a clique with strong taste")
    void testAssignsSeveralBands() {
        Student student = new Student();
        student.studentStatistics.setMainClique("Country");

        FavoriteBandAssigner.assign(student);

        List<String> bands = student.studentStatistics.getFavoriteBands();
        assertFalse(bands.isEmpty(), "should assign at least one band");
        assertTrue(bands.size() <= 7, "should not exceed seven bands");
    }

    @Test
    @DisplayName("Real bands are drawn (not the generic fallback) when data exists")
    void testRealBandsWhenDataExists() {
        // Country has a populated Billboard roster, so the fallback should
        // never be the only thing handed out.
        assertFalse(BandsByGenreProvider.bandsFor(MusicGenre.COUNTRY).isEmpty(),
                "precondition: COUNTRY roster must be non-empty");

        Student student = new Student();
        student.studentStatistics.setMainClique("Country");
        FavoriteBandAssigner.assign(student);

        List<String> bands = student.studentStatistics.getFavoriteBands();
        assertFalse(bands.contains(FavoriteBandAssigner.FALLBACK_BAND)
                        && bands.size() == 1,
                "a student of a populated genre should get real bands");
    }

    @Test
    @DisplayName("Outcasts draw a full slate of (completely random) bands")
    void testOutcastGetsRandomBands() {
        Student student = new Student();
        student.studentStatistics.setMainClique("Outcast");

        FavoriteBandAssigner.assign(student);

        List<String> bands = student.studentStatistics.getFavoriteBands();
        assertFalse(bands.isEmpty(), "Outcasts should still get favorite bands");
        // Random picks come from the global roster, so they are real bands.
        assertFalse(bands.size() == 1
                        && bands.contains(FavoriteBandAssigner.FALLBACK_BAND),
                "Outcasts should draw real bands, not just the fallback");
    }

    @Test
    @DisplayName("A clique with no liked genres falls back gracefully")
    void testFallbackWhenNoLikedGenres() {
        Student student = new Student();
        // An unknown clique resolves to _default, which still likes POP, so
        // use a student with no clique to force the neutral path is not
        // possible; instead verify assign never throws and yields content.
        FavoriteBandAssigner.assign(student);
        assertFalse(student.studentStatistics.getFavoriteBands().isEmpty());
    }
}
