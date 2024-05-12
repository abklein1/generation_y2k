package utility;

import java.util.concurrent.ThreadLocalRandom;

public class Randomizer {
    //Not ideal design but need to add a few helpers here for simulation
    public static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}
