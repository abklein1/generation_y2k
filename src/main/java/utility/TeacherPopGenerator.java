package utility;

import config.TownDemographics;
import entity.Staff;
import view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class TeacherPopGenerator {

    /**
     * Generates staff/teachers and returns them as a list.
     * This is the preferred method for the new Town-based architecture.
     *
     * @param count the number of staff to generate
     * @param view  the game view for output
     * @return list of generated staff
     */
    public static List<Staff> generateStaffList(int count, GameView view) {
        HashMap<Integer, Staff> tempMap = new HashMap<>();
        generateTeachers(count, tempMap, view);
        return new ArrayList<>(tempMap.values());
    }

    /**
     * Generates staff using demographics configuration.
     *
     * @param demographics the demographics configuration
     * @param view         the game view for output
     * @return list of generated staff
     */
    public static List<Staff> generateStaffFromDemographics(TownDemographics demographics, GameView view) {
        int count = demographics.getTotalStaffToGenerate();
        return generateStaffList(count, view);
    }

    /**
     * Generates staff/teachers and populates a HashMap.
     * This method maintains backward compatibility with existing code.
     *
     * @param staffCap     the number of staff to generate
     * @param staffHashMap the HashMap to populate
     * @param view         the game view for output
     */
    public static void generateTeachers(int staffCap, HashMap<Integer, Staff> staffHashMap, GameView view) {

        Map<Integer, String> lNameReference = new HashMap<>();
        String f_name;
        String l_name;

        // Store staff objects in another hashmap
        for (int j = 0; j < staffCap; j++) {
            staffHashMap.put(j, new Staff());
        }
        GameLogger.logGeneration("Randomizing " + staffCap + " staff");
        lNameReference.putAll(NameLoader.readCSVLast());

        for (int l = 0; l < staffCap; l++) {

            Staff staff = staffHashMap.get(l);

            staff.teacherStatistics.setEyeColor(TraitSelection.eyeSelection(setRandom(0, 109)));
            staff.teacherStatistics.setBirthday(BirthdayGenerator.generateRandomBirthdayStaff());
            staff.teacherStatistics.setGender(GenderLoader.genderSelection());
            f_name = NameLoader.nameGenerator(String.valueOf(staff.teacherStatistics.getBirthday().getYear()),
                    staff.teacherStatistics.getGender());
            l_name = lNameReference.get(setRandom(0, lNameReference.size()));
            staff.teacherStatistics.setInitHeight();
            staff.teacherStatistics.setIntelligence((int) GameRandom.nextGaussian(TEACHER_POP_INTELLIGENCE_MEAN,
                    TEACHER_POP_INTELLIGENCE_STANDARD_DEVIATION));
            staff.teacherStatistics.setCharisma(
                    (int) GameRandom.nextGaussian(TEACHER_POP_CHARISMA_MEAN, TEACHER_POP_CHARISMA_STANDARD_DEVIATION));
            staff.teacherStatistics.setAgility(
                    (int) GameRandom.nextGaussian(TEACHER_POP_AGILITY_MEAN, TEACHER_POP_AGILITY_STANDARD_DEVIATION));
            staff.teacherStatistics.setDetermination((int) GameRandom.nextGaussian(TEACHER_POP_DETERMINATION_MEAN,
                    TEACHER_POP_DETERMINATION_STANDARD_DEVIATION));
            staff.teacherStatistics.setPerception((int) GameRandom.nextGaussian(TEACHER_POP_PERCEPTION_MEAN,
                    TEACHER_POP_PERCEPTION_STANDARD_DEVIATION));
            staff.teacherStatistics
                    .setLuck((int) GameRandom.nextGaussian(TEACHER_POP_LUCK_MEAN, TEACHER_POP_LUCK_STANDARD_DEVIATION));
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
            staff.teacherStatistics
                    .setHairColor(TraitSelection.hairSelection(setRandom(0, TEACHER_HAIR_SELECTION_SAMPLE_SIZE),
                            staff.teacherStatistics.getAge(), staff.teacherStatistics.getHairLength()));
            staff.teacherStatistics.setYearsOfExperience(
                    setRandom(0, (staff.teacherStatistics.getAge() - TEACHER_YEARS_OF_EXPERIENCE_MODIFIER)));

            // Determine vision issues based on age and gender
            int age = staff.teacherStatistics.getAge();
            String gender = staff.teacherStatistics.getGender();
            boolean[] visionIssues = TraitSelection.determineAdultVisionIssues(age, gender);
            staff.teacherStatistics.setHasMyopia(visionIssues[0]);
            staff.teacherStatistics.setHasHyperopia(visionIssues[1]);
            staff.teacherStatistics.setHasAstigmatism(visionIssues[2]);

            // Determine corrective lenses if staff has vision issues
            // Adults are much more likely to have corrective lenses
            if (staff.teacherStatistics.hasVisionIssue()) {
                boolean[] correctiveLenses = TraitSelection.determineAdultCorrectiveLensesComplete(age);
                staff.teacherStatistics.setHasGlasses(correctiveLenses[0]);
                staff.teacherStatistics.setHasContacts(correctiveLenses[1]);
            }

            if (suffix != null) {
                GameLogger.logGeneration(
                        "   Generated staff " + f_name + " " + staff.teacherName.getLastName() + " " + suffix);
            } else {
                GameLogger.logGeneration("   Generated staff " + f_name + " " + staff.teacherName.getLastName());
            }
        }

        // Clear map for new values
        lNameReference.clear();
    }
}
