package utility;

import config.TownDemographics;
import entity.Student;
import view.GameView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    /**
     * Gets the current school colors for use by other generators (e.g., SiblingGenerator).
     *
     * @return the school colors array, or null if not set
     */
    public static String[] getSchoolColors() {
        return schoolColors;
    }

    /**
     * Applies base attributes to a student including stats, physical traits, braces, and vision.
     * This method is used by both StudentPopGenerator and SiblingGenerator to ensure consistency.
     * 
     * Note: This method does NOT set level, experience, grade level, birthday, gender, name, or race.
     * Those should be set by the caller before calling this method.
     *
     * @param student the student to apply attributes to
     */
    public static void applyBaseAttributes(Student student) {
        String race = student.studentStatistics.getRace();
        String gradeLevel = student.studentStatistics.getGradeLevel();
        String gender = student.studentStatistics.getGender();
        
        // Physical traits based on race
        student.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
        String eyes = student.studentStatistics.getEyeColor();
        student.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race, eyes));
        String hairColor = student.studentStatistics.getHairColor();
        student.studentStatistics.setInitHeight();
        
        // Base stats using Gaussian distributions
        student.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(STUDENT_POP_INTELLIGENCE_MEAN, STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION));
        student.studentStatistics.setCharisma((int) GameRandom.nextGaussian(STUDENT_POP_CHARISMA_MEAN, STUDENT_POP_CHARISMA_STANDARD_DEVIATION));
        student.studentStatistics.setAgility((int) GameRandom.nextGaussian(STUDENT_POP_AGILITY_MEAN, STUDENT_POP_AGILITY_STANDARD_DEVIATION));
        student.studentStatistics.setDetermination((int) GameRandom.nextGaussian(STUDENT_POP_DETERMINATION_MEAN, STUDENT_POP_DETERMINATION_STANDARD_DEVIATION));
        student.studentStatistics.setPerception((int) GameRandom.nextGaussian(STUDENT_POP_PERCEPTION_MEAN, STUDENT_POP_PERCEPTION_STANDARD_DEVIATION));
        student.studentStatistics.setLuck((int) GameRandom.nextGaussian(STUDENT_POP_LUCK_MEAN, STUDENT_POP_LUCK_STANDARD_DEVIATION));
        
        // Derived stats
        student.studentStatistics.setInitStrength();
        student.studentStatistics.setInitCreativity();
        student.studentStatistics.setInitEmpathy();
        student.studentStatistics.setInitAdaptability();
        student.studentStatistics.setInitInitiative();
        student.studentStatistics.setInitResilience();
        student.studentStatistics.setInitCuriosity();
        student.studentStatistics.setInitResponsibility();
        student.studentStatistics.setInitOpenMind();
        
        // Hair and skin
        student.studentStatistics.setInitHairLength(setRandom(0, STUDENT_HAIR_LENGTH_SAMPLE_SIZE));
        student.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
        student.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));
        
        // Apply braces attributes
        applyBracesAttributes(student);
        
        // Apply vision attributes
        applyVisionAttributes(student);
    }

    /**
     * Applies braces-related attributes to a student.
     * This includes determining if they have/had braces, timing, cosmetics, and charisma effects.
     *
     * @param student the student to apply braces attributes to
     */
    public static void applyBracesAttributes(Student student) {
        LocalDate gameStartDate = LocalDate.of(STARTING_YEAR, STARTING_MONTH + 1, STARTING_DATE);
        String race = student.studentStatistics.getRace();
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

            // Generate braces timing
            // Certain modifiers like ligature ties extend treatment duration
            LocalDate[] bracesTiming = TraitSelection.generateBracesTiming(
                    gradeLevel, gameStartDate, hasElastics, elasticType);
            student.studentStatistics.setBracesStartDate(bracesTiming[0]);
            student.studentStatistics.setBracesEndDate(bracesTiming[1]);

            // Recalculate charisma-dependent stats with braces penalty
            student.studentStatistics.recalculateCharismaDependentStats();
        } else {
            // Check if student had braces in the past
            boolean hadPastBraces = TraitSelection.determinePastBraces(race, incomeLevel, gradeLevel, false);

            if (hadPastBraces) {
                student.studentStatistics.setHadBracesRemoved(true);

                // Generate past braces timing
                LocalDate birthday = student.studentStatistics.getBirthday();
                LocalDate[] pastTiming = TraitSelection.generatePastBracesTiming(birthday, gradeLevel, gameStartDate);
                student.studentStatistics.setBracesStartDate(pastTiming[0]);
                student.studentStatistics.setBracesEndDate(pastTiming[1]);

                // Apply charisma boost
                int currentCharisma = student.studentStatistics.getCharisma();
                student.studentStatistics.setCharisma(currentCharisma + BRACES_CHARISMA_BOOST);
                student.studentStatistics.setBracesCharismaBoost(BRACES_CHARISMA_BOOST);

                // Recalculate secondary stats
                student.studentStatistics.recalculateCharismaDependentStats();
            }
        }
    }

    /**
     * Applies vision-related attributes to a student.
     * This includes myopia, hyperopia, astigmatism, and corrective lenses.
     *
     * @param student the student to apply vision attributes to
     */
    public static void applyVisionAttributes(Student student) {
        String race = student.studentStatistics.getRace();
        String gender = student.studentStatistics.getGender();
        String incomeLevel = student.studentStatistics.getIncomeLevel();
        String gradeLevel = student.studentStatistics.getGradeLevel();

        // Determine vision issues based on NHANES data
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
    }

    /**
     * Generates students and returns them as a list.
     * This is the preferred method for the new Town-based architecture.
     *
     * @param count the number of students to generate
     * @param view the game view for output
     * @return list of generated students
     */
    public static List<Student> generateStudentList(int count, GameView view) {
        HashMap<Integer, Student> tempMap = new HashMap<>();
        generateStudents(count, tempMap, view);
        return new ArrayList<>(tempMap.values());
    }

    /**
     * Generates students using demographics configuration.
     * Uses the gender and income distributions from the demographics.
     *
     * @param demographics the demographics configuration
     * @param view the game view for output
     * @return list of generated students
     */
    public static List<Student> generateStudentsFromDemographics(TownDemographics demographics, GameView view) {
        int count = demographics.getTotalStudentsToGenerate();
        HashMap<Integer, Student> tempMap = new HashMap<>();
        generateStudentsWithDemographics(count, tempMap, view, demographics);
        return new ArrayList<>(tempMap.values());
    }

    /**
     * Generates students and populates a HashMap.
     * This method maintains backward compatibility with existing code.
     *
     * @param studentCap the number of students to generate
     * @param studentHashMap the HashMap to populate
     * @param view the game view for output
     */
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
            
            // Set identity attributes (level, grade, birthday, gender)
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));
            student.studentStatistics.setGender(GenderLoader.genderSelection());
            
            // Set name attributes
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()), student.studentStatistics.getGender());
            l_name = NameLoader.selectWeightedRandom();
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
            
            // Set race and income (required before applyBaseAttributes)
            student.studentStatistics.setRace(race);
            student.studentStatistics.setInitIncomeLevel(setRandom(0, STUDENT_INCOME_LEVEL_SAMPLE_SIZE));
            
            // Apply all base attributes (stats, physical traits, braces, vision)
            applyBaseAttributes(student);

            if (suffix != null) {
                view.appendOutput("   Generated student " + f_name + " " + student.studentName.getLastName() + " " + suffix);
            } else {
                view.appendOutput("   Generated student " + f_name + " " + student.studentName.getLastName());
            }
        }
    }

    /**
     * Generates students using demographics configuration for distributions.
     * Uses custom gender and income distributions from the demographics object.
     *
     * @param studentCap the number of students to generate
     * @param studentHashMap the HashMap to populate
     * @param view the game view for output
     * @param demographics the demographics configuration with distributions
     */
    public static void generateStudentsWithDemographics(int studentCap, HashMap<Integer, Student> studentHashMap, 
            GameView view, TownDemographics demographics) {

        String f_name;
        String[] l_name;
        
        // Get distribution values from demographics
        java.util.Map<String, Double> genderDist = demographics.getGenderDistribution();
        java.util.Map<String, Double> incomeDist = demographics.getIncomeDistribution();
        double malePercent = genderDist.getOrDefault("Male", 0.51);
        double lowIncomePercent = incomeDist.getOrDefault("Low", 0.25);
        double middleIncomePercent = incomeDist.getOrDefault("Middle", 0.60);

        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        view.appendOutput("Randomizing " + studentCap + " students...");
        loadCSVData();

        for (int k = 0; k < studentCap; k++) {
            Student student = studentHashMap.get(k);
            
            // Set identity attributes (level, grade, birthday, gender)
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));
            
            // Use demographics gender distribution
            student.studentStatistics.setGender(GenderLoader.genderSelection(malePercent));
            
            // Set name attributes
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()), student.studentStatistics.getGender());
            l_name = NameLoader.selectWeightedRandom();
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
            
            // Set race and income (required before applyBaseAttributes)
            student.studentStatistics.setRace(race);
            student.studentStatistics.setIncomeFromDistribution(lowIncomePercent, middleIncomePercent);
            
            // Apply all base attributes (stats, physical traits, braces, vision)
            applyBaseAttributes(student);

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
