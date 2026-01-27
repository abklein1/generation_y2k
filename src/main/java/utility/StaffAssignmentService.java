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
     * @param pool the staff pool
     * @param school the school to assign staff to
     * @param count the number of staff to assign
     * @param studentCount the number of students (affects role distribution)
     * @param view the game view for output
     * @return the number of staff actually assigned
     */
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
        
        // Assign roles using existing StaffAssignment logic
        StaffAssignment.initialAssignments(assignedMap, studentCount, view, school);
        
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
}
