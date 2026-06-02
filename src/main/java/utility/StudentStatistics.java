package utility;

import constants.SimConstants;
import entity.AllostaticLoad;
import entity.Items.Outfit;
import entity.Student;
import entity.StudentBlock;
import entity.StudentSchedule;
import entity.academic.StudentAcademicRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentStatistics implements PStatistics {

    private static final long serialVersionUID = 1L;

    private final List<Integer> grades;
    private double height;
    private String eyeColor;
    private String hairColor;
    private String hairLength;
    private String hairType;
    private String build;
    private String gradeLevel;
    private String race;
    private String skinColor;
    private int intelligence;
    private int charisma;
    private int agility;
    private int determination;
    private int strength;
    private int perception;
    private int luck;
    private int experience;
    private int grade_average;
    private String gender;
    private LocalDate birthday;
    private int creativity;
    private int empathy;
    private int adaptability;
    private int initiative;
    private int resilience;
    private int curiosity;
    private int responsibility;
    private int openmindedness;

    // Secondary stat max caps (set once during initialization, represent ceilings)
    private int maxCreativity;
    private int maxEmpathy;
    private int maxAdaptability;
    private int maxInitiative;
    private int maxResilience;
    private int maxCuriosity;
    private int maxResponsibility;
    private int maxOpenmindedness;

    // Allostatic load meter
    private final AllostaticLoad allostaticLoad;
    private String incomeLevel;
    private String neighborhoodName;
    private String neighborhoodWealthLevel;
    private final ArrayList<String> completedClasses;
    private final StudentSchedule studentSchedule;
    private final StudentAcademicRecord academicRecord;
    private final ArrayList<Student> siblingsInSchool;
    private final ArrayList<Student> siblingsNotInSchool;
    private final ArrayList<Student> friendsInSchool;
    private int maxBestFriends;
    private boolean hasBraces;
    private String bracesBandColor;
    private String bracesSecondBandColor; // For alternating band colors
    private String bracesBracketType;
    // Braces timing - when they were put on and when they'll be removed
    private LocalDate bracesStartDate;
    private LocalDate bracesEndDate;
    // Braces cosmetic modifiers - orthodontic elastics
    private boolean bracesHasElastics;
    private String bracesElasticColor;
    private String bracesElasticType;
    // Track if student previously had braces (already removed)
    private boolean hadBracesRemoved;
    // Store the charisma boost that was applied when braces were removed
    private int bracesCharismaBoost;
    // Vision issues - refractive errors (can have multiple)
    private boolean hasMyopia; // Nearsightedness
    private boolean hasHyperopia; // Farsightedness
    private boolean hasAstigmatism; // Astigmatism (can combine with myopia or hyperopia)
    // Corrective lenses - glasses or contacts
    private boolean hasGlasses; // Wears glasses
    private boolean hasContacts; // Wears contact lenses (may also have glasses as backup)
    // Ear piercing attributes
    private boolean hasEarPiercing;
    private int earPiercingLeftCount;       // 0, 1, 2, or 3 piercings on left ear
    private int earPiercingRightCount;      // 0, 1, 2, or 3 piercings on right ear
    private String earPiercingType;         // studs, hoops, gauges, dangling earrings
    private String earPiercingMaterial;     // gold, silver, surgical steel, etc.
    private String earPiercingSize;         // small, medium, large (primarily for gauges/hoops)
    private int earPiercingCharismaBoost;   // minor stat improvement from jewelry

    // Clique identity
    private String mainClique;
    private String subgroup;
    private String secondaryClique;

    // Clique-driven hair cosmetics
    private String hairDye;
    private String hairHighlights;
    private String hairStyle;

    // Charisma-driven unique physical/behavioral traits
    private List<String> uniqueTraits;

    // Favorite bands drawn from the genres this student's clique likes most.
    // Drives band merch generation and flavor text.
    private List<String> favoriteBands;

    // Current outfit (clothing items organized by layer)
    private Outfit currentOutfit;

    public StudentStatistics() {
        this.height = 0;
        this.eyeColor = null;
        this.hairColor = null;
        this.hairLength = null;
        this.hairType = null;
        this.build = null;
        this.race = null;
        this.skinColor = null;
        this.intelligence = 0;
        this.charisma = 0;
        this.agility = 0;
        this.determination = 0;
        this.perception = 0;
        this.strength = 0;
        this.luck = 0;
        this.experience = 0;
        this.grade_average = 0;
        this.grades = new ArrayList<>();
        this.gradeLevel = null;
        this.gender = null;
        this.birthday = null;
        this.creativity = 0;
        this.empathy = 0;
        this.adaptability = 0;
        this.initiative = 0;
        this.resilience = 0;
        this.curiosity = 0;
        this.responsibility = 0;
        this.openmindedness = 0;
        this.maxCreativity = 0;
        this.maxEmpathy = 0;
        this.maxAdaptability = 0;
        this.maxInitiative = 0;
        this.maxResilience = 0;
        this.maxCuriosity = 0;
        this.maxResponsibility = 0;
        this.maxOpenmindedness = 0;
        this.allostaticLoad = new AllostaticLoad();
        this.incomeLevel = null;
        this.neighborhoodName = null;
        this.neighborhoodWealthLevel = null;
        this.completedClasses = new ArrayList<>();
        this.studentSchedule = new StudentSchedule();
        this.academicRecord = new StudentAcademicRecord();
        this.siblingsInSchool = new ArrayList<>();
        this.siblingsNotInSchool = new ArrayList<>();
        this.friendsInSchool = new ArrayList<>();
        this.maxBestFriends = 0;
        this.hasBraces = false;
        this.bracesBandColor = null;
        this.bracesSecondBandColor = null;
        this.bracesBracketType = null;
        this.bracesStartDate = null;
        this.bracesEndDate = null;
        this.bracesHasElastics = false;
        this.bracesElasticColor = null;
        this.bracesElasticType = null;
        this.hadBracesRemoved = false;
        this.bracesCharismaBoost = 0;
        this.hasMyopia = false;
        this.hasHyperopia = false;
        this.hasAstigmatism = false;
        this.hasGlasses = false;
        this.hasContacts = false;
        this.mainClique = null;
        this.subgroup = null;
        this.secondaryClique = null;
        this.hairDye = null;
        this.hairHighlights = null;
        this.hairStyle = null;
        this.uniqueTraits = new ArrayList<>();
        this.favoriteBands = new ArrayList<>();
        this.currentOutfit = new Outfit();
    }

    @Override
    public int getStrength() {
        return this.strength;
    }

    @Override
    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public int getDetermination() {
        return this.determination;
    }

    @Override
    public void setDetermination(int determination) {
        this.determination = determination;
    }

    @Override
    public int getAgility() {
        return this.agility;
    }

    @Override
    public void setAgility(int agility) {
        this.agility = agility;
    }

    @Override
    public int getCharisma() {
        return this.charisma;
    }

    @Override
    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    @Override
    public int getIntelligence() {
        return this.intelligence;
    }

    @Override
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    @Override
    public String getBuild() {
        return this.build;
    }

    @Override
    public void setBuild(String build) {
        this.build = build;
    }

    @Override
    public String getHairColor() {
        return this.hairColor;
    }

    @Override
    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    @Override
    public String getEyeColor() {
        return this.eyeColor;
    }

    @Override
    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = this.experience + experience;
    }

    public void setNewGrade(int grade) {
        this.grades.add(grade);
    }

    public void getAllGrades() {
        GameLogger.logDebug("Grades: " + this.grades);
    }

    public void setGradeAverage() {
        int size = grades.size();
        for (Integer grade : grades) {
            grade_average = grade_average + grade;
        }
        grade_average = grade_average / (size + 1);
    }

    public Integer getGradeAverage() {
        return this.grade_average;
    }

    public String getGradeLevel() {
        return this.gradeLevel;
    }

    public void setGradeLevel(int level) {
        switch (level) {
            case 0 -> this.gradeLevel = "Freshman";
            case 1 -> this.gradeLevel = "Sophomore";
            case 2 -> this.gradeLevel = "Junior";
            case 3 -> this.gradeLevel = "Senior";
        }
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    @Override
    public int getCreativity() {
        return this.creativity;
    }

    @Override
    public void setCreativity(int creativity) {
        this.creativity = creativity;
    }

    @Override
    public int getEmpathy() {
        return this.empathy;
    }

    @Override
    public void setEmpathy(int empathy) {
        this.empathy = empathy;
    }

    @Override
    public int getAdaptability() {
        return this.adaptability;
    }

    @Override
    public void setAdaptability(int adaptability) {
        this.adaptability = adaptability;
    }

    @Override
    public int getInitiative() {
        return this.initiative;
    }

    @Override
    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    @Override
    public int getResilience() {
        return this.resilience;
    }

    @Override
    public void setResilience(int resilience) {
        this.resilience = resilience;
    }

    @Override
    public int getCuriosity() {
        return this.curiosity;
    }

    @Override
    public void setCuriosity(int curiosity) {
        this.curiosity = curiosity;
    }

    @Override
    public int getResponsibility() {
        return this.responsibility;
    }

    @Override
    public void setResponsibility(int responsibility) {
        this.responsibility = responsibility;
    }

    @Override
    public int getOpenMindedness() {
        return this.openmindedness;
    }

    @Override
    public void setOpenMindedness(int openMindedness) {
        this.openmindedness = openMindedness;
    }

    // TODO: remove calculation from Student stats
    public void setInitHeight() {

        double mean = 0;
        double stdDev = 0;

        if (gender.equals("Male")) {
            switch (gradeLevel) {
                case "Freshman" -> {
                    mean = 59;
                    stdDev = 5;
                }
                case "Sophomore" -> {
                    mean = 64.5;
                    stdDev = 5.5;
                }
                case "Junior" -> {
                    mean = 68;
                    stdDev = 4.5;
                }
                case "Senior" -> {
                    mean = 69.5;
                    stdDev = 3.3;
                }
            }
        } else {
            switch (gradeLevel) {
                case "Freshman" -> {
                    mean = 59.5;
                    stdDev = 4.5;
                }
                case "Sophomore" -> {
                    mean = 63.5;
                    stdDev = 4.5;
                }
                case "Junior", "Senior" -> {
                    mean = 64;
                    stdDev = 3;
                }
            }
        }

        this.height = GameRandom.nextGaussian(mean, stdDev);

        this.height = Math.max(this.height, mean - 3 * stdDev);
        this.height = Math.min(this.height, mean + 3 * stdDev);

    }

    @Override
    public int getPerception() {
        return this.perception;
    }

    @Override
    public void setPerception(int perception) {
        this.perception = perception;
    }

    public void setInitStrength() {
        double meanBaseStr = 50;
        double stdDevStr = 10;
        int baseStr = (int) GameRandom.nextGaussian(meanBaseStr, stdDevStr);

        double heightMod = (this.height - 60) * 0.5;
        int genderMod = this.gender.equals("Male") ? 10 : 5;
        int currentYear = 2004;
        int birthYear = this.birthday.getYear();
        int age = currentYear - birthYear;
        double ageMod = calculateAgeModifier(age);

        this.strength = (int) (baseStr + heightMod + genderMod + ageMod);

    }

    private double calculateAgeModifier(int age) {
        if (age < 30) {
            return age - 20;
        } else if (age <= 40) {
            return 10;
        } else {
            return 10 - (age - 40) * 0.5;
        }
    }

    // TODO: basic calculations for now
    public void setInitCreativity() {
        // Primarily driven by intelligence and secondary by perception
        this.creativity = (int) ((this.intelligence * 1.5) + this.perception) / 2;
        this.maxCreativity = this.creativity;
    }

    public void setInitEmpathy() {
        // Primarily driven by charisma and secondary by perception
        this.empathy = (int) ((this.charisma * 1.5) + this.perception) / 2;
        this.maxEmpathy = this.empathy;
    }

    public void setInitAdaptability() {
        // Physical and mental adaptability and tertiary determination
        this.adaptability = (this.agility + this.intelligence + (this.determination / 4)) / 2;
        this.maxAdaptability = this.adaptability;
    }

    public void setInitInitiative() {
        // Primarily driven by determination
        this.initiative = (int) ((this.determination * 1.5) + this.perception) / 2;
        this.maxInitiative = this.initiative;
    }

    public void setInitResilience() {
        // Primary strength and secondary determination
        this.resilience = (int) ((this.strength * 1.5) + this.determination) / 2;
        this.maxResilience = this.resilience;
    }

    public void setInitCuriosity() {
        this.curiosity = (int) ((this.perception * 1.5) + this.intelligence) / 2;
        this.maxCuriosity = this.curiosity;
    }

    public void setInitResponsibility() {
        this.responsibility = (int) ((this.charisma * 1.25) + (this.determination * 1.25)) / 2;
        this.maxResponsibility = this.responsibility;
    }

    public void setInitOpenMind() {
        this.openmindedness = (int) ((this.intelligence * 1.25) + (this.charisma * 1.25)) / 2;
        this.maxOpenmindedness = this.openmindedness;
    }

    /**
     * Initializes the allostatic load tolerance based on this student's stats.
     * Should be called after resilience and determination are set.
     */
    public void initAllostaticLoad() {
        this.allostaticLoad.initTolerance(this.resilience, this.determination);
    }

    // TODO: Experiment with more narrative descriptions. ex. 'Rachel has wavy,
    // brown hair that falls past her shoulders'
    @Override
    public void setInitHairLength(int choice) {
        if (this.gender.equals("male")) {
            if (choice < 30) {
                this.hairLength = "waist-length";
            } else if (choice < 100) {
                this.hairLength = "shoulder-length";
            } else if (choice <= 400) {
                this.hairLength = "long";
            } else if (choice <= 800) {
                this.hairLength = "chin-length";
            } else if (choice <= 8000) {
                this.hairLength = "short";
            } else {
                this.hairLength = "very short";
            }
        } else {
            if (choice <= 4) {
                this.hairLength = "extremely long";
            } else if (choice <= 78) {
                this.hairLength = "waist-length";
            } else if (choice <= 321) {
                this.hairLength = "shoulder-length";
            } else if (choice <= 4500) {
                this.hairLength = "neck-length";
            } else if (choice <= 8300) {
                this.hairLength = "chin-length";
            } else {
                this.hairLength = "short";
            }
        }
    }

    @Override
    public String getHairLength() {
        return this.hairLength;
    }

    @Override
    public void setHairLength(String hairLength) {
        this.hairLength = hairLength;
    }

    public String getHairType() {
        return this.hairType;
    }

    public void setHairType(String hairType) {
        this.hairType = hairType;
    }

    public String getHairDye() {
        return this.hairDye;
    }

    public void setHairDye(String hairDye) {
        this.hairDye = hairDye;
    }

    public String getHairHighlights() {
        return this.hairHighlights;
    }

    public void setHairHighlights(String hairHighlights) {
        this.hairHighlights = hairHighlights;
    }

    public String getHairStyle() {
        return this.hairStyle;
    }

    public void setHairStyle(String hairStyle) {
        this.hairStyle = hairStyle;
    }

    public List<String> getUniqueTraits() {
        return Collections.unmodifiableList(this.uniqueTraits);
    }

    public void setUniqueTraits(List<String> traits) {
        this.uniqueTraits = new ArrayList<>(traits);
    }

    public List<String> getFavoriteBands() {
        if (this.favoriteBands == null) {
            this.favoriteBands = new ArrayList<>();
        }
        return Collections.unmodifiableList(this.favoriteBands);
    }

    public void setFavoriteBands(List<String> bands) {
        this.favoriteBands = (bands == null)
                ? new ArrayList<>()
                : new ArrayList<>(bands);
    }

    public String getRace() {
        return this.race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getSkinColor() {
        return this.skinColor;
    }

    public void setSkinColor(String skinColor) {
        this.skinColor = skinColor;
    }

    public int getLuck() {
        return this.luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public String getIncomeLevel() {
        return incomeLevel;
    }

    public void setIncomeLevel(String incomeLevel) {
        this.incomeLevel = incomeLevel;
    }

    public String getNeighborhoodName() {
        return neighborhoodName;
    }

    public void setNeighborhoodName(String neighborhoodName) {
        this.neighborhoodName = neighborhoodName;
    }

    public String getNeighborhoodWealthLevel() {
        return neighborhoodWealthLevel;
    }

    public void setNeighborhoodWealthLevel(String neighborhoodWealthLevel) {
        this.neighborhoodWealthLevel = neighborhoodWealthLevel;
    }

    public void clearNeighborhoodAssignment() {
        this.neighborhoodName = null;
        this.neighborhoodWealthLevel = null;
    }

    public void setInitIncomeLevel(int choice) {
        if (choice <= SimConstants.INCOME_THRESHOLD_LOW) {
            this.incomeLevel = "low";
        } else if (choice <= SimConstants.INCOME_THRESHOLD_MIDDLE) {
            this.incomeLevel = "middle";
        } else {
            this.incomeLevel = "high";
        }
    }

    /**
     * Sets income level using custom distribution percentages.
     *
     * @param lowPercent    the percentage of low income (0.0 to 1.0)
     * @param middlePercent the percentage of middle income (0.0 to 1.0)
     */
    public void setIncomeFromDistribution(double lowPercent, double middlePercent) {
        double roll = GameRandom.nextDouble();
        if (roll < lowPercent) {
            this.incomeLevel = "low";
        } else if (roll < lowPercent + middlePercent) {
            this.incomeLevel = "middle";
        } else {
            this.incomeLevel = "high";
        }
    }

    public ArrayList<String> getCompletedClasses() {
        return this.completedClasses;
    }

    public void addToCompletedClasses(String completedClass) {
        this.completedClasses.add(completedClass);
    }

    public StudentSchedule getStudentSchedule() {
        return studentSchedule;
    }

    public StudentAcademicRecord getAcademicRecord() {
        return academicRecord;
    }

    public void addStudentSchedule(StudentBlock block) {
        this.studentSchedule.add(block);
    }

    public void removeStudentSchedule(StudentBlock block) {
        this.studentSchedule.remove(block);
    }

    /**
     * Adds a sibling who is currently in school.
     * Prevents duplicates - sibling will only be added if not already present.
     */
    public void addSiblingsInSchool(Student sibling) {
        if (!this.siblingsInSchool.contains(sibling)) {
            this.siblingsInSchool.add(sibling);
        }
    }

    public void removeSiblingsInSchool(Student sibling) {
        this.siblingsInSchool.remove(sibling);
    }

    public ArrayList<Student> getSiblingsInSchool() {
        return siblingsInSchool;
    }

    /**
     * Adds a sibling who is not currently in school.
     * Prevents duplicates - sibling will only be added if not already present.
     */
    public void addSiblingsNotInSchool(Student sibling) {
        if (!this.siblingsNotInSchool.contains(sibling)) {
            this.siblingsNotInSchool.add(sibling);
        }
    }

    public void removeSiblingsNotInSchool(Student sibling) {
        this.siblingsNotInSchool.remove(sibling);
    }

    public ArrayList<Student> getSiblingsNotInSchool() {
        return this.siblingsNotInSchool;
    }

    public ArrayList<Student> getFriendsInSchool() {
        return friendsInSchool;
    }

    public void addFriendInSchool(Student friend) {
        this.friendsInSchool.add(friend);
    }

    public int getMaxBestFriends() {
        return maxBestFriends;
    }

    public void setMaxBestFriends(int maxBestFriends) {
        this.maxBestFriends = maxBestFriends;
    }

    public boolean getHasBraces() {
        return hasBraces;
    }

    public void setHasBraces(boolean hasBraces) {
        this.hasBraces = hasBraces;
    }

    public String getBracesBandColor() {
        return bracesBandColor;
    }

    public void setBracesBandColor(String bracesBandColor) {
        this.bracesBandColor = bracesBandColor;
    }

    public String getBracesSecondBandColor() {
        return bracesSecondBandColor;
    }

    public void setBracesSecondBandColor(String bracesSecondBandColor) {
        this.bracesSecondBandColor = bracesSecondBandColor;
    }

    /**
     * Checks if the student has alternating band colors on their braces.
     *
     * @return true if the student has two band colors, false otherwise
     */
    public boolean hasAlternatingBandColors() {
        return bracesSecondBandColor != null && !bracesSecondBandColor.isEmpty();
    }

    public String getBracesBracketType() {
        return bracesBracketType;
    }

    public void setBracesBracketType(String bracesBracketType) {
        this.bracesBracketType = bracesBracketType;
    }

    public LocalDate getBracesStartDate() {
        return bracesStartDate;
    }

    public void setBracesStartDate(LocalDate bracesStartDate) {
        this.bracesStartDate = bracesStartDate;
    }

    public LocalDate getBracesEndDate() {
        return bracesEndDate;
    }

    public void setBracesEndDate(LocalDate bracesEndDate) {
        this.bracesEndDate = bracesEndDate;
    }

    public boolean getBracesHasElastics() {
        return bracesHasElastics;
    }

    public void setBracesHasElastics(boolean bracesHasElastics) {
        this.bracesHasElastics = bracesHasElastics;
    }

    public String getBracesElasticColor() {
        return bracesElasticColor;
    }

    public void setBracesElasticColor(String bracesElasticColor) {
        this.bracesElasticColor = bracesElasticColor;
    }

    public String getBracesElasticType() {
        return bracesElasticType;
    }

    public void setBracesElasticType(String bracesElasticType) {
        this.bracesElasticType = bracesElasticType;
    }

    public boolean getHadBracesRemoved() {
        return hadBracesRemoved;
    }

    public void setHadBracesRemoved(boolean hadBracesRemoved) {
        this.hadBracesRemoved = hadBracesRemoved;
    }

    public int getBracesCharismaBoost() {
        return bracesCharismaBoost;
    }

    public void setBracesCharismaBoost(int bracesCharismaBoost) {
        this.bracesCharismaBoost = bracesCharismaBoost;
    }

    /**
     * Gets the effective charisma value, accounting for braces effects.
     * Students currently wearing braces have reduced charisma.
     * Students who previously had braces and had them removed get a permanent
     * boost.
     *
     * @return the effective charisma value
     */
    public int getEffectiveCharisma() {
        int effectiveCharisma = this.charisma;

        // Apply penalty if currently wearing braces
        if (hasBraces) {
            effectiveCharisma -= constants.SimConstants.BRACES_CHARISMA_PENALTY;
        }

        // Apply boost if had braces removed (already included in charisma via
        // bracesCharismaBoost)
        // The boost is applied when setHadBracesRemoved is called during generation

        // Apply minor boost from ear piercings (jewelry enhances appearance)
        if (hasEarPiercing) {
            effectiveCharisma += earPiercingCharismaBoost;
        }

        return effectiveCharisma;
    }

    /**
     * Recalculates secondary stats that depend on charisma.
     * Call this after braces are applied or removed to update derived stats.
     */
    public void recalculateCharismaDependentStats() {
        // Recalculate empathy (primarily driven by charisma)
        this.empathy = (int) ((getEffectiveCharisma() * 1.5) + this.perception) / 2;
        // Recalculate responsibility
        this.responsibility = (int) ((getEffectiveCharisma() * 1.25) + (this.determination * 1.25)) / 2;
        // Recalculate open-mindedness
        this.openmindedness = (int) ((this.intelligence * 1.25) + (getEffectiveCharisma() * 1.25)) / 2;
    }

    // Vision issue getters and setters

    public boolean getHasMyopia() {
        return hasMyopia;
    }

    public void setHasMyopia(boolean hasMyopia) {
        this.hasMyopia = hasMyopia;
    }

    public boolean getHasHyperopia() {
        return hasHyperopia;
    }

    public void setHasHyperopia(boolean hasHyperopia) {
        this.hasHyperopia = hasHyperopia;
    }

    public boolean getHasAstigmatism() {
        return hasAstigmatism;
    }

    public void setHasAstigmatism(boolean hasAstigmatism) {
        this.hasAstigmatism = hasAstigmatism;
    }

    /**
     * Checks if the student has any vision issue (myopia, hyperopia, or
     * astigmatism).
     *
     * @return true if the student has any refractive error
     */
    public boolean hasVisionIssue() {
        return hasMyopia || hasHyperopia || hasAstigmatism;
    }

    /**
     * Gets a description of the student's vision issues.
     *
     * @return a String describing the vision issues, or null if none
     */
    public String getVisionIssueDescription() {
        if (!hasVisionIssue()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (hasMyopia && hasAstigmatism) {
            sb.append("myopia with astigmatism");
        } else if (hasHyperopia && hasAstigmatism) {
            sb.append("hyperopia with astigmatism");
        } else if (hasMyopia) {
            sb.append("myopia");
        } else if (hasHyperopia) {
            sb.append("hyperopia");
        } else if (hasAstigmatism) {
            sb.append("astigmatism");
        }

        return sb.toString();
    }

    // Corrective lens getters and setters

    public boolean getHasGlasses() {
        return hasGlasses;
    }

    public void setHasGlasses(boolean hasGlasses) {
        this.hasGlasses = hasGlasses;
    }

    public boolean getHasContacts() {
        return hasContacts;
    }

    public void setHasContacts(boolean hasContacts) {
        this.hasContacts = hasContacts;
    }

    /**
     * Checks if the student has any form of vision correction.
     *
     * @return true if the student has glasses or contacts
     */
    public boolean hasVisionCorrection() {
        return hasGlasses || hasContacts;
    }

    /**
     * Checks if the student has uncorrected vision issues.
     * This means they have a vision problem but no corrective lenses.
     *
     * @return true if they have vision issues without correction
     */
    public boolean hasUncorrectedVision() {
        return hasVisionIssue() && !hasVisionCorrection();
    }

    /**
     * Gets a description of the student's vision correction status.
     *
     * @return a String describing their correction (glasses, contacts, or none)
     */
    public String getVisionCorrectionDescription() {
        if (hasContacts) {
            return "contact lenses";
        } else if (hasGlasses) {
            return "glasses";
        } else {
            return "no correction";
        }
    }

    // Ear piercing getters and setters

    public boolean getHasEarPiercing() {
        return hasEarPiercing;
    }

    public void setHasEarPiercing(boolean hasEarPiercing) {
        this.hasEarPiercing = hasEarPiercing;
    }

    public int getEarPiercingLeftCount() {
        return earPiercingLeftCount;
    }

    public void setEarPiercingLeftCount(int earPiercingLeftCount) {
        this.earPiercingLeftCount = earPiercingLeftCount;
    }

    public int getEarPiercingRightCount() {
        return earPiercingRightCount;
    }

    public void setEarPiercingRightCount(int earPiercingRightCount) {
        this.earPiercingRightCount = earPiercingRightCount;
    }

    public String getEarPiercingType() {
        return earPiercingType;
    }

    public void setEarPiercingType(String earPiercingType) {
        this.earPiercingType = earPiercingType;
    }

    public String getEarPiercingMaterial() {
        return earPiercingMaterial;
    }

    public void setEarPiercingMaterial(String earPiercingMaterial) {
        this.earPiercingMaterial = earPiercingMaterial;
    }

    public String getEarPiercingSize() {
        return earPiercingSize;
    }

    public void setEarPiercingSize(String earPiercingSize) {
        this.earPiercingSize = earPiercingSize;
    }

    public int getEarPiercingCharismaBoost() {
        return earPiercingCharismaBoost;
    }

    public void setEarPiercingCharismaBoost(int earPiercingCharismaBoost) {
        this.earPiercingCharismaBoost = earPiercingCharismaBoost;
    }

    /**
     * Gets a natural-language description of the student's ear piercings.
     * Handles singular/plural forms, asymmetric counts (different number of
     * piercings per ear), and varies phrasing by ear configuration.
     *
     * Examples:
     * - "They have both ears pierced with gold studs."
     * - "Their left ear is pierced with a small silver hoop."
     * - "They have both ears pierced with small, black gauges."
     * - "They have both ears pierced with gold studs (2 per ear)."
     * - "They have both ears pierced with silver studs, with 2 on the left and 1 on the right."
     * - "Their left ear has 2 gold studs."
     *
     * @return a String describing the ear piercings, or null if none
     */
    public String getEarPiercingDescription() {
        if (!hasEarPiercing) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean bothEars = earPiercingLeftCount > 0 && earPiercingRightCount > 0;
        boolean leftOnly = earPiercingLeftCount > 0 && earPiercingRightCount == 0;

        if (bothEars) {
            sb.append("They have both ears pierced with ");
            appendJewelryName(sb, true);

            if (earPiercingLeftCount == earPiercingRightCount && earPiercingLeftCount > 1) {
                sb.append(" (").append(earPiercingLeftCount).append(" per ear)");
            } else if (earPiercingLeftCount != earPiercingRightCount) {
                sb.append(", with ").append(earPiercingLeftCount)
                        .append(" on the left and ").append(earPiercingRightCount).append(" on the right");
            }
        } else {
            String ear = leftOnly ? "left" : "right";
            int count = leftOnly ? earPiercingLeftCount : earPiercingRightCount;

            if (count > 1) {
                sb.append("Their ").append(ear).append(" ear has ").append(count).append(" ");
                appendJewelryName(sb, true);
            } else {
                sb.append("Their ").append(ear).append(" ear is pierced with ");
                appendJewelryName(sb, false);
            }
        }

        sb.append(".");
        return sb.toString();
    }

    /**
     * Appends the jewelry type description (material + type name) to a
     * StringBuilder, handling size prefixes and singular/plural forms.
     *
     * @param sb     the StringBuilder to append to
     * @param plural whether to use plural form of the jewelry type
     */
    private void appendJewelryName(StringBuilder sb, boolean plural) {
        if (!plural) {
            sb.append("a ");
        }

        boolean isGauge = "gauges".equals(earPiercingType);
        boolean isHoop = "hoops".equals(earPiercingType);

        if ((isGauge || isHoop) && earPiercingSize != null) {
            sb.append(earPiercingSize);
            sb.append(isGauge ? ", " : " ");
        }

        sb.append(earPiercingMaterial).append(" ");

        if (isGauge) {
            sb.append(plural ? "gauges" : "gauge");
        } else if (isHoop) {
            sb.append(plural ? "hoops" : "hoop");
        } else if ("dangling earrings".equals(earPiercingType)) {
            sb.append("dangling ").append(plural ? "earrings" : "earring");
        } else {
            sb.append(plural ? "studs" : "stud");
        }
    }

    /**
     * Gets the effective perception value, accounting for uncorrected vision
     * issues.
     * Students with uncorrected vision issues suffer perception penalties.
     *
     * @return the effective perception value
     */
    public int getEffectivePerception() {
        int effectivePerception = this.perception;

        // Only apply penalties if vision is uncorrected
        if (hasUncorrectedVision()) {
            if (hasMyopia) {
                effectivePerception -= constants.SimConstants.VISION_MYOPIA_PERCEPTION_PENALTY;
            }
            if (hasHyperopia) {
                effectivePerception -= constants.SimConstants.VISION_HYPEROPIA_PERCEPTION_PENALTY;
            }
            if (hasAstigmatism) {
                effectivePerception -= constants.SimConstants.VISION_ASTIGMATISM_PERCEPTION_PENALTY;
            }
        }

        // Ensure perception doesn't go below 1
        return Math.max(1, effectivePerception);
    }

    /**
     * Gets the effective agility value, accounting for uncorrected vision issues.
     * Students with uncorrected vision issues suffer agility penalties due to
     * impaired depth perception and spatial awareness.
     *
     * @return the effective agility value
     */
    public int getEffectiveAgility() {
        int effectiveAgility = this.agility;

        // Only apply penalties if vision is uncorrected
        if (hasUncorrectedVision()) {
            if (hasMyopia) {
                effectiveAgility -= constants.SimConstants.VISION_MYOPIA_AGILITY_PENALTY;
            }
            if (hasHyperopia) {
                effectiveAgility -= constants.SimConstants.VISION_HYPEROPIA_AGILITY_PENALTY;
            }
            if (hasAstigmatism) {
                effectiveAgility -= constants.SimConstants.VISION_ASTIGMATISM_AGILITY_PENALTY;
            }
        }

        // Ensure agility doesn't go below 1
        return Math.max(1, effectiveAgility);
    }

    /**
     * Gets the total perception penalty from uncorrected vision.
     *
     * @return the total perception penalty, or 0 if vision is corrected
     */
    public int getVisionPerceptionPenalty() {
        if (!hasUncorrectedVision()) {
            return 0;
        }

        int penalty = 0;
        if (hasMyopia) {
            penalty += constants.SimConstants.VISION_MYOPIA_PERCEPTION_PENALTY;
        }
        if (hasHyperopia) {
            penalty += constants.SimConstants.VISION_HYPEROPIA_PERCEPTION_PENALTY;
        }
        if (hasAstigmatism) {
            penalty += constants.SimConstants.VISION_ASTIGMATISM_PERCEPTION_PENALTY;
        }
        return penalty;
    }

    /**
     * Gets the total agility penalty from uncorrected vision.
     *
     * @return the total agility penalty, or 0 if vision is corrected
     */
    public int getVisionAgilityPenalty() {
        if (!hasUncorrectedVision()) {
            return 0;
        }

        int penalty = 0;
        if (hasMyopia) {
            penalty += constants.SimConstants.VISION_MYOPIA_AGILITY_PENALTY;
        }
        if (hasHyperopia) {
            penalty += constants.SimConstants.VISION_HYPEROPIA_AGILITY_PENALTY;
        }
        if (hasAstigmatism) {
            penalty += constants.SimConstants.VISION_ASTIGMATISM_AGILITY_PENALTY;
        }
        return penalty;
    }

    // --- Allostatic Load implementation ---

    @Override
    public AllostaticLoad getAllostaticLoad() {
        return this.allostaticLoad;
    }

    // --- Secondary Stat Max Cap getters ---

    @Override
    public int getMaxCreativity() {
        return maxCreativity;
    }

    @Override
    public int getMaxEmpathy() {
        return maxEmpathy;
    }

    @Override
    public int getMaxAdaptability() {
        return maxAdaptability;
    }

    @Override
    public int getMaxInitiative() {
        return maxInitiative;
    }

    @Override
    public int getMaxResilience() {
        return maxResilience;
    }

    @Override
    public int getMaxCuriosity() {
        return maxCuriosity;
    }

    @Override
    public int getMaxResponsibility() {
        return maxResponsibility;
    }

    @Override
    public int getMaxOpenMindedness() {
        return maxOpenmindedness;
    }

    /**
     * {@inheritDoc}
     * Drains the specified secondary stat and notifies the allostatic load meter.
     */
    @Override
    public void drainSecondaryStat(String statName, int amount, double stressFactor) {
        if (amount <= 0) {
            return;
        }
        switch (statName.toLowerCase()) {
            case "creativity" -> {
                int drained = Math.min(amount, this.creativity);
                this.creativity = Math.max(0, this.creativity - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxCreativity, stressFactor);
            }
            case "empathy" -> {
                int drained = Math.min(amount, this.empathy);
                this.empathy = Math.max(0, this.empathy - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxEmpathy, stressFactor);
            }
            case "adaptability" -> {
                int drained = Math.min(amount, this.adaptability);
                this.adaptability = Math.max(0, this.adaptability - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxAdaptability, stressFactor);
            }
            case "initiative" -> {
                int drained = Math.min(amount, this.initiative);
                this.initiative = Math.max(0, this.initiative - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxInitiative, stressFactor);
            }
            case "resilience" -> {
                int drained = Math.min(amount, this.resilience);
                this.resilience = Math.max(0, this.resilience - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxResilience, stressFactor);
            }
            case "curiosity" -> {
                int drained = Math.min(amount, this.curiosity);
                this.curiosity = Math.max(0, this.curiosity - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxCuriosity, stressFactor);
            }
            case "responsibility" -> {
                int drained = Math.min(amount, this.responsibility);
                this.responsibility = Math.max(0, this.responsibility - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxResponsibility, stressFactor);
            }
            case "openmindedness" -> {
                int drained = Math.min(amount, this.openmindedness);
                this.openmindedness = Math.max(0, this.openmindedness - amount);
                allostaticLoad.onSecondaryStatDrain(drained, maxOpenmindedness, stressFactor);
            }
            default -> { /* Unknown stat name, do nothing */ }
        }
    }

    /**
     * {@inheritDoc}
     * Restores all secondary stats to their max caps. Called during sleep.
     */
    @Override
    public void replenishAllSecondaryStats() {
        this.creativity = this.maxCreativity;
        this.empathy = this.maxEmpathy;
        this.adaptability = this.maxAdaptability;
        this.initiative = this.maxInitiative;
        this.resilience = this.maxResilience;
        this.curiosity = this.maxCuriosity;
        this.responsibility = this.maxResponsibility;
        this.openmindedness = this.maxOpenmindedness;
    }

    // ---- Clique Identity ----

    public String getMainClique() {
        return mainClique;
    }

    public void setMainClique(String mainClique) {
        this.mainClique = mainClique;
    }

    public String getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(String subgroup) {
        this.subgroup = subgroup;
    }

    public String getCliqueLabel() {
        if (subgroup == null || mainClique == null) {
            return mainClique;
        }
        return subgroup + " " + mainClique;
    }

    public String getSecondaryClique() {
        return secondaryClique;
    }

    public void setSecondaryClique(String secondaryClique) {
        this.secondaryClique = secondaryClique;
    }

    // ---- Outfit ----

    /**
     * {@inheritDoc}
     * Returns the student's current outfit. Never {@code null}; an empty
     * {@link Outfit} is used as the default placeholder before
     * clique-aware clothing generation runs.
     */
    @Override
    public Outfit getCurrentOutfit() {
        if (currentOutfit == null) {
            currentOutfit = new Outfit();
        }
        return currentOutfit;
    }

    /**
     * {@inheritDoc}
     * Stores the given outfit on the student. A {@code null} argument is
     * coerced to an empty {@link Outfit} so callers can rely on
     * {@link #getCurrentOutfit()} never returning {@code null}.
     */
    @Override
    public void setCurrentOutfit(Outfit outfit) {
        this.currentOutfit = outfit == null ? new Outfit() : outfit;
    }

}
