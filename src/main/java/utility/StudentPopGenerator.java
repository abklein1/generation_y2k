package utility;

import entity.Student;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

// TODO: improve performance. It is horrible
public class StudentPopGenerator {
    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap) {

        HashMap<Integer, String> lNameReference = new HashMap<>();
        String f_name;
        String l_name;

        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        System.out.println("Randomizing " + studentCap + " students...");
        lNameReference.putAll(NameLoader.readCSVLast(true));

        for (int k = 0; k < studentCap; k++) {
            Student student = studentHashMap.get(k);
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
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));
            student.studentStatistics.setGender(GenderLoader.genderSelection());
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()), student.studentStatistics.getGender());
            l_name = lNameReference.get(setRandom(0, lNameReference.size()));
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(l_name);
            System.out.println("   Generated student " + f_name + " " + l_name);
        }
        //Clear map for new values
        lNameReference.clear();
    }

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
