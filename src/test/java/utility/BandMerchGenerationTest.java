package utility;

import entity.Items.ClothingItem;
import entity.Items.Outfit;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Band merch generation from favorite bands")
class BandMerchGenerationTest {

    private static final String SENTINEL_BAND = "Sentinel Test Band";

    /**
     * Emo has a populated wardrobe whose top/outerwear lists use the
     * {@code {band}} token, so generating many Emo outfits should both
     * substitute the favorite band and never leak the raw placeholder.
     */
    @Test
    @DisplayName("{band} token is replaced by a favorite band and never leaks")
    void testBandTokenSubstituted() {
        boolean sawSentinel = false;

        for (int i = 0; i < 80; i++) {
            Student student = new Student();
            student.studentStatistics.setMainClique("Emo");
            student.studentStatistics.setGender("Female");
            student.studentStatistics.setFavoriteBands(List.of(SENTINEL_BAND));

            StudentPopGenerator.applyClothingAttributes(student);
            Outfit outfit = student.studentStatistics.getCurrentOutfit();

            for (ClothingItem item : outfit.getItems()) {
                String name = item.getName();
                assertFalse(name.contains("{band}"),
                        "raw placeholder leaked into outfit: " + name);
                if (name.contains(SENTINEL_BAND)) {
                    sawSentinel = true;
                }
            }
        }

        assertTrue(sawSentinel,
                "favorite band should appear on band merch across many outfits");
    }

    @Test
    @DisplayName("Missing favorite bands fall back to a generic band, not a raw token")
    void testFallbackWhenNoBands() {
        for (int i = 0; i < 40; i++) {
            Student student = new Student();
            student.studentStatistics.setMainClique("Emo");
            student.studentStatistics.setGender("Male");
            // No favorite bands assigned.

            StudentPopGenerator.applyClothingAttributes(student);
            for (ClothingItem item : student.studentStatistics
                    .getCurrentOutfit().getItems()) {
                assertFalse(item.getName().contains("{band}"),
                        "raw placeholder leaked with no favorite bands: "
                                + item.getName());
            }
        }
    }
}
