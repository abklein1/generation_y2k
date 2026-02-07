package utility;

import entity.AllostaticLoad;
import entity.StaffType;
import entity.TeacherBlock;
import entity.TeacherSchedule;

import java.time.LocalDate;

public class TeacherStatistics implements PStatistics {

    private static final long serialVersionUID = 1L;

    private double height;
    private String eyeColor;
    private String hairColor;
    private String hairLength;
    private String hairType;
    private String build;
    private int intelligence;
    private int charisma;
    private int agility;
    private int determination;
    private int perception;
    private int strength;
    private int luck;
    private boolean sleep;
    private int boredom;
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
    private Enum staffType;
    private final TeacherSchedule teacherSchedule;
    private int yearsOfExperience;

    // Secondary stat max caps
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
    // Vision issues - refractive errors
    private boolean hasMyopia;
    private boolean hasHyperopia;
    private boolean hasAstigmatism;
    // Corrective lenses
    private boolean hasGlasses;
    private boolean hasContacts;

    public TeacherStatistics() {
        this.height = 0;
        this.eyeColor = null;
        this.hairColor = null;
        this.hairLength = null;
        this.hairType = null;
        this.build = null;
        this.intelligence = 0;
        this.charisma = 0;
        this.agility = 0;
        this.determination = 0;
        this.perception = 0;
        this.strength = 0;
        this.luck = 0;
        this.sleep = false;
        this.boredom = 0;
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
        this.staffType = null;
        this.teacherSchedule = new TeacherSchedule();
        this.yearsOfExperience = 0;
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

    public void setInitHeight() {
        double mean = 0;
        double stdDev = 0;

        if (gender.equals("Male")) {
            mean = 69.2;
            stdDev = 2.66;
        } else {
            mean = 64.3;
            stdDev = 2.58;
        }

        this.height = GameRandom.nextGaussian(mean, stdDev);
    }

    // TODO: change current year/remove hardcode values
    public void setInitStrength() {
        double meanBaseStr = 50;
        double stdDevStr = 10;
        int baseStr = (int) GameRandom.nextGaussian(meanBaseStr, stdDevStr);

        double heightMod = (this.height - 60) * 0.5;
        int genderMod = this.gender.equals("Male") ? 10 : 5;
        int age = getAge();
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
     * Initializes the allostatic load tolerance based on this teacher's stats.
     * Should be called after resilience and determination are set.
     */
    public void initAllostaticLoad() {
        this.allostaticLoad.initTolerance(this.resilience, this.determination);
    }

    @Override
    public int getPerception() {
        return this.perception;
    }

    @Override
    public void setPerception(int perception) {
        this.perception = perception;
    }

    @Override
    public void setInitHairLength(int choice) {
        int age = getAge();

        if (this.gender.equals("male")) {
            if (age <= 29) {
                if (choice <= 30) {
                    this.hairLength = "waist-length";
                } else if (choice <= 250) {
                    this.hairLength = "shoulder-length";
                } else if (choice <= 3250) {
                    this.hairLength = "long";
                } else if (choice <= 4000) {
                    this.hairLength = "chin-length";
                } else if (choice <= 5300) {
                    this.hairLength = "balding";
                } else if (choice <= 5600) {
                    this.hairLength = "bald";
                } else {
                    this.hairLength = "short";
                }
            } else if (age <= 49) {
                if (choice <= 30) {
                    this.hairLength = "waist-length";
                } else if (choice <= 150) {
                    this.hairLength = "shoulder-length";
                } else if (choice <= 1750) {
                    this.hairLength = "long";
                } else if (choice <= 2750) {
                    this.hairLength = "chin-length";
                } else if (choice <= 7750) {
                    this.hairLength = "balding";
                } else if (choice <= 8250) {
                    this.hairLength = "bald";
                } else {
                    this.hairLength = "short";
                }
            } else {
                if (choice <= 20) {
                    this.hairLength = "waist-length";
                } else if (choice <= 150) {
                    this.hairLength = "shoulder-length";
                } else if (choice <= 950) {
                    this.hairLength = "long";
                } else if (choice <= 1500) {
                    this.hairLength = "chin-length";
                } else if (choice <= 6000) {
                    this.hairLength = "balding";
                } else if (choice <= 7000) {
                    this.hairLength = "bald";
                } else {
                    this.hairLength = "short";
                }
            }
        } else {
            if (age <= 30) {
                if (choice <= 40) {
                    this.hairLength = "extremely long";
                } else if (choice <= 78) {
                    this.hairLength = "waist-length";
                } else if (choice <= 321) {
                    this.hairLength = "shoulder-length";
                } else if (choice <= 1621) {
                    this.hairLength = "long";
                } else if (choice <= 8300) {
                    this.hairLength = "chin-length";
                } else {
                    this.hairLength = "short";
                }
            } else if (age <= 50) {
                if (choice <= 20) {
                    this.hairLength = "extremely long";
                } else if (choice <= 60) {
                    this.hairLength = "waist-length";
                } else if (choice <= 400) {
                    this.hairLength = "shoulder-length";
                } else if (choice <= 1600) {
                    this.hairLength = "long";
                } else if (choice <= 7000) {
                    this.hairLength = "chin-length";
                } else {
                    this.hairLength = "short";
                }
            } else {
                if (choice <= 20) {
                    this.hairLength = "extremely long";
                } else if (choice <= 30) {
                    this.hairLength = "waist-length";
                } else if (choice <= 200) {
                    this.hairLength = "shoulder-length";
                } else if (choice <= 1000) {
                    this.hairLength = "long";
                } else if (choice <= 4000) {
                    this.hairLength = "chin-length";
                } else {
                    this.hairLength = "short";
                }
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

    public Enum getStaffType() {
        return this.staffType;
    }

    public void setStaffType(Enum<StaffType> type) {
        this.staffType = type;
    }

    public int getLuck() {
        return this.luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public TeacherSchedule getTeacherSchedule() {
        return teacherSchedule;
    }

    public void addTeacherSchedule(TeacherBlock block) {
        teacherSchedule.add(block);
    }

    public int getAge() {
        int currentYear = 2004;
        int birthYear = this.birthday.getYear();
        return currentYear - birthYear;
    }

    public int getYearsOfExperience() {
        return this.yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
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
     * Checks if the teacher has any vision issue.
     *
     * @return true if they have any refractive error
     */
    public boolean hasVisionIssue() {
        return hasMyopia || hasHyperopia || hasAstigmatism;
    }

    /**
     * Checks if the teacher has any form of vision correction.
     *
     * @return true if they have glasses or contacts
     */
    public boolean hasVisionCorrection() {
        return hasGlasses || hasContacts;
    }

    /**
     * Gets a description of the teacher's vision issues.
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

    /**
     * Gets a description of the teacher's vision correction.
     *
     * @return a String describing their correction
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
}
