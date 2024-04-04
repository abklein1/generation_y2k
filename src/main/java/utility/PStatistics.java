package utility;

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

    double getHeight();

    void setCreativity(int creativity);

    int getCreativity();

    void setEmpathy(int empathy);

    int getEmpathy();

    void setAdaptability(int adaptability);

    int getAdaptability();

    void setInitiative(int initiative);

    int getInitiative();

    void setResilience(int resilience);

    int getResilience();

    void setCuriosity(int curiosity);

    int getCuriosity();

    void setResponsibility(int responsibility);

    int getResponsibility();

    void setOpenMindedness(int openMindedness);

    int getOpenMindedness();

}
