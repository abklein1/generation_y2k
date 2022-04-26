public interface PStatistics {
    void setHeight(int height);

    void setEyeColor(String eyeColor);

    void setHairColor(String hairColor);

    void setBuild(String build);

    void setIntelligence(int intelligence);

    void setCharisma(int charisma);

    void setAgility(int agility);

    void setDetermination(int determination);

    void setStrength(int strength);

    void setSleepState(boolean sleepState);

    void setBoredom(int boredom);

    int getBoredom();

    boolean getSleepState();

    int getStrength();

    int getDetermination();

    int getAgility();

    int getCharisma();

    int getIntelligence();

    String getBuild();

    String getHairColor();

    String getEyeColor();

    int getHeight();

}
