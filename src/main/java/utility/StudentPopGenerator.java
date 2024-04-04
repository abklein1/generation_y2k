package utility;

import entity.Student;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// TODO: improve performance. It is horrible
public class StudentPopGenerator {
    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap) {

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
        int per_stdDev = 10;
        int per_mean = 50;

        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        System.out.println("Randomizing " + studentCap + " students...");
        lNameReference.putAll(NameLoader.readCSVLast(true));

        for (int k = 0; k < studentCap; k++) {
            Student student = studentHashMap.get(k);
            student.studentStatistics.setHairColor(TraitSelection.hairSelection(setRandom(0, 102)));
            student.studentStatistics.setEyeColor(TraitSelection.eyeSelection(setRandom(0, 109)));
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));
            student.studentStatistics.setGender(GenderLoader.genderSelection());
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()), student.studentStatistics.getGender());
            l_name = lNameReference.get(setRandom(0, lNameReference.size()));
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(l_name);
            student.studentStatistics.setInitHeight();
            student.studentStatistics.setIntelligence((int) (distribution.nextGaussian()*int_stdDev+int_mean));
            student.studentStatistics.setCharisma((int) (distribution.nextGaussian()*chr_stdDev+chr_mean));
            student.studentStatistics.setAgility((int) (distribution.nextGaussian()*agl_stdDev+agl_mean));
            student.studentStatistics.setDetermination((int) (distribution.nextGaussian()*det_stdDev+det_mean));
            student.studentStatistics.setPerception((int) (distribution.nextGaussian()*per_stdDev+per_mean));
            student.studentStatistics.setInitStrength();
            student.studentStatistics.setInitCreativity();
            student.studentStatistics.setInitEmpathy();
            student.studentStatistics.setInitAdaptability();
            student.studentStatistics.setInitInitiative();
            student.studentStatistics.setInitResilience();
            student.studentStatistics.setInitCuriosity();
            student.studentStatistics.setInitResponsibility();
            student.studentStatistics.setInitOpenMind();

            System.out.println("   Generated student " + f_name + " " + l_name);
        }
        //Clear map for new values
        lNameReference.clear();
    }

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
