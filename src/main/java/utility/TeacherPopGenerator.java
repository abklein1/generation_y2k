package utility;

import entity.Staff;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TeacherPopGenerator {
    public static void generateTeachers(int staffCap, HashMap<Integer, Staff> staffHashMap, HashMap<Integer, String> fNameReference, HashMap<Integer, String> lNameReference) {
        //Store staff objects in another hashmap
        for (int j = 0; j < staffCap; j++) {
            staffHashMap.put(j, new Staff());
        }
        System.out.println("Randomizing " + staffCap + " staff");
        fNameReference.putAll(NameLoader.readCSVFirst(false));
        lNameReference.putAll(NameLoader.readCSVLast(false));
        for (int l = 0; l < staffCap; l++) {
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() - 1));
            System.out.println("   Generating staff " + f_name + " " + l_name);
            Staff staff = staffHashMap.get(l);
            staff.teacherName.setFirstName(f_name);
            staff.teacherName.setLastName(l_name);
            staff.teacherStatistics.setHairColor(TraitSelection.hairSelection(setRandom(0, 102)));
            staff.teacherStatistics.setEyeColor(TraitSelection.eyeSelection(setRandom(0, 109)));
            staff.teacherStatistics.setHeight(setRandom(50, 84));
            staff.teacherStatistics.setIntelligence(setRandom(2, 22));
            staff.teacherStatistics.setCharisma(setRandom(1, 22));
            staff.teacherStatistics.setAgility(setRandom(1, 19));
            staff.teacherStatistics.setDetermination(setRandom(1, 20));
            staff.teacherStatistics.setStrength(setRandom(1, 20));
            staff.teacherStatistics.setBirthday(BirthdayGenerator.generateRandomBirthdayStaff());
        }

        //Clear map for new values
        fNameReference.clear();
        lNameReference.clear();
    }

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
