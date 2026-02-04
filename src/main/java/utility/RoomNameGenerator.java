package utility;

import entity.Rooms.*;
import entity.Staff;
import entity.StandardSchool;

import java.util.HashMap;

import static utility.BirthdayGenerator.generateRandomBirthdayStaff;
import static utility.GenderLoader.genderSelection;
import static utility.NameLoader.*;
import static utility.Randomizer.setRandom;

public class RoomNameGenerator {
    public static void generateRoomName(Room room, StandardSchool standardSchool) {
        HashMap<Integer, String> lNameReference = new HashMap<>();
        lNameReference.putAll(readCSVLast());

        if (room instanceof Gym) {
            room.setRoomName(generateGymName(lNameReference));
        } else if (room instanceof AthleticField) {
            room.setRoomName(generateAthleticFieldName(standardSchool, lNameReference));
        } else if (room instanceof LibraryR) {
            room.setRoomName(generateLibraryName(lNameReference));
        } else if (room instanceof Auditorium) {
            room.setRoomName(generateAuditoriumName(lNameReference));
        } else {
            GameLogger.logGeneration("Room name not generated");
        }
    }


    private static String generateGymName(HashMap<Integer, String> lNameReference) {

        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        lastName = StudentName.capitalizeName(lastName);
        char middleInitial = generateMiddleInitial();
        String gymOrGymnasium = gymOrGymnasium();

        return firstName + " " + middleInitial + ". " + lastName + " " + gymOrGymnasium;
    }

    private static String generateAthleticFieldName(StandardSchool standardSchool, HashMap<Integer, String> lNameReference) {

        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        lastName = StudentName.capitalizeName(lastName);
        char middleInitial = generateMiddleInitial();
        String athleticFieldChoice = fieldOrComplexOrStadiumOrFieldHouse();
        int roll = GameRandom.nextInt(1, 100);

        if (roll <= 25) {
            return standardSchool.getSchoolName() + " " + athleticFieldChoice;
        } else if (roll <= 50) {
            return standardSchool.getMascot() + " " + athleticFieldChoice;
        } else {
            return firstName + " " + middleInitial + ". " + lastName + " " + athleticFieldChoice;
        }
    }

    private static String generateLibraryName(HashMap<Integer, String> lNameReference) {
        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        lastName = StudentName.capitalizeName(lastName);
        char middleInitial = generateMiddleInitial();

        return firstName + " " + middleInitial + ". " + lastName + " Library";
    }

    private static String generateAuditoriumName(HashMap<Integer, String> lNameReference) {
        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        lastName = StudentName.capitalizeName(lastName);
        char middleInitial = generateMiddleInitial();

        return firstName + " " + middleInitial + ". " + lastName + " Auditorium";
    }

    /**
     * Generates a classroom name based on the assigned teacher.
     * Format: "[Teacher Last Name]'s [Subject] Classroom"
     * If no teacher is assigned, returns the original room name or a default.
     *
     * @param classroom the classroom to generate a name for
     * @param staff the teacher assigned to the classroom (can be null)
     * @param originalName the original name to fall back to if no teacher
     * @return the generated classroom name
     */
    public static String generateClassroomName(Classroom classroom, Staff staff, String originalName) {
        if (staff == null || staff.teacherName == null) {
            return originalName;
        }

        String lastName = staff.teacherName.getLastName();
        if (lastName == null || lastName.isEmpty()) {
            return originalName;
        }

        // Get the subject from the staff type
        Object staffType = staff.teacherStatistics.getStaffType();
        String subject = (staffType != null) ? staffType.toString() : "General";

        // Format: "LastName's Subject Classroom"
        return lastName + "'s " + subject + " Classroom";
    }

    private static String gymOrGymnasium() {
        int roll = GameRandom.nextInt(1, 100);

        if (roll <= 50) {
            return "Gym";
        } else {
            return "Gymnasium";
        }
    }

    private static String fieldOrComplexOrStadiumOrFieldHouse() {
        int roll = GameRandom.nextInt(1, 100);

        if (roll <= 25) {
            return "Field";
        } else if (roll <= 50) {
            return "Complex";
        } else if (roll <= 75) {
            return "Stadium";
        } else {
            return "Field House";
        }
    }
}
