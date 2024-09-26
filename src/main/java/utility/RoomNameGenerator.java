package utility;

import entity.Rooms.*;
import entity.StandardSchool;

import java.util.HashMap;
import java.util.Random;

import static utility.GenderLoader.*;
import static utility.BirthdayGenerator.*;
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
            System.out.println("Room name not generated");
        }
    }


    private static String generateGymName(HashMap<Integer, String> lNameReference) {

        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        char middleInitial = generateMiddleInitial();
        String gymOrGymnasium = gymOrGymnasium();

        return firstName + " " + middleInitial + ". " + lastName + " " + gymOrGymnasium;
    }

    private static String generateAthleticFieldName(StandardSchool standardSchool, HashMap<Integer, String> lNameReference) {

        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        char middleInitial = generateMiddleInitial();
        String athleticFieldChoice = fieldOrComplexOrStadiumOrFieldHouse();
        Random r = new Random();
        int roll = r.nextInt(100)+1;

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
        char middleInitial = generateMiddleInitial();

        return firstName + " " + middleInitial + ". " + lastName + " Library";
    }

    private static String generateAuditoriumName(HashMap<Integer, String> lNameReference) {
        String year = String.valueOf(generateRandomBirthdayStaff().getYear());
        String gender = genderSelection();
        String firstName = nameGenerator(year, gender);
        String lastName = lNameReference.get(setRandom(0, lNameReference.size()));
        char middleInitial = generateMiddleInitial();

        return firstName + " " + middleInitial + ". " + lastName + " Auditorium";
    }

    private static String gymOrGymnasium() {
        Random r = new Random();
        int roll = r.nextInt(100)+1;

        if (roll <= 50) {
            return "Gym";
        } else {
            return "Gymnasium";
        }
    }

    private static String fieldOrComplexOrStadiumOrFieldHouse() {
        Random r = new Random();
        int roll = r.nextInt(100)+1;

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
