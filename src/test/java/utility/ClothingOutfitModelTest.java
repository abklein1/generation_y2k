package utility;

import entity.Items.ClothingItem;
import entity.Items.Outfit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClothingItem and Outfit models")
class ClothingOutfitModelTest {

    @Test
    @DisplayName("ClothingItem display name prepends color/pattern/material")
    void testDisplayNamePrependsQualifiers() {
        ClothingItem flannel = new ClothingItem("flannel shirt", "tops", "tops",
                "upper torso", "cotton", "blue", "plaid");

        assertEquals("blue plaid cotton flannel shirt", flannel.getDisplayName());
    }

    @Test
    @DisplayName("ClothingItem display name skips qualifiers already in the name")
    void testDisplayNameSkipsRedundantQualifiers() {
        ClothingItem tee = new ClothingItem("black band t-shirt", "tops", "tops",
                "upper torso", null, "black", null);

        assertEquals("black band t-shirt", tee.getDisplayName());
    }

    @Test
    @DisplayName("ClothingItem display name handles all-null qualifiers")
    void testDisplayNameAllNull() {
        ClothingItem jeans = new ClothingItem("jeans", "bottoms", "bottoms",
                "lower torso", null, null, null);

        assertEquals("jeans", jeans.getDisplayName());
    }

    @Test
    @DisplayName("ClothingItem display name works when only color is set")
    void testDisplayNameColorOnly() {
        // Mirrors the common case where a clique defines a color
        // palette but leaves patterns and materials empty (e.g. a plain
        // black t-shirt with no fabric callout in the prose).
        ClothingItem tee = new ClothingItem("MCR t-shirt", "tops", "tops",
                "upper torso", null, "black", null);
        ClothingItem jeans = new ClothingItem("skinny jeans", "bottoms",
                "bottoms", "lower torso", null, "navy", null);

        assertEquals("black MCR t-shirt", tee.getDisplayName());
        assertEquals("navy skinny jeans", jeans.getDisplayName());
    }

    @Test
    @DisplayName("ClothingItem display name treats empty qualifiers like null")
    void testDisplayNameBlankQualifiersSkipped() {
        ClothingItem tee = new ClothingItem("t-shirt", "tops", "tops",
                "upper torso", "", "blue", "");

        assertEquals("blue t-shirt", tee.getDisplayName());
    }

    @Test
    @DisplayName("Outfit adds items in order and exposes them via getItems")
    void testOutfitPreservesOrder() {
        Outfit outfit = new Outfit("layered_top");
        ClothingItem tee = new ClothingItem("t-shirt", "tops", "tops",
                "upper torso", null, "white", null);
        ClothingItem flannel = new ClothingItem("flannel", "tops", "tops",
                "upper torso", "cotton", "red", "plaid");
        ClothingItem jeans = new ClothingItem("jeans", "bottoms", "bottoms",
                "lower torso", "denim", null, null);

        outfit.addItem(tee);
        outfit.addItem(flannel);
        outfit.addItem(jeans);

        assertEquals(3, outfit.size());
        assertEquals(List.of(tee, flannel, jeans), outfit.getItems());
    }

    @Test
    @DisplayName("Outfit groups items by layer, preserving insertion order")
    void testGetItemsByLayerAndGrouping() {
        Outfit outfit = new Outfit("jacket_outfit");
        ClothingItem tee = new ClothingItem("t-shirt", "tops", "tops",
                "upper torso", null, "white", null);
        ClothingItem jacket = new ClothingItem("denim jacket", "outerwear",
                "outerwear", "upper torso", "denim", "blue", null);
        ClothingItem jeans = new ClothingItem("jeans", "bottoms", "bottoms",
                "lower torso", "denim", null, null);

        outfit.addItem(jacket);
        outfit.addItem(tee);
        outfit.addItem(jeans);

        assertEquals(List.of(tee), outfit.getItemsByLayer("tops"));
        assertEquals(List.of(jacket), outfit.getItemsByLayer("outerwear"));

        Map<String, List<ClothingItem>> grouped = outfit.getItemsGroupedByLayer();
        assertEquals(3, grouped.size());
        assertTrue(grouped.containsKey("outerwear"));
        assertTrue(grouped.containsKey("tops"));
        assertTrue(grouped.containsKey("bottoms"));
    }

    @Test
    @DisplayName("Outfit description joins display names with commas")
    void testGetDescription() {
        Outfit outfit = new Outfit("shirt_and_pants");
        outfit.addItem(new ClothingItem("t-shirt", "tops", "tops",
                "upper torso", null, "black", null));
        outfit.addItem(new ClothingItem("jeans", "bottoms", "bottoms",
                "lower torso", "denim", null, null));
        outfit.addItem(new ClothingItem("sneakers", "shoes", "shoes",
                "feet", null, "white", null));

        assertEquals("black t-shirt, denim jeans, white sneakers",
                outfit.getDescription());
    }

    @Test
    @DisplayName("Empty Outfit reports empty and produces empty description")
    void testEmptyOutfit() {
        Outfit outfit = new Outfit();

        assertNotNull(outfit.getItems());
        assertTrue(outfit.isEmpty());
        assertEquals(0, outfit.size());
        assertEquals("", outfit.getDescription());
    }

    @Test
    @DisplayName("getItemsByLayer is case-insensitive and tolerates null")
    void testGetItemsByLayerCaseInsensitive() {
        Outfit outfit = new Outfit();
        outfit.addItem(new ClothingItem("t-shirt", "tops", "tops",
                "upper torso", null, null, null));

        assertEquals(1, outfit.getItemsByLayer("TOPS").size());
        assertTrue(outfit.getItemsByLayer("bottoms").isEmpty());
        assertTrue(outfit.getItemsByLayer(null).isEmpty());
    }
}
