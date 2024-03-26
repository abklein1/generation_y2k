package utility;

import entity.Student;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class StudentPopGenerator {
    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap, HashMap<Integer, String> fNameReference, HashMap<Integer, String> lNameReference) {
        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        System.out.println("Randomizing " + studentCap + " students...");
        fNameReference.putAll(NameLoader.readCSVFirst(true));
        lNameReference.putAll(NameLoader.readCSVLast(true));

        for (int k = 0; k < studentCap; k++) {
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() - 1));
            System.out.println("   Generating student " + f_name + " " + l_name);
            Student student = studentHashMap.get(k);
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(l_name);
            student.studentStatistics.setHairColor(TraitSelection.hairSelection(setRandom(0, 102)));
            student.studentStatistics.setEyeColor(TraitSelection.eyeSelection(setRandom(0, 109)));
            student.studentStatistics.setHeight(setRandom(48, 78));
            student.studentStatistics.setIntelligence(setRandom(0, 15));
            student.studentStatistics.setCharisma(setRandom(0, 15));
            student.studentStatistics.setAgility(setRandom(0, 15));
            student.studentStatistics.setDetermination(setRandom(0, 15));
            student.studentStatistics.setStrength(setRandom(0, 15));
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0,3));
        }
        //Clear map for new values
        fNameReference.clear();
        lNameReference.clear();
    }

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
