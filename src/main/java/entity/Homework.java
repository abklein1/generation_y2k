package entity;

import utility.GameRandom;

public class Homework implements Boss {

    private final int questions;
    private final int time;
    private final int difficulty;
    private String fullName;

    public Homework() {
        this.questions = GameRandom.nextInt(3, 30);
        this.time = GameRandom.nextInt(10, 120);
        this.difficulty = GameRandom.nextInt(10, 100);
        this.fullName = "entity.Homework";
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
