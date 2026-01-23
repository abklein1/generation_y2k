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
    
    /**
     * Gets the number of blocks in the schedule.
     *
     * @return the schedule size
     */
    public int size() {
        return classSchedule.size();
    }
    
    /**
     * Gets a block by index.
     *
     * @param index the block index (0-based)
     * @return the StudentBlock at that index, or null if out of bounds
     */
    public StudentBlock get(int index) {
        if (index < 0 || index >= classSchedule.size()) {
            return null;
        }
        return classSchedule.get(index);
    }
    
    /**
     * Gets a block by block number (1-based period number).
     *
     * @param blockNumber the block/period number (1-4)
     * @return the StudentBlock for that period, or null if not found
     */
    public StudentBlock getByBlockNumber(int blockNumber) {
        for (StudentBlock block : classSchedule) {
            if (block.getBlockNumber() == blockNumber) {
                return block;
            }
        }
        return null;
    }


    public List<String> toStringArray() {
        List<String> studentScheduleString = new ArrayList<>();

        for (StudentBlock block : classSchedule) {
            studentScheduleString.add(block.getClassName());
        }

        return studentScheduleString;
    }
}
