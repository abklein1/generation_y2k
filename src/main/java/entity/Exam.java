package entity;

import utility.GameRandom;

public class Exam implements Boss {

    private final int questions;
    private final int time;
    private final int difficulty;
    private String fullName;

    public Exam() {
        this.questions = GameRandom.nextInt(6, 50);
        this.time = GameRandom.nextInt(30, 130);
        this.difficulty = GameRandom.nextInt(30, 200);
        this.fullName = "entity.Exam";
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
