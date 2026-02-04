package entity.Rooms;

import entity.Staff;
import entity.Student;
import utility.GameLogger;
import utility.StaffAssignmentService;
import view.GameView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utility.StudentSeatingAssigner.initialSeatingGenerator;

public abstract class Room implements Serializable {

    protected List<Staff> staffAssign;
    protected List<Student> students;
    protected HashMap<Integer, Student[][]> seatingArrangements;
    protected int numOfConnections;
    protected int windowCount;
    protected String roomName;
    protected int numOfDoors;
    protected int staffCap;
    protected int studentCap;
    protected String roomNumber;
    protected boolean studentRestriction;
    protected Student[][] seats;
    protected int stallNumber;
    protected boolean restrictM;
    protected boolean restrictF;
    protected String classRoomType;
    
    // Room divider fields - allows a room to be split into two teaching spaces
    /** Whether this room has a divider that can split it into two spaces */
    protected boolean hasDivider = false;
    /** Whether the divider is currently deployed (room is split) */
    protected boolean isDivided = false;
    /** Reference to the other "half" of the room when divided */
    protected Room dividedPartner = null;
    /** Original capacity before division (for restoration) */
    protected int originalCapacity = 0;
    /** Reference to the second teacher when room is divided */
    protected Staff secondTeacher = null;

    public Room() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = null;
        this.studentRestriction = false;
        this.stallNumber = 0;
        this.hasDivider = false;
        this.isDivided = false;
        this.originalCapacity = 0;
        this.staffAssign = new ArrayList<>();
        this.students = new ArrayList<>();
        this.seatingArrangements = new HashMap<>();
    }

    public void reset() {
    }

    public int getConnections() {
        return this.numOfConnections;
    }

    public void setConnections(int connections) {
        this.numOfConnections = connections;
    }

    public void setWindowCount(int windows) {
        this.windowCount = windows;
    }

    public void setDoors(int doors) {
        this.numOfDoors = doors;
    }

    public void setInitialStaff(int staffCount) {
        this.staffCap = staffCount;
    }

    public void setInitialStudents(int studentCount) {
        this.studentCap = studentCount;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setStudentRestriction(boolean studentRestriction) {
        this.studentRestriction = studentRestriction;
    }

    public int getStudentCapacity() {
        return studentCap;
    }

    public int getStaffCapacity() {
        return staffCap;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<Staff> getAssignedStaff() {
        return this.staffAssign;
    }

    public void setAssignedStaff(Staff staff) {
        staffAssign.add(staff);
    }

    public void removeAssignedStaff(Staff staff) {
        staffAssign.remove(staff);
    }
    // TODO: add logic for different rooms since this is now a base class and remove magic numbers
    public void setSeatArrangement() {
        seats = initialSeatingGenerator(studentCap);
    }

    public Student[][] getSeatArrangement() {
        return seats;
    }

    public Student getStudentInSeat(int x, int y) {
        return seats[x][y];
    }

    public int[] getStudentSeatCoordinate(Student student) {
        int[] coords = new int[2];
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                if (seats[i][j].equals(student)) {
                    coords[0] = i;
                    coords[1] = j;
                    return coords;
                }
            }
        }
        GameLogger.logScheduling("Can't find student " + student.studentName);
        return coords;
    }

    public void addStudentToSeat(Student student, int x, int y) {
        if (seats[x][y] != null) {
            GameLogger.logScheduling(student.studentName + " can't be assigned to seat because there is already someone there!");
        } else {
            seats[x][y] = student;
        }
    }

    public void removeStudentFromSeat(Student student) {
        int[] coords = getStudentSeatCoordinate(student);
        seats[coords[0]][coords[1]] = null;
    }

    public void swapStudentSeats(Student student1, Student student2) {
        int[] coords1 = getStudentSeatCoordinate(student1);
        int[] coords2 = getStudentSeatCoordinate(student2);

        removeStudentFromSeat(student1);
        removeStudentFromSeat(student2);

        addStudentToSeat(student1, coords2[0], coords2[1]);
        addStudentToSeat(student2, coords1[0], coords1[1]);
    }

    public int getRoomCapacity() {
        return this.studentCap + this.staffCap;
    }

    public void setStudentCap(int studentCap) {
        this.studentCap = studentCap;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public List<Student> getStudents() {
        return this.students;
    }

    public void setPeriodSeatingArrangement(int period, Student[][] seatArrangement) {
        seatingArrangements.put(period, seatArrangement);
    }

    public HashMap<Integer, Student[][]> getPeriodSeatingArrangement() {
        return seatingArrangements;
    }

    public void initializeSeatingArrangements(int totalPeriods) {
        for (int period = 0; period < totalPeriods; period++) {
            setPeriodSeatingArrangement(period, getSeatArrangement());
        }
    }

    public void setStallNumber(int stallNumber) {
        this.stallNumber = stallNumber;
    }

    public void setRoomRestrictions(boolean restrictM, boolean restrictF) {
        this.restrictM = restrictM;
        this.restrictF = restrictF;
    }

    public void reassignClassroomByTeacher(HashMap<Integer, Staff> staffHashMap, GameView view) {
        String roomType = getClassRoomType();
        String staffType;
        if (!getAssignedStaff().isEmpty()) {
            staffType = getAssignedStaff().get(0).teacherStatistics.getStaffType().toString();
            if (!roomType.equals(staffType)) {
                setClassRoomType(staffType);
                view.appendOutput("Classroom " + getRoomName() + " reassigned to " + staffType + " from " + roomType);
            }
        } else {
            view.appendOutput("Classroom " + getRoomName() + " has no staff!");
            StaffAssignmentService.reassignSubToRoom(staffHashMap, view, this);
            // Only recurse if a substitute was actually assigned - prevents infinite recursion
            if (!getAssignedStaff().isEmpty()) {
                reassignClassroomByTeacher(staffHashMap, view);
            } else {
                // No substitute available - leave room without staff assignment
                view.appendOutput("WARNING: No substitute available for " + getRoomName() + " - room will remain unassigned");
            }
        }
    }

    public void setClassroomType(int select) {
        switch (select) {
            case 0 -> this.classRoomType = "Math";
            case 1 -> this.classRoomType = "English";
            case 2 -> this.classRoomType = "Science";
            case 3 -> this.classRoomType = "History";
            case 4 -> this.classRoomType = "Language";
            case 5 -> this.classRoomType = "Electives";
            case 6 -> this.classRoomType = "Study Hall";
        }
    }

    public void setDetention() {
        this.classRoomType = "Detention";
    }

    public String getClassTypeAbbr() {
        String abbr = null;
        String type = getClassRoomType();

        switch (type) {
            case "Math" -> abbr = "MAT";
            case "English" -> abbr = "ENG";
            case "Science" -> abbr = "SCI";
            case "History" -> abbr = "HST";
            case "Language" -> abbr = "LNG";
            case "Vocational" -> abbr = "VOC";
            case "Consumer Science", "Business", "Computer Science" -> abbr = "ELC";
            case "Study Hall" -> abbr = "STY";
            default -> GameLogger.logScheduling("No known class type!");
        }

        return abbr;

    }

    public String getClassRoomType() {
        return classRoomType;
    }

    public void setClassRoomType(String type) {
        this.classRoomType = type;
    }

    public void setUtilityType(UtilityRoom.utilityType utilityType) {
    }

    protected enum utilityType {
        IT_CLOSET, JANITOR, KITCHEN, POWER_PLANT, STORAGE
    }

    @Override
    public String toString() {
        return this.roomName;
    }

    // ==================== Room Divider Methods ====================

    /**
     * Checks if this room has a divider that can split it into two spaces.
     *
     * @return true if the room has a divider
     */
    public boolean hasDivider() {
        return hasDivider;
    }

    /**
     * Sets whether this room has a divider.
     *
     * @param hasDivider true if the room should have a divider
     */
    public void setHasDivider(boolean hasDivider) {
        this.hasDivider = hasDivider;
    }

    /**
     * Checks if the room is currently divided into two teaching spaces.
     *
     * @return true if the room is currently divided
     */
    public boolean isDivided() {
        return isDivided;
    }

    /**
     * Gets the effective capacity of this room, accounting for division.
     * When divided, capacity is halved.
     *
     * @return the effective student capacity
     */
    public int getEffectiveCapacity() {
        if (isDivided) {
            return studentCap / 2;
        }
        return studentCap;
    }

    /**
     * Divides the room into two teaching spaces using the divider.
     * Each half gets approximately 50% of the original capacity.
     * Requires the room to have a divider and not already be divided.
     *
     * @return true if division was successful
     */
    public boolean divide() {
        if (!hasDivider) {
            GameLogger.logScheduling("Cannot divide " + roomName + " - no divider installed");
            return false;
        }
        if (isDivided) {
            GameLogger.logScheduling("Cannot divide " + roomName + " - already divided");
            return false;
        }
        
        isDivided = true;
        originalCapacity = studentCap;
        // Note: studentCap remains the same, but getEffectiveCapacity() returns half
        GameLogger.logScheduling(roomName + " divided - effective capacity now " + getEffectiveCapacity() + 
                          " per side (was " + originalCapacity + " total)");
        return true;
    }

    /**
     * Removes the division, restoring the room to a single teaching space.
     *
     * @return true if undivision was successful
     */
    public boolean undivide() {
        if (!isDivided) {
            GameLogger.logScheduling("Cannot undivide " + roomName + " - not currently divided");
            return false;
        }
        
        isDivided = false;
        secondTeacher = null;
        dividedPartner = null;
        GameLogger.logScheduling(roomName + " undivided - capacity restored to " + studentCap);
        return true;
    }

    /**
     * Gets the original capacity before division.
     *
     * @return the original capacity, or current capacity if never divided
     */
    public int getOriginalCapacity() {
        return originalCapacity > 0 ? originalCapacity : studentCap;
    }

    /**
     * Gets the second teacher assigned to the divided room.
     *
     * @return the second teacher, or null if not divided or no second teacher
     */
    public Staff getSecondTeacher() {
        return secondTeacher;
    }

    /**
     * Sets the second teacher for the divided room.
     *
     * @param teacher the second teacher to assign
     * @return true if successful
     */
    public boolean setSecondTeacher(Staff teacher) {
        if (!isDivided) {
            GameLogger.logScheduling("Cannot assign second teacher to " + roomName + " - room not divided");
            return false;
        }
        this.secondTeacher = teacher;
        return true;
    }

    /**
     * Gets the divided partner room (the other "half" when divided).
     *
     * @return the partner room, or null if not divided
     */
    public Room getDividedPartner() {
        return dividedPartner;
    }

    /**
     * Sets the divided partner room.
     *
     * @param partner the partner room
     */
    public void setDividedPartner(Room partner) {
        this.dividedPartner = partner;
    }

    /**
     * Checks if this room can be divided (has divider and large enough capacity).
     *
     * @param minCapacity the minimum capacity required for division
     * @return true if the room can be divided
     */
    public boolean canDivide(int minCapacity) {
        return hasDivider && !isDivided && studentCap >= minCapacity;
    }

}
