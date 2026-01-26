package utility;

import entity.Student;
import entity.StudentBlock;
import entity.StudentSchedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentStatistics implements PStatistics {

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
    private boolean sleep;
    private int boredom;
    private int level;
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
    private String incomeLevel;
    private final ArrayList<String> completedClasses;
    private final StudentSchedule studentSchedule;
    private final ArrayList<Student> siblingsInSchool;
    private final ArrayList<String> siblingsNotInSchool;
    private final ArrayList<Student> friendsInSchool;
    private int maxBestFriends;
    private boolean hasBraces;
    private String bracesBandColor;
    private String bracesSecondBandColor;  // For alternating band colors
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
    private boolean hasMyopia;          // Nearsightedness
    private boolean hasHyperopia;       // Farsightedness
    private boolean hasAstigmatism;     // Astigmatism (can combine with myopia or hyperopia)
    // Corrective lenses - glasses or contacts
    private boolean hasGlasses;         // Wears glasses
    private boolean hasContacts;        // Wears contact lenses (may also have glasses as backup)



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
        this.sleep = false;
        this.boredom = 0;
        this.level = 0;
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
        this.incomeLevel = null;
        this.completedClasses = new ArrayList<>();
        this.studentSchedule = new StudentSchedule();
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
    }

    @Override
    public int getBoredom() {
        return this.boredom;
    }

    @Override
    public void setBoredom(int boredom) {
        this.boredom = boredom;
    }

    @Override
    public boolean getSleepState() {
        return this.sleep;
    }

    @Override
    public void setSleepState(boolean sleepState) {
        this.sleep = sleepState;
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

    public void setLevel(int level) {
        this.level = level;
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
        System.out.println(this.grades);
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

    //TODO: remove calculation from Student stats
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

    //TODO: basic calculations for now
    public void setInitCreativity() {
        // Primarily driven by intelligence and secondary by perception
        this.creativity = (int) ((this.intelligence * 1.5) + this.perception) / 2;
    }

    public void setInitEmpathy() {
        // Primarily driven by charisma and secondary by perception
        this.empathy = (int) ((this.charisma * 1.5) + this.perception) / 2;
    }

    public void setInitAdaptability() {
        // Physical and mental adaptability and tertiary determination
        this.adaptability = (this.agility + this.intelligence + (this.determination / 4)) / 2;
    }

    public void setInitInitiative() {
        // Primarily driven by determination
        this.initiative = (int) ((this.determination * 1.5) + this.perception) / 2;
    }

    public void setInitResilience() {
        // Primary strength and secondary determination
        this.resilience = (int) ((this.strength * 1.5) + this.determination) / 2;
    }

    public void setInitCuriosity() {
        this.curiosity = (int) ((this.perception * 1.5) + this.intelligence) / 2;
    }

    public void setInitResponsibility() {
        this.responsibility = (int) ((this.charisma * 1.25) + (this.determination * 1.25)) / 2;
    }

    public void setInitOpenMind() {
        this.openmindedness = (int) ((this.intelligence * 1.25) + (this.charisma * 1.25)) / 2;
    }

    //TODO: Experiment with more narrative descriptions. ex. 'Rachel has wavy, brown hair that falls past her shoulders'
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

    public void setInitIncomeLevel(int choice) {
        if (choice <= 25) {
            this.incomeLevel = "low";
        } else if (choice <= 85) {
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

    public void addStudentSchedule(StudentBlock block) {
        this.studentSchedule.add(block);
    }

    public void removeStudentSchedule(StudentBlock block) {
        this.studentSchedule.remove(block);
    }

    public void addSiblingsInSchool(Student sibling) {
        this.siblingsInSchool.add(sibling);
    }

    public void removeSiblingsInSchool(Student sibling) {
        this.siblingsInSchool.remove(sibling);
    }

    public ArrayList<Student> getSiblingsInSchool() {
        return siblingsInSchool;
    }

    public void addSiblingsNotInSchool(String name) {
        this.siblingsNotInSchool.add(name);
    }

    public void removeSiblingsNotInSchool(String name) {
        this.siblingsNotInSchool.remove(name);
    }

    public ArrayList<String> getSiblingsNotInSchool() {
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
     * Students who previously had braces and had them removed get a permanent boost.
     *
     * @return the effective charisma value
     */
    public int getEffectiveCharisma() {
        int effectiveCharisma = this.charisma;

        // Apply penalty if currently wearing braces
        if (hasBraces) {
            effectiveCharisma -= constants.SimConstants.BRACES_CHARISMA_PENALTY;
        }

        // Apply boost if had braces removed (already included in charisma via bracesCharismaBoost)
        // The boost is applied when setHadBracesRemoved is called during generation

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
     * Checks if the student has any vision issue (myopia, hyperopia, or astigmatism).
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

    /**
     * Gets the effective perception value, accounting for uncorrected vision issues.
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

}
