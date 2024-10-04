package utility;

import entity.Staff;
import view.GameView;

import java.util.HashMap;
import java.util.Random;
import java.util.Map;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class TeacherPopGenerator {
    public static void generateTeachers(int staffCap, HashMap<Integer, Staff> staffHashMap, GameView view) {

        Map<Integer, String> lNameReference = new HashMap<>();
        String f_name;
        String l_name;
        Random distribution = new Random();

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
            staff.teacherStatistics.setIntelligence((int) (distribution.nextGaussian() * TEACHER_POP_INTELLIGENCE_STANDARD_DEVIATION + TEACHER_POP_INTELLIGENCE_MEAN));
            staff.teacherStatistics.setCharisma((int) (distribution.nextGaussian() * TEACHER_POP_CHARISMA_STANDARD_DEVIATION + TEACHER_POP_CHARISMA_MEAN));
            staff.teacherStatistics.setAgility((int) (distribution.nextGaussian() * TEACHER_POP_AGILITY_STANDARD_DEVIATION + TEACHER_POP_AGILITY_MEAN));
            staff.teacherStatistics.setDetermination((int) (distribution.nextGaussian() * TEACHER_POP_DETERMINATION_STANDARD_DEVIATION + TEACHER_POP_DETERMINATION_MEAN));
            staff.teacherStatistics.setPerception((int) (distribution.nextGaussian() * TEACHER_POP_PERCEPTION_STANDARD_DEVIATION + TEACHER_POP_PERCEPTION_MEAN));
            staff.teacherStatistics.setLuck((int) (distribution.nextGaussian() * TEACHER_POP_LUCK_STANDARD_DEVIATION + TEACHER_POP_LUCK_MEAN));
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
            l_name = staff.teacherName.capitalizeName(l_name);
            staff.teacherName.setLastName(l_name);
            if (setRandom(0, SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
                staff.teacherName.setSuffix(NameLoader.suffixNameGenerator(staff.teacherStatistics.getGender()));
            }
            if (setRandom(0, TEACHER_HYPHEN_GENERATION_SAMPLE_SIZE) < TEACHER_HYPHEN_GENERATION_RATE) {
                String hyphenName = lNameReference.get(setRandom(0, lNameReference.size()));
                hyphenName = staff.teacherName.capitalizeName(hyphenName);
                staff.teacherName.setLastName(l_name + "-" + hyphenName);
            }
            String suffix = staff.teacherName.getSuffix();
            staff.teacherStatistics.setInitHairLength(setRandom(0, TEACHER_HAIR_LENGTH_SAMPLE_SIZE));
            staff.teacherStatistics.setHairType(TraitSelection.hairType(setRandom(0, TEACHER_HAIR_TYPE_SAMPLE_SIZE)));
            staff.teacherStatistics.setHairColor(TraitSelection.hairSelection(setRandom(0, TEACHER_HAIR_SELECTION_SAMPLE_SIZE), staff.teacherStatistics.getAge(), staff.teacherStatistics.getHairLength()));
            staff.teacherStatistics.setYearsOfExperience(setRandom(0, (staff.teacherStatistics.getAge() - TEACHER_YEARS_OF_EXPERIENCE_MODIFIER)));
            if (suffix != null) {
                view.appendOutput("   Generated staff " + f_name + " " + staff.teacherName.getLastName() + " " + suffix);
            } else {
                view.appendOutput("   Generated staff " + f_name + " " + staff.teacherName.getLastName());
            }
        }

        //Clear map for new values
        lNameReference.clear();
    }
}
