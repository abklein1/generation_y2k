package utility;

import java.util.concurrent.ThreadLocalRandom;

public class Randomizer {
    public static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}
