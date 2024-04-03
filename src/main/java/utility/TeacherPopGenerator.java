package utility;

import entity.Staff;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

// TODO: improve performance. It is horrible
public class TeacherPopGenerator {
    public static void generateTeachers(int staffCap, HashMap<Integer, Staff> staffHashMap) {

        HashMap<Integer, String> lNameReference = new HashMap<>();
        String f_name;
        String l_name;

        //Store staff objects in another hashmap
        for (int j = 0; j < staffCap; j++) {
            staffHashMap.put(j, new Staff());
        }
        System.out.println("Randomizing " + staffCap + " staff");
        lNameReference.putAll(NameLoader.readCSVLast(false));

        for (int l = 0; l < staffCap; l++) {

            Staff staff = staffHashMap.get(l);

            staff.teacherStatistics.setHairColor(TraitSelection.hairSelection(setRandom(0, 102)));
            staff.teacherStatistics.setEyeColor(TraitSelection.eyeSelection(setRandom(0, 109)));
            staff.teacherStatistics.setHeight(setRandom(50, 84));
            staff.teacherStatistics.setIntelligence(setRandom(2, 22));
            staff.teacherStatistics.setCharisma(setRandom(1, 22));
            staff.teacherStatistics.setAgility(setRandom(1, 19));
            staff.teacherStatistics.setDetermination(setRandom(1, 20));
            staff.teacherStatistics.setStrength(setRandom(1, 20));
            staff.teacherStatistics.setBirthday(BirthdayGenerator.generateRandomBirthdayStaff());
            staff.teacherStatistics.setGender(GenderLoader.genderSelection());
            f_name = NameLoader.nameGenerator(String.valueOf(staff.teacherStatistics.getBirthday().getYear()), staff.teacherStatistics.getGender());
            l_name = lNameReference.get(setRandom(0, lNameReference.size()));
            staff.teacherName.setFirstName(f_name);
            staff.teacherName.setLastName(l_name);
            System.out.println("   Generated staff " + f_name + " " + l_name);
        }

        //Clear map for new values
        lNameReference.clear();
    }

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
