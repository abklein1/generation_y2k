package utility;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocationSelector")
class LocationSelectorTest {

    @BeforeAll
    static void seedRandom() {
        GameRandom.initialize(123L);
    }

    @Test
    @DisplayName("Mountain keywords pick a mountainous region")
    void testMountainKeywords() {
        String picked = LocationSelector.pick("Forest Hill High");
        Set<String> allowed = Set.of(LocationSelector.ALASKA,
                LocationSelector.SPOKANE, LocationSelector.NEW_YORK);
        assertTrue(allowed.contains(picked));
    }

    @Test
    @DisplayName("Coastal keywords pick a coastal region")
    void testCoastalKeywords() {
        String picked = LocationSelector.pick("Ocean Bay High");
        Set<String> allowed = Set.of(LocationSelector.ALASKA,
                LocationSelector.SPOKANE, LocationSelector.LOS_ANGELES,
                LocationSelector.WEST_PALM, LocationSelector.NEW_YORK);
        assertTrue(allowed.contains(picked));
    }

    @Test
    @DisplayName("Prairie keywords pick a plains region")
    void testPrairieKeywords() {
        String picked = LocationSelector.pick("Prairie Valley High");
        Set<String> allowed = Set.of(LocationSelector.AUSTIN,
                LocationSelector.KANSAS);
        assertTrue(allowed.contains(picked));
    }

    @Test
    @DisplayName("Desert keywords pick a hot dry region")
    void testDesertKeywords() {
        String picked = LocationSelector.pick("Desert Canyon High");
        Set<String> allowed = Set.of(LocationSelector.PHOENIX,
                LocationSelector.LOS_ANGELES);
        assertTrue(allowed.contains(picked));
    }

    @Test
    @DisplayName("Default school name picks any of the nine regions")
    void testDefaultPick() {
        String picked = LocationSelector.pick("Generic High");
        Set<String> allowed = Set.of(LocationSelector.ALASKA,
                LocationSelector.AUSTIN, LocationSelector.KANSAS,
                LocationSelector.LOS_ANGELES, LocationSelector.MACON,
                LocationSelector.NEW_YORK, LocationSelector.PHOENIX,
                LocationSelector.SPOKANE, LocationSelector.WEST_PALM);
        assertTrue(allowed.contains(picked));
    }

    @Test
    @DisplayName("East-of-Mississippi regions are correctly classified")
    void testEastClassification() {
        for (String region : List.of(LocationSelector.CHICAGO,
                LocationSelector.MACON, LocationSelector.NEW_YORK,
                LocationSelector.WEST_PALM)) {
            assertTrue(LocationSelector.isEastOfMississippi(region),
                    region + " should be east of the Mississippi");
        }
    }

    @Test
    @DisplayName("West-of-Mississippi regions are correctly classified")
    void testWestClassification() {
        for (String region : List.of(LocationSelector.ALASKA,
                LocationSelector.AUSTIN, LocationSelector.KANSAS,
                LocationSelector.LOS_ANGELES, LocationSelector.PHOENIX,
                LocationSelector.SPOKANE)) {
            assertFalse(LocationSelector.isEastOfMississippi(region),
                    region + " should be west of the Mississippi");
        }
    }

    @Test
    @DisplayName("Unknown region throws IllegalArgumentException")
    void testUnknownRegion() {
        assertThrows(IllegalArgumentException.class,
                () -> LocationSelector.isEastOfMississippi("atlantis"));
    }

    @Test
    @DisplayName("All ten regions partition cleanly into east/west sets")
    void testNoOverlap() {
        Set<String> regions = new HashSet<>();
        regions.add(LocationSelector.ALASKA);
        regions.add(LocationSelector.AUSTIN);
        regions.add(LocationSelector.CHICAGO);
        regions.add(LocationSelector.KANSAS);
        regions.add(LocationSelector.LOS_ANGELES);
        regions.add(LocationSelector.MACON);
        regions.add(LocationSelector.NEW_YORK);
        regions.add(LocationSelector.PHOENIX);
        regions.add(LocationSelector.SPOKANE);
        regions.add(LocationSelector.WEST_PALM);
        assertEquals(10, regions.size());
        for (String region : regions) {
            // Should not throw for any known region.
            LocationSelector.isEastOfMississippi(region);
        }
    }
}
