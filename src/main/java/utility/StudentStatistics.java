package utility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public void setHeight(int height) {
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
            case 0:
                this.gradeLevel = "Freshman";
                break;
            case 1:
                this.gradeLevel = "Sophomore";
                break;
            case 2:
                this.gradeLevel = "Junior";
                break;
            case 3:
                this.gradeLevel = "Senior";
                break;
        }
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

        Random random = new Random();
        double mean = 0;
        double stdDev = 0;

        if (gender.equals("Male")) {
            switch (gradeLevel) {
                case "Freshman":
                    mean = 59;
                    stdDev = 5;
                    break;
                case "Sophomore":
                    mean = 64.5;
                    stdDev = 5.5;
                    break;
                case "Junior":
                    mean = 68;
                    stdDev = 4.5;
                    break;
                case "Senior":
                    mean = 69.5;
                    stdDev = 3.3;
                    break;
            }
        } else {
            switch (gradeLevel) {
                case "Freshman":
                    mean = 59.5;
                    stdDev = 4.5;
                    break;
                case "Sophomore":
                    mean = 63.5;
                    stdDev = 4.5;
                    break;
                case "Junior":
                case "Senior":
                    mean = 64;
                    stdDev = 3;
                    break;
            }
        }

        this.height = mean + stdDev * random.nextGaussian();

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
        Random random = new Random();
        double meanBaseStr = 50;
        double stdDevStr = 10;
        int baseStr = (int) (meanBaseStr + stdDevStr * random.nextGaussian());

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
}
