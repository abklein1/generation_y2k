public class Office implements Room{

    private int roomCapacity;
    private int numOfConnections;
    private int windowCount;
    private String roomName;
    private int numOfDoors;
    private int staffCap;
    private int studentCap;
    private int roomNumber;

    public Office() {
        this.roomCapacity = 0;
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setRoomCapacity(int capacity) {

    }

    @Override
    public void setConnections(int connections) {

    }

    @Override
    public void setWindowCount(int windows) {

    }

    @Override
    public void setRoomName(String roomName) {

    }

    @Override
    public void setDoors(int doors) {

    }

    @Override
    public void setInitialStaff(int staffCount) {

    }

    @Override
    public void setInitialStudents(int studentCount) {

    }

    @Override
    public void setRoomNumber(int roomNumber) {

    }

    @Override
    public void setStudentRestriction(boolean studentRestriction) {

    }
}
