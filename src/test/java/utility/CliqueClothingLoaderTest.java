package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
