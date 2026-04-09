package utility;

import entity.AllostaticLoad;

import java.io.Serializable;

public interface PStatistics extends Serializable {
    int getStrength();

    void setStrength(int strength);

    int getDetermination();

    void setDetermination(int determination);

    int getAgility();

    void setAgility(int agility);

    int getCharisma();

    void setCharisma(int charisma);

    int getIntelligence();

    void setIntelligence(int intelligence);

    String getBuild();

    void setBuild(String build);

    String getHairColor();

    void setHairColor(String hairColor);

    String getEyeColor();

    void setEyeColor(String eyeColor);

    double getHeight();

    void setHeight(double height);

    int getCreativity();

    void setCreativity(int creativity);

    int getEmpathy();

    void setEmpathy(int empathy);

    int getAdaptability();

    void setAdaptability(int adaptability);

    int getInitiative();

    void setInitiative(int initiative);

    int getResilience();

    void setResilience(int resilience);

    int getCuriosity();

    void setCuriosity(int curiosity);

    int getResponsibility();

    void setResponsibility(int responsibility);

    int getOpenMindedness();

    void setOpenMindedness(int openMindedness);

    int getPerception();

    void setPerception(int perception);

    void setInitHairLength(int choice);

    String getHairLength();

    void setHairLength(String hairLength);

    // --- Allostatic Load ---

    /**
     * Gets the allostatic load meter for this person.
     *
     * @return the AllostaticLoad instance
     */
    AllostaticLoad getAllostaticLoad();

    // --- Secondary Stat Max Caps ---
    // Max caps represent the initial calculated ceiling for each secondary stat.
    // Current values (returned by the standard getters) can be drained below these
    // caps through daily activities and are replenished during sleep.

    int getMaxCreativity();

    int getMaxEmpathy();

    int getMaxAdaptability();

    int getMaxInitiative();

    int getMaxResilience();

    int getMaxCuriosity();

    int getMaxResponsibility();

    int getMaxOpenMindedness();

    /**
     * Drains a secondary stat by the given amount, clamping at 0.
     * Also notifies the allostatic load system of the drain.
     *
     * @param statName     the name of the stat to drain (e.g. "empathy",
     *                     "resilience")
     * @param amount       the amount to drain
     * @param stressFactor how stressful this drain is (1.0 = normal)
     */
    void drainSecondaryStat(String statName, int amount, double stressFactor);

    /**
     * Replenishes all secondary stats to their max caps.
     * Called when a person sleeps at the end of the day.
     */
    void replenishAllSecondaryStats();
}
