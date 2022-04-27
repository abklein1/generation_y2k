import java.util.concurrent.ThreadLocalRandom;

public class Quiz implements Boss {

    private final int questions;
    private final int time;
    private final int difficulty;
    private String fullName;

    public Quiz() {
        this.questions = setRandom(5, 15);
        this.time = setRandom(10, 30);
        this.difficulty = setRandom(10, 110);
        this.fullName = "Quiz";
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
