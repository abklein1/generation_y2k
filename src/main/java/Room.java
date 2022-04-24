public interface Room {

    void reset();

    void setRoomCapacity(int capacity);

    void setConnections(int connections);

    void setWindowCount(int windows);

    void setRoomName(String roomName);

    void setDoors(int doors);

    void setInitialStaff(int staffCount);

    void setInitialStudents(int studentCount);

    void setRoomNumber(int roomNumber);

    void setStudentRestriction(boolean studentRestriction);

}
