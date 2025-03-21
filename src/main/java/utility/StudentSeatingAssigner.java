package utility;

import entity.*;
import entity.Rooms.Room;

import java.util.*;

//TODO: think about different seating arrangements and settings based on teacher experience and student stats/preferences
public class StudentSeatingAssigner {
    public static void seatInitialStudents(StandardSchool standardSchool) {
        for (Room room : getAllRooms(standardSchool)) {
            seatHelper(room);
        }
    }

    private static void seatHelper(Room room) {
        List<Staff> assignedStaff = room.getAssignedStaff();
        if (assignedStaff == null || assignedStaff.isEmpty() || assignedStaff.get(0) == null) {
            System.out.println("Warning: Room " + room.getRoomName() + " has no assigned teacher.");
            return;
        }

        Staff teacher = assignedStaff.get(0);
        TeacherSchedule teacherSchedule = teacher.teacherStatistics.getTeacherSchedule();
        List<TeacherBlock> teacherBlocks = teacherSchedule.getTeacherSchedule();
        Student[][] seats = null;

        for (TeacherBlock block : teacherBlocks) {
            int blockNumber = block.getBlockNumber();
            List<Student> students = block.getClassPopulation();
            seats = room.getSeatArrangement();

            if (seats == null) {
                System.out.println("Warning: Room " + room.getRoomName() + " has a null seat arrangement.");
                continue;
            }

            seats = new Student[seats.length][seats[0].length];

            for (Student student : students) {
                seatStudent(student, seats);
            }

            room.setPeriodSeatingArrangement(blockNumber, seats);
        }
    }

    private static void seatStudent(Student student, Student[][] seats) {
        int maxAttempts = seats.length * seats[0].length;
        // Initially try to seat the student in a random empty seat
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = Randomizer.setRandom(0, seats.length - 1);
            int y = Randomizer.setRandom(0, seats[0].length - 1);
            if (seats[x][y] == null) {
                seats[x][y] = student;
                return;
            }
        }
        // If unable to seat the student in a random empty seat, try to seat them in the first empty seat
        for (int x = 0; x < seats.length; x++) {
            for (int y = 0; y < seats[0].length; y++) {
                if (seats[x][y] == null) {
                    seats[x][y] = student;
                    return;
                }
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

    public static Student[][] initialSeatingGenerator(int studentCap) {
        int[] selections = selectFactors(findTotalFactors(studentCap));
        return new Student[selections[0]][selections[1]];
    }

    public static ArrayList<Integer> findTotalFactors(int studentCap) {
        double base = Math.ceil(Math.sqrt(studentCap));
        // find the next perfect square value that will serve as upper limit
        int upperLimit = (int) Math.pow(base, 2);
        ArrayList<Integer> factors = new ArrayList<>();
        if (studentCap <= 4) {
            factors.add(2);
            factors.add(2);
        } else {
            // Iterate from studentCap (the lowest range) to next perfect square (the highest range)
            for (int j = studentCap; j <= upperLimit; j++) {
                int step = j % 2 == 0 ? 1 : 2;
                for (int i = 1; i <= Math.sqrt(j); i += step) {
                    // Do not store factors where i is less than 2 so rooms aren't narrow
                    // e.g. 2 x 50 or 1 x 100
                    if (j % i == 0 && (i > 2)) {
                        factors.add(i);
                        factors.add(j / i);
                    }
                }
            }
        }
        return factors;
    }

    public static int[] selectFactors(ArrayList<Integer> factors) {
        int [] factorStore = new int[2];
        // TODO: better error handling
        if (factors.size() <= 1) {
            System.out.println("Factors missing for room");
        } else if (factors.size() == 2) {
            factorStore[0] = factors.get(0);
            factorStore[1] = factors.get(1);
        } else {
            int random = Randomizer.setRandom(1, factors.size() - 2);
            factorStore[0] = factors.get(random);
            // Get next selection since factors are stored in pairs. If they are even move forward
            // since random + 1 will be the factor pair, otherwise move back
            if(random % 2 == 0) {
                random++;
            } else {
                random--;
            }
            factorStore[1] = factors.get(random);
        }

        return factorStore;
    }


}