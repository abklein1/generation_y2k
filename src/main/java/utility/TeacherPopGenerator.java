package utility;

import entity.Staff;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// TODO: improve performance. It is horrible
public class TeacherPopGenerator {
    public static void generateTeachers(int staffCap, HashMap<Integer, Staff> staffHashMap) {

        HashMap<Integer, String> lNameReference = new HashMap<>();
        String f_name;
        String l_name;
        Random distribution = new Random();
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 10;
        int chr_mean = 50;
        int agl_stdDev = 10;
        int agl_mean = 50;
        int det_stdDev = 10;
        int det_mean = 50;

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
            staff.teacherStatistics.setBirthday(BirthdayGenerator.generateRandomBirthdayStaff());
            staff.teacherStatistics.setGender(GenderLoader.genderSelection());
            f_name = NameLoader.nameGenerator(String.valueOf(staff.teacherStatistics.getBirthday().getYear()), staff.teacherStatistics.getGender());
            l_name = lNameReference.get(setRandom(0, lNameReference.size()));
            staff.teacherStatistics.setInitHeight();
            staff.teacherStatistics.setIntelligence((int) (distribution.nextGaussian()*int_stdDev+int_mean));
            staff.teacherStatistics.setCharisma((int) (distribution.nextGaussian()*chr_stdDev+chr_mean));
            staff.teacherStatistics.setAgility((int) (distribution.nextGaussian()*agl_stdDev+agl_mean));
            staff.teacherStatistics.setDetermination((int) (distribution.nextGaussian()*det_stdDev+det_mean));
            staff.teacherStatistics.setInitStrength();
            staff.teacherStatistics.setInitCreativity();
            staff.teacherStatistics.setInitEmpathy();
            staff.teacherStatistics.setInitAdaptability();
            staff.teacherStatistics.setInitInitiative();
            staff.teacherStatistics.setInitResilience();
            staff.teacherStatistics.setInitCuriosity();
            staff.teacherStatistics.setInitResponsibility();
            staff.teacherStatistics.setInitOpenMind();
            staff.teacherName.setFirstName(f_name);
            staff.teacherName.setLastName(l_name);
            staff.teacherStatistics.setInitHairLength();
            System.out.println("   Generated staff " + f_name + " " + l_name);
        }

        //Clear map for new values
        lNameReference.clear();
    }

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
