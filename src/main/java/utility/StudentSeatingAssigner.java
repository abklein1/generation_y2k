package utility;

import entity.Rooms.*;
import entity.*;

import java.util.List;

//TODO: think about different seating arrangements and settings based on teacher experience and student stats/preferences
public class StudentSeatingAssigner {
    public static void seatInitialStudents(StandardSchool standardSchool) {
        Classroom[] classrooms = standardSchool.getClassrooms();
        ArtStudio[] artStudios = standardSchool.getArtStudios();
        AthleticField[] athleticFields = standardSchool.getAthleticFields();
        Auditorium[] auditoriums = standardSchool.getAuditoriums();
        DramaRoom[] dramaRooms = standardSchool.getDramaRooms();
        Gym[] gyms = standardSchool.getGyms();
        MusicRoom[] musicRooms = standardSchool.getMusicRooms();
        VocationalRoom[] vocationalRooms = standardSchool.getVocationalRooms();

        for (Classroom classroom : classrooms) {
            seatHelper(classroom);
        }

        for (ArtStudio artStudio : artStudios) {
            seatHelper(artStudio);
        }

        for (AthleticField athleticField : athleticFields) {
            seatHelper(athleticField);
        }

        for (Auditorium auditorium : auditoriums) {
            seatHelper(auditorium);
        }

        for (DramaRoom dramaRoom : dramaRooms) {
            seatHelper(dramaRoom);
        }

        for (Gym gym : gyms) {
            seatHelper(gym);
        }

        for (MusicRoom musicRoom : musicRooms) {
            seatHelper(musicRoom);
        }

        for (VocationalRoom vocationalRoom : vocationalRooms) {
            seatHelper(vocationalRoom);
        }
    }

    private static void seatHelper(Room room) {
        Staff teacher = room.getAssignedStaff().get(0);
        TeacherSchedule teacherSchedule = teacher.teacherStatistics.getTeacherSchedule();
        List<TeacherBlock> teacherBlocks = teacherSchedule.getTeacherSchedule();
        Student[][] seats = room.getSeatArrangement();
        int upperXBound = seats.length;
        int upperYBound = seats[0].length;
        for (TeacherBlock block : teacherBlocks) {
            int blockNumber = block.getBlockNumber();
            List<Student> students = block.getClassPopulation();
            for (Student student : students) {
                boolean seated = false;
                int counter = 0;
                while (!seated && counter <= (upperXBound * upperYBound)) {
                    int xChoice = Randomizer.setRandom(0, upperXBound - 1);
                    int yChoice = Randomizer.setRandom(0, upperYBound - 1);
                    if (seats[xChoice][yChoice] == null) {
                        room.addStudentToSeat(student, xChoice, yChoice);
                        seated = true;
                    } else {
                        counter++;
                    }
                }
            }
            room.setPeriodSeatingArrangement(blockNumber, seats);
        }
    }
}