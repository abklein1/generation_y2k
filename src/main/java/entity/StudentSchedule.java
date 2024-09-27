package entity;

import java.util.ArrayList;
import java.util.List;

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

    public List<StudentBlock> getClassSchedule() {
        return new ArrayList<>(classSchedule);
    }


    public List<String> toStringArray() {
        List<String> studentScheduleString = new ArrayList<>();

        for (StudentBlock block : classSchedule) {
            studentScheduleString.add(block.getClassName());
        }

        return studentScheduleString;
    }
}
