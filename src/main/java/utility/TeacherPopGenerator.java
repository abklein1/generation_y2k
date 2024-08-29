package utility;

import entity.Staff;
import view.GameView;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class TeacherPopGenerator {
    public static void generateTeachers(int staffCap, HashMap<Integer, Staff> staffHashMap, GameView view) {

        HashMap<Integer, String> lNameReference = new HashMap<>();
        String f_name;
        String l_name;
        Random distribution = new Random();
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        //Store staff objects in another hashmap
        for (int j = 0; j < staffCap; j++) {
            staffHashMap.put(j, new Staff());
        }
        view.appendOutput("Randomizing " + staffCap + " staff");
        lNameReference.putAll(NameLoader.readCSVLast());

        for (int l = 0; l < staffCap; l++) {

            Staff staff = staffHashMap.get(l);

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
            staff.teacherStatistics.setLuck((int) (distribution.nextGaussian()*lck_stdDev+lck_mean));
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
            staff.teacherStatistics.setInitHairLength(setRandom(0,10000));
            staff.teacherStatistics.setHairType(TraitSelection.hairType(setRandom(0, 975)));
            staff.teacherStatistics.setHairColor(TraitSelection.hairSelection(setRandom(0, 102), staff.teacherStatistics.getAge(), staff.teacherStatistics.getHairLength()));
            staff.teacherStatistics.setYearsOfExperience(setRandom(0,(staff.teacherStatistics.getAge() - 23)));
            view.appendOutput("   Generated staff " + f_name + " " + staff.teacherName.getLastName());
        }

        //Clear map for new values
        lNameReference.clear();
    }
}
