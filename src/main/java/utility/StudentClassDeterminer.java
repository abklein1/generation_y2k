package utility;

import entity.Student;

import java.util.*;

import static constants.SimConstants.*;

/**
 * Determines what classes each student should take based on their traits
 * (grade level, intelligence, determination, income, gender, etc.).
 *
 * This class has zero dependencies on school or staff -- it only reads
 * student traits and constants. It can be called before any school exists.
 *
 * Extracted from EnhancedStudentScheduleAssigner (Phase 1a).
 */
public class StudentClassDeterminer {

    /**
     * Cache for student class determination to ensure consistent results across
     * phases.
     * Without this cache, classProbabilityLoader re-rolls random paths each call,
     * causing demand analysis and actual assignment to produce different class
     * lists.
     */
    private static final Map<Student, List<String>> studentClassCache = new HashMap<>();

    // ------------------------------------------------------------------ public API

    /**
     * Determines class lists for all students in the map.
     * Returns a map from each student to their ordered list of class names.
     *
     * @param studentHashMap the students to analyze
     * @return student -> class list
     */
    public static Map<Student, List<String>> determineAllClasses(HashMap<Integer, Student> studentHashMap) {
        Map<Student, List<String>> result = new LinkedHashMap<>();
        for (Student student : studentHashMap.values()) {
            result.put(student, determineStudentClasses(student));
        }
        return result;
    }

    /**
     * Uses existing trait logic to determine what classes a student should take.
     * Results are cached per student so that demand analysis and assignment phases
     * always see the same class list.
     */
    public static List<String> determineStudentClasses(Student student) {
        List<String> cached = studentClassCache.get(student);
        if (cached != null) {
            return cached;
        }

        List<String> allClasses = new ArrayList<>();

        String year = student.studentStatistics.getGradeLevel();
        int intelligence = student.studentStatistics.getIntelligence();
        int determination = student.studentStatistics.getDetermination();
        String income = student.studentStatistics.getIncomeLevel();

        String englishPath = classProbabilityLoader(intelligence, income, determination);
        String mathPath = classProbabilityLoader(intelligence, income, determination);
        String sciencePath = classProbabilityLoader(intelligence, income, determination);
        String historyPath = classProbabilityLoader(intelligence, income, determination);

        allClasses.addAll(determineEnglishClasses(year, englishPath));
        allClasses.addAll(determineMathClasses(year, mathPath));
        allClasses.addAll(determineScienceClasses(year, sciencePath));
        allClasses.addAll(determineHistoryClasses(year, historyPath));
        allClasses.addAll(determineLanguageClasses(year, student));
        allClasses.addAll(determinePhysEdClasses(year, student));
        allClasses.addAll(determineVocationalClasses(year, student));

        studentClassCache.put(student, allClasses);
        return allClasses;
    }

    /** Clears the cache so each scheduling run starts fresh. */
    public static void clearCache() {
        studentClassCache.clear();
    }

    /** Returns the cached class map (read-only view). */
    public static Map<Student, List<String>> getStudentClassCache() {
        return Collections.unmodifiableMap(studentClassCache);
    }

    /** Returns expected schedule size based on grade level. */
    public static int getExpectedScheduleSize(String grade) {
        return switch (grade) {
            case "Freshman" -> 8;
            case "Sophomore" -> 6;
            case "Junior" -> 6;
            case "Senior" -> 6;
            default -> 6;
        };
    }

    /** Extracts language base from class name (e.g., "French I" -&gt; "French"). */
    public static String getLanguageBase(String className) {
        if (className.contains(" I")) {
            return className.substring(0, className.indexOf(" I"));
        } else if (className.contains(" II")) {
            return className.substring(0, className.indexOf(" II"));
        }
        return className;
    }

    // ----------------------------------------- subject-specific class
    // determination

    public static List<String> determineEnglishClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("English I");
            case "Sophomore" -> classes.add("English II");
            case "Junior" -> classes.add(path.equals("AP") ? "AP English Language & Composition" : "English III");
            case "Senior" -> classes.add(path.equals("AP") ? "AP English Literature & Composition" : "English IV");
        }
        return classes;
    }

    public static List<String> determineMathClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> {
                classes.add(path.equals("AP") || path.equals("Honors") ? "Geometry" : "Fundamentals of Math");
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Algebra I");
                } else {
                    classes.add("Geometry");
                }
            }
            case "Sophomore" -> {
                classes.add(path.equals("AP") || path.equals("Honors") ? "Algebra II" : "Algebra I");
                classes.add(path.equals("AP") || path.equals("Honors") ? "Trigonometry" : "Algebra II");
            }
            case "Junior" -> {
                classes.add(path.equals("AP") ? "Precalculus" : path.equals("Honors") ? "Precalculus" : "Trigonometry");
                if (path.equals("AP")) {
                    classes.add("AP Statistics");
                } else if (!path.equals("Honors")) {
                    classes.add("Math for Data and Financial Literacy");
                }
            }
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Calculus AB");
                    classes.add("AP Calculus BC");
                } else if (path.equals("Honors")) {
                    classes.add("Precalculus");
                } else {
                    classes.add("Precalculus");
                }
            }
        }
        return classes;
    }

    public static List<String> determineScienceClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("Biology");
            case "Sophomore" -> {
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Chemistry");
                } else {
                    String[] options = { "Earth and Space Science", "Physical Science" };
                    classes.add(options[Randomizer.setRandom(0, options.length - 1)]);
                }
            }
            case "Junior" -> {
                if (path.equals("AP")) {
                    String[] apScienceOptions = { "AP Biology", "AP Chemistry" };
                    classes.add(apScienceOptions[Randomizer.setRandom(0, apScienceOptions.length - 1)]);
                } else {
                    classes.add("Anatomy and Physiology");
                }
            }
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Physics B");
                    classes.add("AP Physics C");
                } else if (path.equals("Honors")) {
                    classes.add("Physics");
                } else {
                    classes.add("Environmental Science");
                }
            }
        }
        return classes;
    }

    public static List<String> determineHistoryClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add(path.equals("AP") ? "AP Human Geography" : "World Geography");
            case "Sophomore" -> classes.add(path.equals("AP") ? "AP World History" : "World History");
            case "Junior" -> classes.add(path.equals("AP") ? "AP US History" : "US History");
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP US Government");
                    classes.add("AP Economics Macro");
                } else {
                    classes.add("US Government");
                }
            }
        }
        return classes;
    }

    public static List<String> determineLanguageClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        if (year.equals("Freshman")) {
            int langChoice = Randomizer.setRandom(0, LANGUAGE_CHOICE_SAMPLE_SIZE);
            switch (langChoice) {
                case 0 -> {
                    classes.add("Spanish I");
                    classes.add("Spanish II");
                }
                case 1 -> {
                    classes.add("French I");
                    classes.add("French II");
                }
                case 2 -> {
                    classes.add("German I");
                    classes.add("German II");
                }
                case 3 -> {
                    classes.add("American Sign Language I");
                    classes.add("American Sign Language II");
                }
                case 4 -> {
                    classes.add("Latin I");
                    classes.add("Latin II");
                }
            }
        }
        return classes;
    }

    public static List<String> determinePhysEdClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        if (year.equals("Freshman")) {
            classes.add("Health");
        } else if (year.equals("Sophomore")) {
            String[] choices = physicalEdDecision(student);
            classes.add(choices[0]);
        }
        return classes;
    }

    public static List<String> determineVocationalClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        if (!year.equals("Freshman")) {
            if (year.equals("Senior") || student.studentStatistics
                    .getDetermination() >= SENIOR_VOCATIONAL_CLASS_DETERMINATION_THRESHOLD) {
                String[] fallChoices = vocationalDecision(student, "Fall");
                String[] springChoices = vocationalDecision(student, "Spring");
                Set<String> selectedClasses = new LinkedHashSet<>();

                addBalancedVocationalChoice(classes, selectedClasses, fallChoices, 4);
                addBalancedVocationalChoice(classes, selectedClasses, springChoices, 4);

                if (year.equals("Senior")) {
                    addBalancedVocationalChoice(classes, selectedClasses, fallChoices, 6);
                    addBalancedVocationalChoice(classes, selectedClasses, springChoices, 6);
                }
            }
        }
        return classes;
    }

    private static void addBalancedVocationalChoice(List<String> classes, Set<String> selectedClasses,
            String[] rankedChoices, int candidateLimit) {
        String choice = pickBalancedVocationalChoice(rankedChoices, selectedClasses, candidateLimit);
        if (choice != null) {
            classes.add(choice);
            selectedClasses.add(choice);
        }
    }

    private static String pickBalancedVocationalChoice(String[] rankedChoices, Set<String> selectedClasses,
            int candidateLimit) {
        List<String> normalizedChoices = normalizeVocationalChoices(rankedChoices);
        List<String> availableChoices = normalizedChoices.stream()
                .filter(choice -> !selectedClasses.contains(choice))
                .toList();
        if (availableChoices.isEmpty()) {
            return null;
        }

        int limit = Math.min(candidateLimit, availableChoices.size());
        int totalWeight = 0;
        for (int i = 0; i < limit; i++) {
            totalWeight += limit - i;
        }

        int roll = Randomizer.setRandom(1, totalWeight);
        int cumulative = 0;
        for (int i = 0; i < limit; i++) {
            cumulative += limit - i;
            if (roll <= cumulative) {
                return availableChoices.get(i);
            }
        }

        return availableChoices.get(0);
    }

    private static List<String> normalizeVocationalChoices(String[] rankedChoices) {
        List<String> normalizedChoices = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String choice : rankedChoices) {
            if (choice == null || choice.isBlank() || seen.contains(choice)) {
                continue;
            }
            normalizedChoices.add(choice);
            seen.add(choice);
        }
        return normalizedChoices;
    }

    // --------------------------------------------------------- physical ed
    // decision

    public static String[] physicalEdDecision(Student student) {
        String gender = student.studentStatistics.getGender();
        int strength = student.studentStatistics.getStrength();
        int determination = student.studentStatistics.getDetermination();

        if (gender.equals("Male")) {
            return getMalePhysicalEdDecision(strength, determination);
        } else {
            return getFemalePhysicalEdDecision(strength, determination);
        }
    }

    public static String[] getMalePhysicalEdDecision(int strength, int determination) {
        if (strength > PHYSICAL_ED_MALE_STRENGTH_THRESHOLD || (strength < PHYSICAL_ED_MALE_LOW_STRENGTH_THRESHOLD
                && determination > PHYSICAL_ED_MALE_DETERMINATION_THRESHOLD)) {
            return new String[] { "Weightlifting", "Team Sports", "Specialized Sports", "Lifetime Recreation",
                    "Dance" };
        } else if (strength < PHYSICAL_ED_MALE_STRENGTH_THRESHOLD
                && strength > PHYSICAL_ED_MALE_LOW_STRENGTH_THRESHOLD) {
            return new String[] { "Team Sports", "Specialized Sports", "Weightlifting", "Lifetime Recreation",
                    "Dance" };
        } else if (determination < PHYSICAL_ED_MALE_LOW_DETERMINATION_THRESHOLD) {
            return new String[] { "Lifetime Recreation", "Specialized Sports", "Team Sports", "Dance",
                    "Weightlifting" };
        } else {
            return new String[] { "Specialized Sports", "Team Sports", "Weightlifting", "Dance",
                    "Lifetime Recreation" };
        }
    }

    public static String[] getFemalePhysicalEdDecision(int strength, int determination) {
        if (strength > PHYSICAL_ED_FEMALE_STRENGTH_THRESHOLD || (strength < PHYSICAL_ED_FEMALE_LOW_STRENGTH_THRESHOLD
                && determination > PHYSICAL_ED_FEMALE_DETERMINATION_THRESHOLD)) {
            return new String[] { "Dance", "Team Sports", "Specialized Sports", "Weightlifting",
                    "Lifetime Recreation" };
        } else if (strength < PHYSICAL_ED_FEMALE_STRENGTH_THRESHOLD
                && strength > PHYSICAL_ED_FEMALE_LOW_STRENGTH_THRESHOLD) {
            return new String[] { "Specialized Sports", "Lifetime Recreation", "Dance", "Weightlifting",
                    "Team Sports" };
        } else if (determination < PHYSICAL_ED_FEMALE_LOW_DETERMINATION_THRESHOLD) {
            return new String[] { "Lifetime Recreation", "Specialized Sports", "Dance", "Team Sports",
                    "Weightlifting" };
        } else {
            return new String[] { "Specialized Sports", "Team Sports", "Weightlifting", "Dance",
                    "Lifetime Recreation" };
        }
    }

    // -------------------------------------------------------- academic path loader

    public static String classProbabilityLoader(int intelligence, String income, int determination) {
        int random = Randomizer.setRandom(0, CLASS_PROBABILITY_LOADER_SAMPLE_SIZE);
        double apProbability;
        double honorsProbability;
        double onLevelProbability;

        if (intelligence >= CLASS_PROBABILITY_LOADER_GIFTED_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_GIFTED_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_GIFTED_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_GIFTED_ON_LEVEL_PROBABILITY;
        } else if (intelligence >= CLASS_PROBABILITY_LOADER_HIGH_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_HIGH_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_HIGH_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_HIGH_ON_LEVEL_PROBABILITY;
        } else if (intelligence >= CLASS_PROBABILITY_LOADER_AVERAGE_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_AVERAGE_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_AVERAGE_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_AVERAGE_ON_LEVEL_PROBABILITY;
        } else if (intelligence <= CLASS_PROBABILITY_LOADER_LOW_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_LOW_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_LOW_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_LOW_ON_LEVEL_PROBABILITY;
        } else {
            apProbability = CLASS_PROBABILITY_LOADER_OTHER_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_OTHER_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_OTHER_ON_LEVEL_PROBABILITY;
        }

        switch (income) {
            case "high" -> {
                apProbability *= CLASS_PROBABILITY_LOADER_INCOME_HIGH_AP_ADJUSTMENT;
                honorsProbability *= CLASS_PROBABILITY_LOADER_INCOME_HIGH_HONORS_ADJUSTMENT;
                onLevelProbability *= CLASS_PROBABILITY_LOADER_INCOME_HIGH_ON_LEVEL_ADJUSTMENT;
            }
            case "low" -> {
                apProbability *= CLASS_PROBABILITY_LOADER_INCOME_LOW_AP_ADJUSTMENT;
                honorsProbability *= CLASS_PROBABILITY_LOADER_INCOME_LOW_HONORS_ADJUSTMENT;
                onLevelProbability *= CLASS_PROBABILITY_LOADER_INCOME_LOW_ON_LEVEL_ADJUSTMENT;
            }
        }

        double determinationFactor = (determination - CLASS_PROBABILITY_LOADER_DETERMINATION_THRESHOLD)
                / CLASS_PROBABILITY_LOADER_DETERMINATION_FACTOR_DIVISOR;
        apProbability += apProbability * determinationFactor;
        honorsProbability += honorsProbability * determinationFactor
                / CLASS_PROBABILITY_LOADER_DETERMINATION_HONORS_ADJUSTMENT;
        onLevelProbability -= onLevelProbability * determinationFactor
                / CLASS_PROBABILITY_LOADER_DETERMINATION_ON_LEVEL_ADJUSTMENT;

        double totalProbability = apProbability + honorsProbability + onLevelProbability;
        apProbability = (apProbability / totalProbability) * 100;
        honorsProbability = (honorsProbability / totalProbability) * 100;

        if (random < apProbability) {
            return "AP";
        } else if (random < apProbability + honorsProbability) {
            return "Honors";
        } else {
            return "On-Level";
        }
    }

    // -------------------------------------------------------- vocational decision

    public static String[] vocationalDecision(Student student, String semester) {
        String[] choiceRank = new String[8];
        int determination = student.studentStatistics.getDetermination();
        int charisma = student.studentStatistics.getCharisma();
        int creativity = student.studentStatistics.getCreativity();
        int perception = student.studentStatistics.getPerception();
        int intelligence = student.studentStatistics.getIntelligence();
        int curiosity = student.studentStatistics.getCuriosity();
        String year = student.studentStatistics.getGradeLevel();

        if (semester.equals("Fall")) {
            if (charisma > CHARISMA_VOCATIONAL_LOWER_BOUND && determination > DETERMINATION_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Theater I";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Musical Theater I";
                        choiceRank[3] = "Dance Techniques I";
                        choiceRank[4] = "Choir";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Introduction to Business";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Business Management";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Debate";
                        choiceRank[1] = "Choir";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Concert Band";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "ROTC";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "Business Management";
                    }
                }
            } else if (creativity > CREATIVITY_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "2D Studio Art I";
                        choiceRank[1] = "Photography I";
                        choiceRank[2] = "3D Studio Art I";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "3D Studio Art I";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Photography I";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Theater Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Photography I";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
            } else if (determination > DETERMINATION_VOCATIONAL_LOWER_BOUND_BAND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Computer Aided Drafting I";
                        choiceRank[4] = "Intro to Programming";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Auto Body Repair";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "AP Music Theory";
                        choiceRank[4] = "AP Philosophy";
                        choiceRank[5] = "Intro to Programming";
                        choiceRank[6] = "Spanish III";
                        choiceRank[7] = "Debate";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
            } else if (curiosity > CURIOSITY_VOCATIONAL_LOWER_BOUND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting I";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting I";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "AP Music Theory";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Spanish III";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Philosophy";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
            } else if (determination < LOW_DETERMINATION_VOCATIONAL_UPPER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Home Economics";
                        choiceRank[2] = "Woodworking";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art I";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art I";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            } else {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Team Sports";
                        choiceRank[2] = "Specialized Sports";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art I";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art I";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            }
        } else {
            // Spring semester
            if (charisma > CHARISMA_VOCATIONAL_LOWER_BOUND && determination > DETERMINATION_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Theater II";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Musical Theater II";
                        choiceRank[3] = "Dance Techniques II";
                        choiceRank[4] = "Choir";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Theater IV";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Digital Production Technology";
                        choiceRank[5] = "Entrepreneurial Skills";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Digital Production Technology";
                        choiceRank[5] = "Marketing";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Debate";
                        choiceRank[1] = "Choir";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Concert Band";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "ROTC";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "Marketing";
                    }
                }
            } else if (creativity > CREATIVITY_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "2D Studio Art II";
                        choiceRank[1] = "Photography II";
                        choiceRank[2] = "3D Studio Art II";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "3D Studio Art II";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Photography II";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Theater Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Photography II";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Studio History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
            } else if (determination > DETERMINATION_VOCATIONAL_LOWER_BOUND_BAND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > INTELLIGENCE_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Computer Aided Drafting II";
                        choiceRank[4] = "Intro to Programming";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Auto Body Repair";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "AP Music Theory";
                        choiceRank[4] = "AP Philosophy";
                        choiceRank[5] = "Intro to Programming";
                        choiceRank[6] = "AP Spanish Literature";
                        choiceRank[7] = "Debate";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
            } else if (curiosity > CURIOSITY_VOCATIONAL_LOWER_BOUND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting II";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting II";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "AP Music Theory";
                        choiceRank[5] = "AP Spanish Literature";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Philosophy";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
            } else if (determination < LOW_DETERMINATION_VOCATIONAL_UPPER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Home Economics";
                        choiceRank[2] = "Woodworking";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art II";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art II";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            } else {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Team Sports";
                        choiceRank[2] = "Specialized Sports";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art II";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art II";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            }
        }
        return choiceRank;
    }
}
