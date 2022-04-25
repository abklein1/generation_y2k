//*******************************************************************
//  Classroom.java
//  Description: This represents a classroom object. Implements Room
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

public class Classroom implements Room {

    private int roomCapacity;
    private int numOfConnections;
    private int windowCount;
    private String roomName;
    private int numOfDoors;
    private int staffCap;
    private int studentCap;
    private int roomNumber;
    private boolean studentRestriction;
    private String classRoomType;

    public Classroom() {
        this.roomCapacity = 0;
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = 0;
        this.studentRestriction = false;
        this.classRoomType = null;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setRoomCapacity(int capacity) {
        this.roomCapacity = capacity;
    }

    @Override
    public void setConnections(int connections) {
        this.numOfConnections = connections;
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
    public void setRoomNumber(int roomNumber) {
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
        switch (select){
            case 0: this.classRoomType = "Math";
            case 1: this.classRoomType = "English";
            case 2: this.classRoomType = "Science";
            case 3: this.classRoomType = "Social Studies";
            case 4: this.classRoomType = "Electives";
            case 5: this.classRoomType = "Homeroom";
            case 6: this.classRoomType = "Study Hall";
        }
    }

    public void setDetention(){
        this.classRoomType = "Detention";
    }
}
