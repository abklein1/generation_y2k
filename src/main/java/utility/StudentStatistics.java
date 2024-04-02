package utility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return this.gender;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDate getBirthday() {
        return birthday;
    }
}
