package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CliqueClothingLoader")
class CliqueClothingLoaderTest {

    // Cliques whose JSON schema is still empty (no clothing inventory
    // populated). Updated as authors fill in additional cliques; pick
    // entries that have never been populated to keep these tests stable.
    private static final String UNPOPULATED_CLIQUE_A = "Goth";
    private static final String UNPOPULATED_CLIQUE_B = "Latino";

    @Test
    @DisplayName("Schema-only entries report no clothing data per clique")
    void testEmptySchemaReportsNoData() {
        assertFalse(CliqueClothingLoader.hasClothingData(UNPOPULATED_CLIQUE_A));
        assertFalse(CliqueClothingLoader.hasClothingData(UNPOPULATED_CLIQUE_A, "Female"));
        assertFalse(CliqueClothingLoader.hasClothingData(UNPOPULATED_CLIQUE_B, "Male"));
    }

    @Test
    @DisplayName("Returns empty lists for unpopulated categories")
    void testEmptyCategoriesReturnEmptyLists() {
        assertTrue(CliqueClothingLoader.getItems(UNPOPULATED_CLIQUE_A, "Female",
                "tops").isEmpty());
        assertTrue(CliqueClothingLoader.getItems(UNPOPULATED_CLIQUE_A, "Female",
                "bottoms").isEmpty());
        assertTrue(CliqueClothingLoader.getColors(UNPOPULATED_CLIQUE_A,
                "Female").isEmpty());
        assertTrue(CliqueClothingLoader.getPatterns(UNPOPULATED_CLIQUE_A,
                "Female").isEmpty());
        assertTrue(CliqueClothingLoader.getMaterials(UNPOPULATED_CLIQUE_A,
                "Female").isEmpty());
    }

    @Test
    @DisplayName("Returns non-null data object for known clique/gender")
    void testReturnsDataObjectForKnownClique() {
        CliqueClothingLoader.CliqueClothingData data =
                CliqueClothingLoader.getData(UNPOPULATED_CLIQUE_A, "Female");

        assertNotNull(data);
        assertNotNull(data.getItemsByCategory());
        assertNotNull(data.getColors());
        assertNotNull(data.getPatterns());
        assertNotNull(data.getMaterials());
        assertFalse(data.hasAnyItems());
    }

    @Test
    @DisplayName("Returns null/empty for an unknown clique")
    void testUnknownClique() {
        assertFalse(CliqueClothingLoader.hasClothingData("NotARealClique"));
        assertTrue(CliqueClothingLoader.getItems("NotARealClique", "Female",
                "tops").isEmpty());
    }

    @Test
    @DisplayName("Gender lookup is case-insensitive")
    void testGenderCaseInsensitive() {
        assertNotNull(CliqueClothingLoader.getData(UNPOPULATED_CLIQUE_A, "FEMALE"));
        assertNotNull(CliqueClothingLoader.getData(UNPOPULATED_CLIQUE_A, "Male"));
        assertNotNull(CliqueClothingLoader.getData(UNPOPULATED_CLIQUE_A, "male"));
    }

    @Test
    @DisplayName("Per-item materials attach only to the garments that list them")
    void testPerItemMaterialsAreScopedToTheGarment() {
        // Emo jeans should carry denim; Emo tops must NOT, so we never
        // produce "denim t-shirt".
        List<CliqueClothingLoader.ClothingOption> bottoms =
                CliqueClothingLoader.getOptions("Emo", "Female", "bottoms");
        assertFalse(bottoms.isEmpty());
        boolean anyDenimBottom = bottoms.stream()
                .anyMatch(o -> o.getMaterials().contains("denim"));
        assertTrue(anyDenimBottom, "Emo jeans should list denim as a material");

        List<CliqueClothingLoader.ClothingOption> tops =
                CliqueClothingLoader.getOptions("Emo", "Female", "tops");
        assertFalse(tops.isEmpty());
        boolean anyTopMaterial = tops.stream()
                .anyMatch(o -> !o.getMaterials().isEmpty());
        assertFalse(anyTopMaterial, "Emo tops must not carry fabric materials");
    }

    @Test
    @DisplayName("Plain-string entries parse into option names with empty descriptors")
    void testPlainStringEntriesParse() {
        List<CliqueClothingLoader.ClothingOption> tops =
                CliqueClothingLoader.getOptions("Emo", "Female", "tops");
        assertFalse(tops.isEmpty());
        CliqueClothingLoader.ClothingOption first = tops.get(0);
        assertNotNull(first.getName());
        assertNotNull(first.getBrands());
        assertNotNull(first.getMaterials());
        assertNotNull(first.getPatterns());
    }

    @Test
    @DisplayName("getItems still returns garment names for populated cliques")
    void testGetItemsReturnsNames() {
        List<String> names =
                CliqueClothingLoader.getItems("Emo", "Female", "bottoms");
        assertFalse(names.isEmpty());
        assertTrue(names.contains("skinny jeans"));
    }

    @Test
    @DisplayName("Populated cliques expose their weighted outfit type list")
    void testOutfitTypeRefsParse() {
        List<CliqueClothingLoader.OutfitTypeRef> refs =
                CliqueClothingLoader.getOutfitTypeRefs("Emo", "Female");
        assertFalse(refs.isEmpty());

        CliqueClothingLoader.OutfitTypeRef layered = refs.stream()
                .filter(r -> "layered_top".equals(r.getName()))
                .findFirst().orElse(null);
        assertNotNull(layered, "Emo should reference the layered_top recipe");
        assertEquals(3, layered.getWeight());

        boolean hasDress = refs.stream()
                .anyMatch(r -> "dress".equals(r.getName()));
        assertFalse(hasDress, "Emo should not reference dress recipes");
    }

    @Test
    @DisplayName("Cliques without an outfit_types list return an empty list")
    void testOutfitTypeRefsEmptyForUnpopulatedClique() {
        assertTrue(CliqueClothingLoader.getOutfitTypeRefs(
                UNPOPULATED_CLIQUE_A, "Female").isEmpty());
        assertTrue(CliqueClothingLoader.getOutfitTypeRefs(
                "NotARealClique", "Male").isEmpty());
    }

    @Test
    @DisplayName("Explicit warmth overrides parse; unset warmth stays null")
    void testWarmthParsing() {
        List<CliqueClothingLoader.ClothingOption> bottoms =
                CliqueClothingLoader.getOptions("Skater", "Female", "bottoms");
        assertFalse(bottoms.isEmpty());

        CliqueClothingLoader.ClothingOption shorts = bottoms.stream()
                .filter(o -> "cargo shorts".equals(o.getName()))
                .findFirst().orElse(null);
        assertNotNull(shorts);
        assertEquals(1, shorts.getWarmth(),
                "cargo shorts should override warmth to 1");

        CliqueClothingLoader.ClothingOption jeans = bottoms.stream()
                .filter(o -> "skinny jeans".equals(o.getName()))
                .findFirst().orElse(null);
        assertNotNull(jeans);
        assertNull(jeans.getWarmth(),
                "garments without a warmth key should report null so the "
                        + "per-category default applies");
    }
}
