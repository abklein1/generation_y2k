package entity.Rooms;//*******************************************************************
//  entity.Rooms.Classroom.java
//  Description: This represents a classroom object. Implements entity.Rooms.Room
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.Staff;
import entity.Student;
import utility.Randomizer;
import utility.StaffAssignment;
import view.GameView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Classroom implements Room, Serializable {

    private final List<Staff> staffAssign;
    private final List<Student> students;
    private int numOfConnections;
    private int windowCount;
    private String roomName;
    private int numOfDoors;
    private int staffCap;
    private int studentCap;
    private String roomNumber;
    private boolean studentRestriction;
    private String classRoomType;
    private Student[][] seats;
    private HashMap<Integer, Student[][]> seatingArrangements;

    public Classroom() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = null;
        this.studentRestriction = false;
        this.classRoomType = null;
        this.staffAssign = new ArrayList<>();
        this.students = new ArrayList<>();
        this.seatingArrangements = new HashMap<>();
    }

    @Override
    public void reset() {

    }

    @Override
    public void setWindowCount(int windows) {
        this.windowCount = windows;
    }

    @Override
    public void setDoors(int doors) {
        this.numOfDoors = doors;
    }

    @Override
    public void setInitialStaff(int staffCount) {
        this.staffCap = staffCount;
    }

    @Override
    public void setInitialStudents(int studentCount) {
        this.studentCap = studentCount;
    }

    @Override
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public void setStudentRestriction(boolean studentRestriction) {
        this.studentRestriction = studentRestriction;
    }

    public String getRoomName() {
        return this.roomName;
    }

    @Override
    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    @Override
    public int getStudentCapacity() {
        return studentCap;
    }

    @Override
    public int getStaffCapacity() {
        return staffCap;
    }

    public String getClassRoomType() {
        return classRoomType;
    }

    public void setClassRoomType(String type) {
        this.classRoomType = type;
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
            default -> System.out.println("No known class type!");
        }

        return abbr;

    }

    @Override
    public int getConnections() {
        return this.numOfConnections;
    }

    @Override
    public void setConnections(int connections) {
        this.numOfConnections = connections;
    }

    @Override
    public String toString() {
        return this.roomName;
    }

    @Override
    public List<Staff> getAssignedStaff() {
        return this.staffAssign;
    }

    @Override
    public void setAssignedStaff(Staff staff) {
        staffAssign.add(staff);
    }

    @Override
    public void removeAssignedStaff(Staff staff) {
        staffAssign.remove(staff);
    }

    public void reassignClassroomByTeacher(HashMap<Integer, Staff> staffHashMap, GameView view) {
        String roomType = getClassRoomType();
        String staffType;
        if(!getAssignedStaff().isEmpty()) {
            staffType = getAssignedStaff().get(0).teacherStatistics.getStaffType().toString();
            if (!roomType.equals(staffType)) {
                setClassRoomType(staffType);
                view.appendOutput("Classroom " + getRoomName() + " reassigned to " + staffType + " from " + roomType);
            }
        } else {
            view.appendOutput("Classroom " + getRoomName() + " has no staff!");
            StaffAssignment.reassignSubToRoom(staffHashMap,view,this);
            // recursive call be careful here
            reassignClassroomByTeacher(staffHashMap, view);
        }
    }

    // TODO: Later we can make desk arrangements that are not only squares/rectangles
    @Override
    public void setSeatArrangement() {
        int choice = Randomizer.setRandom(0, 2);
        if (studentCap <= 16) {
            if (choice == 0) {
                seats = new Student[4][4];
            } else if (choice == 1) {
                seats = new Student[4][5];
            } else {
                seats = new Student[5][4];
            }
        } else if (studentCap <= 25) {
            if (choice == 0) {
                seats = new Student[5][5];
            } else if (choice == 1) {
                seats = new Student[5][6];
            } else {
                seats = new Student[6][5];
            }
        } else if (studentCap <= 48) {
            if (choice == 0) {
                seats = new Student[6][8];
            } else if (choice == 1) {
                seats = new Student[8][6];
            } else {
                seats = new Student[12][4];
            }
        } else {
            // TODO: Better error handling later
            System.out.println("Can't find student cap!");
        }
    }

    @Override
    public Student[][] getSeatArrangement() {
        return seats;
    }

    @Override
    public Student getStudentInSeat(int x, int y) {
        return seats[x][y];
    }

    @Override
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
        System.out.println("Can't find student " + student.studentName);
        return coords;
    }

    @Override
    public void addStudentToSeat(Student student, int x, int y) {
        if (seats[x][y] != null) {
            System.out.println(student.studentName + " can't be assigned to seat because there is already someone there!");
        } else {
            seats[x][y] = student;
        }
    }

    @Override
    public void removeStudentFromSeat(Student student) {
        int[] coords = getStudentSeatCoordinate(student);
        seats[coords[0]][coords[1]] = null;
    }

    @Override
    public void swapStudentSeats(Student student1, Student student2) {
        int[] coords1 = getStudentSeatCoordinate(student1);
        int[] coords2 = getStudentSeatCoordinate(student2);

        removeStudentFromSeat(student1);
        removeStudentFromSeat(student2);

        addStudentToSeat(student1, coords2[0], coords2[1]);
        addStudentToSeat(student2, coords1[0], coords1[1]);
    }

    @Override
    public void setStudentCap(int studentCap) {
        this.studentCap = studentCap;
    }

    @Override
    public int getRoomCapacity() {return this.studentCap + this.staffCap;}

    @Override
    public void addStudent(Student student) {students.add(student);}
    @Override
    public List<Student> getStudents() { return this.students;}

    @Override
    public void setPeriodSeatingArrangement(int period, Student[][] seatArrangement) {
        seatingArrangements.put(period, seatArrangement);
    }

    @Override
    public HashMap<Integer, Student[][]> getPeriodSeatingArrangement() {
        return seatingArrangements;
    }

}
