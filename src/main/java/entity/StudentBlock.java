package entity;

import entity.Rooms.Room;

public class StudentBlock {
    int blockNumber;
    String className;
    Staff teacher;
    boolean lunch;
    char lunchBlock;
    Room room;
    String semester;

    public StudentBlock() {
        blockNumber = 0;
        className = "";
        teacher = null;
        lunch = false;
        lunchBlock = ' ';
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

    public Staff getTeacher() {
        return teacher;
    }

    public void setTeacher(Staff teacher) {
        this.teacher = teacher;
    }

    public boolean isLunch() {
        return lunch;
    }

    public void setLunch(boolean lunch) {
        this.lunch = lunch;
    }

    public char getLunchBlock() {
        return lunchBlock;
    }

    public void setLunchBlock(char lunchBlock) {
        this.lunchBlock = lunchBlock;
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
