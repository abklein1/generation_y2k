package utility;

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

@DisplayName("StudentPopGenerator clothing application")
class StudentClothingGenerationTest {

    @Test
    @DisplayName("New students start with a non-null empty outfit")
    void testDefaultOutfitIsEmpty() {
        Student student = new Student();

        Outfit outfit = student.studentStatistics.getCurrentOutfit();
        assertNotNull(outfit);
        assertTrue(outfit.isEmpty());
        assertEquals("", outfit.getDescription());
    }

    // A clique whose JSON schema is still empty. Update if this clique
    // gets populated; keep generation tests targeting an empty entry.
    private static final String UNPOPULATED_CLIQUE = "Goth";

    @Test
    @DisplayName("applyClothingAttributes is a no-op when the clique has no inventory")
    void testApplyClothingNoCliqueDataLeavesEmptyOutfit() {
        Student student = new Student();
        student.studentStatistics.setMainClique(UNPOPULATED_CLIQUE);
        student.studentStatistics.setGender("Female");

        Outfit before = student.studentStatistics.getCurrentOutfit();
        StudentPopGenerator.applyClothingAttributes(student);
        Outfit after = student.studentStatistics.getCurrentOutfit();

        assertNotNull(after);
        assertTrue(after.isEmpty(),
                "Outfit should remain empty when clique inventory is empty");
        // Reference equality is fine here: nothing should have replaced it.
        assertEquals(before, after);
    }

    @Test
    @DisplayName("applyClothingAttributes is safe with null clique or gender")
    void testApplyClothingNullCliqueOrGender() {
        Student noClique = new Student();
        noClique.studentStatistics.setGender("Female");
        StudentPopGenerator.applyClothingAttributes(noClique);
        assertTrue(noClique.studentStatistics.getCurrentOutfit().isEmpty());

        Student noGender = new Student();
        noGender.studentStatistics.setMainClique(UNPOPULATED_CLIQUE);
        StudentPopGenerator.applyClothingAttributes(noGender);
        assertTrue(noGender.studentStatistics.getCurrentOutfit().isEmpty());
    }

    @Test
    @DisplayName("setCurrentOutfit(null) coerces to a non-null empty outfit")
    void testSetCurrentOutfitNullCoercion() {
        Student student = new Student();

        student.studentStatistics.setCurrentOutfit(null);

        Outfit outfit = student.studentStatistics.getCurrentOutfit();
        assertNotNull(outfit);
        assertTrue(outfit.isEmpty());
    }

    @Test
    @DisplayName("Materials never leak onto the wrong garment (no 'denim t-shirt')")
    void testMaterialsStayPerItem() {
        // Run many outfits so the randomized generator has plenty of
        // chances to (incorrectly) attach denim to a top or accessory.
        for (int i = 0; i < 200; i++) {
            Student student = new Student();
            student.studentStatistics.setMainClique("Emo");
            student.studentStatistics.setGender("Female");

            StudentPopGenerator.applyClothingAttributes(student);
            Outfit outfit = student.studentStatistics.getCurrentOutfit();

            for (ClothingItem item : outfit.getItems()) {
                // Other layers (outerwear, accessories) legitimately list
                // their own per-item materials; Emo tops define none, so
                // any material on a top means descriptors leaked across
                // garments.
                if ("tops".equals(item.getLayer())) {
                    assertTrue(item.getMaterial() == null
                                    || item.getMaterial().isBlank(),
                            "Emo top should carry no fabric: "
                                    + item.getDisplayName());
                }
            }
        }
    }

    @Test
    @DisplayName("Recipe picks are restricted to the clique's outfit_types list")
    void testRecipePicksRespectCliqueList() {
        // Emo references shirt_and_pants, layered_top, and jacket_outfit;
        // dress recipes must never appear even across many rolls.
        for (int i = 0; i < 100; i++) {
            Student student = new Student();
            student.studentStatistics.setMainClique("Emo");
            student.studentStatistics.setGender("Female");

            StudentPopGenerator.applyClothingAttributes(student);
            Outfit outfit = student.studentStatistics.getCurrentOutfit();
            assertFalse(outfit.isEmpty());

            String recipe = outfit.getOutfitType();
            assertTrue("shirt_and_pants".equals(recipe)
                            || "layered_top".equals(recipe)
                            || "jacket_outfit".equals(recipe),
                    "Emo outfit used a recipe outside its list: " + recipe);
        }
    }

    @Test
    @DisplayName("Generation pre-fills a 7-outfit wardrobe and wears the first")
    void testWardrobeGeneration() {
        Student student = new Student();
        student.studentStatistics.setMainClique("Emo");
        student.studentStatistics.setGender("Female");

        StudentPopGenerator.applyClothingAttributes(student);

        Wardrobe wardrobe = student.studentStatistics.getWardrobe();
        assertNotNull(wardrobe, "Populated cliques should get a wardrobe");
        assertEquals(7, wardrobe.size());
        assertEquals(1, wardrobe.getNextUnwornIndex(),
                "Exactly the first outfit should be marked worn on day 1");

        // Reference equality intended: day 1 wears wardrobe outfit #1.
        assertTrue(wardrobe.getOutfits().get(0)
                        == student.studentStatistics.getCurrentOutfit(),
                "Day-1 outfit should be the first wardrobe outfit");
    }

    @Test
    @DisplayName("No wardrobe is created when the clique has no inventory")
    void testNoWardrobeForUnpopulatedClique() {
        Student student = new Student();
        student.studentStatistics.setMainClique(UNPOPULATED_CLIQUE);
        student.studentStatistics.setGender("Female");

        StudentPopGenerator.applyClothingAttributes(student);

        assertTrue(student.studentStatistics.getWardrobe() == null);
    }

    @Test
    @DisplayName("Generated garments carry category-default or overridden warmth")
    void testGeneratedItemsCarryWarmth() {
        for (int i = 0; i < 50; i++) {
            Student student = new Student();
            student.studentStatistics.setMainClique("Emo");
            student.studentStatistics.setGender("Female");

            StudentPopGenerator.applyClothingAttributes(student);
            Outfit outfit = student.studentStatistics.getCurrentOutfit();
            assertFalse(outfit.isEmpty());
            assertTrue(outfit.getTotalWarmth() > 0,
                    "A full outfit should have positive total warmth");

            for (ClothingItem item : outfit.getItems()) {
                switch (item.getLayer()) {
                    case "bottoms" -> assertEquals(2, item.getWarmth());
                    case "outerwear" -> assertEquals(3, item.getWarmth());
                    case "tops" -> assertTrue(item.getWarmth() == 1
                                    || item.getWarmth() == 2,
                            "Tops are warmth 2 by default, 1 for tank tops");
                    default -> assertEquals(0, item.getWarmth());
                }
            }
        }
    }

    @Test
    @DisplayName("Emo outfits are colored entirely from the dark scheme")
    void testEmoOutfitStaysDark() {
        List<String> dark = ColorSchemeLoader.getSchemeColors("dark");
        assertFalse(dark.isEmpty());

        for (int i = 0; i < 200; i++) {
            Student student = new Student();
            student.studentStatistics.setMainClique("Emo");
            student.studentStatistics.setGender("Female");

            StudentPopGenerator.applyClothingAttributes(student);
            Outfit outfit = student.studentStatistics.getCurrentOutfit();

            for (ClothingItem item : outfit.getItems()) {
                String color = item.getColor();
                if (color != null && !color.isBlank()) {
                    assertTrue(dark.contains(color),
                            "Emo garment color should come from the dark "
                                    + "scheme but was: " + color);
                }
            }
        }
    }
}
