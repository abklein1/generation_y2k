package utility;

import entity.Rooms.Classroom;
import entity.Rooms.Lunchroom;
import entity.Rooms.Room;
import entity.Staff;
import entity.StaffPool;
import entity.StaffType;
import entity.StandardSchool;
import entity.Town;
import view.GameView;

import java.util.*;
import java.util.Map;
import java.util.Optional;

/**
 * Service for assigning staff from a population pool to schools.
 * Handles role assignment, room assignment, and class scheduling.
 */
public class StaffAssignmentService {

    /**
     * Assigns staff from the town's pool to a school based on school requirements.
     *
     * @param town         the town containing the staff pool
     * @param school       the school to assign staff to
     * @param studentCount the number of students (affects staff requirements)
     * @param view         the game view for output
     * @return the number of staff assigned
     */
    public static int assignStaffToSchool(Town town, StandardSchool school, int studentCount, GameView view) {
        StaffPool pool = town.getStaffPool();
        int required = school.getMinimumStaffRequirements();

        return assignStaffToSchool(pool, school, required, studentCount, view);
    }

    /**
     * Assigns a specific number of staff from the pool to a school.
     * 
     * @deprecated Use
     *             {@link #assignStaffByDemand(StaffPool, StandardSchool, Map, GameView)}
     *             instead
     *             for demand-driven staffing based on curriculum requirements.
     *
     * @param pool         the staff pool
     * @param school       the school to assign staff to
     * @param count        the number of staff to assign
     * @param studentCount the number of students (affects role distribution)
     * @param view         the game view for output
     * @return the number of staff actually assigned
     */
    @Deprecated
    public static int assignStaffToSchool(StaffPool pool, StandardSchool school, int count,
            int studentCount, GameView view) {
        List<Staff> unassigned = pool.getUnassignedStaff();
        int toAssign = Math.min(count, unassigned.size());

        view.appendOutput("Assigning " + toAssign + " staff to " + school.getSchoolName());

        // Create a HashMap for role assignment (for compatibility with existing code)
        HashMap<Integer, Staff> assignedMap = new HashMap<>();

        int assigned = 0;
        for (int i = 0; i < toAssign && i < unassigned.size(); i++) {
            Staff staff = unassigned.get(i);
            if (pool.assignToSchool(staff, school)) {
                assignedMap.put(assigned, staff);
                assigned++;
            }
        }

        // NOTE: Legacy role assignment removed - use assignStaffByDemand() or
        // assignInitialStaffRoles() instead
        // These methods assign roles based on curriculum requirements

        // Assign to classrooms
        RoomAssignment.initialClassroomAssignments(school, assignedMap);

        // Reassign classrooms by teacher type
        Classroom[] classrooms = school.getClassrooms();
        for (Classroom classroom : classrooms) {
            classroom.reassignClassroomByTeacher(assignedMap, view);
        }

        view.appendOutput("Successfully assigned " + assigned + " staff to " + school.getSchoolName());
        return assigned;
    }

    /**
     * Hires a new staff member for a school.
     *
     * @param pool   the staff pool
     * @param staff  the staff member to hire
     * @param school the school to hire for
     * @param role   the role to assign
     * @param view   the game view for output
     * @return true if hiring was successful
     */
    public static boolean hireStaff(StaffPool pool, Staff staff, StandardSchool school,
            StaffType role, GameView view) {
        if (!pool.assignToSchool(staff, school)) {
            view.appendOutput("Failed to hire staff: not in pool");
            return false;
        }

        // Assign role
        staff.teacherStatistics.setStaffType(role);
        view.appendOutput("Hired " + staff.teacherName.getFirstName() + " " +
                staff.teacherName.getLastName() + " as " + role);
        return true;
    }

    /**
     * Gets a substitute from the pool to fill in at a school.
     *
     * @param pool    the staff pool
     * @param school  the school needing a substitute
     * @param forType the type of staff being substituted for
     * @param view    the game view for output
     * @return the substitute staff member, or null if none available
     */
    public static Staff getSubstitute(StaffPool pool, StandardSchool school,
            StaffType forType, GameView view) {
        // First try to get an unassigned staff member of the same type
        List<Staff> available = pool.getAvailableStaffForSubject(forType);

        if (!available.isEmpty()) {
            Staff sub = available.get(0);
            pool.assignToSchool(sub, school);
            view.appendOutput("Substitute " + sub.teacherName.getFirstName() + " " +
                    sub.teacherName.getLastName() + " assigned to " + school.getSchoolName());
            return sub;
        }

        view.appendOutput("No substitute available for " + forType);
        return null;
    }

    /**
     * Returns a substitute to the unassigned pool.
     *
     * @param pool       the staff pool
     * @param substitute the substitute to return
     * @param view       the game view for output
     */
    public static void returnSubstitute(StaffPool pool, Staff substitute, GameView view) {
        pool.unassignFromSchool(substitute);
        view.appendOutput("Substitute " + substitute.teacherName.getFirstName() + " " +
                substitute.teacherName.getLastName() + " returned to substitute pool");
    }

    /**
     * Transfers a staff member from one school to another.
     *
     * @param pool       the staff pool
     * @param staff      the staff member to transfer
     * @param fromSchool the current school
     * @param toSchool   the destination school
     * @param view       the game view for output
     * @return true if transfer was successful
     */
    public static boolean transferStaff(StaffPool pool, Staff staff,
            StandardSchool fromSchool, StandardSchool toSchool, GameView view) {
        if (!pool.transferStaff(staff, toSchool)) {
            view.appendOutput("Failed to transfer staff: pool transfer failed");
            return false;
        }

        view.appendOutput("Transferred " + staff.teacherName.getFirstName() + " " +
                staff.teacherName.getLastName() + " from " + fromSchool.getSchoolName() +
                " to " + toSchool.getSchoolName());
        return true;
    }

    /**
     * Releases a staff member from a school (returns to unassigned pool).
     *
     * @param pool   the staff pool
     * @param staff  the staff member to release
     * @param school the school to release from
     * @param view   the game view for output
     * @return true if release was successful
     */
    public static boolean releaseStaff(StaffPool pool, Staff staff, StandardSchool school, GameView view) {
        pool.unassignFromSchool(staff);
        view.appendOutput("Released " + staff.teacherName.getFirstName() + " " +
                staff.teacherName.getLastName() + " from " + school.getSchoolName());
        return true;
    }

    /**
     * Gets the count of staff by type for a school.
     *
     * @param pool   the staff pool
     * @param school the school
     * @return map of staff type to count
     */
    public static Map<StaffType, Integer> getStaffByType(StaffPool pool, StandardSchool school) {
        Map<StaffType, Integer> counts = new HashMap<>();

        for (Staff staff : pool.getStaffBySchool(school)) {
            Enum<?> typeEnum = staff.teacherStatistics.getStaffType();
            if (typeEnum instanceof StaffType type) {
                counts.merge(type, 1, Integer::sum);
            }
        }

        return counts;
    }

    /**
     * Gets the number of available substitutes in the pool.
     *
     * @param pool the staff pool
     * @return the number of unassigned staff members
     */
    public static int getAvailableSubstituteCount(StaffPool pool) {
        return pool.getUnassignedCount();
    }

    /**
     * Convenience overload that accepts a {@link DemandAnalyzer.DemandResult}
     * directly.
     *
     * @param pool   the staff pool
     * @param school the school to assign staff to
     * @param demand the pre-computed demand result
     * @param view   the game view for output
     * @return the total number of staff assigned
     */
    public static int assignStaffByDemand(StaffPool pool, StandardSchool school,
            DemandAnalyzer.DemandResult demand, GameView view) {
        return assignStaffByDemand(pool, school, demand.staffNeeds(), view);
    }

    /**
     * Assigns staff to a school based on curriculum demand analysis.
     * This method assigns staff by type to meet specific curriculum requirements.
     *
     * @param pool       the staff pool
     * @param school     the school to assign staff to
     * @param staffNeeds map of staff type to number needed
     * @param view       the game view for output
     * @return the total number of staff assigned
     */
    public static int assignStaffByDemand(StaffPool pool, StandardSchool school,
            Map<StaffType, Integer> staffNeeds, GameView view) {
        view.appendOutput("Assigning staff by curriculum demand to " + school.getSchoolName());

        HashMap<Integer, Staff> assignedMap = new HashMap<>();
        int totalAssigned = 0;
        int shortages = 0;

        // Assign staff by type in priority order
        StaffType[] priorityOrder = {
                // Core teaching staff first
                StaffType.ENGLISH, StaffType.MATH, StaffType.SCIENCE, StaffType.HISTORY,
                // Then specialized teaching staff
                StaffType.LANGUAGES, StaffType.PHYSICAL_ED, StaffType.VISUAL_ARTS,
                StaffType.PERFORMING_ARTS, StaffType.COMP_SCI, StaffType.VOCATIONAL,
                StaffType.BUSINESS, StaffType.CONSUMER_SCI,
                // Then support staff
                StaffType.PRINCIPAL, StaffType.VICE_PRINCIPAL, StaffType.GUIDANCE,
                StaffType.LIBRARY, StaffType.NURSE, StaffType.OFFICE,
                StaffType.MAINTENANCE, StaffType.LUNCH, StaffType.SUB
        };

        for (StaffType type : priorityOrder) {
            int needed = staffNeeds.getOrDefault(type, 0);
            if (needed <= 0)
                continue;

            // Get available staff of this type from the pool
            List<Staff> availableOfType = pool.getAvailableStaffForSubject(type);
            int toAssign = Math.min(needed, availableOfType.size());

            int assigned = 0;
            for (int i = 0; i < toAssign; i++) {
                Staff staff = availableOfType.get(i);
                if (pool.assignToSchool(staff, school)) {
                    // Set the staff type
                    staff.teacherStatistics.setStaffType(type);
                    assignedMap.put(totalAssigned, staff);
                    assigned++;
                    totalAssigned++;
                }
            }

            if (assigned < needed) {
                int shortage = needed - assigned;
                shortages += shortage;
                view.appendOutput("  " + type + ": assigned " + assigned + "/" + needed +
                        " (shortage: " + shortage + ")");
            } else {
                view.appendOutput("  " + type + ": assigned " + assigned + "/" + needed);
            }
        }

        // If there are shortages, try to fill with unassigned staff, assigning them
        // to the specific StaffType that needs them (not SUB).
        if (shortages > 0) {
            view.appendOutput("  Total shortages: " + shortages + " - attempting to fill with available staff");

            // Build a queue of StaffTypes that still need teachers, ordered by priority
            Map<StaffType, Integer> shortageByType = new LinkedHashMap<>();
            for (StaffType type : priorityOrder) {
                int needed = staffNeeds.getOrDefault(type, 0);
                if (needed <= 0) continue;
                long alreadyAssigned = assignedMap.values().stream()
                        .filter(s -> type.equals(s.teacherStatistics.getStaffType()))
                        .count();
                int remainingNeed = needed - (int) alreadyAssigned;
                if (remainingNeed > 0) {
                    shortageByType.put(type, remainingNeed);
                }
            }

            Set<Staff> alreadyUsed = new HashSet<>();
            int filled = 0;
            for (Map.Entry<StaffType, Integer> shortageEntry : shortageByType.entrySet()) {
                StaffType neededType = shortageEntry.getKey();
                int neededCount = shortageEntry.getValue();
                int filledForType = 0;
                // Re-fetch unassigned each iteration to get a fresh list
                List<Staff> remaining = pool.getUnassignedStaff();
                for (Staff staff : remaining) {
                    if (filledForType >= neededCount) break;
                    if (alreadyUsed.contains(staff)) continue;
                    if (pool.assignToSchool(staff, school)) {
                        staff.teacherStatistics.setStaffType(neededType);
                        assignedMap.put(totalAssigned, staff);
                        totalAssigned++;
                        filled++;
                        filledForType++;
                        alreadyUsed.add(staff);
                        view.appendOutput("  Filled " + neededType + " shortage with " +
                                staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName());
                    }
                }
            }
            if (filled > 0) {
                view.appendOutput("  Filled " + filled + " shortage positions with available staff");
            }
        }

        // Assign to classrooms
        if (!assignedMap.isEmpty()) {
            RoomAssignment.initialClassroomAssignments(school, assignedMap);

            // Reassign classrooms by teacher type
            Classroom[] classrooms = school.getClassrooms();
            for (Classroom classroom : classrooms) {
                classroom.reassignClassroomByTeacher(assignedMap, view);
            }
        }

        view.appendOutput("Successfully assigned " + totalAssigned + " staff to " + school.getSchoolName());
        return totalAssigned;
    }

    /**
     * Fills remaining staffing shortages by generating new staff for the town,
     * used when the town staff pool is exhausted during initial population.
     * Newly hired teaching staff receive rooms downstream via
     * {@link TeacherBlockBuilder#ensureTeachersHaveRooms}.
     *
     * @param town      the town whose pool will receive the generated staff
     * @param school    the school to hire for
     * @param shortages map of staff type to unfilled count
     * @param view      the game view for output
     * @return the number of staff generated and hired
     */
    public static int fillShortagesWithGeneratedStaff(Town town, StandardSchool school,
            Map<StaffType, Integer> shortages, GameView view) {
        int totalShort = shortages.values().stream().mapToInt(Integer::intValue).sum();
        if (totalShort <= 0) {
            return 0;
        }

        StaffPool pool = town.getStaffPool();
        view.appendOutput("  Staff pool exhausted - generating " + totalShort +
                " additional staff to meet curriculum demand...");
        List<Staff> generated = TownPopulationGenerator.generateAdditionalStaff(town, totalShort, view);

        int hired = 0;
        int index = 0;
        for (Map.Entry<StaffType, Integer> entry : shortages.entrySet()) {
            StaffType type = entry.getKey();
            for (int i = 0; i < entry.getValue() && index < generated.size(); i++) {
                Staff staff = generated.get(index++);
                staff.teacherStatistics.setStaffType(type);
                if (pool.assignToSchool(staff, school)) {
                    hired++;
                    view.appendOutput("  Generated and hired " + staff.teacherName.getFirstName() + " " +
                            staff.teacherName.getLastName() + " as " + type);
                }
            }
        }

        return hired;
    }

    /**
     * Assigns additional teachers to a school for post-generation expansion.
     * This method is used to hire more teachers after initial school population
     * to address scheduling gaps and capacity shortages.
     *
     * @param town       the town containing the staff pool
     * @param school     the school to assign additional teachers to
     * @param staffNeeds map of staff type to number of additional teachers needed
     * @param view       the game view for output
     * @return the number of additional teachers assigned
     */
    public static int assignAdditionalTeachers(Town town, StandardSchool school,
            Map<StaffType, Integer> staffNeeds, GameView view) {
        StaffPool pool = town.getStaffPool();
        view.appendOutput("Hiring additional teachers for " + school.getSchoolName() + "...");

        HashMap<Integer, Staff> newlyAssigned = new HashMap<>();
        int totalAssigned = 0;
        Map<StaffType, Integer> remainingNeeds = new LinkedHashMap<>();
        for (Map.Entry<StaffType, Integer> entry : staffNeeds.entrySet()) {
            if (entry.getValue() > 0) {
                remainingNeeds.put(entry.getKey(), entry.getValue());
            }
        }

        HashMap<Integer, Staff> currentSchoolStaff = town.getStaffPool().getStaffBySchoolAsMap(school);
        List<Staff> repurposedStaff = repurposeExistingStaffForShortages(currentSchoolStaff, remainingNeeds, view);
        for (Staff repurposed : repurposedStaff) {
            newlyAssigned.put(totalAssigned, repurposed);
            totalAssigned++;
        }

        // Assign teachers by type based on remaining needs
        for (Map.Entry<StaffType, Integer> need : remainingNeeds.entrySet()) {
            StaffType type = need.getKey();
            int count = need.getValue();

            if (count <= 0)
                continue;

            List<Staff> availableOfType = pool.getAvailableStaffForSubject(type);
            int assigned = 0;
            for (Staff staff : availableOfType) {
                if (assigned >= count)
                    break;
                if (pool.assignToSchool(staff, school)) {
                    staff.teacherStatistics.setStaffType(type);
                    newlyAssigned.put(totalAssigned, staff);
                    assigned++;
                    totalAssigned++;
                    view.appendOutput("  Hired " + staff.teacherName.getFirstName() + " " +
                            staff.teacherName.getLastName() + " as " + type);
                }
            }

            remainingNeeds.put(type, Math.max(0, count - assigned));
            if (assigned < count) {
                view.appendOutput("  " + type + ": hired " + assigned + "/" + count +
                        " (remaining shortage: " + (count - assigned) + ")");
            }
        }

        // Try to fill remaining shortages with any available staff
        for (Map.Entry<StaffType, Integer> need : remainingNeeds.entrySet()) {
            StaffType type = need.getKey();
            int count = need.getValue();
            if (count <= 0)
                continue;

            view.appendOutput("  Attempting to fill remaining " + count + " " + type +
                    " position(s) with available staff...");
            List<Staff> remaining = new ArrayList<>(pool.getUnassignedStaff());
            int filled = 0;

            for (Staff staff : remaining) {
                if (filled >= count)
                    break;
                if (pool.assignToSchool(staff, school)) {
                    staff.teacherStatistics.setStaffType(type);
                    newlyAssigned.put(totalAssigned, staff);
                    totalAssigned++;
                    filled++;
                    view.appendOutput("  Reassigned available staff " + staff.teacherName.getFirstName() + " " +
                            staff.teacherName.getLastName() + " as " + type);
                }
            }

            remainingNeeds.put(type, Math.max(0, count - filled));
        }

        // If the pool is exhausted, synthesize additional staff so expansion can
        // react to actual shortages instead of stalling on town-pop limits.
        int syntheticCount = remainingNeeds.values().stream().mapToInt(Integer::intValue).sum();
        if (syntheticCount > 0) {
            view.appendOutput("  Generating " + syntheticCount + " additional staff to cover remaining shortages...");
            List<Staff> generatedStaff = TownPopulationGenerator.generateAdditionalStaff(town, syntheticCount, view);
            int generatedIndex = 0;

            for (Map.Entry<StaffType, Integer> entry : remainingNeeds.entrySet()) {
                StaffType type = entry.getKey();
                for (int i = 0; i < entry.getValue() && generatedIndex < generatedStaff.size(); i++) {
                    Staff generated = generatedStaff.get(generatedIndex++);
                    generated.teacherStatistics.setStaffType(type);
                    if (pool.assignToSchool(generated, school)) {
                        newlyAssigned.put(totalAssigned, generated);
                        totalAssigned++;
                        view.appendOutput("  Generated and hired " + generated.teacherName.getFirstName() + " " +
                                generated.teacherName.getLastName() + " as " + type);
                    }
                }
            }
        }

        view.appendOutput("Hired " + totalAssigned + " additional teachers for " + school.getSchoolName());
        return totalAssigned;
    }

    private static List<Staff> repurposeExistingStaffForShortages(HashMap<Integer, Staff> currentSchoolStaff,
            Map<StaffType, Integer> remainingNeeds,
            GameView view) {
        List<Staff> repurposed = new ArrayList<>();
        Set<Staff> alreadyUsed = new HashSet<>();

        for (Map.Entry<StaffType, Integer> need : remainingNeeds.entrySet()) {
            StaffType neededType = need.getKey();
            int neededCount = need.getValue();
            if (neededCount <= 0) {
                continue;
            }

            List<Staff> candidates = currentSchoolStaff.values().stream()
                    .filter(staff -> canRepurposeExistingStaff((StaffType) staff.teacherStatistics.getStaffType()))
                    .filter(staff -> !alreadyUsed.contains(staff))
                    .sorted(Comparator.comparingInt(staff -> repurposePriority(
                            (StaffType) staff.teacherStatistics.getStaffType())))
                    .toList();

            int filled = 0;
            for (Staff candidate : candidates) {
                if (filled >= neededCount) {
                    break;
                }

                StaffType previousType = (StaffType) candidate.teacherStatistics.getStaffType();
                candidate.teacherStatistics.setStaffType(neededType);
                repurposed.add(candidate);
                alreadyUsed.add(candidate);
                filled++;
                view.appendOutput("  Repurposed " + candidate.teacherName.getFirstName() + " " +
                        candidate.teacherName.getLastName() + " from " + previousType + " to " + neededType);
            }

            remainingNeeds.put(neededType, Math.max(0, neededCount - filled));
        }

        return repurposed;
    }

    private static boolean canRepurposeExistingStaff(StaffType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case SUB, LIBRARY, LUNCH, OFFICE, MAINTENANCE -> true;
            default -> false;
        };
    }

    private static int repurposePriority(StaffType type) {
        if (type == null) {
            return Integer.MAX_VALUE;
        }
        return switch (type) {
            case SUB -> 1;
            case LIBRARY -> 2;
            case LUNCH -> 3;
            case OFFICE -> 4;
            case MAINTENANCE -> 5;
            default -> 10;
        };
    }

    /**
     * Checks if the school has sufficient staff for its curriculum needs.
     *
     * @param pool       the staff pool
     * @param school     the school
     * @param staffNeeds the required staff by type
     * @return true if all needs are met
     */
    public static boolean hasAdequateStaff(StaffPool pool, StandardSchool school,
            Map<StaffType, Integer> staffNeeds) {
        Map<StaffType, Integer> current = getStaffByType(pool, school);

        for (Map.Entry<StaffType, Integer> need : staffNeeds.entrySet()) {
            int have = current.getOrDefault(need.getKey(), 0);
            if (have < need.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the staffing shortages for a school based on curriculum needs.
     *
     * @param pool       the staff pool
     * @param school     the school
     * @param staffNeeds the required staff by type
     * @return map of staff type to shortage count (0 if adequately staffed)
     */
    public static Map<StaffType, Integer> getStaffingShortages(StaffPool pool, StandardSchool school,
            Map<StaffType, Integer> staffNeeds) {
        Map<StaffType, Integer> shortages = new HashMap<>();
        Map<StaffType, Integer> current = getStaffByType(pool, school);

        for (Map.Entry<StaffType, Integer> need : staffNeeds.entrySet()) {
            int have = current.getOrDefault(need.getKey(), 0);
            int shortage = Math.max(0, need.getValue() - have);
            if (shortage > 0) {
                shortages.put(need.getKey(), shortage);
            }
        }
        return shortages;
    }

    /**
     * Gets all staff members of a specific type from a staff map.
     *
     * @param staffHashMap the map of staff members
     * @param type         the staff type to filter by
     * @return list of staff members matching the type
     */
    public static List<Staff> getTeachersOfType(HashMap<Integer, Staff> staffHashMap, StaffType type) {
        List<Staff> staffList = new ArrayList<>();
        for (Map.Entry<Integer, Staff> staff : staffHashMap.entrySet()) {
            if (staff.getValue().teacherStatistics.getStaffType() != null
                    && staff.getValue().teacherStatistics.getStaffType().equals(type)) {
                staffList.add(staff.getValue());
            }
        }
        return staffList;
    }

    /**
     * Assigns a substitute teacher to a room without changing their staff type.
     * This allows the sub to still be counted as a substitute for future needs.
     * The sub is assigned to cover the room temporarily.
     *
     * @param staffHashMap the map of staff members
     * @param view         the game view for output
     * @param room         the room to assign a substitute to
     */
    public static void reassignSubToRoom(HashMap<Integer, Staff> staffHashMap, GameView view, Room room) {
        List<Staff> subs = getTeachersOfType(staffHashMap, StaffType.SUB);
        if (subs.isEmpty()) {
            view.appendOutput("List of subs is empty!");
            view.appendOutput(
                    "WARNING: No substitute available for " + room.getRoomName() + " - room will remain unassigned");
            return;
        }

        // Find a sub that isn't already assigned to a room, or use any sub if all are
        // assigned
        Staff selectedSub = null;
        for (Staff sub : subs) {
            // Prefer subs that don't have a room assignment yet
            selectedSub = sub;
            break;
        }

        if (selectedSub == null) {
            selectedSub = subs.get(0);
        }

        if (subs.size() == 1) {
            view.appendOutput("Subs list is only of size 1");
        }

        // Assign sub to the room WITHOUT changing their staff type
        // This keeps them in the substitute pool for future use
        room.setAssignedStaff(selectedSub);
        view.appendOutput(selectedSub.teacherName.getFirstName() + " " + selectedSub.teacherName.getLastName() +
                " (substitute) assigned to cover " + room.getRoomName());
    }

    /**
     * Calculates initial staff demand dynamically based on student capacity and
     * school
     * facilities. Uses proportional ratios rather than fixed formulas so that
     * staffing
     * scales smoothly with enrollment and is not locked into arbitrary thresholds.
     *
     * Core teacher counts are derived from an assumed student-to-teacher ratio
     * (students / periods / optimal class size) plus a small buffer. Elective and
     * support staff scale with both room availability and student enrollment.
     *
     * @param studentCap the student capacity
     * @param school     the school with room counts
     * @return map of staff type to number needed
     */
    public static Map<StaffType, Integer> calculateInitialStaffDemand(int studentCap, StandardSchool school) {
        Map<StaffType, Integer> demand = new HashMap<>();

        // Scheduling parameters - in a 4x4 block schedule, each teacher teaches
        // up to 4 periods per semester (8 per year). We use 4 as the divisor to
        // account for semester distribution and ensure adequate coverage for all
        // grade levels' required classes.
        int teachingPeriodsPerTeacher = 4;
        // Optimal class size scales with school size (smaller schools can have smaller
        // classes)
        int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));

        // Administration - scales with student population
        demand.put(StaffType.PRINCIPAL, 1);
        demand.put(StaffType.VICE_PRINCIPAL, Math.max(1, studentCap / 500));
        demand.put(StaffType.GUIDANCE, Math.max(2, studentCap / 250));

        // Core teachers - proportional to enrollment
        // Every student takes 1 core class per subject area per semester.
        // Sections needed = studentCap / optimalClassSize (per period slot)
        // Teachers needed = total sections / teachingPeriodsPerTeacher
        int coreSectionsPerSubject = (int) Math.ceil((double) studentCap / optimalClassSize);
        int coreTeachersPerSubject = Math.max(2, (int) Math.ceil(
                (double) coreSectionsPerSubject / teachingPeriodsPerTeacher));

        // English is taken by ALL students every year, so slightly higher demand
        int englishTeachers = Math.max(3, (int) Math.ceil(coreTeachersPerSubject * 1.15));
        demand.put(StaffType.ENGLISH, englishTeachers);
        demand.put(StaffType.MATH, coreTeachersPerSubject);
        demand.put(StaffType.SCIENCE, coreTeachersPerSubject);
        demand.put(StaffType.HISTORY, coreTeachersPerSubject);

        // Language teachers - proportional to freshman enrollment (~25% of students)
        // Each language needs at least 1 teacher; scale with population
        int estimatedFreshmen = studentCap / 4;
        int languageSections = (int) Math.ceil((double) estimatedFreshmen / optimalClassSize);
        int languageTeachers = Math.max(2, (int) Math.ceil(
                (double) languageSections / teachingPeriodsPerTeacher));
        demand.put(StaffType.LANGUAGES, languageTeachers);

        // Elective teachers - driven by available rooms AND student interest
        // Use the larger of room count or estimated demand
        int estimatedElectiveStudents = studentCap / 4; // ~25% take each elective type
        int electiveSectionsFromDemand = Math.max(1,
                (int) Math.ceil((double) estimatedElectiveStudents / optimalClassSize / teachingPeriodsPerTeacher));

        demand.put(StaffType.VISUAL_ARTS, Math.max(electiveSectionsFromDemand, school.getArtStudios().length));
        demand.put(StaffType.PHYSICAL_ED, Math.max(electiveSectionsFromDemand,
                school.getAthleticFields().length + school.getGyms().length));
        demand.put(StaffType.PERFORMING_ARTS, Math.max(electiveSectionsFromDemand,
                school.getMusicRooms().length + school.getDramaRooms().length));
        demand.put(StaffType.VOCATIONAL, Math.max(1, school.getVocationalRooms().length));
        demand.put(StaffType.COMP_SCI, Math.max(1, school.getComputerLabs().length));
        demand.put(StaffType.BUSINESS, Math.max(1, studentCap / 600));
        demand.put(StaffType.CONSUMER_SCI, Math.max(1, studentCap / 800));

        // Support staff - proportional to student population
        demand.put(StaffType.MAINTENANCE, Math.max(2, school.getUtilityrooms().length + studentCap / 500));
        demand.put(StaffType.LIBRARY, Math.max(1, school.getLibraries().length));
        demand.put(StaffType.OFFICE, Math.max(2, studentCap / 350));
        demand.put(StaffType.NURSE, Math.max(1, studentCap / 500));

        // Lunch staff based on lunchroom capacity
        int lunchStaff = 0;
        for (Lunchroom lunchroom : school.getLunchrooms()) {
            lunchStaff += lunchroom.getStaffCapacity();
        }
        demand.put(StaffType.LUNCH, Math.max(2, lunchStaff));

        // Substitutes - proportional pool for coverage
        demand.put(StaffType.SUB, Math.max(5, studentCap / 200));

        return demand;
    }

    /**
     * Assigns initial roles to staff from a HashMap.
     * This method assigns staff types based on calculated demand from student
     * capacity
     * and school facilities.
     *
     * @param staffHashMap the map of staff members to assign roles to
     * @param studentCap   the student capacity
     * @param view         the game view for output
     * @param school       the school with room information
     */
    public static void assignInitialStaffRoles(HashMap<Integer, Staff> staffHashMap, int studentCap,
            GameView view, StandardSchool school) {
        // Calculate demand
        Map<StaffType, Integer> demand = calculateInitialStaffDemand(studentCap, school);

        // Create a working copy of the staff map
        HashMap<Integer, Staff> availableStaff = new HashMap<>(staffHashMap);

        // Assign staff by type in priority order
        StaffType[] priorityOrder = {
                StaffType.PRINCIPAL, StaffType.VICE_PRINCIPAL, StaffType.GUIDANCE,
                StaffType.ENGLISH, StaffType.MATH, StaffType.SCIENCE, StaffType.HISTORY,
                StaffType.LANGUAGES, StaffType.VISUAL_ARTS, StaffType.PHYSICAL_ED,
                StaffType.PERFORMING_ARTS, StaffType.VOCATIONAL, StaffType.COMP_SCI,
                StaffType.MAINTENANCE, StaffType.LIBRARY, StaffType.OFFICE,
                StaffType.NURSE, StaffType.LUNCH, StaffType.BUSINESS
        };

        for (StaffType type : priorityOrder) {
            int needed = demand.getOrDefault(type, 0);
            for (int i = 0; i < needed; i++) {
                Optional<Staff> optionalStaff = selectRandomUnassignedStaff(availableStaff);
                if (optionalStaff.isPresent()) {
                    Staff staff = optionalStaff.get();
                    staff.teacherStatistics.setStaffType(type);
                    view.appendOutput("Staff " + staff.teacherName.getFirstName() + " " +
                            staff.teacherName.getLastName() + " assigned as " + type);
                } else {
                    view.appendOutput("Warning: Not enough staff to assign as " + type);
                    break;
                }
            }
        }

        // Assign remaining staff as substitutes
        for (Staff staff : availableStaff.values()) {
            if (staff.teacherStatistics.getStaffType() == null) {
                staff.teacherStatistics.setStaffType(StaffType.SUB);
                view.appendOutput("Staff " + staff.teacherName.getFirstName() + " " +
                        staff.teacherName.getLastName() + " assigned as substitute");
            }
        }
    }

    /**
     * Selects a random staff member who doesn't yet have a type assigned.
     *
     * @param staffHashMap the map of staff members
     * @return an optional containing an unassigned staff member, or empty if none
     *         available
     */
    private static Optional<Staff> selectRandomUnassignedStaff(HashMap<Integer, Staff> staffHashMap) {
        List<Integer> keys = new ArrayList<>(staffHashMap.keySet());
        int counter = 0;

        while (counter < staffHashMap.size()) {
            int randomIndex = GameRandom.nextInt(keys.size());
            int key = keys.get(randomIndex);
            Staff staff = staffHashMap.get(key);

            if (staff.teacherStatistics.getStaffType() == null) {
                return Optional.of(staff);
            }
            counter++;
        }

        return Optional.empty();
    }
}
