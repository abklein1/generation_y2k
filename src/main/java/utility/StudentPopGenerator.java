package utility;

import entity.Student;
import view.GameView;

import java.util.HashMap;
import java.util.Random;

import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class StudentPopGenerator {
    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap, GameView view) {

        String f_name;
        String[] l_name;
        Random distribution = new Random();
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int per_stdDev = 15;
        int per_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        view.appendOutput("Randomizing " + studentCap + " students...");
        loadCSVData();

        for (int k = 0; k < studentCap; k++) {
            Student student = studentHashMap.get(k);
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));
            student.studentStatistics.setGender(GenderLoader.genderSelection());
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()), student.studentStatistics.getGender());
            l_name = NameLoader.selectWeightedRandom();
            // Race distribution tied to last names
            String lastName = l_name[0];
            String race = l_name[1];
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(lastName);
            if(setRandom(0,170) == 100) {
                student.studentName.setSuffix(NameLoader.middleNameGenerator(student.studentStatistics.getGender()));
            }
            String suffix = student.studentName.getSuffix();
            student.studentStatistics.setRace(race);
            student.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
            String eyes = student.studentStatistics.getEyeColor();
            student.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race,eyes));
            String hairColor = student.studentStatistics.getHairColor();
            student.studentStatistics.setInitHeight();
            student.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * int_stdDev + int_mean));
            student.studentStatistics.setCharisma((int) (distribution.nextGaussian() * chr_stdDev + chr_mean));
            student.studentStatistics.setAgility((int) (distribution.nextGaussian() * agl_stdDev + agl_mean));
            student.studentStatistics.setDetermination((int) (distribution.nextGaussian() * det_stdDev + det_mean));
            student.studentStatistics.setPerception((int) (distribution.nextGaussian() * per_stdDev + per_mean));
            student.studentStatistics.setLuck((int) (distribution.nextGaussian() * lck_stdDev + lck_mean));
            student.studentStatistics.setInitStrength();
            student.studentStatistics.setInitCreativity();
            student.studentStatistics.setInitEmpathy();
            student.studentStatistics.setInitAdaptability();
            student.studentStatistics.setInitInitiative();
            student.studentStatistics.setInitResilience();
            student.studentStatistics.setInitCuriosity();
            student.studentStatistics.setInitResponsibility();
            student.studentStatistics.setInitOpenMind();
            student.studentStatistics.setInitHairLength(setRandom(0,10000));
            student.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
            student.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));
            student.studentStatistics.setInitIncomeLevel(setRandom(0,100));
            if(suffix != null) {
                view.appendOutput("   Generated student " + f_name + " " + student.studentName.getLastName() + " " + suffix);
            } else {
                view.appendOutput("   Generated student " + f_name + " " + student.studentName.getLastName());
            }
        }
    }

    private static void loadCSVData() {
        // Read large file into memory first
        NameLoader.readCSVFirst("1986");
        NameLoader.readCSVFirst("1987");
        NameLoader.readCSVFirst("1988");
        NameLoader.readCSVFirst("1989");
        NameLoader.readCSVFirst("1990");
        NameLoader.readCSVLastStudent();
    }
}
