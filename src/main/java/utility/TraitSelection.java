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
     * Determines if a student has alternating band colors on their braces.
     * This is relatively uncommon.
     *
     * @return true if the student has alternating band colors
     */
    public static boolean determineAlternatingBandColors() {
        return Randomizer.setRandom(0, BRACES_ALTERNATING_BAND_SAMPLE_SIZE) < BRACES_ALTERNATING_BAND_PROBABILITY;
    }

    /**
     * Selects band colors for braces, with option to use school colors.
     * When alternating bands are chosen, there's a higher chance of using school colors.
     *
     * @param useSchoolColors whether to use school colors
     * @param schoolColors the school's colors array (may be null)
     * @param firstColor the first color already selected (to avoid duplicates)
     * @return a randomly selected band color
     */
    public static String selectBracesBandColorWithSchoolOption(boolean useSchoolColors, 
                                                                String[] schoolColors, 
                                                                String firstColor) {
        if (useSchoolColors && schoolColors != null && schoolColors.length >= 2) {
            // Determine if we should use school colors (60% chance when alternating)
            if (Randomizer.setRandom(0, BRACES_SCHOOL_COLOR_SAMPLE_SIZE) < BRACES_SCHOOL_COLOR_PROBABILITY) {
                // Return the school color that isn't already used as the first color
                if (firstColor != null && firstColor.equalsIgnoreCase(schoolColors[0])) {
                    return schoolColors[1].toLowerCase();
                } else if (firstColor != null && firstColor.equalsIgnoreCase(schoolColors[1])) {
                    return schoolColors[0].toLowerCase();
                } else {
                    // First color wasn't a school color, pick one randomly
                    return schoolColors[Randomizer.setRandom(0, 1)].toLowerCase();
                }
            }
        }

        // Otherwise, pick a random color that's different from the first color
        String secondColor;
        do {
            secondColor = selectBracesBandColor();
        } while (secondColor.equals(firstColor));

        return secondColor;
    }

    /**
     * Selects the first band color, potentially using school colors if alternating.
     *
     * @param hasAlternating whether the student has alternating band colors
     * @param schoolColors the school's colors array (may be null)
     * @return the first band color
     */
    public static String selectFirstBandColor(boolean hasAlternating, String[] schoolColors) {
        if (hasAlternating && schoolColors != null && schoolColors.length >= 2) {
            // When alternating, higher chance to use school colors
            if (Randomizer.setRandom(0, BRACES_SCHOOL_COLOR_SAMPLE_SIZE) < BRACES_SCHOOL_COLOR_PROBABILITY) {
                return schoolColors[0].toLowerCase();
            }
        }
        return selectBracesBandColor();
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
     * Certain orthodontic modifiers (like ligature ties or power chains) indicate
     * more complex treatment, which tends to extend duration towards the upper range.
     *
     * @param gradeLevel the student's grade level
     * @param gameStartDate the date the game starts (typically August 2004)
     * @param hasElastics whether the student has orthodontic elastics
     * @param elasticType the type of elastics (may be null if no elastics)
     * @return an array of two LocalDates: [startDate, endDate]
     */
    public static LocalDate[] generateBracesTiming(String gradeLevel, LocalDate gameStartDate,
                                                    boolean hasElastics, String elasticType) {
        // Base duration parameters
        int baseMean = BRACES_MEAN_DURATION_MONTHS;
        int baseMin = BRACES_MIN_DURATION_MONTHS;
        int baseMax = BRACES_MAX_DURATION_MONTHS;

        // Adjust duration based on elastic type - certain modifiers indicate longer treatment
        if (hasElastics && elasticType != null) {
            switch (elasticType) {
                case "ligature ties" -> {
                    // Ligature ties often used for complex alignment, longer treatment
                    baseMean += 4;  // Shift mean towards upper range
                    baseMin += 3;   // Higher minimum duration
                }
                case "power chains" -> {
                    // Power chains used to close gaps, indicates moderate complexity
                    baseMean += 2;  // Slight increase in mean duration
                    baseMin += 2;
                }
                case "rubber bands" -> {
                    // Rubber bands for bite correction, moderate complexity
                    baseMean += 1;
                }
                // "elastic bands" - standard, no adjustment needed
            }
        }

        // Generate total treatment duration (normally distributed around adjusted mean)
        int totalDurationMonths = (int) GameRandom.nextGaussian(baseMean, BRACES_DURATION_STANDARD_DEVIATION);
        totalDurationMonths = Math.max(baseMin, Math.min(baseMax, totalDurationMonths));

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

    /**
     * Determines if a student has myopia (nearsightedness) based on race and gender.
     * 
     * Based on 1999-2004 NHANES vision examination data:
     * - Overall age-standardized prevalence: 33.1%
     * - Females (20-39): 40%
     * - Males (20-39): 33%
     * - Non-Hispanic whites: 35.2%
     * - Non-Hispanic blacks: 28.6%
     * - Mexican Americans: 25.1%
     * 
     * A youth multiplier is applied since myopia often develops/worsens during teen years.
     *
     * @param race the student's race category
     * @param gender the student's gender
     * @return true if the student has myopia, false otherwise
     */
    public static boolean determineMyopia(String race, String gender) {
        // Base rate by race (from NHANES data)
        double baseRate = switch (race) {
            case "white" -> VISION_MYOPIA_WHITE_RATE;
            case "black" -> VISION_MYOPIA_BLACK_RATE;
            case "hispanic" -> VISION_MYOPIA_HISPANIC_RATE;
            case "api" -> VISION_MYOPIA_API_RATE;
            case "aian" -> VISION_MYOPIA_AIAN_RATE;
            case "2prace" -> VISION_MYOPIA_2PRACE_RATE;
            default -> VISION_MYOPIA_BASE_RATE;
        };

        // Apply gender adjustment (20-39 age group data)
        // Females have higher myopia rate than males
        if (gender.equalsIgnoreCase("Female")) {
            // Adjust towards female rate
            baseRate = baseRate * (VISION_MYOPIA_FEMALE_RATE / VISION_MYOPIA_BASE_RATE);
        } else {
            // Adjust towards male rate
            baseRate = baseRate * (VISION_MYOPIA_MALE_RATE / VISION_MYOPIA_BASE_RATE);
        }

        // Apply youth multiplier (myopia tends to develop in teen years)
        baseRate *= VISION_YOUTH_MYOPIA_MULTIPLIER;

        // Cap at reasonable bounds
        baseRate = Math.min(0.50, baseRate);

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines if a student has hyperopia (farsightedness).
     * 
     * Based on 1999-2004 NHANES vision examination data:
     * - Age-standardized prevalence: 3.6%
     * - Less common in younger persons (aged <60 years)
     * 
     * Note: Hyperopia and myopia are generally mutually exclusive.
     * This method should only be called if the student does NOT have myopia.
     *
     * @return true if the student has hyperopia, false otherwise
     */
    public static boolean determineHyperopia() {
        // Hyperopia is less common in young people
        // Base rate from NHANES is 3.6% for adults, but lower for teens
        double baseRate = VISION_HYPEROPIA_BASE_RATE * 0.7;  // Reduce for youth

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines if a student has astigmatism.
     * 
     * Based on 1999-2004 NHANES vision examination data:
     * - Age-standardized prevalence: 36.2%
     * - Persons aged ≥60 years were more likely to have astigmatism
     * 
     * Astigmatism can occur alongside myopia or hyperopia.
     *
     * @return true if the student has astigmatism, false otherwise
     */
    public static boolean determineAstigmatism() {
        // Astigmatism is slightly less common in younger people
        double baseRate = VISION_ASTIGMATISM_BASE_RATE * 0.85;  // Reduce for youth

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines all vision issues for a student.
     * Returns a boolean array: [hasMyopia, hasHyperopia, hasAstigmatism]
     * 
     * Note: Myopia and hyperopia are mutually exclusive (can't have both).
     * Astigmatism can occur with either myopia or hyperopia.
     *
     * @param race the student's race category
     * @param gender the student's gender
     * @return boolean array with [hasMyopia, hasHyperopia, hasAstigmatism]
     */
    public static boolean[] determineVisionIssues(String race, String gender) {
        boolean hasMyopia = determineMyopia(race, gender);
        boolean hasHyperopia = false;
        boolean hasAstigmatism = determineAstigmatism();

        // Only check for hyperopia if they don't have myopia
        // (myopia and hyperopia are mutually exclusive)
        if (!hasMyopia) {
            hasHyperopia = determineHyperopia();
        }

        return new boolean[]{hasMyopia, hasHyperopia, hasAstigmatism};
    }

    /**
     * Determines if a student with vision issues has corrective lenses (glasses or contacts).
     * 
     * Based on 1988 Medical Expenditure Panel Survey:
     * - 25.4% of children 6-18 had corrective lenses
     * - Girls had greater odds than boys (OR 1.41)
     * - Income/insurance significantly affects access
     * - For higher income families, odds increase with age
     * 
     * This method assumes the student already has a vision issue.
     *
     * @param race the student's race category
     * @param gender the student's gender
     * @param incomeLevel the family income level (low, middle, high)
     * @param gradeLevel the student's grade level
     * @return true if the student has corrective lenses, false otherwise
     */
    public static boolean determineCorrectionLenses(String race, String gender, 
                                                     String incomeLevel, String gradeLevel) {
        // Base rate - majority of people with vision issues have glasses
        double baseRate = CORRECTIVE_LENS_BASE_RATE;

        // Apply gender multiplier (girls 1.41x more likely)
        if (gender.equalsIgnoreCase("Female")) {
            baseRate *= CORRECTIVE_LENS_FEMALE_MULTIPLIER;
        } else {
            baseRate *= CORRECTIVE_LENS_MALE_MULTIPLIER;
        }

        // Apply income multiplier
        double incomeMultiplier = switch (incomeLevel) {
            case "high" -> CORRECTIVE_LENS_HIGH_INCOME_MULTIPLIER;
            case "middle" -> CORRECTIVE_LENS_MIDDLE_INCOME_MULTIPLIER;
            case "low" -> CORRECTIVE_LENS_LOW_INCOME_MULTIPLIER;
            default -> CORRECTIVE_LENS_MIDDLE_INCOME_MULTIPLIER;
        };
        baseRate *= incomeMultiplier;

        // Apply race multiplier
        double raceMultiplier = switch (race) {
            case "white" -> CORRECTIVE_LENS_WHITE_MULTIPLIER;
            case "black" -> CORRECTIVE_LENS_BLACK_MULTIPLIER;
            case "hispanic" -> CORRECTIVE_LENS_HISPANIC_MULTIPLIER;
            case "api" -> CORRECTIVE_LENS_API_MULTIPLIER;
            case "aian" -> CORRECTIVE_LENS_AIAN_MULTIPLIER;
            case "2prace" -> CORRECTIVE_LENS_2PRACE_MULTIPLIER;
            default -> 1.0;
        };
        baseRate *= raceMultiplier;

        // Apply age/grade effect (only significant for higher income families)
        if (!incomeLevel.equals("low")) {
            double gradeMultiplier = switch (gradeLevel) {
                case "Senior" -> CORRECTIVE_LENS_SENIOR_MULTIPLIER;
                case "Junior" -> CORRECTIVE_LENS_JUNIOR_MULTIPLIER;
                case "Sophomore" -> CORRECTIVE_LENS_SOPHOMORE_MULTIPLIER;
                case "Freshman" -> CORRECTIVE_LENS_FRESHMAN_MULTIPLIER;
                default -> 1.0;
            };
            baseRate *= gradeMultiplier;
        }

        // Cap at reasonable bounds
        baseRate = Math.max(0.20, Math.min(0.95, baseRate));

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines if a student with glasses also has contact lenses.
     * Contact lens usage varies significantly by income level.
     * 
     * Higher income students have much greater access to contacts.
     *
     * @param incomeLevel the family income level (low, middle, high)
     * @return true if the student has contact lenses, false otherwise
     */
    public static boolean determineContactLenses(String incomeLevel) {
        double contactRate = switch (incomeLevel) {
            case "high" -> CONTACTS_HIGH_INCOME_RATE;
            case "middle" -> CONTACTS_MIDDLE_INCOME_RATE;
            case "low" -> CONTACTS_LOW_INCOME_RATE;
            default -> CONTACTS_MIDDLE_INCOME_RATE;
        };

        return GameRandom.nextDouble() < contactRate;
    }

    /**
     * Determines corrective lens status for a student with vision issues.
     * Returns a boolean array: [hasGlasses, hasContacts]
     * 
     * Note: Students with contacts are assumed to also have glasses as backup.
     *
     * @param race the student's race category
     * @param gender the student's gender
     * @param incomeLevel the family income level (low, middle, high)
     * @param gradeLevel the student's grade level
     * @return boolean array with [hasGlasses, hasContacts]
     */
    public static boolean[] determineCorrectiveLenses(String race, String gender,
                                                       String incomeLevel, String gradeLevel) {
        boolean hasGlasses = determineCorrectionLenses(race, gender, incomeLevel, gradeLevel);
        boolean hasContacts = false;

        // Only check for contacts if they have glasses
        // (contacts require existing vision care relationship)
        if (hasGlasses) {
            hasContacts = determineContactLenses(incomeLevel);
        }

        return new boolean[]{hasGlasses, hasContacts};
    }

    // ==================== ADULT/TEACHER VISION METHODS ====================

    /**
     * Determines if an adult has myopia (nearsightedness) based on age and gender.
     * Myopia rates are relatively stable in adults but can decrease slightly with age
     * as presbyopia (age-related farsightedness) becomes more dominant.
     *
     * @param age the adult's age
     * @param gender the adult's gender
     * @return true if they have myopia, false otherwise
     */
    public static boolean determineAdultMyopia(int age, String gender) {
        // Base rate similar to general population
        double baseRate = VISION_MYOPIA_BASE_RATE;

        // Apply gender adjustment
        if (gender.equalsIgnoreCase("Female")) {
            baseRate *= (VISION_MYOPIA_FEMALE_RATE / VISION_MYOPIA_BASE_RATE);
        } else {
            baseRate *= (VISION_MYOPIA_MALE_RATE / VISION_MYOPIA_BASE_RATE);
        }

        // Myopia prevalence decreases slightly with age as hyperopia becomes more common
        if (age >= 60) {
            baseRate *= 0.7;  // Reduced in older adults
        } else if (age >= 40) {
            baseRate *= 0.9;  // Slightly reduced in middle age
        }

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines if an adult has hyperopia (farsightedness) based on age.
     * Hyperopia/presbyopia increases significantly with age, especially after 40.
     *
     * @param age the adult's age
     * @param hasMyopia whether they already have myopia (mutually exclusive)
     * @return true if they have hyperopia, false otherwise
     */
    public static boolean determineAdultHyperopia(int age, boolean hasMyopia) {
        // Hyperopia and myopia are mutually exclusive
        if (hasMyopia) {
            return false;
        }

        // Hyperopia rate increases significantly with age (presbyopia)
        double baseRate;
        if (age >= 60) {
            baseRate = ADULT_HYPEROPIA_60_PLUS_RATE;
        } else if (age >= 40) {
            baseRate = ADULT_HYPEROPIA_40_TO_59_RATE;
        } else {
            baseRate = ADULT_HYPEROPIA_UNDER_40_RATE;
        }

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines if an adult has astigmatism based on age.
     * Astigmatism prevalence increases with age.
     *
     * @param age the adult's age
     * @return true if they have astigmatism, false otherwise
     */
    public static boolean determineAdultAstigmatism(int age) {
        double baseRate = VISION_ASTIGMATISM_BASE_RATE;

        // Apply age multiplier
        if (age >= 60) {
            baseRate *= ADULT_ASTIGMATISM_60_PLUS_MULTIPLIER;
        } else if (age >= 40) {
            baseRate *= ADULT_ASTIGMATISM_40_TO_59_MULTIPLIER;
        } else {
            baseRate *= ADULT_ASTIGMATISM_UNDER_40_MULTIPLIER;
        }

        // Cap at reasonable bounds
        baseRate = Math.min(0.55, baseRate);

        return GameRandom.nextDouble() < baseRate;
    }

    /**
     * Determines all vision issues for an adult/teacher.
     * Returns a boolean array: [hasMyopia, hasHyperopia, hasAstigmatism]
     *
     * @param age the adult's age
     * @param gender the adult's gender
     * @return boolean array with vision issues
     */
    public static boolean[] determineAdultVisionIssues(int age, String gender) {
        boolean hasMyopia = determineAdultMyopia(age, gender);
        boolean hasHyperopia = determineAdultHyperopia(age, hasMyopia);
        boolean hasAstigmatism = determineAdultAstigmatism(age);

        return new boolean[]{hasMyopia, hasHyperopia, hasAstigmatism};
    }

    /**
     * Determines if an adult with vision issues has corrective lenses.
     * Adults are much more likely to have corrective lenses than children,
     * especially working professionals who need functional vision.
     *
     * @return true if they have corrective lenses, false otherwise
     */
    public static boolean determineAdultCorrectiveLenses() {
        // Adults almost always have corrective lenses if needed
        return GameRandom.nextDouble() < ADULT_CORRECTIVE_LENS_RATE;
    }

    /**
     * Determines if an adult with glasses also has contact lenses.
     * Contact lens usage decreases with age due to dry eye and other issues.
     *
     * @param age the adult's age
     * @return true if they have contacts, false otherwise
     */
    public static boolean determineAdultContactLenses(int age) {
        double contactRate;
        if (age >= 60) {
            contactRate = ADULT_CONTACTS_60_PLUS_RATE;
        } else if (age >= 40) {
            contactRate = ADULT_CONTACTS_40_TO_59_RATE;
        } else {
            contactRate = ADULT_CONTACTS_UNDER_40_RATE;
        }

        return GameRandom.nextDouble() < contactRate;
    }

    /**
     * Determines corrective lens status for an adult with vision issues.
     * Returns a boolean array: [hasGlasses, hasContacts]
     *
     * @param age the adult's age
     * @return boolean array with [hasGlasses, hasContacts]
     */
    public static boolean[] determineAdultCorrectiveLensesComplete(int age) {
        boolean hasGlasses = determineAdultCorrectiveLenses();
        boolean hasContacts = false;

        if (hasGlasses) {
            hasContacts = determineAdultContactLenses(age);
        }

        return new boolean[]{hasGlasses, hasContacts};
    }
}
