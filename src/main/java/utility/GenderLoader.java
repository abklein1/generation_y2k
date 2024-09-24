package utility;

import java.util.concurrent.ThreadLocalRandom;
import constants.SimConstants.*;

import static constants.SimConstants.*;

// TODO: Revise this as needed. May set up for seeding
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
