package entity;

import java.util.ArrayList;
import java.util.List;

// TODO: Why am I doing this
public class TeacherSchedule {
    ArrayList<TeacherBlock> teacherSchedule;

    public TeacherSchedule() {
        teacherSchedule = new ArrayList<>();
    }

    public void add(TeacherBlock block) {
        teacherSchedule.add(block);
    }

    public void remove(TeacherBlock block) {
        teacherSchedule.remove(block);
    }

    public int size() {return teacherSchedule.size();}

    public List<TeacherBlock> getBlocksByClassName(String className) {
        List<TeacherBlock> blocks = new ArrayList<>();
        for (TeacherBlock block : teacherSchedule) {
            if (block.className.equals(className)) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    public TeacherBlock getBlockByClassNameAndAvailability(String className) {
        for (TeacherBlock block : teacherSchedule) {
            if (block.className.equals(className) && block.room.getStudentCapacity() > 0) {
                return block;
            }
        }
        return null;
    }
}
