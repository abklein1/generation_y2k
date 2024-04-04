package utility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StudentStatistics implements PStatistics {

    private final List<Integer> grades;
    private int height;
    private String eyeColor;
    private String hairColor;
    private String build;
    private String gradeLevel;
    private int intelligence;
    private int charisma;
    private int agility;
    private int determination;
    private int strength;
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
        this.build = null;
        this.intelligence = 0;
        this.charisma = 0;
        this.agility = 0;
        this.determination = 0;
        this.strength = 0;
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

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
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
    public int getHeight() {
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

    // TODO: set buckets for grade levels, maybe make into enum
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

    // TODO: values should really be a distribution across range, not random within
    public void setHeight(String gender, String gradeLevel) {
        if (gender.equals("Male")) {
            switch (gradeLevel) {
                case "Freshman":
                    this.height = setRandom(54, 64);
                    break;
                case "Sophomore":
                    this.height = setRandom(59, 70);
                    break;
                case "Junior":
                    this.height = setRandom(63, 73);
                    break;
                case "Senior":
                    this.height = setRandom(65, 74);
                    break;
            }
        } else {
            switch (gradeLevel) {
                case "Freshman":
                    this.height = setRandom(55, 64);
                    break;
                case "Sophomore":
                    this.height = setRandom(59, 68);
                    break;
                case "Junior":
                case "Senior":
                    this.height = setRandom(60, 68);
                    break;
            }
        }
    }
}
