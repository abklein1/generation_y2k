package utility;

import entity.Rooms.Room;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Predicate;

/**
 * Generic "spread from source rooms through connected rooms" traversal
 * over the school's room-connection graph.
 *
 * <p>Runs a multi-source shortest-hop search (Dijkstra with small
 * integer edge costs) from a set of source rooms and returns, for every
 * reachable room, its effective hop distance from the nearest source.
 * Rooms matching the {@code blocked} predicate are never entered or
 * traversed, so anything only reachable through a blocked room is
 * simply absent from the result.</p>
 *
 * <p>Each edge crossing is subject to a spread-chance roll: on a failed
 * roll the neighbor is treated as one hop farther instead of being cut
 * off entirely. This adds mild per-recompute variation while keeping
 * rooms near a source reliably reached.</p>
 *
 * <p>Built for the HVAC system (conditioned air spreading from utility
 * rooms) but deliberately source-agnostic so future systems that spread
 * room to room (rumors, sickness) can reuse it with their own sources
 * and block rules.</p>
 */
public final class RoomPropagation {

    private RoomPropagation() {
    }

    /**
     * Computes effective hop distances from the nearest source room.
     *
     * @param graph        the room-connection graph
     * @param sources      rooms the spread originates from; sources that
     *                     are blocked or absent from the graph are skipped
     * @param blocked      rooms the spread can never enter or pass through
     * @param spreadChance probability in [0, 1] that an edge propagates
     *                     cleanly; a failed roll costs one extra hop
     * @return map of reachable room to effective hop distance
     *         (0 for the sources themselves)
     */
    public static Map<Room, Integer> propagate(Graph<Room, DefaultEdge> graph,
                                               Collection<Room> sources,
                                               Predicate<Room> blocked,
                                               double spreadChance) {
        Map<Room, Integer> distance = new HashMap<>();
        if (graph == null || sources == null) {
            return distance;
        }

        PriorityQueue<RoomDistance> frontier = new PriorityQueue<>();
        for (Room source : sources) {
            if (source == null || !graph.containsVertex(source)
                    || blocked.test(source)) {
                continue;
            }
            distance.put(source, 0);
            frontier.add(new RoomDistance(source, 0));
        }

        while (!frontier.isEmpty()) {
            RoomDistance current = frontier.poll();
            Integer best = distance.get(current.room);
            if (best == null || current.hops > best) {
                continue; // stale queue entry
            }
            for (DefaultEdge edge : graph.edgesOf(current.room)) {
                Room neighbor = Graphs.getOppositeVertex(
                        graph, edge, current.room);
                if (blocked.test(neighbor)) {
                    continue;
                }
                int hopCost = GameRandom.nextDouble() < spreadChance ? 1 : 2;
                int candidate = current.hops + hopCost;
                Integer known = distance.get(neighbor);
                if (known == null || candidate < known) {
                    distance.put(neighbor, candidate);
                    frontier.add(new RoomDistance(neighbor, candidate));
                }
            }
        }
        return distance;
    }

    private static final class RoomDistance implements Comparable<RoomDistance> {
        private final Room room;
        private final int hops;

        private RoomDistance(Room room, int hops) {
            this.room = room;
            this.hops = hops;
        }

        @Override
        public int compareTo(RoomDistance other) {
            return Integer.compare(this.hops, other.hops);
        }
    }
}
