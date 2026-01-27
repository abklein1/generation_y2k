package utility;

import static constants.SimConstants.GENDER_MALE_RATE;
import static constants.SimConstants.GENDER_SAMPLE_SIZE;

// TODO: Add input for year based on gender table to change gender distribution
public class GenderLoader {
    /**
     * Selects a gender using the default distribution from SimConstants.
     *
     * @return "male" or "female"
     */
    public static String genderSelection() {
        int selection = GameRandom.nextInt(0, GENDER_SAMPLE_SIZE - 1);
        String gender;
        if (selection <= GENDER_MALE_RATE) {
            gender = "male";
        } else {
            gender = "female";
        }
        return gender;
    }

    /**
     * Selects a gender using a custom male percentage.
     *
     * @param malePercent the percentage of males (0.0 to 1.0)
     * @return "male" or "female"
     */
    public static String genderSelection(double malePercent) {
        double roll = GameRandom.nextDouble();
        return roll < malePercent ? "male" : "female";
    }
}
