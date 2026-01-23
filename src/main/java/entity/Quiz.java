package entity;

import utility.GameRandom;

public class Quiz implements Boss {

    private final int questions;
    private final int time;
    private final int difficulty;
    private String fullName;

    public Quiz() {
        this.questions = GameRandom.nextInt(5, 15);
        this.time = GameRandom.nextInt(10, 30);
        this.difficulty = GameRandom.nextInt(10, 110);
        this.fullName = "entity.Quiz";
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
