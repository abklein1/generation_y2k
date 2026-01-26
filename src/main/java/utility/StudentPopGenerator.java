package utility;

import entity.Student;
import view.GameView;

import java.time.LocalDate;
import java.util.HashMap;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class StudentPopGenerator {

    // School colors for braces band color selection (set before generation)
    private static String[] schoolColors = null;

    /**
     * Sets the school colors for use in braces band color selection.
     * Should be called before generateStudents if school colors are available.
     *
     * @param colors the school colors array
     */
    public static void setSchoolColors(String[] colors) {
        schoolColors = colors;
    }

    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap, GameView view) {

        String f_name;
        String[] l_name;

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
            if (setRandom(0, SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
                student.studentName.setSuffix(NameLoader.suffixNameGenerator(student.studentStatistics.getGender()));
            }
            if (setRandom(0, STUDENT_HYPHEN_GENERATION_SAMPLE_SIZE) < STUDENT_HYPHEN_GENERATION_RATE) {
                String hyphenName = NameLoader.selectWeightedRandom()[0];
                hyphenName = student.studentName.capitalizeName(hyphenName);
                student.studentName.setLastName(lastName + "-" + hyphenName);
            }
            String suffix = student.studentName.getSuffix();
            student.studentStatistics.setRace(race);
            student.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
            String eyes = student.studentStatistics.getEyeColor();
            student.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race, eyes));
            String hairColor = student.studentStatistics.getHairColor();
            student.studentStatistics.setInitHeight();
            student.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(STUDENT_POP_INTELLIGENCE_MEAN, STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION));
            student.studentStatistics.setCharisma((int) GameRandom.nextGaussian(STUDENT_POP_CHARISMA_MEAN, STUDENT_POP_CHARISMA_STANDARD_DEVIATION));
            student.studentStatistics.setAgility((int) GameRandom.nextGaussian(STUDENT_POP_AGILITY_MEAN, STUDENT_POP_AGILITY_STANDARD_DEVIATION));
            student.studentStatistics.setDetermination((int) GameRandom.nextGaussian(STUDENT_POP_DETERMINATION_MEAN, STUDENT_POP_DETERMINATION_STANDARD_DEVIATION));
            student.studentStatistics.setPerception((int) GameRandom.nextGaussian(STUDENT_POP_PERCEPTION_MEAN, STUDENT_POP_PERCEPTION_STANDARD_DEVIATION));
            student.studentStatistics.setLuck((int) GameRandom.nextGaussian(STUDENT_POP_LUCK_MEAN, STUDENT_POP_LUCK_STANDARD_DEVIATION));
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
            // Game starts in August 2004
            LocalDate gameStartDate = LocalDate.of(STARTING_YEAR, STARTING_MONTH + 1, STARTING_DATE);
            String incomeLevel = student.studentStatistics.getIncomeLevel();
            String gradeLevel = student.studentStatistics.getGradeLevel();

            boolean hasBraces = TraitSelection.determineBraces(race, incomeLevel, gradeLevel);
            student.studentStatistics.setHasBraces(hasBraces);

            if (hasBraces) {
                // Determine if student has alternating band colors (relatively rare)
                boolean hasAlternating = TraitSelection.determineAlternatingBandColors();

                // Set braces band colors (with potential for alternating/school colors)
                String firstBandColor = TraitSelection.selectFirstBandColor(hasAlternating, schoolColors);
                student.studentStatistics.setBracesBandColor(firstBandColor);

                if (hasAlternating) {
                    // Select second color, with higher chance of school colors
                    String secondBandColor = TraitSelection.selectBracesBandColorWithSchoolOption(
                            true, schoolColors, firstBandColor);
                    student.studentStatistics.setBracesSecondBandColor(secondBandColor);
                }

                // Set bracket type
                student.studentStatistics.setBracesBracketType(TraitSelection.selectBracesBracketType());

                // Determine if student has orthodontic elastics FIRST
                // (elastic type affects treatment duration)
                boolean hasElastics = TraitSelection.determineHasElastics();
                String elasticType = null;
                student.studentStatistics.setBracesHasElastics(hasElastics);
                if (hasElastics) {
                    student.studentStatistics.setBracesElasticColor(TraitSelection.selectBracesElasticColor());
                    elasticType = TraitSelection.selectBracesElasticType();
                    student.studentStatistics.setBracesElasticType(elasticType);
                }

                // Generate braces timing (when put on, when coming off)
                // Certain modifiers like ligature ties extend treatment duration
                LocalDate[] bracesTiming = TraitSelection.generateBracesTiming(
                        gradeLevel, gameStartDate, hasElastics, elasticType);
                student.studentStatistics.setBracesStartDate(bracesTiming[0]);
                student.studentStatistics.setBracesEndDate(bracesTiming[1]);

                // Recalculate charisma-dependent stats with braces penalty
                student.studentStatistics.recalculateCharismaDependentStats();
            } else {
                // Check if student had braces in the past (already removed)
                boolean hadPastBraces = TraitSelection.determinePastBraces(
                        race, incomeLevel, gradeLevel, false);

                if (hadPastBraces) {
                    student.studentStatistics.setHadBracesRemoved(true);

                    // Generate past braces timing
                    LocalDate birthday = student.studentStatistics.getBirthday();
                    LocalDate[] pastTiming = TraitSelection.generatePastBracesTiming(
                            birthday, gradeLevel, gameStartDate);
                    student.studentStatistics.setBracesStartDate(pastTiming[0]);
                    student.studentStatistics.setBracesEndDate(pastTiming[1]);

                    // Apply charisma boost for having completed braces treatment
                    int currentCharisma = student.studentStatistics.getCharisma();
                    student.studentStatistics.setCharisma(currentCharisma + BRACES_CHARISMA_BOOST);
                    student.studentStatistics.setBracesCharismaBoost(BRACES_CHARISMA_BOOST);

                    // Recalculate secondary stats with boosted charisma
                    student.studentStatistics.recalculateCharismaDependentStats();
                }
            }

            // Determine vision issues based on NHANES data
            String gender = student.studentStatistics.getGender();
            boolean[] visionIssues = TraitSelection.determineVisionIssues(race, gender);
            student.studentStatistics.setHasMyopia(visionIssues[0]);
            student.studentStatistics.setHasHyperopia(visionIssues[1]);
            student.studentStatistics.setHasAstigmatism(visionIssues[2]);

            // Determine corrective lenses if student has vision issues
            if (student.studentStatistics.hasVisionIssue()) {
                boolean[] correctiveLenses = TraitSelection.determineCorrectiveLenses(
                        race, gender, incomeLevel, gradeLevel);
                student.studentStatistics.setHasGlasses(correctiveLenses[0]);
                student.studentStatistics.setHasContacts(correctiveLenses[1]);
            }

            if (suffix != null) {
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
