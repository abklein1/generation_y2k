package utility;

import entity.Rooms.*;
import entity.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

//TODO: think about different seating arrangements and settings based on teacher experience and student stats/preferences
public class StudentSeatingAssigner {
    public static void seatInitialStudents(StandardSchool standardSchool) {
        // ... existing code ...

        for (Room room : getAllRooms(standardSchool)) {
            seatHelper(room);
        }
    }

    private static void seatHelper(Room room) {
        Staff teacher = room.getAssignedStaff().get(0);
        TeacherSchedule teacherSchedule = teacher.teacherStatistics.getTeacherSchedule();
        List<TeacherBlock> teacherBlocks = teacherSchedule.getTeacherSchedule();
        
        for (TeacherBlock block : teacherBlocks) {
            int blockNumber = block.getBlockNumber();
            List<Student> students = block.getClassPopulation();
            Student[][] seats = new Student[room.getSeatArrangement().length][room.getSeatArrangement()[0].length];
            
            for (Student student : students) {
                seatStudent(student, seats);
            }
            
            room.setPeriodSeatingArrangement(blockNumber, seats);
        }
    }

    private static void seatStudent(Student student, Student[][] seats) {
        int maxAttempts = seats.length * seats[0].length;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = Randomizer.setRandom(0, seats.length - 1);
            int y = Randomizer.setRandom(0, seats[0].length - 1);
            if (seats[x][y] == null) {
                seats[x][y] = student;
                return;
            }
        }
        System.out.println("Warning: Unable to seat student " + student.studentName);
    }

    private static List<Room> getAllRooms(StandardSchool school) {
        List<Room> allRooms = new ArrayList<>();
        allRooms.addAll(Arrays.asList(school.getClassrooms()));
        allRooms.addAll(Arrays.asList(school.getArtStudios()));
        // Add other room types...
        return allRooms;
    }
}