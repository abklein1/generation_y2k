package entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages a pool of staff with tracking for school assignments.
 * This allows staff to exist independently of schools and be assigned/transferred as needed.
 * Supports features like substitute teachers, staff pools for rival schools, and mid-year hires.
 */
public class StaffPool implements Serializable {

    private static final long serialVersionUID = 1L;

    // All staff in the pool, keyed by a unique ID
    private final HashMap<Integer, Staff> allStaff;

    // Tracks which school each staff member is assigned to (null = unassigned/substitute pool)
    private final Map<Staff, StandardSchool> staffAssignments;

    // Counter for generating unique staff IDs
    private int nextStaffId;

    /**
     * Creates a new empty StaffPool.
     */
    public StaffPool() {
        this.allStaff = new HashMap<>();
        this.staffAssignments = new HashMap<>();
        this.nextStaffId = 0;
    }

    // ==================== Adding Staff ====================

    /**
     * Adds a staff member to the pool.
     *
     * @param staff the staff member to add
     * @return the ID assigned to the staff member
     */
    public int addStaff(Staff staff) {
        int id = nextStaffId++;
        allStaff.put(id, staff);
        staffAssignments.put(staff, null); // Initially unassigned
        return id;
    }

    /**
     * Adds multiple staff members to the pool.
     *
     * @param staffList the list of staff to add
     */
    public void addStaff(List<Staff> staffList) {
        for (Staff staff : staffList) {
            addStaff(staff);
        }
    }

    /**
     * Adds staff from a HashMap (for compatibility with existing code).
     *
     * @param staffHashMap the HashMap of staff to add
     */
    public void addStaffFromMap(HashMap<Integer, Staff> staffHashMap) {
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            // Use the existing key if it's larger than our counter
            int id = entry.getKey();
            if (id >= nextStaffId) {
                nextStaffId = id + 1;
            }
            allStaff.put(id, entry.getValue());
            staffAssignments.put(entry.getValue(), null);
        }
    }

    // ==================== Assignment Management ====================

    /**
     * Assigns a staff member to a school.
     *
     * @param staff the staff member to assign
     * @param school the school to assign to
     * @return true if assignment was successful, false if staff not in pool
     */
    public boolean assignToSchool(Staff staff, StandardSchool school) {
        if (!staffAssignments.containsKey(staff)) {
            return false;
        }
        staffAssignments.put(staff, school);
        return true;
    }

    /**
     * Unassigns a staff member from their current school (returns to substitute pool).
     *
     * @param staff the staff member to unassign
     * @return the school they were assigned to, or null if not assigned
     */
    public StandardSchool unassignFromSchool(Staff staff) {
        StandardSchool previousSchool = staffAssignments.get(staff);
        if (previousSchool != null) {
            staffAssignments.put(staff, null);
        }
        return previousSchool;
    }

    /**
     * Transfers a staff member from one school to another.
     *
     * @param staff the staff member to transfer
     * @param newSchool the new school
     * @return true if transfer was successful
     */
    public boolean transferStaff(Staff staff, StandardSchool newSchool) {
        if (!staffAssignments.containsKey(staff)) {
            return false;
        }
        staffAssignments.put(staff, newSchool);
        return true;
    }

    /**
     * Gets the school a staff member is assigned to.
     *
     * @param staff the staff member
     * @return the school, or null if unassigned
     */
    public StandardSchool getAssignedSchool(Staff staff) {
        return staffAssignments.get(staff);
    }

    /**
     * Checks if a staff member is assigned to any school.
     *
     * @param staff the staff member
     * @return true if assigned to a school
     */
    public boolean isAssigned(Staff staff) {
        return staffAssignments.get(staff) != null;
    }

    // ==================== Querying Staff ====================

    /**
     * Gets all staff in the pool.
     *
     * @return HashMap of all staff
     */
    public HashMap<Integer, Staff> getAllStaff() {
        return new HashMap<>(allStaff);
    }

    /**
     * Gets all unassigned staff (substitute pool).
     *
     * @return list of unassigned staff
     */
    public List<Staff> getUnassignedStaff() {
        return staffAssignments.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets all unassigned staff - alias for getUnassignedStaff for clarity.
     *
     * @return list of available substitute staff
     */
    public List<Staff> getSubstitutes() {
        return getUnassignedStaff();
    }

    /**
     * Gets all staff assigned to a specific school.
     *
     * @param school the school
     * @return list of staff assigned to that school
     */
    public List<Staff> getStaffBySchool(StandardSchool school) {
        return staffAssignments.entrySet().stream()
                .filter(entry -> school.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets staff assigned to a school as a HashMap (for compatibility).
     *
     * @param school the school
     * @return HashMap of staff assigned to that school
     */
    public HashMap<Integer, Staff> getStaffBySchoolAsMap(StandardSchool school) {
        HashMap<Integer, Staff> result = new HashMap<>();
        int index = 0;
        for (Map.Entry<Integer, Staff> entry : allStaff.entrySet()) {
            Staff staff = entry.getValue();
            if (school.equals(staffAssignments.get(staff))) {
                result.put(index++, staff);
            }
        }
        return result;
    }

    /**
     * Gets unassigned staff by staff type.
     *
     * @param staffType the staff type
     * @return list of unassigned staff of that type
     */
    public List<Staff> getUnassignedByType(StaffType staffType) {
        return getUnassignedStaff().stream()
                .filter(s -> staffType.equals(s.teacherStatistics.getStaffType()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all staff by staff type (regardless of assignment).
     *
     * @param staffType the staff type
     * @return list of staff of that type
     */
    public List<Staff> getByType(StaffType staffType) {
        return allStaff.values().stream()
                .filter(s -> staffType.equals(s.teacherStatistics.getStaffType()))
                .collect(Collectors.toList());
    }

    /**
     * Gets available staff that can teach a specific subject.
     *
     * @param staffType the subject/staff type needed
     * @return list of available staff qualified to teach that subject
     */
    public List<Staff> getAvailableStaffForSubject(StaffType staffType) {
        return getUnassignedStaff().stream()
                .filter(s -> {
                    Enum<?> typeEnum = s.teacherStatistics.getStaffType();
                    // SUB can teach any subject, or match the specific type
                    return typeEnum == StaffType.SUB || typeEnum == staffType;
                })
                .collect(Collectors.toList());
    }

    // ==================== Statistics ====================

    /**
     * Gets the total number of staff in the pool.
     *
     * @return total staff count
     */
    public int getTotalCount() {
        return allStaff.size();
    }

    /**
     * Gets the number of unassigned staff.
     *
     * @return unassigned staff count
     */
    public int getUnassignedCount() {
        return (int) staffAssignments.values().stream()
                .filter(school -> school == null)
                .count();
    }

    /**
     * Gets the number of staff assigned to a specific school.
     *
     * @param school the school
     * @return assigned staff count for that school
     */
    public int getAssignedCount(StandardSchool school) {
        return (int) staffAssignments.values().stream()
                .filter(s -> school.equals(s))
                .count();
    }

    /**
     * Checks if there are any unassigned staff.
     *
     * @return true if there are unassigned staff
     */
    public boolean hasUnassigned() {
        return staffAssignments.values().stream().anyMatch(school -> school == null);
    }

    /**
     * Gets a staff member by their ID.
     *
     * @param id the staff ID
     * @return the staff member, or null if not found
     */
    public Staff getStaffById(int id) {
        return allStaff.get(id);
    }

    /**
     * Removes a staff member from the pool entirely.
     *
     * @param staff the staff member to remove
     * @return true if the staff member was removed
     */
    public boolean removeStaff(Staff staff) {
        Integer keyToRemove = null;
        for (Map.Entry<Integer, Staff> entry : allStaff.entrySet()) {
            if (entry.getValue().equals(staff)) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null) {
            allStaff.remove(keyToRemove);
            staffAssignments.remove(staff);
            return true;
        }
        return false;
    }

    /**
     * Clears all staff from the pool.
     */
    public void clear() {
        allStaff.clear();
        staffAssignments.clear();
        nextStaffId = 0;
    }

    @Override
    public String toString() {
        return "StaffPool{" +
                "total=" + getTotalCount() +
                ", unassigned=" + getUnassignedCount() +
                '}';
    }
}
