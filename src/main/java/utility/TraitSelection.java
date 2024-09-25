package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
            if (selection > TEACHER_MIDDLE_AGE_GRAY_HAIR_THRESHOLD ) {
                hairColor = "graying";
            }
            if (selection >= TEACHER_MIDDLE_AGE_BLACK_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_BLACK_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", black";
                } else {
                    hairColor = "black";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_DARK_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_DARK_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dark brown";
                } else {
                    hairColor = "dark brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_MEDIUM_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_MEDIUM_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", medium brown";
                } else {
                    hairColor = "medium brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_LIGHT_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_LIGHT_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light brown";
                } else {
                    hairColor = "light brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_BLONDE_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_BLONDE_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", blond";
                } else {
                    hairColor = "blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_CHESTNUT_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_CHESTNUT_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", chestnut";
                } else {
                    hairColor = "chestnut";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_MAHOGANY_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_MAHOGANY_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", mahogany";
                } else {
                    hairColor = "mahogany";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_DIRTY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_DIRTY_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dirty blond";
                } else {
                    hairColor = "dirty blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_GOLDEN_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_GOLDEN_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden blond";
                } else {
                    hairColor = "golden blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_LIGHT_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_LIGHT_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light blond";
                } else {
                    hairColor = "light blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_GOLDEN_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_GOLDEN_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden brown";
                } else {
                    hairColor = "golden brown";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_CARAMEL_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_CARAMEL_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", caramel";
                } else {
                    hairColor = "caramel";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_STRAWBERRY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_STRAWBERRY_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", strawberry blond";
                } else {
                    hairColor = "strawberry blond";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_COPPER_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_COPPER_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", copper";
                } else {
                    hairColor = "copper";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_RED_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_RED_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", red";
                } else {
                    hairColor = "red";
                }
            } else if (selection >= TEACHER_MIDDLE_AGE_PLATINUM_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_MIDDLE_AGE_PLATINUM_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
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
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", black";
                } else {
                    hairColor = "black";
                }
            } else if (selection >= TEACHER_OLD_AGE_DARK_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_DARK_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dark brown";
                } else {
                    hairColor = "dark brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_MEDIUM_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_MEDIUM_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", medium brown";
                } else {
                    hairColor = "medium brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_LIGHT_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_LIGHT_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light brown";
                } else {
                    hairColor = "light brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_BLONDE_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_BLONDE_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", blond";
                } else {
                    hairColor = "blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_CHESTNUT_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_CHESTNUT_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", chestnut";
                } else {
                    hairColor = "chestnut";
                }
            } else if (selection >= TEACHER_OLD_AGE_MAHOGANY_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_MAHOGANY_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", mahogany";
                } else {
                    hairColor = "mahogany";
                }
            } else if (selection >= TEACHER_OLD_AGE_DIRTY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_DIRTY_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dirty blond";
                } else {
                    hairColor = "dirty blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_GOLDEN_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_GOLDEN_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden blond";
                } else {
                    hairColor = "golden blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_LIGHT_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_LIGHT_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light blond";
                } else {
                    hairColor = "light blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_GOLDEN_BROWN_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_GOLDEN_BROWN_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden brown";
                } else {
                    hairColor = "golden brown";
                }
            } else if (selection >= TEACHER_OLD_AGE_CARAMEL_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_CARAMEL_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", caramel";
                } else {
                    hairColor = "caramel";
                }
            } else if (selection >= TEACHER_OLD_AGE_STRAWBERRY_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_STRAWBERRY_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", strawberry blond";
                } else {
                    hairColor = "strawberry blond";
                }
            } else if (selection >= TEACHER_OLD_AGE_COPPER_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_COPPER_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", copper";
                } else {
                    hairColor = "copper";
                }
            } else if (selection >= TEACHER_OLD_AGE_RED_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_RED_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", red";
                } else {
                    hairColor = "red";
                }
            } else if (selection >= TEACHER_OLD_AGE_PLATINUM_BLOND_HAIR_LOWER_BOUND && selection <= TEACHER_OLD_AGE_PLATINUM_BLOND_HAIR_UPPER_BOUND) {
                if(!hairColor.isBlank()) {
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
        if(hairColor.equals("no")) {
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

        double random = new Random().nextDouble() * totalWeight;
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

        double random = new Random().nextDouble() * totalWeight;
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
}
