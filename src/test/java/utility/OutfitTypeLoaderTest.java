package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OutfitTypeLoader")
class OutfitTypeLoaderTest {

    @Test
    @DisplayName("Loads at least one outfit type from the resource file")
    void testHasOutfitTypes() {
        assertTrue(OutfitTypeLoader.hasOutfitTypes());
        List<String> names = OutfitTypeLoader.getOutfitTypeNames();
        assertFalse(names.isEmpty());
    }

    @Test
    @DisplayName("Parses required and optional layers for shirt_and_pants")
    void testShirtAndPantsRecipe() {
        OutfitTypeLoader.OutfitTypeData recipe =
                OutfitTypeLoader.getOutfitType("shirt_and_pants");

        assertNotNull(recipe);
        assertEquals("shirt_and_pants", recipe.getName());

        List<String> required = recipe.getRequiredLayers();
        assertTrue(required.contains("tops"));
        assertTrue(required.contains("bottoms"));
        assertTrue(required.contains("shoes"));

        List<String> optional = recipe.getOptionalLayers();
        assertTrue(optional.contains("outerwear"));
        assertTrue(optional.contains("accessories"));

        assertTrue(recipe.hasLayer("tops"));
        assertTrue(recipe.hasLayer("accessories"));
        assertFalse(recipe.hasLayer("nonexistent_layer"));
    }

    @Test
    @DisplayName("getMaxForLayer returns the JSON value, defaulting to 1")
    void testGetMaxForLayer() {
        OutfitTypeLoader.OutfitTypeData recipe =
                OutfitTypeLoader.getOutfitType("shirt_and_pants");
        assertNotNull(recipe);

        assertEquals(1, recipe.getMaxForLayer("tops"));
        assertEquals(1, recipe.getMaxForLayer("bottoms"));
        assertEquals(3, recipe.getMaxForLayer("accessories"));
        assertEquals(1, recipe.getMaxForLayer("layer_not_in_recipe"));
    }

    @Test
    @DisplayName("layered_top allows two top items")
    void testLayeredTopAllowsTwoTops() {
        OutfitTypeLoader.OutfitTypeData recipe =
                OutfitTypeLoader.getOutfitType("layered_top");

        assertNotNull(recipe);
        assertEquals(2, recipe.getMaxForLayer("tops"));
    }

    @Test
    @DisplayName("Returns null for an unknown outfit type")
    void testUnknownOutfitType() {
        assertNull(OutfitTypeLoader.getOutfitType("not_a_real_recipe"));
    }
}
