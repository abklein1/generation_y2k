package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NameLoader placeholder first-name filter")
class NameLoaderPlaceholderTest {

    @Test
    @DisplayName("Recognizes census/SSA placeholder names")
    void testRecognizesPlaceholders() {
        assertTrue(NameLoader.isPlaceholderFirstName("Unknown"));
        assertTrue(NameLoader.isPlaceholderFirstName("infant"));
        assertTrue(NameLoader.isPlaceholderFirstName("Baby"));
        assertTrue(NameLoader.isPlaceholderFirstName("BABYGIRL"));
        assertTrue(NameLoader.isPlaceholderFirstName("Babyboy"));
        assertTrue(NameLoader.isPlaceholderFirstName("Unnamed"));
        assertTrue(NameLoader.isPlaceholderFirstName("Male"));
        assertTrue(NameLoader.isPlaceholderFirstName("Female"));
        assertTrue(NameLoader.isPlaceholderFirstName("Boy"));
        assertTrue(NameLoader.isPlaceholderFirstName("Girl"));
        assertTrue(NameLoader.isPlaceholderFirstName("Newborn"));
        assertTrue(NameLoader.isPlaceholderFirstName("Na"));
        assertTrue(NameLoader.isPlaceholderFirstName("Doe"));
        assertTrue(NameLoader.isPlaceholderFirstName("Unkown"));
        assertTrue(NameLoader.isPlaceholderFirstName("Unk"));
        assertTrue(NameLoader.isPlaceholderFirstName("Infantof"));
        assertTrue(NameLoader.isPlaceholderFirstName("Infantboy"));
        assertTrue(NameLoader.isPlaceholderFirstName("Babby"));
    }

    @Test
    @DisplayName("Does not treat ordinary given names as placeholders")
    void testAllowsOrdinaryNames() {
        assertFalse(NameLoader.isPlaceholderFirstName("Alexander"));
        assertFalse(NameLoader.isPlaceholderFirstName("Maria"));
        assertFalse(NameLoader.isPlaceholderFirstName("Boyd"));
        assertFalse(NameLoader.isPlaceholderFirstName("Boyce"));
        assertFalse(NameLoader.isPlaceholderFirstName("Babe"));
        assertFalse(NameLoader.isPlaceholderFirstName(null));
        assertFalse(NameLoader.isPlaceholderFirstName(""));
    }

    @Test
    @DisplayName("Loaded yob data never yields placeholder first names")
    void testGeneratedNamesExcludePlaceholders() {
        // 1989 is pre-cached by school generation and is dense with placeholders
        NameLoader.readCSVFirst("1989");
        GameRandom.reset();
        GameRandom.initialize(42L);

        for (int i = 0; i < 500; i++) {
            String male = NameLoader.nameGenerator("1989", "Male");
            String female = NameLoader.nameGenerator("1989", "Female");
            assertFalse(NameLoader.isPlaceholderFirstName(male),
                    "Generated male name should not be a placeholder: " + male);
            assertFalse(NameLoader.isPlaceholderFirstName(female),
                    "Generated female name should not be a placeholder: " + female);
        }
    }
}
