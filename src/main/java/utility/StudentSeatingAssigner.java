package utility;

import entity.Rooms.*;
import entity.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

//TODO: think about different seating arrangements and settings based on teacher experience and student stats/preferences
public class StudentSeatingAssigner {
    public static void seatInitialStudents(StandardSchool standardSchool) {
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
        System.out.println("Warning: Unable to seat student " + student.studentName.getFirstName() + " " + student.studentName.getLastName());
    }

    private static List<Room> getAllRooms(StandardSchool school) {
        List<Room> allRooms = new ArrayList<>();
        allRooms.addAll(Arrays.asList(school.getClassrooms()));
        allRooms.addAll(Arrays.asList(school.getArtStudios()));
        allRooms.addAll(Arrays.asList(school.getDramaRooms()));
        allRooms.addAll(Arrays.asList(school.getMusicRooms()));
        allRooms.addAll(Arrays.asList(school.getAthleticFields()));
        allRooms.addAll(Arrays.asList(school.getAuditoriums()));
        allRooms.addAll(Arrays.asList(school.getBreakrooms()));
        allRooms.addAll(Arrays.asList(school.getBathrooms()));
        allRooms.addAll(Arrays.asList(school.getComputerLabs()));
        allRooms.addAll(Arrays.asList(school.getConferenceRooms()));
        allRooms.addAll(Arrays.asList(school.getCourtyards()));
        allRooms.addAll(Arrays.asList(school.getScienceLabs()));
        allRooms.addAll(Arrays.asList(school.getGyms()));
        allRooms.addAll(Arrays.asList(school.getLibraries()));
        allRooms.addAll(Arrays.asList(school.getVocationalRooms()));
        allRooms.addAll(Arrays.asList(school.getHallways()));
        allRooms.addAll(Arrays.asList(school.getUtilityrooms()));
        allRooms.addAll(Arrays.asList(school.getOffices()));
        allRooms.addAll(Arrays.asList(school.getParkingLots()));
        return allRooms;
    }
}