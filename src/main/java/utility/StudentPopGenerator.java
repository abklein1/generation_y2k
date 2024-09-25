package utility;

import entity.Student;
import view.GameView;

import java.util.HashMap;
import java.util.Random;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class StudentPopGenerator {
    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap, GameView view) {

        String f_name;
        String[] l_name;
        Random distribution = new Random();

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
            lastName = student.studentName.capitalizeName(lastName);
            student.studentName.setLastName(lastName);
            if(setRandom(0,SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
                student.studentName.setSuffix(NameLoader.suffixNameGenerator(student.studentStatistics.getGender()));
            }
            if(setRandom(0,HYPHEN_GENERATION_SAMPLE_SIZE) < HYPHEN_GENERATION_RATE) {
                String hyphenName = NameLoader.selectWeightedRandom()[0];
                hyphenName = student.studentName.capitalizeName(hyphenName);
                student.studentName.setLastName(lastName + "-" + hyphenName);
            }
            String suffix = student.studentName.getSuffix();
            student.studentStatistics.setRace(race);
            student.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
            String eyes = student.studentStatistics.getEyeColor();
            student.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race,eyes));
            String hairColor = student.studentStatistics.getHairColor();
            student.studentStatistics.setInitHeight();
            student.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION + STUDENT_POP_INTELLIGENCE_MEAN));
            student.studentStatistics.setCharisma((int) (distribution.nextGaussian() * STUDENT_POP_CHARISMA_STANDARD_DEVIATION + STUDENT_POP_CHARISMA_MEAN));
            student.studentStatistics.setAgility((int) (distribution.nextGaussian() * STUDENT_POP_AGILITY_STANDARD_DEVIATION + STUDENT_POP_AGILITY_MEAN));
            student.studentStatistics.setDetermination((int) (distribution.nextGaussian() * STUDENT_POP_DETERMINATION_STANDARD_DEVIATION + STUDENT_POP_DETERMINATION_MEAN));
            student.studentStatistics.setPerception((int) (distribution.nextGaussian() * STUDENT_POP_PERCEPTION_STANDARD_DEVIATION + STUDENT_POP_PERCEPTION_MEAN));
            student.studentStatistics.setLuck((int) (distribution.nextGaussian() * STUDENT_POP_LUCK_STANDARD_DEVIATION + STUDENT_POP_LUCK_MEAN));
            student.studentStatistics.setInitStrength();
            student.studentStatistics.setInitCreativity();
            student.studentStatistics.setInitEmpathy();
            student.studentStatistics.setInitAdaptability();
            student.studentStatistics.setInitInitiative();
            student.studentStatistics.setInitResilience();
            student.studentStatistics.setInitCuriosity();
            student.studentStatistics.setInitResponsibility();
            student.studentStatistics.setInitOpenMind();
            student.studentStatistics.setInitHairLength(setRandom(0, STUDENT_HAIR_LENGTH_SAMPLE_SIZE));
            student.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
            student.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));
            student.studentStatistics.setInitIncomeLevel(setRandom(0, STUDENT_INCOME_LEVEL_SAMPLE_SIZE));
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
