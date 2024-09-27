package utility;

import java.util.concurrent.ThreadLocalRandom;

import static constants.SimConstants.GENDER_MALE_RATE;
import static constants.SimConstants.GENDER_SAMPLE_SIZE;

// TODO: Revise this as needed. May set up for seeding
// TODO: Add input for year based on gender table to change gender distribution
public class GenderLoader {
    public static String genderSelection() {
        int selection = ThreadLocalRandom.current().nextInt(0, GENDER_SAMPLE_SIZE);
        String gender;
        if (selection <= GENDER_MALE_RATE) {
            gender = "male";
        } else {
            gender = "female";
        }
        return gender;
    }


}
