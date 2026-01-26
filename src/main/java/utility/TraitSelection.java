package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

import static constants.SimConstants.*;

public class TraitSelection {

    public static String hairSelection(int selection, int age, String hairLength) {
        if (hairLength.equals("bald")) {
            return "";
        }
        if (age <= TEACHER_YOUNGER_AGE_HAIR_COLOR_THRESHOLD) {
            if (selection >= TEACHER_BLACK_HAIR_LOWER_BOUND && selection <= TEACHER_BLACK_HAIR_UPPER_BOUND) {
                return "black";
            } else if (selection >= TEACHER_DARK_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_DARK_BROWN_HAIR_UPPER_BOUND) {
                return "dark brown";
            } else if (selection >= TEACHER_MEDIUM_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MEDIUM_BROWN_HAIR_UPPER_BOUND) {
                return "medium brown";
            } else if (selection >= TEACHER_LIGHT_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_LIGHT_BROWN_HAIR_UPPER_BOUND) {
                return "light brown";
            } else if (selection >= TEACHER_BLONDE_HAIR_LOWER_BOUND && selection <= TEACHER_BLONDE_HAIR_UPPER_BOUND) {
                return "blond";
            } else if (selection >= TEACHER_CHESTNUT_HAIR_LOWER_BOUND && selection <= TEACHER_CHESTNUT_HAIR_UPPER_BOUND) {
                return "chestnut";
            } else if (selection >= TEACHER_MAHOGANY_HAIR_LOWER_BOUND && selection <= TEACHER_MAHOGANY_HAIR_UPPER_BOUND) {
                return "mahogany";
            } else if (selection >= TEACHER_DIRTY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_DIRTY_BLOND_HAIR_UPPER_BOUND) {
                return "dirty blond";
            } else if (selection >= TEACHER_GOLDEN_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_GOLDEN_BLOND_HAIR_UPPER_BOUND) {
                return "golden blond";
            } else if (selection >= TEACHER_LIGHT_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_LIGHT_BLOND_HAIR_UPPER_BOUND) {
                return "light blond";
            } else if (selection >= TEACHER_GOLDEN_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_GOLDEN_BROWN_HAIR_UPPER_BOUND) {
                return "golden brown";
            } else if (selection >= TEACHER_CARAMEL_HAIR_LOWER_BOUND && selection <= TEACHER_CARAMEL_HAIR_UPPER_BOUND) {
                return "caramel";
            } else if (selection >= TEACHER_STRAWBERRY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_STRAWBERRY_BLOND_HAIR_UPPER_BOUND) {
                return "strawberry blond";
            } else if (selection >= TEACHER_COPPER_HAIR_LOWER_BOUND && selection <= TEACHER_COPPER_HAIR_UPPER_BOUND) {
                return "copper";
            } else if (selection >= TEACHER_RED_HAIR_LOWER_BOUND && selection <= TEACHER_RED_HAIR_UPPER_BOUND) {
                return "red";
            } else if (selection >= TEACHER_PLATINUM_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_PLATINUM_BLOND_HAIR_UPPER_BOUND) {
                return "platinum blond";
            } else {
                int random = Randomizer.setRandom(0, TEACHER_OTHER_HAIR_SAMPLE_SIZE);
                if (random == 0) {
                    return "auburn";
                } else if (random == 1) {
                    return "amber";
                } else if (random == 2) {
                    return "titian";
                } else if (random == 3) {
                    return "white";
                } else if (random == 4) {
                    return "gray";
                } else {
                    return "champagne";
                }
            }
        } else if (age <= TEACHER_MIDDLE_AGE_HAIR_COLOR_THRESHOLD) {
            String hairColor = "";
            if (selection > TEACHER_MIDDLE_AGE_GRAY_HAIR_THRESHOLD) {
                hairColor = "graying";
            }
            if (selection >= TEACHER_MIDDLE_AGE_BLACK_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_BLACK_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", black";
                } else {
                    hairColor = "black";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_DARK_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_DARK_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", dark brown";
                } else {
                    hairColor = "dark brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_MEDIUM_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_MEDIUM_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", medium brown";
                } else {
                    hairColor = "medium brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_LIGHT_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_LIGHT_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", light brown";
                } else {
                    hairColor = "light brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_BLONDE_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_BLONDE_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", blond";
                } else {
                    hairColor = "blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_CHESTNUT_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_CHESTNUT_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", chestnut";
                } else {
                    hairColor = "chestnut";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_MAHOGANY_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_MAHOGANY_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", mahogany";
                } else {
                    hairColor = "mahogany";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_DIRTY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_DIRTY_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", dirty blond";
                } else {
                    hairColor = "dirty blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_GOLDEN_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_GOLDEN_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden blond";
                } else {
                    hairColor = "golden blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_LIGHT_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_LIGHT_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", light blond";
                } else {
                    hairColor = "light blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_GOLDEN_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_GOLDEN_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden brown";
                } else {
                    hairColor = "golden brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_CARAMEL_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_CARAMEL_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", caramel";
                } else {
                    hairColor = "caramel";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_STRAWBERRY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_STRAWBERRY_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", strawberry blond";
                } else {
                    hairColor = "strawberry blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_COPPER_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_COPPER_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", copper";
                } else {
                    hairColor = "copper";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_RED_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_RED_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", red";
                } else {
                    hairColor = "red";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_PLATINUM_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_PLATINUM_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", platinum blond";
                } else {
                    hairColor = "platinum blond";
                }
            } else {
                return "gray";
            }
            return hairColor;
        } else {
            String hairColor = "";
            if (selection > TEACHER_OLD_AGE_GRAY_HAIR_THRESHOLD) {
                hairColor = "graying";
            }
            if (selection >= TEACHER_OLD_AGE_BLACK_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_BLACK_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", black";
                } else {
                    hairColor = "black";
                }
            } else if (selection >= TEACHER_OLD_AGE_DARK_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_DARK_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", dark brown";
                } else {
                    hairColor = "dark brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_MEDIUM_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_MEDIUM_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", medium brown";
                } else {
                    hairColor = "medium brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_LIGHT_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_LIGHT_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", light brown";
                } else {
                    hairColor = "light brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_BLONDE_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_BLONDE_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", blond";
                } else {
                    hairColor = "blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_CHESTNUT_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_CHESTNUT_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", chestnut";
                } else {
                    hairColor = "chestnut";
                }
            } else if (selection >= TEACHER_OLD_AGE_MAHOGANY_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_MAHOGANY_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", mahogany";
                } else {
                    hairColor = "mahogany";
                }
            } else if (selection >= TEACHER_OLD_AGE_DIRTY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_DIRTY_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", dirty blond";
                } else {
                    hairColor = "dirty blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_GOLDEN_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_GOLDEN_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden blond";
                } else {
                    hairColor = "golden blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_LIGHT_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_LIGHT_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", light blond";
                } else {
                    hairColor = "light blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_GOLDEN_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_GOLDEN_BROWN_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden brown";
                } else {
                    hairColor = "golden brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_CARAMEL_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_CARAMEL_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", caramel";
                } else {
                    hairColor = "caramel";
                }
            } else if (selection >= TEACHER_OLD_AGE_STRAWBERRY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_STRAWBERRY_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", strawberry blond";
                } else {
                    hairColor = "strawberry blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_COPPER_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_COPPER_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", copper";
                } else {
                    hairColor = "copper";
                }
            } else if (selection >= TEACHER_OLD_AGE_RED_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_RED_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", red";
                } else {
                    hairColor = "red";
                }
            } else if (selection >= TEACHER_OLD_AGE_PLATINUM_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_PLATINUM_BLOND_HAIR_UPPER_BOUND) {
                if (!hairColor.isBlank()) {
                    hairColor = hairColor + ", platinum blond";
                } else {
                    hairColor = "platinum blond";
                }
            } else {
                return "gray";
            }
            return hairColor;
        }
    }

    public static String eyeSelection(int selection) {
        if (selection >= TEACHER_DARK_BROWN_EYE_LOWER_BOUND && selection <= TEACHER_DARK_BROWN_EYE_UPPER_BOUND) {
            return "dark brown";
        } else if (selection >= TEACHER_LIGHT_BROWN_EYE_LOWER_BOUND && selection <= TEACHER_LIGHT_BROWN_EYE_UPPER_BOUND) {
            return "light brown";
        } else if (selection >= TEACHER_BLUE_EYE_LOWER_BOUND && selection <= TEACHER_BLUE_EYE_UPPER_BOUND) {
            return "blue";
        } else if (selection >= TEACHER_LIGHT_BLUE_EYE_LOWER_BOUND && selection <= TEACHER_LIGHT_BLUE_EYE_UPPER_BOUND) {
            return "light blue";
        } else if (selection >= TEACHER_HAZEL_EYE_LOWER_BOUND && selection <= TEACHER_HAZEL_EYE_UPPER_BOUND) {
            return "hazel";
        } else if (selection >= TEACHER_AMBER_EYE_LOWER_BOUND && selection <= TEACHER_AMBER_EYE_UPPER_BOUND) {
            return "amber";
        } else if (selection >= TEACHER_GREEN_EYE_LOWER_BOUND && selection <= TEACHER_GREEN_EYE_UPPER_BOUND) {
            return "green";
        } else if (selection >= TEACHER_GRAY_EYE_LOWER_BOUND && selection <= TEACHER_GRAY_EYE_UPPER_BOUND) {
            return "gray";
        } else if (selection >= TEACHER_VIOLET_EYE_LOWER_BOUND && selection <= TEACHER_VIOLET_EYE_UPPER_BOUND) {
            return "violet";
        } else if (selection >= TEACHER_BLACK_EYE_LOWER_BOUND && selection <= TEACHER_BLACK_EYE_UPPER_BOUND) {
            return "black";
        } else {
            return "heterochromatic";
        }
    }

    public static String hairType(int selection) {
        if (selection >= TEACHER_FINE_STRAIGHT_HAIR_LOWER_BOUND && selection <= TEACHER_FINE_STRAIGHT_HAIR_UPPER_BOUND) {
            return "fine, straight";
        } else if (selection >= TEACHER_STRAIGHT_HAIR_LOWER_BOUND && selection <= TEACHER_STRAIGHT_HAIR_UPPER_BOUND) {
            return "straight";
        } else if (selection >= TEACHER_COARSE_STRAIGHT_HAIR_LOWER_BOUND && selection <= TEACHER_COARSE_STRAIGHT_HAIR_UPPER_BOUND) {
            return "coarse, straight";
        } else if (selection >= TEACHER_THIN_WAVEY_HAIR_LOWER_BOUND && selection <= TEACHER_THIN_WAVEY_HAIR_UPPER_BOUND) {
            return "thin, wavy";
        } else if (selection >= TEACHER_WAVEY_HAIR_LOWER_BOUND && selection <= TEACHER_WAVEY_HAIR_UPPER_BOUND) {
            return "wavy";
        } else if (selection >= TEACHER_THICK_WAVEY_HAIR_LOWER_BOUND && selection <= TEACHER_THICK_WAVEY_HAIR_UPPER_BOUND) {
            return "thick, wavy";
        } else if (selection >= TEACHER_LOOSE_CURLY_HAIR_LOWER_BOUND && selection <= TEACHER_LOOSE_CURLY_HAIR_UPPER_BOUND) {
            return "loose, curly";
        } else if (selection >= TEACHER_CURLY_HAIR_LOWER_BOUND && selection <= TEACHER_CURLY_HAIR_UPPER_BOUND) {
            return "curly";
        } else if (selection >= TEACHER_DENSE_CURLY_HAIR_LOWER_BOUND && selection <= TEACHER_DENSE_CURLY_HAIR_UPPER_BOUND) {
            return "dense, curly";
        } else if (selection >= TEACHER_TIGHT_COILY_HAIR_LOWER_BOUND && selection <= TEACHER_TIGHT_COILY_HAIR_UPPER_BOUND) {
            return "tight, coily";
        } else if (selection >= TEACHER_COILY_HAIR_LOWER_BOUND && selection <= TEACHER_COILY_HAIR_UPPER_BOUND) {
            return "coily";
        } else {
            return "dense, coily";
        }
    }

    public static String studentHairType(String race, String hairColor) {
        if (hairColor.equals("no")) {
            return "";
        }

        JSONObject choices = loadHairTypeData();
        JSONObject weights = (JSONObject) choices.get(race);
        if (weights == null) {
            throw new IllegalArgumentException("Race not found");
        }

        List<String> hairTypes = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();

        for (Object key : weights.keySet()) {
            String type = (String) key;
            Double probability = ((Number) weights.get(type)).doubleValue();
            hairTypes.add(type);
            probabilities.add(probability);
        }

        return weightedRandomSelection(hairTypes, probabilities);
    }

    private static JSONObject loadHairTypeData() {
        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader("src/main/java/Resources.People/hair_type.json");
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load hair type data", e);
        }
    }

    //TODO: some repeated code that needs cleaning eventually
    public static String studentHairSelection(String race, String eyes) {
        // TODO: possibly separate logic from hair and have separate gen for genetic disorders
        // albinism
        if (eyes.equals("red") || eyes.equals("violet")) {
            return "white";
        }

        JSONObject choices = loadHairColorData();
        JSONObject weights = (JSONObject) choices.get(race);
        if (weights == null) {
            throw new IllegalArgumentException("Race not found");
        }

        List<String> hairColors = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();

        for (Object key : weights.keySet()) {
            String color = (String) key;
            Double probability = ((Number) weights.get(color)).doubleValue();
            hairColors.add(color);
            probabilities.add(probability);
        }

        return weightedRandomSelection(hairColors, probabilities);
    }

    private static JSONObject loadHairColorData() {
        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader("src/main/java/Resources.People/hair_color.json");
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load hair color data", e);
        }
    }

    private static String weightedRandomSelection(List<String> items, List<Double> weights) {
        double totalWeight = 0.0;
        for (Double weight : weights) {
            totalWeight += weight;
        }

        double random = GameRandom.nextDouble() * totalWeight;
        for (int i = 0; i < items.size(); i++) {
            random -= weights.get(i);
            if (random <= 0.0) {
                return items.get(i);
            }
        }

        return null;
    }

    public static String studentEyeColorSelection(String race) {
        JSONObject choices = loadEyeColorData();

        // Get the main eye color weights for the specified race
        JSONObject raceData = (JSONObject) choices.get(race);
        if (raceData == null) {
            throw new IllegalArgumentException("Race " + race + " not found in the dataset");
        }

        // Lists to hold main eye color categories and their weights
        List<String> mainColors = new ArrayList<>();
        List<Double> mainWeights = new ArrayList<>();

        // Populate lists from JSON data
        for (Object key : raceData.keySet()) {
            String color = (String) key;
            JSONObject colorDetails = (JSONObject) raceData.get(color);
            Double totalWeight = ((Number) colorDetails.get("Total")).doubleValue();
            mainColors.add(color);
            mainWeights.add(totalWeight);
        }

        // Weighted random selection of main color
        String selectedMainColor = weightedRandomSelectionEyes(mainColors, mainWeights);

        // Now select a specific shade within the chosen main color category
        JSONObject selectedColorDetails = (JSONObject) raceData.get(selectedMainColor);
        List<String> shades = new ArrayList<>();
        List<Double> shadeWeights = new ArrayList<>();

        for (Object key : selectedColorDetails.keySet()) {
            if (!"Total".equals(key)) {
                String shade = (String) key;
                Double weight = ((Number) selectedColorDetails.get(shade)).doubleValue();
                shades.add(shade);
                shadeWeights.add(weight);
            }
        }

        return weightedRandomSelection(shades, shadeWeights);
    }

    private static JSONObject loadEyeColorData() {
        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader("src/main/java/Resources.People/eye_color.json");
            return (JSONObject) parser.parse(reader);
        } catch (IOException | org.json.simple.parser.ParseException e) {
            throw new RuntimeException("Failed to load eye color data", e);
        }
    }

    private static String weightedRandomSelectionEyes(List<String> items, List<Double> weights) {
        double totalWeight = 0.0;
        for (Double weight : weights) {
            totalWeight += weight;
        }

        double random = GameRandom.nextDouble() * totalWeight;
        for (int i = 0; i < items.size(); i++) {
            random -= weights.get(i);
            if (random <= 0.0) {
                return items.get(i);
            }
        }

        return null;
    }

    public static String studentSkinColorSelection(String race, String eyes) {
        if (eyes.equals("red") || eyes.equals("violet")) {
            return "pale white";
        }

        JSONObject choices = loadSkinColorData();
        JSONObject weights = (JSONObject) choices.get(race);
        if (weights == null) {
            throw new IllegalArgumentException("Race not found");
        }

        List<String> skinColors = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();

        for (Object key : weights.keySet()) {
            String color = (String) key;
            Double probability = ((Number) weights.get(color)).doubleValue();
            skinColors.add(color);
            probabilities.add(probability);
        }

        return weightedRandomSelection(skinColors, probabilities);

    }

    private static JSONObject loadSkinColorData() {
        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader("src/main/java/Resources.People/skin_distribution.json");
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load skin color data", e);
        }
    }

    private static final String[] BRACES_BAND_COLORS = {
            "blue", "red", "green", "purple", "pink", "orange", "yellow", "teal",
            "black", "silver", "gold", "light blue", "dark blue", "turquoise",
            "lime green", "hot pink", "navy", "maroon", "white", "gray"
    };

    private static final String[] BRACES_BRACKET_TYPES = {
            "clear", "metal"
    };

    private static final String[] BRACES_ELASTIC_COLORS = {
            "clear", "blue", "red", "green", "purple", "pink", "orange", "teal",
            "light blue", "dark blue", "lime green", "hot pink", "white", "gray"
    };

    private static final String[] BRACES_ELASTIC_TYPES = {
            "elastic bands", "rubber bands", "ligature ties", "power chains"
    };

    /**
     * Selects a random band color for braces.
     *
     * @return a randomly selected band color
     */
    public static String selectBracesBandColor() {
        int index = (int) (GameRandom.nextDouble() * BRACES_BAND_COLORS.length);
        return BRACES_BAND_COLORS[index];
    }

    /**
     * Selects a random bracket type for braces.
     *
     * @return either "clear" or "metal"
     */
    public static String selectBracesBracketType() {
        int index = (int) (GameRandom.nextDouble() * BRACES_BRACKET_TYPES.length);
        return BRACES_BRACKET_TYPES[index];
    }

    /**
     * Determines if a student has braces based on race, income level, and grade level.
     * 
     * Base rates from research data:
     * - White: 31% receive orthodontic treatment
     * - Hispanic/Mexican American: 11% receive orthodontic treatment
     * - Black/African American: 8% receive orthodontic treatment
     * 
     * Income significantly affects access:
     * - Suburban/affluent areas: 50%+ utilization
     * - Inner city/low income: less than 10% utilization
     * 
     * Grade level adjustment (braces are less common later in high school
     * as many students have had them removed by junior/senior year):
     * - Freshman: highest probability
     * - Sophomore: slightly lower
     * - Junior: lower (many getting removed)
     * - Senior: lowest (most have completed treatment)
     *
     * @param race the student's race category
     * @param incomeLevel the family income level (low, middle, high)
     * @param gradeLevel the student's grade level (Freshman, Sophomore, Junior, Senior)
     * @return true if the student has braces, false otherwise
     */
    public static boolean determineBraces(String race, String incomeLevel, String gradeLevel) {
        // Base rates by race (from orthodontic research data)
        double baseRate = switch (race) {
            case "white" -> 0.31;       // 31% of White teenagers
            case "hispanic" -> 0.11;    // 11% of Mexican American teenagers
            case "black" -> 0.08;       // 8% of Black teenagers
            case "api" -> 0.25;         // Estimated based on income demographics
            case "aian" -> 0.10;        // Estimated similar to other minorities
            case "2prace" -> 0.18;      // Weighted average of groups
            default -> 0.15;            // Default fallback
        };

        // Income multiplier (reflects suburban affluent vs inner city disparity)
        // High income areas have 50%+ utilization, low income less than 10%
        double incomeMultiplier = switch (incomeLevel) {
            case "high" -> 1.6;     // Affluent areas have ~50%+ for white students
            case "middle" -> 1.0;   // Base rates apply
            case "low" -> 0.3;      // Low income areas have <10% utilization
            default -> 1.0;
        };

        // Grade level multiplier (braces less common in later high school years)
        // Many students get braces in middle school/early high school and
        // have them removed by junior/senior year
        double gradeMultiplier = switch (gradeLevel) {
            case "Freshman" -> 1.2;     // Just got braces or in active treatment
            case "Sophomore" -> 1.1;    // Still commonly in treatment
            case "Junior" -> 0.85;      // Many getting braces removed
            case "Senior" -> 0.65;      // Most have completed treatment
            default -> 1.0;
        };

        // Calculate final probability
        double probability = baseRate * incomeMultiplier * gradeMultiplier;

        // Cap probability at reasonable bounds (5% to 60%)
        probability = Math.max(0.05, Math.min(0.60, probability));

        // Random determination
        return GameRandom.nextDouble() < probability;
    }

    /**
     * Selects a random elastic color for braces.
     *
     * @return a randomly selected elastic color
     */
    public static String selectBracesElasticColor() {
        int index = (int) (GameRandom.nextDouble() * BRACES_ELASTIC_COLORS.length);
        return BRACES_ELASTIC_COLORS[index];
    }

    /**
     * Selects a random elastic type for braces.
     *
     * @return a randomly selected elastic type description
     */
    public static String selectBracesElasticType() {
        int index = (int) (GameRandom.nextDouble() * BRACES_ELASTIC_TYPES.length);
        return BRACES_ELASTIC_TYPES[index];
    }

    /**
     * Determines if a student with braces has orthodontic elastics.
     * Approximately 60% of orthodontic patients have elastics at some point.
     *
     * @return true if the student has elastics, false otherwise
     */
    public static boolean determineHasElastics() {
        return Randomizer.setRandom(0, BRACES_ELASTIC_SAMPLE_SIZE) < BRACES_ELASTIC_PROBABILITY;
    }

    /**
     * Generates braces timing for a student who currently has braces.
     * The game starts in August 2004, so we calculate when braces were put on
     * (in the past) and when they will be removed (in the future).
     *
     * Students in later grades have a higher chance of having less time left,
     * since they are older and presumably had braces put on further in the past.
     *
     * @param gradeLevel the student's grade level
     * @param gameStartDate the date the game starts (typically August 2004)
     * @return an array of two LocalDates: [startDate, endDate]
     */
    public static LocalDate[] generateBracesTiming(String gradeLevel, LocalDate gameStartDate) {
        // Generate total treatment duration (12-36 months, normally distributed around 24)
        int totalDurationMonths = (int) GameRandom.nextGaussian(
                BRACES_MEAN_DURATION_MONTHS,
                BRACES_DURATION_STANDARD_DEVIATION);
        totalDurationMonths = Math.max(BRACES_MIN_DURATION_MONTHS,
                Math.min(BRACES_MAX_DURATION_MONTHS, totalDurationMonths));

        // Determine how far into treatment the student is based on grade level
        // Older students have been wearing braces longer on average
        double progressRatio;
        switch (gradeLevel) {
            case "Senior" -> progressRatio = 0.5 + (GameRandom.nextDouble() * 0.45);   // 50-95% done
            case "Junior" -> progressRatio = 0.35 + (GameRandom.nextDouble() * 0.50);  // 35-85% done
            case "Sophomore" -> progressRatio = 0.20 + (GameRandom.nextDouble() * 0.55); // 20-75% done
            case "Freshman" -> progressRatio = 0.05 + (GameRandom.nextDouble() * 0.60); // 5-65% done
            default -> progressRatio = 0.25 + (GameRandom.nextDouble() * 0.50);        // 25-75% done
        }

        int monthsElapsed = (int) (totalDurationMonths * progressRatio);
        int monthsRemaining = totalDurationMonths - monthsElapsed;

        // Calculate start date (in the past) and end date (in the future)
        LocalDate startDate = gameStartDate.minusMonths(monthsElapsed);
        LocalDate endDate = gameStartDate.plusMonths(monthsRemaining);

        return new LocalDate[]{startDate, endDate};
    }

    /**
     * Determines if a student has had braces removed in the past (before game start).
     * This accounts for students who completed orthodontic treatment before high school
     * or earlier in their high school career.
     *
     * The calculation ensures the total number of people who have ever had braces
     * (current + past) matches the 2004 demographic research data.
     *
     * @param race the student's race category
     * @param incomeLevel the family income level (low, middle, high)
     * @param gradeLevel the student's grade level (Freshman, Sophomore, Junior, Senior)
     * @param currentlyHasBraces whether the student currently has braces
     * @return true if the student had braces removed in the past, false otherwise
     */
    public static boolean determinePastBraces(String race, String incomeLevel,
                                               String gradeLevel, boolean currentlyHasBraces) {
        // If they currently have braces, they can't have had them removed already
        if (currentlyHasBraces) {
            return false;
        }

        // Calculate total orthodontic treatment rate for this demographic
        double totalRate = switch (race) {
            case "white" -> BRACES_TOTAL_RATE_WHITE;
            case "hispanic" -> BRACES_TOTAL_RATE_HISPANIC;
            case "black" -> BRACES_TOTAL_RATE_BLACK;
            case "api" -> BRACES_TOTAL_RATE_API;
            case "aian" -> BRACES_TOTAL_RATE_AIAN;
            case "2prace" -> BRACES_TOTAL_RATE_2PRACE;
            default -> BRACES_TOTAL_RATE_DEFAULT;
        };

        // Apply income multiplier
        double incomeMultiplier = switch (incomeLevel) {
            case "high" -> BRACES_INCOME_MULTIPLIER_HIGH;
            case "middle" -> BRACES_INCOME_MULTIPLIER_MIDDLE;
            case "low" -> BRACES_INCOME_MULTIPLIER_LOW;
            default -> BRACES_INCOME_MULTIPLIER_MIDDLE;
        };

        totalRate *= incomeMultiplier;

        // Cap at reasonable bounds
        totalRate = Math.max(0.05, Math.min(0.60, totalRate));

        // Determine what fraction of total eligible students have already completed treatment
        // Older grades are more likely to have already had braces removed
        double pastRateMultiplier = switch (gradeLevel) {
            case "Freshman" -> BRACES_PAST_RATE_FRESHMAN;
            case "Sophomore" -> BRACES_PAST_RATE_SOPHOMORE;
            case "Junior" -> BRACES_PAST_RATE_JUNIOR;
            case "Senior" -> BRACES_PAST_RATE_SENIOR;
            default -> BRACES_PAST_RATE_SOPHOMORE;
        };

        // The probability of having had braces in the past is:
        // (total treatment rate) * (fraction already completed) for this grade
        double pastBracesProbability = totalRate * pastRateMultiplier;

        return GameRandom.nextDouble() < pastBracesProbability;
    }

    /**
     * Generates past braces timing for a student who had braces removed before the game starts.
     * This creates realistic start and end dates for when braces were worn.
     *
     * @param birthday the student's birthday
     * @param gradeLevel the student's grade level
     * @param gameStartDate the date the game starts
     * @return an array of two LocalDates: [startDate, endDate] when braces were worn
     */
    public static LocalDate[] generatePastBracesTiming(LocalDate birthday, String gradeLevel,
                                                        LocalDate gameStartDate) {
        // Most orthodontic treatment happens between ages 10-16
        // Calculate how old the student was when they got braces
        int ageWhenStarted = 10 + Randomizer.setRandom(0, 4); // Ages 10-14 typically

        // Calculate the date when braces were put on
        LocalDate bracesStartDate = birthday.plusYears(ageWhenStarted);

        // Make sure the start date is before the game start
        if (bracesStartDate.isAfter(gameStartDate.minusMonths(BRACES_MIN_DURATION_MONTHS))) {
            bracesStartDate = gameStartDate.minusMonths(BRACES_MIN_DURATION_MONTHS + 12);
        }

        // Generate treatment duration
        int durationMonths = (int) GameRandom.nextGaussian(
                BRACES_MEAN_DURATION_MONTHS,
                BRACES_DURATION_STANDARD_DEVIATION);
        durationMonths = Math.max(BRACES_MIN_DURATION_MONTHS,
                Math.min(BRACES_MAX_DURATION_MONTHS, durationMonths));

        LocalDate bracesEndDate = bracesStartDate.plusMonths(durationMonths);

        // Ensure end date is before game start (they should have been removed already)
        if (bracesEndDate.isAfter(gameStartDate)) {
            // Shift both dates back so end date is before game start
            long monthsToShift = java.time.temporal.ChronoUnit.MONTHS.between(gameStartDate, bracesEndDate) + 1;
            bracesStartDate = bracesStartDate.minusMonths(monthsToShift);
            bracesEndDate = bracesEndDate.minusMonths(monthsToShift);
        }

        return new LocalDate[]{bracesStartDate, bracesEndDate};
    }
}
