package simulation;

import constants.SimConstants;
import entity.Rooms.AthleticField;
import entity.Rooms.Courtyard;
import entity.Rooms.ParkingLot;
import entity.Rooms.Room;
import entity.Rooms.UtilityRoom;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import utility.RoomPropagation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes and caches the indoor temperature of every room on the
 * school's room-connection graph.
 *
 * <p>The central heating/cooling system originates in utility rooms and
 * spreads through connected rooms ({@link RoomPropagation}). Rooms close
 * to a utility room sit near the comfort setpoint; rooms farther away
 * blend toward the outdoor temperature. Outdoor spaces (courtyards,
 * athletic fields, parking lots) block the spread and sit exactly at the
 * outdoor temperature, which naturally cuts portables off from the
 * central system: they only get their own weak insulation. Upper floors
 * run a few degrees hotter because heat rises.</p>
 *
 * <p>Temperatures live here rather than on {@link Room} so the persisted
 * room graph is untouched by save/load; they are recomputed from weather
 * on load and at every morning/afternoon refresh.</p>
 */
public class RoomTemperatureManager {

    private final Graph<Room, DefaultEdge> roomGraph;
    private final Map<Room, Double> roomTemperatureF = new HashMap<>();
    private final Set<Room> unservicedRooms = new HashSet<>();

    // Most recently constructed manager, so static UI helpers (Inspector)
    // can look up room temperatures without plumbing a reference through
    // every call site. Only one school simulation is active at a time.
    private static RoomTemperatureManager active;

    /**
     * Creates a manager over the school's room-connection graph and
     * registers it as the active instance for UI lookups.
     *
     * @param roomGraph the graph built by {@code RoomConnector}
     */
    public RoomTemperatureManager(Graph<Room, DefaultEdge> roomGraph) {
        this.roomGraph = roomGraph;
        active = this;
    }

    /**
     * Recomputes every room's temperature for the given outdoor
     * temperature. Called each morning (day's low) and each afternoon
     * (day's high); every call re-rolls the per-edge spread chances so
     * distant rooms wobble slightly between recomputes.
     *
     * @param outdoorTempF the current outdoor temperature in Fahrenheit
     */
    public void recompute(int outdoorTempF) {
        roomTemperatureF.clear();
        unservicedRooms.clear();
        if (roomGraph == null) {
            return;
        }

        double setpoint = setpointFor(outdoorTempF);

        List<Room> sources = new ArrayList<>();
        for (Room room : roomGraph.vertexSet()) {
            if (room instanceof UtilityRoom) {
                sources.add(room);
            }
        }
        Map<Room, Integer> hops = RoomPropagation.propagate(
                roomGraph, sources, RoomTemperatureManager::isOutdoor,
                SimConstants.HVAC_SPREAD_CHANCE);

        for (Room room : roomGraph.vertexSet()) {
            if (isOutdoor(room)) {
                roomTemperatureF.put(room, (double) outdoorTempF);
                continue;
            }

            double temp;
            Integer hopCount = hops.get(room);
            if (hopCount != null) {
                // Serviced by the central system: fully conditioned at
                // the utility room, drifting toward outdoors with hops.
                // The blend is capped so distant rooms stay mostly
                // conditioned -- the building envelope never lets an
                // indoor room fall all the way to the outdoor temp.
                double outdoorBlend = Math.min(
                        SimConstants.HVAC_MAX_OUTDOOR_BLEND,
                        hopCount * SimConstants.HVAC_DECAY_PER_HOP);
                temp = lerp(setpoint, outdoorTempF, outdoorBlend);
            } else {
                // Portables and anything else cut off from the central
                // air: their own weak units and insulation close most
                // of the gap toward the setpoint, but they remain the
                // draftiest rooms in the school.
                unservicedRooms.add(room);
                temp = lerp(outdoorTempF, setpoint,
                        SimConstants.HVAC_PORTABLE_INSULATION);
            }

            int floorsAboveFirst = room.getFloorNumber() - 1;
            if (floorsAboveFirst > 0) {
                temp += floorsAboveFirst * SimConstants.HVAC_UPPER_FLOOR_HEAT_F;
            }
            roomTemperatureF.put(room, temp);
        }
    }

    /**
     * @param room the room to look up
     * @return the room's temperature in Fahrenheit, or {@code null} if
     *         the room is unknown (off the graph) or nothing has been
     *         computed yet
     */
    public Double getRoomTemperatureF(Room room) {
        if (room == null) {
            return null;
        }
        return roomTemperatureF.get(room);
    }

    /**
     * Looks up a room's temperature on the active manager, for static
     * UI code (e.g. the Inspector) that has no engine reference.
     *
     * @param room the room to look up
     * @return the temperature in Fahrenheit, or {@code null} if no
     *         manager is active or the room has no computed value
     */
    public static Double lookupTemperatureF(Room room) {
        return active != null ? active.getRoomTemperatureF(room) : null;
    }

    /**
     * The temperature the central system drives conditioned rooms
     * toward: heat when it's cold out, cool when it's hot, and idle
     * (tracking outdoors) in between.
     */
    private static double setpointFor(int outdoorTempF) {
        if (outdoorTempF < SimConstants.HVAC_HEAT_SETPOINT_F) {
            return SimConstants.HVAC_HEAT_SETPOINT_F;
        }
        if (outdoorTempF > SimConstants.HVAC_COOL_SETPOINT_F) {
            return SimConstants.HVAC_COOL_SETPOINT_F;
        }
        return outdoorTempF;
    }

    /**
     * Outdoor spaces sit at the outdoor temperature and block the
     * central system's spread. Portables are intentionally not listed:
     * they are indoor rooms, just only reachable through outdoor spaces,
     * which is exactly what disconnects them from the central air.
     */
    private static boolean isOutdoor(Room room) {
        return room instanceof Courtyard
                || room instanceof AthleticField
                || room instanceof ParkingLot;
    }

    private static double lerp(double from, double to, double fraction) {
        return from + (to - from) * fraction;
    }

    /**
     * True when the room was off the central system at the last
     * recompute (portables, plus any indoor room whose only paths to a
     * utility room cross outdoor space). Lets the Inspector label
     * unserviced rooms.
     *
     * @param room the room to check
     * @return whether the room lacks central heating/cooling
     */
    public boolean isUnserviced(Room room) {
        return unservicedRooms.contains(room);
    }
}
