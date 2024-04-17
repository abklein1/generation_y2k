package utility;

import java.util.concurrent.ThreadLocalRandom;

// TODO: Revise this as needed. May set up for seeding
public class GenderLoader {
    public static String genderSelection() {
        int selection = ThreadLocalRandom.current().nextInt(0, 11);
        String gender = null;
        if (selection <= 4) {
            gender = "male";
        } else {
            gender = "female";
        }
        return gender;
    }


}
