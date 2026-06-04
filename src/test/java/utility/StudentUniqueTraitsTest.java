package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.traits.TraitDataset;
import utility.traits.TraitDatasetLoader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Student unique traits")
class StudentUniqueTraitsTest {

    @Test
    @DisplayName("Routes known genders to separate trait pools")
    void testGenderSpecificTraitPools() {
        String femalePath = StudentPopGenerator.uniqueTraitsPathForGender("Female");
        String malePath = StudentPopGenerator.uniqueTraitsPathForGender("male");

        assertNotEquals(femalePath, malePath);
        assertTrue(femalePath.endsWith("unique_traits_female.json"));
        assertTrue(malePath.endsWith("unique_traits_male.json"));
    }

    @Test
    @DisplayName("Falls back to the shared trait pool for unknown gender")
    void testUnknownGenderUsesDefaultTraitPool() {
        String defaultPath = StudentPopGenerator.uniqueTraitsPathForGender(null);
        String unknownPath = StudentPopGenerator.uniqueTraitsPathForGender("unknown");

        assertTrue(defaultPath.endsWith("unique_traits.json"));
        assertTrue(unknownPath.endsWith("unique_traits.json"));
    }

    @Test
    @DisplayName("Gender-specific trait pools load with selectable data")
    void testGenderSpecificPoolsLoad() {
        TraitDatasetLoader.resetCache();

        TraitDataset femaleDataset = TraitDatasetLoader.load(
                StudentPopGenerator.uniqueTraitsPathForGender("female"));
        TraitDataset maleDataset = TraitDatasetLoader.load(
                StudentPopGenerator.uniqueTraitsPathForGender("male"));

        assertFalse(femaleDataset.getCategories().isEmpty());
        assertFalse(maleDataset.getCategories().isEmpty());
        assertFalse(femaleDataset.getSubcategories().isEmpty());
        assertFalse(maleDataset.getSubcategories().isEmpty());
        assertFalse(femaleDataset.getTraits("positive", "face").isEmpty());
        assertFalse(maleDataset.getTraits("positive", "face").isEmpty());
    }
}
