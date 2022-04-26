public class StudentStatistics implements PStatistics {

    int height;
    String eyeColor;
    String hairColor;
    String build;
    int intelligence;
    int charisma;
    int agility;
    int determination;
    int strength;
    boolean sleep;
    int boredom;
    int level;
    int experience;

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
}
