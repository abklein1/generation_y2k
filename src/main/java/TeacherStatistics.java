public class TeacherStatistics implements PStatistics {
    private int height;
    private String eyeColor;
    private String hairColor;
    private String build;
    private int intelligence;
    private int charisma;
    private int agility;
    private int determination;
    private int strength;
    private boolean sleep;
    private int boredom;

    public TeacherStatistics(){
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
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    @Override
    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    @Override
    public void setBuild(String build) {
        this.build = build;
    }

    @Override
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    @Override
    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    @Override
    public void setAgility(int agility) {
        this.agility = agility;
    }

    @Override
    public void setDetermination(int determination) {
        this.determination = determination;
    }

    @Override
    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public void setSleepState(boolean sleepState) {
        this.sleep = sleepState;
    }

    @Override
    public void setBoredom(int boredom) {
        this.boredom = boredom;
    }

    @Override
    public int getBoredom() {
        return this.boredom;
    }

    @Override
    public boolean getSleepState() {
        return this.sleep;
    }

    @Override
    public int getStrength() {
        return this.strength;
    }

    @Override
    public int getDetermination() {
        return this.determination;
    }

    @Override
    public int getAgility() {
        return this.agility;
    }

    @Override
    public int getCharisma() {
        return this.charisma;
    }

    @Override
    public int getIntelligence() {
        return this.intelligence;
    }

    @Override
    public String getBuild() {
        return this.build;
    }

    @Override
    public String getHairColor() {
        return this.hairColor;
    }

    @Override
    public String getEyeColor() {
        return this.eyeColor;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
