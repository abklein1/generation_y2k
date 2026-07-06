package utility;

import constants.SimConstants;
import entity.Items.ClothingItem;
import entity.Items.Outfit;
import entity.Items.Wardrobe;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DailyOutfitAssigner")
class DailyOutfitAssignerTest {

    private static Student emoStudentWithWardrobe() {
        Student student = new Student();
        student.studentStatistics.setMainClique("Emo");
        student.studentStatistics.setGender("Female");
        StudentPopGenerator.applyClothingAttributes(student);
        assertNotNull(student.studentStatistics.getWardrobe());
        return student;
    }

    private static ClothingItem item(String name, String layer, int warmth) {
        return new ClothingItem(name, layer, layer, "upper torso",
                null, null, null, null, warmth);
    }

    private static Outfit outfitOfWarmth(String name, int warmth) {
        Outfit outfit = new Outfit("shirt_and_pants");
        outfit.addItem(item(name, "tops", warmth));
        return outfit;
    }

    @Test
    @DisplayName("Temperature bands map to the expected ideal warmth")
    void testIdealWarmthForTemps() {
        assertEquals(SimConstants.CLOTHING_IDEAL_WARMTH_COLD,
                DailyOutfitAssigner.idealWarmthForTemps(35, 25));
        assertEquals(SimConstants.CLOTHING_IDEAL_WARMTH_COOL,
                DailyOutfitAssigner.idealWarmthForTemps(55, 45));
        assertEquals(SimConstants.CLOTHING_IDEAL_WARMTH_MILD,
                DailyOutfitAssigner.idealWarmthForTemps(70, 60));
        assertEquals(SimConstants.CLOTHING_IDEAL_WARMTH_WARM,
                DailyOutfitAssigner.idealWarmthForTemps(80, 70));
        assertEquals(SimConstants.CLOTHING_IDEAL_WARMTH_HOT,
                DailyOutfitAssigner.idealWarmthForTemps(100, 80));
    }

    @Test
    @DisplayName("Week 1 consumes each pre-generated outfit exactly once")
    void testFirstWeekConsumesWardrobeSequence() {
        Student student = emoStudentWithWardrobe();
        Wardrobe wardrobe = student.studentStatistics.getWardrobe();
        int size = wardrobe.size();
        assertEquals(1, wardrobe.getNextUnwornIndex());

        for (int day = 2; day <= size; day++) {
            DailyOutfitAssigner.assignOutfitForDay(student,
                    SimConstants.CLOTHING_IDEAL_WARMTH_MILD);
            assertEquals(day, wardrobe.getNextUnwornIndex());
            // The worn outfit must be one of the pre-generated ones.
            assertTrue(wardrobe.getOutfits().contains(
                    student.studentStatistics.getCurrentOutfit()));
        }
        assertFalse(wardrobe.hasUnwornOutfits(),
                "All outfits should be worn after the first week");
    }

    @Test
    @DisplayName("Unworn outfit with the closest warmth is worn first")
    void testBestFitSelection() {
        Wardrobe wardrobe = new Wardrobe();
        wardrobe.addOutfit(outfitOfWarmth("heavy sweater", 7));
        wardrobe.addOutfit(outfitOfWarmth("tank top", 1));
        wardrobe.addOutfit(outfitOfWarmth("t-shirt", 3));

        Student student = new Student();
        student.studentStatistics.setWardrobe(wardrobe);

        DailyOutfitAssigner.assignOutfitForDay(student, 1);
        assertEquals(1,
                student.studentStatistics.getCurrentOutfit().getTotalWarmth(),
                "Hot day should pick the lightest unworn outfit");

        DailyOutfitAssigner.assignOutfitForDay(student, 7);
        assertEquals(7,
                student.studentStatistics.getCurrentOutfit().getTotalWarmth(),
                "Cold day should pick the warmest remaining outfit");
    }

    @Test
    @DisplayName("After week 1, outfits are recombined only from owned pieces")
    void testRecombinationUsesOnlyOwnedItems() {
        Student student = emoStudentWithWardrobe();
        Wardrobe wardrobe = student.studentStatistics.getWardrobe();
        while (wardrobe.hasUnwornOutfits()) {
            wardrobe.wearNext();
        }

        List<ClothingItem> owned = wardrobe.getAllItems();
        for (int day = 0; day < 20; day++) {
            DailyOutfitAssigner.assignOutfitForDay(student,
                    SimConstants.CLOTHING_IDEAL_WARMTH_MILD);
            Outfit worn = student.studentStatistics.getCurrentOutfit();
            assertFalse(worn.isEmpty(),
                    "Recombination should always dress the student");
            for (ClothingItem item : worn.getItems()) {
                assertTrue(owned.contains(item),
                        "Recombined outfit used an unowned item: "
                                + item.getDisplayName());
            }
        }
    }

    @Test
    @DisplayName("Recombination biases total warmth toward the day's ideal")
    void testRecombinationBiasesTowardIdealWarmth() {
        Student student = emoStudentWithWardrobe();
        Wardrobe wardrobe = student.studentStatistics.getWardrobe();
        while (wardrobe.hasUnwornOutfits()) {
            wardrobe.wearNext();
        }

        double hotTotal = 0;
        double coldTotal = 0;
        int runs = 50;
        for (int i = 0; i < runs; i++) {
            DailyOutfitAssigner.assignOutfitForDay(student,
                    SimConstants.CLOTHING_IDEAL_WARMTH_HOT);
            hotTotal += student.studentStatistics.getCurrentOutfit()
                    .getTotalWarmth();
            DailyOutfitAssigner.assignOutfitForDay(student,
                    SimConstants.CLOTHING_IDEAL_WARMTH_COLD);
            coldTotal += student.studentStatistics.getCurrentOutfit()
                    .getTotalWarmth();
        }
        assertTrue(coldTotal / runs > hotTotal / runs,
                "Cold days should average warmer outfits than hot days");
    }

    @Test
    @DisplayName("Students without a wardrobe keep their current outfit")
    void testNoWardrobeIsANoOp() {
        Student student = new Student();
        Outfit before = student.studentStatistics.getCurrentOutfit();

        DailyOutfitAssigner.assignOutfitForDay(student,
                SimConstants.CLOTHING_IDEAL_WARMTH_MILD);

        assertEquals(before, student.studentStatistics.getCurrentOutfit());
    }
}
