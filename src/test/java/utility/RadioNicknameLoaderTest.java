package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RadioNicknameLoader")
class RadioNicknameLoaderTest {

    @Test
    @DisplayName("Loads a non-empty list of nicknames from the resource file")
    void testLoadsNicknames() {
        List<String> names = RadioNicknameLoader.getNicknames();
        assertNotNull(names);
        assertFalse(names.isEmpty(),
                "station_nicknames.json should contribute at least one entry");
    }

    @Test
    @DisplayName("All loaded nicknames are unique")
    void testNoDuplicates() {
        List<String> names = RadioNicknameLoader.getNicknames();
        Set<String> dedup = new HashSet<>(names);
        assertEquals(names.size(), dedup.size(),
                "Duplicate nicknames found in station_nicknames.json: " + names);
    }

    @Test
    @DisplayName("getNicknames returns an unmodifiable view")
    void testUnmodifiable() {
        List<String> names = RadioNicknameLoader.getNicknames();
        assertThrows(UnsupportedOperationException.class,
                () -> names.add("Should Fail"));
    }

    @Test
    @DisplayName("pickRandomNickname avoids names already used")
    void testPickAvoidsUsed() {
        List<String> all = RadioNicknameLoader.getNicknames();
        Set<String> used = new HashSet<>(all.subList(0, all.size() - 1));
        String picked = RadioNicknameLoader.pickRandomNickname(new Random(42L), used);
        assertNotNull(picked);
        assertFalse(used.contains(picked));
    }

    @Test
    @DisplayName("pickRandomNickname returns null when every nickname is taken")
    void testPickAllUsed() {
        Set<String> used = new HashSet<>(RadioNicknameLoader.getNicknames());
        assertNull(RadioNicknameLoader.pickRandomNickname(new Random(0L), used));
    }

    @Test
    @DisplayName("pickRandomNickname is deterministic for the same seed/used set")
    void testDeterministic() {
        Set<String> used = new HashSet<>();
        String first = RadioNicknameLoader.pickRandomNickname(new Random(7L), used);
        String again = RadioNicknameLoader.pickRandomNickname(new Random(7L), used);
        assertEquals(first, again);
        assertTrue(RadioNicknameLoader.getNicknames().contains(first));
    }
}
