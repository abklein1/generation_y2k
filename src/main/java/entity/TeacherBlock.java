package entity;

import entity.Rooms.Room;

public class TeacherBlock {
    int blockNumber;
    String className;
    Room room;
    String semester;

    public TeacherBlock() {
        blockNumber = 0;
        className = "";
        room = null;
        semester = "";
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
