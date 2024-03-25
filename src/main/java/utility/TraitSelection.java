package utility;

import java.util.concurrent.ThreadLocalRandom;

public class TraitSelection {

    private static Integer setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String hairSelection(int selection) {
        if (selection >= 0 && selection <= 21) {
            return "Black";
        } else if (selection >= 22 && selection <= 37) {
            return "Dark Brown";
        } else if (selection >= 38 && selection <= 48) {
            return "Medium Brown";
        } else if (selection >= 49 && selection <= 56) {
            return "Light Brown";
        } else if (selection >= 57 && selection <= 64) {
            return "Blond";
        } else if (selection >= 65 && selection <= 71) {
            return "Chestnut";
        } else if (selection >= 72 && selection <= 78) {
            return "Mahogany";
        } else if (selection >= 79 && selection <= 84) {
            return "Dirty Blond";
        } else if (selection >= 85 && selection <= 89) {
            return "Golden Blond";
        } else if (selection >= 90 && selection <= 93) {
            return "Light Blond";
        } else if (selection >= 94 && selection < 96) {
            return "Golden Brown";
        } else if (selection == 97) {
            return "Caramel";
        } else if (selection == 98) {
            return "Strawberry Blond";
        } else if (selection == 99) {
            return "Copper";
        } else if (selection == 100) {
            return "Red";
        } else if (selection == 101) {
            return "Platinum Blond";
        } else {
            int random = setRandom(0, 6);
            if (random == 0) {
                return "Auburn";
            } else if (random == 1) {
                return "Amber";
            } else if (random == 2) {
                return "Titian";
            } else if (random == 3) {
                return "White";
            } else if (random == 4) {
                return "No";
            } else if (random == 5) {
                return "Gray";
            } else {
                return "Champagne";
            }
        }
    }

    public static String eyeSelection(int selection) {
        if (selection >= 0 && selection <= 52) {
            return "Dark Brown";
        } else if (selection >= 53 && selection <= 75) {
            return "Light Brown";
        } else if (selection >= 76 && selection <= 83) {
            return "Blue";
        } else if (selection >= 84 && selection <= 90) {
            return "Light Blue";
        } else if (selection >= 91 && selection <= 96) {
            return "Hazel";
        } else if (selection >= 97 && selection <= 102) {
            return "Amber";
        } else if (selection >= 103 && selection <= 105) {
            return "Green";
        } else if (selection == 106) {
            return "Gray";
        } else if (selection == 107) {
            return "Violet";
        } else if (selection == 108) {
            return "Black";
        } else {
            return "Heterochromia";
        }
    }

}
