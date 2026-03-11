package utility;

import entity.Neighborhood;
import entity.Staff;
import entity.Student;
import entity.Town;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Creates and assigns town neighborhoods for generated residents.
 */
public final class NeighborhoodAssignmentService {

    private static final double STAFF_LOW_WEIGHT = 0.20;
    private static final double STAFF_MIDDLE_WEIGHT = 0.60;
    private static final int LOW_CAPACITY_MIN = 150;
    private static final int LOW_CAPACITY_MAX = 225;
    private static final int MIDDLE_CAPACITY_MIN = 165;
    private static final int MIDDLE_CAPACITY_MAX = 260;
    private static final int HIGH_CAPACITY_MIN = 150;
    private static final int HIGH_CAPACITY_MAX = 210;

    private NeighborhoodAssignmentService() {
    }

    public static void assignNeighborhoods(Town town) {
        if (town == null) {
            return;
        }

        List<Student> students = getAllStudents(town);
        List<Staff> staff = getAllStaff(town);
        clearNeighborhoodAssignments(students, staff);

        Set<String> usedNames = new HashSet<>();
        List<Neighborhood> neighborhoods = createInitialNeighborhoods(students, staff.size(), usedNames);
        assignStudentHouseholds(buildStudentHouseholds(students), neighborhoods, usedNames);
        assignStaffMembers(staff, neighborhoods, usedNames);
        town.setNeighborhoods(neighborhoods);
    }

    public static void assignNeighborhoodsForNewResidents(Town town) {
        if (town == null) {
            return;
        }

        List<Neighborhood> neighborhoods = town.getNeighborhoods();
        if (neighborhoods.isEmpty()) {
            assignNeighborhoods(town);
            return;
        }

        List<Student> students = getAllStudents(town);
        List<Staff> staff = getAllStaff(town);
        Map<String, Neighborhood> neighborhoodsByName = indexNeighborhoodsByName(neighborhoods);
        rebuildNeighborhoodOccupancy(neighborhoodsByName, students, staff);

        List<Student> unassignedStudents = students.stream()
                .filter(student -> student.studentStatistics.getNeighborhoodName() == null)
                .toList();
        List<Staff> unassignedStaff = staff.stream()
                .filter(staffMember -> staffMember.teacherStatistics.getNeighborhoodName() == null)
                .toList();

        if (unassignedStudents.isEmpty() && unassignedStaff.isEmpty()) {
            town.setNeighborhoods(neighborhoods);
            return;
        }

        Set<String> usedNames = new HashSet<>(neighborhoodsByName.keySet());
        assignStudentHouseholds(buildStudentHouseholds(unassignedStudents), neighborhoods, usedNames);
        assignStaffMembers(unassignedStaff, neighborhoods, usedNames);
        town.setNeighborhoods(neighborhoods);
    }

    private static List<Neighborhood> createInitialNeighborhoods(List<Student> students, int staffCount, Set<String> usedNames) {
        List<Neighborhood> neighborhoods = new ArrayList<>();
        Map<String, Integer> studentCounts = countStudentsByIncome(students);

        int lowTarget = studentCounts.get("low") + (int) Math.round(staffCount * STAFF_LOW_WEIGHT);
        int middleTarget = studentCounts.get("middle") + (int) Math.round(staffCount * STAFF_MIDDLE_WEIGHT);
        int highTarget = studentCounts.get("high") + Math.max(0, staffCount - (lowTarget - studentCounts.get("low"))
                - (middleTarget - studentCounts.get("middle")));

        createTierNeighborhoods("low", lowTarget, neighborhoods, usedNames);
        createTierNeighborhoods("middle", middleTarget, neighborhoods, usedNames);
        createTierNeighborhoods("high", highTarget, neighborhoods, usedNames);

        return neighborhoods;
    }

    private static void createTierNeighborhoods(String wealthLevel, int targetResidents,
            List<Neighborhood> neighborhoods, Set<String> usedNames) {
        if (targetResidents <= 0) {
            return;
        }

        int totalCapacity = 0;
        int capacityTarget = targetResidents + calculateBuffer(targetResidents);
        while (totalCapacity < capacityTarget) {
            Neighborhood neighborhood = createNeighborhood(wealthLevel, usedNames);
            neighborhoods.add(neighborhood);
            totalCapacity += neighborhood.getPopulationCapacity();
        }
    }

    private static void assignStudentHouseholds(List<StudentHousehold> households, List<Neighborhood> neighborhoods,
            Set<String> usedNames) {
        households.sort(Comparator
                .comparingInt(NeighborhoodAssignmentService::getHouseholdStrictness)
                .thenComparing(Comparator.comparingInt(StudentHousehold::size).reversed()));

        for (StudentHousehold household : households) {
            Neighborhood neighborhood = findNeighborhoodForHousehold(household, neighborhoods);
            if (neighborhood == null) {
                neighborhood = createNeighborhood(household.incomeLevel(), usedNames);
                neighborhoods.add(neighborhood);
            }
            assignHouseholdToNeighborhood(household, neighborhood);
        }
    }

    private static Neighborhood findNeighborhoodForHousehold(StudentHousehold household, List<Neighborhood> neighborhoods) {
        for (String tier : getAllowedTiers(household.incomeLevel())) {
            List<Neighborhood> candidates = neighborhoods.stream()
                    .filter(neighborhood -> tier.equals(neighborhood.getWealthLevel()))
                    .filter(neighborhood -> neighborhood.hasCapacityFor(household.size()))
                    .toList();
            if (!candidates.isEmpty()) {
                return chooseByRemainingCapacity(candidates);
            }
        }
        return null;
    }

    private static void assignHouseholdToNeighborhood(StudentHousehold household, Neighborhood neighborhood) {
        for (Student student : household.members()) {
            student.studentStatistics.setNeighborhoodName(neighborhood.getName());
            student.studentStatistics.setNeighborhoodWealthLevel(neighborhood.getWealthLevel());
            neighborhood.addResident(student);
        }
    }

    private static void assignStaffMembers(List<Staff> staffMembers, List<Neighborhood> neighborhoods, Set<String> usedNames) {
        for (Staff staffMember : staffMembers) {
            Neighborhood neighborhood = findNeighborhoodForStaff(neighborhoods);
            if (neighborhood == null) {
                String preferredTier = rollStaffTierPreference();
                neighborhood = createNeighborhood(preferredTier, usedNames);
                neighborhoods.add(neighborhood);
            }

            staffMember.teacherStatistics.setNeighborhoodName(neighborhood.getName());
            staffMember.teacherStatistics.setNeighborhoodWealthLevel(neighborhood.getWealthLevel());
            neighborhood.addResident(staffMember);
        }
    }

    private static Neighborhood findNeighborhoodForStaff(List<Neighborhood> neighborhoods) {
        String preferredTier = rollStaffTierPreference();
        for (String tier : getStaffTierFallbackOrder(preferredTier)) {
            List<Neighborhood> candidates = neighborhoods.stream()
                    .filter(neighborhood -> tier.equals(neighborhood.getWealthLevel()))
                    .filter(neighborhood -> neighborhood.hasCapacityFor(1))
                    .toList();
            if (!candidates.isEmpty()) {
                return chooseByRemainingCapacity(candidates);
            }
        }
        return null;
    }

    private static Neighborhood chooseByRemainingCapacity(List<Neighborhood> candidates) {
        int totalWeight = candidates.stream()
                .mapToInt(neighborhood -> Math.max(1, neighborhood.getRemainingCapacity()))
                .sum();
        int roll = GameRandom.nextInt(totalWeight);
        int cumulative = 0;
        for (Neighborhood neighborhood : candidates) {
            cumulative += Math.max(1, neighborhood.getRemainingCapacity());
            if (roll < cumulative) {
                return neighborhood;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private static List<StudentHousehold> buildStudentHouseholds(Collection<Student> students) {
        List<StudentHousehold> households = new ArrayList<>();
        Set<Student> studentSet = new HashSet<>(students);
        Set<Student> visited = new HashSet<>();

        for (Student student : students) {
            if (!visited.add(student)) {
                continue;
            }

            List<Student> members = new ArrayList<>();
            Queue<Student> queue = new ArrayDeque<>();
            queue.add(student);

            while (!queue.isEmpty()) {
                Student current = queue.remove();
                members.add(current);

                for (Student sibling : getLinkedStudents(current)) {
                    if (sibling != null && studentSet.contains(sibling) && visited.add(sibling)) {
                        queue.add(sibling);
                    }
                }
            }

            households.add(new StudentHousehold(members, resolveHouseholdIncomeLevel(members)));
        }

        return households;
    }

    private static List<Student> getLinkedStudents(Student student) {
        List<Student> linked = new ArrayList<>(student.studentStatistics.getSiblingsInSchool());
        linked.addAll(student.studentStatistics.getSiblingsNotInSchool());
        return linked;
    }

    private static String resolveHouseholdIncomeLevel(List<Student> householdMembers) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("low", 0);
        counts.put("middle", 0);
        counts.put("high", 0);

        for (Student student : householdMembers) {
            String incomeLevel = normalizeIncomeLevel(student.studentStatistics.getIncomeLevel());
            counts.put(incomeLevel, counts.get(incomeLevel) + 1);
        }

        return counts.entrySet().stream()
                .max(Comparator
                        .comparingInt(Map.Entry<String, Integer>::getValue)
                        .thenComparing(entry -> getIncomePreferenceRank(entry.getKey())))
                .map(Map.Entry::getKey)
                .orElse("middle");
    }

    private static int getIncomePreferenceRank(String incomeLevel) {
        return switch (incomeLevel) {
            case "middle" -> 3;
            case "high" -> 2;
            case "low" -> 1;
            default -> 0;
        };
    }

    private static Map<String, Integer> countStudentsByIncome(List<Student> students) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("low", 0);
        counts.put("middle", 0);
        counts.put("high", 0);

        for (Student student : students) {
            String incomeLevel = normalizeIncomeLevel(student.studentStatistics.getIncomeLevel());
            counts.put(incomeLevel, counts.get(incomeLevel) + 1);
        }

        return counts;
    }

    private static String normalizeIncomeLevel(String incomeLevel) {
        if (incomeLevel == null) {
            return "middle";
        }

        return switch (incomeLevel.trim().toLowerCase()) {
            case "low" -> "low";
            case "high" -> "high";
            default -> "middle";
        };
    }

    private static Neighborhood createNeighborhood(String wealthLevel, Set<String> usedNames) {
        String normalizedWealthLevel = normalizeIncomeLevel(wealthLevel);
        String name = NeighborhoodNameLoader.generateUniqueNeighborhoodName(normalizedWealthLevel, usedNames);
        usedNames.add(name);
        return new Neighborhood(name, normalizedWealthLevel, rollCapacity(normalizedWealthLevel));
    }

    private static int rollCapacity(String wealthLevel) {
        return switch (wealthLevel) {
            case "low" -> GameRandom.nextInt(LOW_CAPACITY_MIN, LOW_CAPACITY_MAX);
            case "high" -> GameRandom.nextInt(HIGH_CAPACITY_MIN, HIGH_CAPACITY_MAX);
            default -> GameRandom.nextInt(MIDDLE_CAPACITY_MIN, MIDDLE_CAPACITY_MAX);
        };
    }

    private static int calculateBuffer(int targetResidents) {
        return Math.max(20, (int) Math.ceil(targetResidents * 0.08));
    }

    private static List<Student> getAllStudents(Town town) {
        return new ArrayList<>(town.getStudentPool().getAllStudents().values());
    }

    private static List<Staff> getAllStaff(Town town) {
        return new ArrayList<>(town.getStaffPool().getAllStaff().values());
    }

    private static void clearNeighborhoodAssignments(List<Student> students, List<Staff> staffMembers) {
        for (Student student : students) {
            student.studentStatistics.clearNeighborhoodAssignment();
        }
        for (Staff staffMember : staffMembers) {
            staffMember.teacherStatistics.clearNeighborhoodAssignment();
        }
    }

    private static Map<String, Neighborhood> indexNeighborhoodsByName(List<Neighborhood> neighborhoods) {
        Map<String, Neighborhood> neighborhoodsByName = new LinkedHashMap<>();
        for (Neighborhood neighborhood : neighborhoods) {
            neighborhood.clearResidents();
            neighborhoodsByName.put(neighborhood.getName(), neighborhood);
        }
        return neighborhoodsByName;
    }

    private static void rebuildNeighborhoodOccupancy(Map<String, Neighborhood> neighborhoodsByName, List<Student> students,
            List<Staff> staffMembers) {
        for (Student student : students) {
            String neighborhoodName = student.studentStatistics.getNeighborhoodName();
            Neighborhood neighborhood = neighborhoodsByName.get(neighborhoodName);
            if (neighborhood == null) {
                student.studentStatistics.clearNeighborhoodAssignment();
                continue;
            }
            student.studentStatistics.setNeighborhoodWealthLevel(neighborhood.getWealthLevel());
            neighborhood.addResident(student);
        }

        for (Staff staffMember : staffMembers) {
            String neighborhoodName = staffMember.teacherStatistics.getNeighborhoodName();
            Neighborhood neighborhood = neighborhoodsByName.get(neighborhoodName);
            if (neighborhood == null) {
                staffMember.teacherStatistics.clearNeighborhoodAssignment();
                continue;
            }
            staffMember.teacherStatistics.setNeighborhoodWealthLevel(neighborhood.getWealthLevel());
            neighborhood.addResident(staffMember);
        }
    }

    private static int getHouseholdStrictness(StudentHousehold household) {
        return switch (household.incomeLevel()) {
            case "low", "high" -> 0;
            default -> 1;
        };
    }

    private static List<String> getAllowedTiers(String incomeLevel) {
        return switch (normalizeIncomeLevel(incomeLevel)) {
            case "low" -> List.of("low", "middle");
            case "high" -> List.of("high", "middle");
            default -> List.of("middle", "low", "high");
        };
    }

    private static String rollStaffTierPreference() {
        double roll = GameRandom.nextDouble();
        if (roll < STAFF_LOW_WEIGHT) {
            return "low";
        }
        if (roll < STAFF_LOW_WEIGHT + STAFF_MIDDLE_WEIGHT) {
            return "middle";
        }
        return "high";
    }

    private static List<String> getStaffTierFallbackOrder(String preferredTier) {
        return switch (preferredTier) {
            case "low" -> List.of("low", "middle", "high");
            case "high" -> List.of("high", "middle", "low");
            default -> List.of("middle", "low", "high");
        };
    }

    private record StudentHousehold(List<Student> members, String incomeLevel) {
        private int size() {
            return members.size();
        }
    }
}
