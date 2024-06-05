package entity;

import java.util.ArrayList;

public class StudentSchedule {

    ArrayList<StudentBlock> classSchedule;

    public StudentSchedule() {
        classSchedule = new ArrayList<>();
    }

    public void add(StudentBlock block) {
        classSchedule.add(block);
    }

    public void remove(StudentBlock block) {
        classSchedule.remove(block);
    }
}
