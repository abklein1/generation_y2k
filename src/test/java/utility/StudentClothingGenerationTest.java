package utility;

import entity.Items.ClothingItem;
import entity.Items.Outfit;
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
                if (!"bottoms".equals(item.getLayer())) {
                    assertTrue(item.getMaterial() == null
                                    || item.getMaterial().isBlank(),
                            "Non-bottom garment should carry no fabric: "
                                    + item.getDisplayName());
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
