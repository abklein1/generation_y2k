import java.util.concurrent.ThreadLocalRandom;

public class Homework implements Boss{

    private int questions;
    private int time;
    private int difficulty;
    private String fullName;

    public Homework(){
        this.questions = setRandom(3, 30);
        this.time = setRandom(10,120);
        this.difficulty = setRandom(1, 10);
        this.fullName = "Homework";
    }

    @Override
    public void setName(String name) {
        fullName = name;
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

    private static Integer setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }
}
