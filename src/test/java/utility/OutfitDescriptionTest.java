package utility;

import entity.Items.ClothingItem;
import entity.Items.Outfit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Inspector outfit description")
class OutfitDescriptionTest {

    @Test
    @DisplayName("Returns null for a null or empty outfit")
    void testNullAndEmptyReturnNull() throws Exception {
        assertNull(invokeBuildOutfitDescription(null));
        assertNull(invokeBuildOutfitDescription(new Outfit()));
    }

    @Test
    @DisplayName("Lists a single garment as a singular sentence")
    void testSingleItem() throws Exception {
        Outfit outfit = new Outfit();
        // Material "denim" already appears in the item name, so
        // ClothingItem.getDisplayName() will not duplicate it.
        outfit.addItem(new ClothingItem("denim jacket", "outerwear", "outerwear",
                "upper torso", "denim", "blue", null));

        String description = invokeBuildOutfitDescription(outfit);

        assertEquals("They are wearing a blue denim jacket.", description);
    }

    @Test
    @DisplayName("Joins multiple garments with comma + 'and' and bottom-to-top order")
    void testMultipleItemsOrderedAndJoined() throws Exception {
        Outfit outfit = new Outfit("jacket_outfit");
        // Add in a deliberately mixed order; description should reorder
        // them to one_piece -> tops -> bottoms -> outerwear -> shoes ->
        // accessories.
        outfit.addItem(new ClothingItem("sneakers", "shoes", "shoes",
                "feet", null, "white", null));
        outfit.addItem(new ClothingItem("denim jacket", "outerwear",
                "outerwear", "upper torso", "denim", null, null));
        outfit.addItem(new ClothingItem("band t-shirt", "tops", "tops",
                "upper torso", null, "black", null));
        outfit.addItem(new ClothingItem("jeans", "bottoms", "bottoms",
                "lower torso", "denim", null, null));

        String description = invokeBuildOutfitDescription(outfit);

        assertTrue(description.startsWith("They are wearing "),
                "Description should start with 'They are wearing '");
        assertTrue(description.endsWith("."),
                "Description should end with a period");
        // Tops appears before bottoms appears before outerwear appears
        // before shoes in our canonical layer order. ClothingItem
        // suppresses duplicate qualifiers, so "denim jeans" /
        // "denim jacket" don't repeat the material.
        int topIdx = description.indexOf("black band t-shirt");
        int bottomIdx = description.indexOf("denim jeans");
        int outerIdx = description.indexOf("denim jacket");
        int shoeIdx = description.indexOf("white sneakers");
        assertTrue(topIdx > 0 && topIdx < bottomIdx,
                "tops should precede bottoms in prose");
        assertTrue(bottomIdx < outerIdx,
                "bottoms should precede outerwear in prose");
        assertTrue(outerIdx < shoeIdx,
                "outerwear should precede shoes in prose");
        assertTrue(description.contains(", and "),
                "Multi-item list should use ', and ' as final separator");
    }

    @Test
    @DisplayName("Uses 'an' article for vowel-starting first garments")
    void testVowelArticle() throws Exception {
        Outfit outfit = new Outfit();
        outfit.addItem(new ClothingItem("emerald dress", "one_piece", "one_piece",
                "full body", null, null, null));

        String description = invokeBuildOutfitDescription(outfit);

        assertEquals("They are wearing an emerald dress.", description);
    }

    private static String invokeBuildOutfitDescription(Outfit outfit)
            throws Exception {
        Method method = Inspector.class.getDeclaredMethod(
                "buildOutfitDescription", Outfit.class);
        method.setAccessible(true);
        return (String) method.invoke(null, outfit);
    }
}
