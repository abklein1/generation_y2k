package utility;

import entity.Rooms.Classroom;
import entity.Staff;
import entity.StaffPool;
import entity.StaffType;
import entity.StandardSchool;
import entity.Town;
import view.GameView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for assigning staff from a population pool to schools.
 * Handles role assignment, room assignment, and class scheduling.
 */
public class StaffAssignmentService {

    /**
     * Assigns staff from the town's pool to a school based on school requirements.
     *
     * @param town the town containing the staff pool
     * @param school the school to assign staff to
     * @param studentCount the number of students (affects staff requirements)
     * @param view the game view for output
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
     * @deprecated Use {@link #assignStaffByDemand(StaffPool, StandardSchool, Map, GameView)} instead
     *             for demand-driven staffing based on curriculum requirements.
     *
     * @param pool the staff pool
     * @param school the school to assign staff to
     * @param count the number of staff to assign
     * @param studentCount the number of students (affects role distribution)
     * @param view the game view for output
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
        
        // NOTE: Legacy role assignment removed - use assignStaffByDemand() instead
        // The demand-driven approach assigns roles based on curriculum requirements
        // StaffAssignment.initialAssignments() is deprecated and should not be called here
        
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
     * Assigns classes to staff schedules.
     *
     * @param pool the staff pool
     * @param school the school
     * @param view the game view for output
     */
    public static void assignClassesToStaff(StaffPool pool, StandardSchool school, GameView view) {
        HashMap<Integer, Staff> staffMap = pool.getStaffBySchoolAsMap(school);
        StaffAssignment.assignClassesToStaff(staffMap, school, view);
    }

    /**
     * Hires a new staff member for a school.
     *
     * @param pool the staff pool
     * @param staff the staff member to hire
     * @param school the school to hire for
     * @param role the role to assign
     * @param view the game view for output
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
     * @param pool the staff pool
     * @param school the school needing a substitute
     * @param forType the type of staff being substituted for
     * @param view the game view for output
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
     * @param pool the staff pool
     * @param substitute the substitute to return
     * @param view the game view for output
     */
    public static void returnSubstitute(StaffPool pool, Staff substitute, GameView view) {
        pool.unassignFromSchool(substitute);
        view.appendOutput("Substitute " + substitute.teacherName.getFirstName() + " " + 
                substitute.teacherName.getLastName() + " returned to substitute pool");
    }

    /**
     * Transfers a staff member from one school to another.
     *
     * @param pool the staff pool
     * @param staff the staff member to transfer
     * @param fromSchool the current school
     * @param toSchool the destination school
     * @param view the game view for output
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
     * @param pool the staff pool
     * @param staff the staff member to release
     * @param school the school to release from
     * @param view the game view for output
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
     * @param pool the staff pool
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
     * Assigns staff to a school based on curriculum demand analysis.
     * This method assigns staff by type to meet specific curriculum requirements.
     *
     * @param pool the staff pool
     * @param school the school to assign staff to
     * @param staffNeeds map of staff type to number needed
     * @param view the game view for output
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
            if (needed <= 0) continue;
            
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
        
        // If there are shortages, try to fill with substitutes or unassigned staff
        if (shortages > 0) {
            view.appendOutput("  Total shortages: " + shortages + " - attempting to fill with available staff");
            List<Staff> remaining = pool.getUnassignedStaff();
            int filled = 0;
            for (Staff staff : remaining) {
                if (filled >= shortages) break;
                if (pool.assignToSchool(staff, school)) {
                    // Ensure staff has a type - default to SUB if null
                    if (staff.teacherStatistics.getStaffType() == null) {
                        staff.teacherStatistics.setStaffType(StaffType.SUB);
                    }
                    assignedMap.put(totalAssigned, staff);
                    totalAssigned++;
                    filled++;
                }
            }
            if (filled > 0) {
                view.appendOutput("  Filled " + filled + " positions with available staff");
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
     * Checks if the school has sufficient staff for its curriculum needs.
     *
     * @param pool the staff pool
     * @param school the school
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
     * @param pool the staff pool
     * @param school the school
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
}
