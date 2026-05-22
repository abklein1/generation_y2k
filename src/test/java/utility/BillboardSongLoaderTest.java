package utility;

import entity.Radio.Song;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BillboardSongLoader")
class BillboardSongLoaderTest {

    @Test
    @DisplayName("Indexes thousands of chart weeks from the CSV")
    void testHasManyChartWeeks() {
        BillboardSongLoader.ensureLoaded();
        assertTrue(BillboardSongLoader.getChartWeekCount() > 1000,
                "Expected more than 1,000 chart weeks; got "
                        + BillboardSongLoader.getChartWeekCount());
    }

    @Test
    @DisplayName("getChart returns 100 entries for the 7/24/2004 chart week")
    void testKnownChartWeek() {
        LocalDate week = LocalDate.of(2004, 7, 24);
        List<Song> chart = BillboardSongLoader.getChart(week);
        assertEquals(100, chart.size(), "Hot 100 should have 100 rows");
        Song top = chart.get(0);
        assertEquals(1, top.getPosition());
        assertEquals("Confessions Part II", top.getTitle());
        assertEquals("Usher", top.getPerformer());
    }

    @Test
    @DisplayName("Charts are sorted in ascending position order")
    void testChartSortedByPosition() {
        LocalDate week = LocalDate.of(2004, 7, 24);
        List<Song> chart = BillboardSongLoader.getChart(week);
        for (int i = 0; i < chart.size(); i++) {
            assertEquals(i + 1, chart.get(i).getPosition(),
                    "Position mismatch at row " + i);
        }
    }

    @Test
    @DisplayName("Quoted titles with embedded commas are parsed correctly")
    void testQuotedTitleParsing() {
        // From the dataset: "Pisces, Aquarius, Capricorn, And Jones Ltd."
        // appeared as the #1 album in the late 60s; for songs we have
        // "No Chemise, Please" by Gerry Granahan in the 8/4/1958 chart.
        LocalDate week = LocalDate.of(1958, 8, 4);
        List<Song> chart = BillboardSongLoader.getChart(week);
        assertFalse(chart.isEmpty());
        boolean foundQuoted = chart.stream()
                .anyMatch(s -> s.getTitle().equals("No Chemise, Please"));
        assertTrue(foundQuoted,
                "Expected quoted title to round-trip with its embedded comma");
    }

    @Test
    @DisplayName("findChartWeekOnOrBefore picks the nearest earlier Saturday")
    void testFindChartWeekOnOrBefore() {
        // 7/26/2004 is a Monday; previous chart Saturday is 7/24/2004.
        LocalDate query = LocalDate.of(2004, 7, 26);
        LocalDate found = BillboardSongLoader.findChartWeekOnOrBefore(query);
        assertNotNull(found);
        assertTrue(!found.isAfter(query));
        assertEquals(LocalDate.of(2004, 7, 24), found);
    }

    @Test
    @DisplayName("findChartWeekOnOrBefore returns the exact week when matching")
    void testFindChartWeekExactMatch() {
        LocalDate week = LocalDate.of(2004, 7, 24);
        assertEquals(week, BillboardSongLoader.findChartWeekOnOrBefore(week));
    }

    @Test
    @DisplayName("findChartWeekOnOrBefore returns null before the dataset starts")
    void testFindChartWeekBeforeData() {
        LocalDate ancient = LocalDate.of(1800, 1, 1);
        assertNull(BillboardSongLoader.findChartWeekOnOrBefore(ancient));
    }

    @Test
    @DisplayName("getAllChartWeeksBefore is strictly less than the cutoff")
    void testGetAllChartWeeksBefore() {
        LocalDate cutoff = LocalDate.of(1959, 1, 1);
        List<LocalDate> weeks =
                BillboardSongLoader.getAllChartWeeksBefore(cutoff);
        assertFalse(weeks.isEmpty());
        for (LocalDate w : weeks) {
            assertTrue(w.isBefore(cutoff), "Found chart week >= cutoff: " + w);
        }
    }

    @Test
    @DisplayName("CSV line splitter handles quoted commas and unquoted fields")
    void testParseCsvLine() {
        String line = "8/4/1958,36,\"No Chemise, Please\",Gerry Granahan,NA,36,1";
        String[] fields = BillboardSongLoader.parseCsvLine(line);
        assertEquals(7, fields.length);
        assertEquals("8/4/1958", fields[0]);
        assertEquals("36", fields[1]);
        assertEquals("No Chemise, Please", fields[2]);
        assertEquals("Gerry Granahan", fields[3]);
        assertEquals("NA", fields[4]);
        assertEquals("36", fields[5]);
        assertEquals("1", fields[6]);
    }
}
