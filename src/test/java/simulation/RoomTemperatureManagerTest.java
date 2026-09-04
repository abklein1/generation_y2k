package simulation;

import constants.SimConstants;
import entity.Rooms.Classroom;
import entity.Rooms.Courtyard;
import entity.Rooms.Portable;
import entity.Rooms.Room;
import entity.Rooms.UtilityRoom;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RoomTemperatureManager}: indoor rooms must stay
 * within a sane band of the comfort setpoint regardless of how far they
 * sit from a utility room, portables get their own weak conditioning,
 * and outdoor spaces simply track the weather.
 */
public class RoomTemperatureManagerTest {

    private static final double EPS = 1e-9;

    private Graph<Room, DefaultEdge> graph;

    @BeforeEach
    void setUp() {
        GameRandom.reset();
        GameRandom.initialize(42L);
        graph = new Multigraph<>(DefaultEdge.class);
    }

    private UtilityRoom addUtilityRoom() {
        UtilityRoom utility = new UtilityRoom();
        utility.setFloorNumber(1);
        graph.addVertex(utility);
        return utility;
    }

    private Classroom addClassroom(Room connectTo) {
        Classroom classroom = new Classroom();
        classroom.setFloorNumber(1);
        graph.addVertex(classroom);
        graph.addEdge(connectTo, classroom);
        return classroom;
    }

    /** Utility room + a 12-room chain hanging off it. */
    private List<Room> buildLongChain() {
        List<Room> chain = new ArrayList<>();
        Room previous = addUtilityRoom();
        chain.add(previous);
        for (int i = 0; i < 12; i++) {
            previous = addClassroom(previous);
            chain.add(previous);
        }
        return chain;
    }

    /** Worst allowed indoor temp for a serviced room on a heating day. */
    private static double heatingFloor(int outdoorF) {
        return SimConstants.HVAC_HEAT_SETPOINT_F
                + (outdoorF - SimConstants.HVAC_HEAT_SETPOINT_F)
                * SimConstants.HVAC_MAX_OUTDOOR_BLEND;
    }

    @Test
    @DisplayName("Cold morning: no serviced room drops below the conditioning floor")
    void testColdMorningKeepsIndoorRoomsConditioned() {
        List<Room> chain = buildLongChain();
        RoomTemperatureManager manager = new RoomTemperatureManager(graph);
        manager.recompute(40);

        double floor = heatingFloor(40); // 70 - 30 * 0.3 = 61F
        for (Room room : chain) {
            double temp = manager.getRoomTemperatureF(room);
            assertTrue(temp >= floor - EPS,
                    "Room " + chain.indexOf(room) + " hops out fell to "
                            + temp + "F, below the " + floor + "F floor");
            assertFalse(manager.isUnserviced(room),
                    "Chain rooms should all be centrally serviced");
        }

        // The utility room itself sits exactly at the heat setpoint, and
        // the first classroom out is at most two hops away (one failed
        // spread roll), i.e. still almost fully conditioned.
        assertEquals(SimConstants.HVAC_HEAT_SETPOINT_F,
                manager.getRoomTemperatureF(chain.get(0)), EPS);
        double twoHopTemp = SimConstants.HVAC_HEAT_SETPOINT_F
                + (40 - SimConstants.HVAC_HEAT_SETPOINT_F)
                * 2 * SimConstants.HVAC_DECAY_PER_HOP;
        assertTrue(manager.getRoomTemperatureF(chain.get(1)) >= twoHopTemp - EPS,
                "First room off the utility room should be nearly fully conditioned");
    }

    @Test
    @DisplayName("Portables behind outdoor space are unserviced but their own units keep them livable")
    void testPortableInsulation() {
        Room utility = addUtilityRoom();
        Courtyard courtyard = new Courtyard();
        courtyard.setFloorNumber(1);
        graph.addVertex(courtyard);
        graph.addEdge(utility, courtyard);
        Portable portable = new Portable();
        portable.setFloorNumber(1);
        graph.addVertex(portable);
        graph.addEdge(courtyard, portable);

        RoomTemperatureManager manager = new RoomTemperatureManager(graph);
        manager.recompute(40);

        assertTrue(manager.isUnserviced(portable),
                "Portables only reachable through outdoor space are off the central system");
        double expected = 40 + (SimConstants.HVAC_HEAT_SETPOINT_F - 40)
                * SimConstants.HVAC_PORTABLE_INSULATION; // 40 + 30 * 0.7 = 61F
        assertEquals(expected, manager.getRoomTemperatureF(portable), EPS);
        assertTrue(expected >= heatingFloor(40) - EPS,
                "Portables should be no colder than the worst serviced room");
    }

    @Test
    @DisplayName("Outdoor spaces sit exactly at the outdoor temperature")
    void testOutdoorRoomsTrackWeather() {
        Room utility = addUtilityRoom();
        Courtyard courtyard = new Courtyard();
        courtyard.setFloorNumber(1);
        graph.addVertex(courtyard);
        graph.addEdge(utility, courtyard);

        RoomTemperatureManager manager = new RoomTemperatureManager(graph);
        manager.recompute(40);
        assertEquals(40.0, manager.getRoomTemperatureF(courtyard), EPS);
    }

    @Test
    @DisplayName("Hot afternoon: no serviced room rises above the cooling ceiling")
    void testHotDayCapsIndoorHeat() {
        List<Room> chain = buildLongChain();
        RoomTemperatureManager manager = new RoomTemperatureManager(graph);
        manager.recompute(95);

        double ceiling = SimConstants.HVAC_COOL_SETPOINT_F
                + (95 - SimConstants.HVAC_COOL_SETPOINT_F)
                * SimConstants.HVAC_MAX_OUTDOOR_BLEND; // 74 + 21 * 0.3 = 80.3F
        for (Room room : chain) {
            assertTrue(manager.getRoomTemperatureF(room) <= ceiling + EPS,
                    "Serviced rooms should never exceed the cooling ceiling");
        }
    }

    @Test
    @DisplayName("Mild day inside the idle band: every room tracks the outdoor temperature")
    void testMildDayIdles() {
        List<Room> chain = buildLongChain();
        RoomTemperatureManager manager = new RoomTemperatureManager(graph);
        manager.recompute(72);

        for (Room room : chain) {
            assertEquals(72.0, manager.getRoomTemperatureF(room), EPS,
                    "With outdoors between the setpoints the system idles");
        }
    }

    @Test
    @DisplayName("Upper floors run a few degrees warmer than the first floor")
    void testUpperFloorBonus() {
        UtilityRoom groundFloor = addUtilityRoom();
        UtilityRoom secondFloor = addUtilityRoom();
        secondFloor.setFloorNumber(2);

        RoomTemperatureManager manager = new RoomTemperatureManager(graph);
        manager.recompute(40);

        assertEquals(manager.getRoomTemperatureF(groundFloor)
                        + SimConstants.HVAC_UPPER_FLOOR_HEAT_F,
                manager.getRoomTemperatureF(secondFloor), EPS);
    }
}
