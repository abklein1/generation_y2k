package utility;

import java.util.concurrent.ThreadLocalRandom;

public class TraitSelection {

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String hairSelection(int selection) {
        if (selection >= 0 && selection <= 21) {
            return "black";
        } else if (selection >= 22 && selection <= 37) {
            return "dark brown";
        } else if (selection >= 38 && selection <= 48) {
            return "medium brown";
        } else if (selection >= 49 && selection <= 56) {
            return "light brown";
        } else if (selection >= 57 && selection <= 64) {
            return "blond";
        } else if (selection >= 65 && selection <= 71) {
            return "chestnut";
        } else if (selection >= 72 && selection <= 78) {
            return "mahogany";
        } else if (selection >= 79 && selection <= 84) {
            return "dirty blond";
        } else if (selection >= 85 && selection <= 89) {
            return "golden blond";
        } else if (selection >= 90 && selection <= 93) {
            return "light blond";
        } else if (selection >= 94 && selection < 96) {
            return "golden brown";
        } else if (selection == 97) {
            return "caramel";
        } else if (selection == 98) {
            return "strawberry blond";
        } else if (selection == 99) {
            return "copper";
        } else if (selection == 100) {
            return "red";
        } else if (selection == 101) {
            return "platinum blond";
        } else {
            int random = setRandom(0, 6);
            if (random == 0) {
                return "auburn";
            } else if (random == 1) {
                return "amber";
            } else if (random == 2) {
                return "titian";
            } else if (random == 3) {
                return "white";
            } else if (random == 4) {
                return "no";
            } else if (random == 5) {
                return "gray";
            } else {
                return "champagne";
            }
        }
    }

    public static String eyeSelection(int selection) {
        if (selection >= 0 && selection <= 52) {
            return "dark brown";
        } else if (selection >= 53 && selection <= 75) {
            return "light brown";
        } else if (selection >= 76 && selection <= 83) {
            return "blue";
        } else if (selection >= 84 && selection <= 90) {
            return "light blue";
        } else if (selection >= 91 && selection <= 96) {
            return "hazel";
        } else if (selection >= 97 && selection <= 102) {
            return "amber";
        } else if (selection >= 103 && selection <= 105) {
            return "green";
        } else if (selection == 106) {
            return "gray";
        } else if (selection == 107) {
            return "violet";
        } else if (selection == 108) {
            return "black";
        } else {
            return "heterochromia";
        }
    }

    public static String hairType(int selection) {
        if (selection >= 0 && selection <= 50) {
            return "fine, straight";
        } else if (selection >= 51 && selection <= 250) {
            return "straight";
        } else if (selection >= 251 && selection <= 350) {
            return "coarse, straight";
        } else if (selection >= 351 && selection <= 400) {
            return "thin, wavy";
        } else if (selection >= 401 && selection <= 550) {
            return "wavy";
        } else if (selection >= 551 && selection <= 650) {
            return "thick, wavy";
        } else if (selection >= 651 && selection <= 700) {
            return "loose, curly";
        } else if (selection >= 701 && selection <= 750) {
            return "curly";
        } else if (selection >= 751 && selection <= 800) {
            return "dense, curly";
        } else if (selection >= 851 && selection <= 900) {
            return "tight, coily";
        } else if (selection >= 901 && selection <= 950) {
            return "coily";
        } else {
            return "dense, coily";
        }
    }
}
