package entity;

import java.util.concurrent.ThreadLocalRandom;

public class Homework implements Boss {

    private final int questions;
    private final int time;
    private final int difficulty;
    private String fullName;

    public Homework() {
        this.questions = setRandom(3, 30);
        this.time = setRandom(10, 120);
        this.difficulty = setRandom(10, 100);
        this.fullName = "entity.Homework";
    }

    private static Integer setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

    @Override
    public int getStatsNumberOfQuestions() {
        return this.questions;
    }

    @Override
    public int getStatsDifficulty() {
        return this.difficulty;
    }

    @Override
    public int getStatsTime() {
        return this.time;
    }

    @Override
    public String getName() {
        return this.fullName;
    }

    @Override
    public void setName(String name) {
        fullName = name;
    }
}
