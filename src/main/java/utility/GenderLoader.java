package utility;

import static constants.SimConstants.GENDER_MALE_RATE;
import static constants.SimConstants.GENDER_SAMPLE_SIZE;

// TODO: Add input for year based on gender table to change gender distribution
public class GenderLoader {
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
}
