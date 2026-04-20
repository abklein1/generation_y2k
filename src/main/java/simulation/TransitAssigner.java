package simulation;

import entity.*;
import utility.GameRandom;

import java.util.*;

/**
 * Assigns a {@link TransitMode} and departure time to every student based on
 * their neighborhood distance and family income.  Also builds transit groups
 * so co-travelers can socialize during the commute.
 *
 * <h3>Mode selection rules</h3>
 * <ul>
 *   <li><b>WALK</b> — distance &le; 1 mile (any income), or &le; 2 miles and low income.</li>
 *   <li><b>BUS</b>  — default for 2-7 miles, or low/middle income beyond walking range.</li>
 *   <li><b>DRIVE</b> — distance &gt; 4 miles and high income, or seniors with middle+ income.</li>
 *   <li><b>CARPOOL</b> — middle income, 3-7 miles.</li>
 * </ul>
 */
public class TransitAssigner {

    /** Students should aim to arrive by this time (minutes from midnight). */
    private static final int TARGET_ARRIVAL_MINUTES = 8 * 60 + 5; // 8:05 AM

    /** Earliest a student can depart (minutes from midnight). */
    private static final int EARLIEST_DEPARTURE_MINUTES = 7 * 60; // 7:00 AM

    /** Standard deviation for departure jitter in minutes. */
    private static final double DEPARTURE_JITTER_STD = 5.0;

    private TransitAssigner() { }

    /**
     * Assigns transit mode, travel time, and departure time to every student
     * in the provided map, then builds transit groups.
     *
     * @param students      all students keyed by id
     * @param town          the town (used to look up neighborhoods)
     */
    public static void assignAll(HashMap<Integer, Student> students, Town town) {
        if (students == null || town == null) {
            return;
        }

        // Phase 1: assign mode + times
        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            Neighborhood neighborhood = resolveNeighborhood(student, town);
            int distance = (neighborhood != null) ? neighborhood.getDistanceFromSchoolMiles() : 0;
            String income = student.studentStatistics.getIncomeLevel();
            String grade = student.studentStatistics.getGradeLevel();

            TransitMode mode = selectMode(distance, income, grade);
            int travelMinutes = mode.getTravelTimeMinutes(distance);

            int baseDeparture = TARGET_ARRIVAL_MINUTES - travelMinutes;
            int jitter = (int) Math.round(GameRandom.nextGaussian(0, DEPARTURE_JITTER_STD));
            int departure = Math.max(EARLIEST_DEPARTURE_MINUTES, baseDeparture + jitter);

            state.setTransitMode(mode);
            state.setTravelTimeMinutes(travelMinutes);
            state.setDepartureTimeMinutes(departure);
        }

        // Phase 2: build transit groups (same neighborhood + same mode)
        buildTransitGroups(students, town);
    }

    /**
     * Selects the transit mode for a student based on distance, income, and grade.
     */
    static TransitMode selectMode(int distanceMiles, String income, String grade) {
        boolean isLow = "low".equalsIgnoreCase(income);
        boolean isHigh = "high".equalsIgnoreCase(income);
        boolean isMiddle = "middle".equalsIgnoreCase(income);
        boolean isSenior = "Senior".equalsIgnoreCase(grade);

        // Walking range
        if (distanceMiles <= 1) {
            return TransitMode.WALK;
        }
        if (distanceMiles <= 2 && isLow) {
            return TransitMode.WALK;
        }

        // Driving: high income + far, or seniors with middle+ income
        if (distanceMiles > 4 && isHigh) {
            return TransitMode.DRIVE;
        }
        if (isSenior && (isMiddle || isHigh) && distanceMiles > 2) {
            return TransitMode.DRIVE;
        }

        // Carpool: middle income, moderate distance
        if (isMiddle && distanceMiles >= 3 && distanceMiles <= 7) {
            return TransitMode.CARPOOL;
        }

        // Default to bus
        return TransitMode.BUS;
    }

    /**
     * Groups students by (neighborhood, transit mode) and assigns the list as
     * each member's transit group.  Bus riders from neighborhoods within
     * 2 miles of each other are merged into shared route groups.
     */
    private static void buildTransitGroups(HashMap<Integer, Student> students, Town town) {
        // Key: "neighborhoodName|MODE"
        Map<String, List<Student>> rawGroups = new HashMap<>();

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null || state.getTransitMode() == null) {
                continue;
            }
            String nhName = student.studentStatistics.getNeighborhoodName();
            if (nhName == null) {
                nhName = "unknown";
            }
            String key = nhName + "|" + state.getTransitMode().name();
            rawGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(student);
        }

        // Merge bus groups from nearby neighborhoods into shared routes
        List<String> busKeys = rawGroups.keySet().stream()
                .filter(k -> k.endsWith("|BUS"))
                .sorted()
                .toList();

        Map<String, List<Student>> mergedBusRoutes = mergeBusRoutes(busKeys, rawGroups, town);

        // Apply transit groups to entity states
        for (List<Student> group : rawGroups.values()) {
            for (Student s : group) {
                EntityState state = s.getEntityState();
                if (state != null) {
                    state.setTransitGroup(Collections.unmodifiableList(group));
                }
            }
        }
        // Override bus riders with merged routes
        for (List<Student> route : mergedBusRoutes.values()) {
            List<Student> unmodifiable = Collections.unmodifiableList(route);
            for (Student s : route) {
                EntityState state = s.getEntityState();
                if (state != null) {
                    state.setTransitGroup(unmodifiable);
                }
            }
        }
    }

    /**
     * Merges bus groups from neighborhoods within 2 miles of each other.
     */
    private static Map<String, List<Student>> mergeBusRoutes(
            List<String> busKeys,
            Map<String, List<Student>> rawGroups,
            Town town) {

        Map<String, List<Student>> routes = new LinkedHashMap<>();
        Set<String> merged = new HashSet<>();

        for (int i = 0; i < busKeys.size(); i++) {
            if (merged.contains(busKeys.get(i))) {
                continue;
            }
            String nhNameA = busKeys.get(i).split("\\|")[0];
            Neighborhood a = town.getNeighborhoodByName(nhNameA);
            int distA = (a != null) ? a.getDistanceFromSchoolMiles() : 0;

            List<Student> route = new ArrayList<>(rawGroups.get(busKeys.get(i)));
            merged.add(busKeys.get(i));

            for (int j = i + 1; j < busKeys.size(); j++) {
                if (merged.contains(busKeys.get(j))) {
                    continue;
                }
                String nhNameB = busKeys.get(j).split("\\|")[0];
                Neighborhood b = town.getNeighborhoodByName(nhNameB);
                int distB = (b != null) ? b.getDistanceFromSchoolMiles() : 0;

                if (Math.abs(distA - distB) <= 2) {
                    route.addAll(rawGroups.get(busKeys.get(j)));
                    merged.add(busKeys.get(j));
                }
            }

            routes.put("bus_route_" + routes.size(), route);
        }
        return routes;
    }

    private static Neighborhood resolveNeighborhood(Student student, Town town) {
        String name = student.studentStatistics.getNeighborhoodName();
        if (name == null) {
            return null;
        }
        return town.getNeighborhoodByName(name);
    }
}
