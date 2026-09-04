package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import entity.Rooms.Room;
import utility.GameLogger;

public class TeacherBlock implements Serializable {
    private static final long serialVersionUID = 1L;

    int blockNumber;
    String className;
    Room room;
    String semester;
    List<Student> classPopulation;
    int capacity;

    public TeacherBlock() {
        blockNumber = 0;
        className = "";
        room = null;
        semester = "";
        capacity = -1;
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

    public void addClassPopulationBlock(int studentCap) {
        setBlockPopulation(studentCap);
        classPopulation = new ArrayList<>(capacity);
    }

    public int getClassPopulationBlock() {
        return classPopulation.size();
    }

    public void addStudentToBlock(Student student) {
        if (classPopulation == null) {
            GameLogger.logScheduling("Block does not exist!");
            return;
        }

        if (classPopulation.contains(student)) {
            return;
        }

        if (classPopulation.size() >= capacity) {
            GameLogger.logScheduling("Block is full!");
            return;
        }

        classPopulation.add(student);
    }

    public int getBlockPopulation() {
        return capacity;
    }

    public void setBlockPopulation(int studentCap) {
        capacity = studentCap;
    }

    public List<Student> getClassPopulation() {
        return classPopulation;
    }
}