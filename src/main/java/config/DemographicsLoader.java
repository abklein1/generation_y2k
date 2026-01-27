package config;

/**
 * Utility class to create TownDemographics configurations.
 * All demographic distributions are centralized in SimConstants and used
 * by TownDemographics when it initializes its default values.
 */
public class DemographicsLoader {

    /**
     * Creates a default TownDemographics using values from SimConstants.
     *
     * @return a TownDemographics with default values
     */
    public static TownDemographics createDefault() {
        return new TownDemographics();
    }

    /**
     * Returns the default TownDemographics configuration.
     * This method exists for backward compatibility.
     *
     * @return the default TownDemographics
     */
    public static TownDemographics loadOrDefault() {
        return createDefault();
    }

    /**
     * Creates a TownDemographics for a small school/town.
     *
     * @return demographics configured for a small population
     */
    public static TownDemographics createSmall() {
        TownDemographics demographics = new TownDemographics();
        demographics.setTotalStudentPopulation(400);
        demographics.setTotalStaffPopulation(35);
        return demographics;
    }

    /**
     * Creates a TownDemographics for a medium school/town.
     *
     * @return demographics configured for a medium population
     */
    public static TownDemographics createMedium() {
        TownDemographics demographics = new TownDemographics();
        demographics.setTotalStudentPopulation(800);
        demographics.setTotalStaffPopulation(65);
        return demographics;
    }

    /**
     * Creates a TownDemographics for a large school/town.
     *
     * @return demographics configured for a large population
     */
    public static TownDemographics createLarge() {
        TownDemographics demographics = new TownDemographics();
        demographics.setTotalStudentPopulation(1600);
        demographics.setTotalStaffPopulation(130);
        return demographics;
    }

    /**
     * Creates a TownDemographics for testing with minimal population.
     *
     * @return demographics configured for testing
     */
    public static TownDemographics createTest() {
        TownDemographics demographics = new TownDemographics();
        demographics.setTotalStudentPopulation(50);
        demographics.setTotalStaffPopulation(10);
        demographics.setExtraStudentPoolPercent(0.0);
        demographics.setExtraStaffPoolPercent(0.0);
        return demographics;
    }
}
