package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TraitSelection {

    // TODO: Make these hair descriptions more accurate to teachers/older people
    public static String hairSelection(int selection, int age, String hairLength) {
        if (hairLength.equals("bald")) {
            return "";
        }
        if (age <= 37) {
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
                int random = Randomizer.setRandom(0, 5);
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
        } else if (age <= 47) {
            String hairColor = "";
            if (selection > 90 ) {
                hairColor = "graying";
            }
            if (selection >= 0 && selection <= 21) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", black";
                } else {
                    hairColor = "black";
                }
            } else if (selection >= 22 && selection <= 37) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dark brown";
                } else {
                    hairColor = "dark brown";
                }
            } else if (selection >= 38 && selection <= 48) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", medium brown";
                } else {
                    hairColor = "medium brown";
                }
            } else if (selection >= 49 && selection <= 56) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light brown";
                } else {
                    hairColor = "light brown";
                }
            } else if (selection >= 57 && selection <= 64) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", blond";
                } else {
                    hairColor = "blond";
                }
            } else if (selection >= 65 && selection <= 71) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", chestnut";
                } else {
                    hairColor = "chestnut";
                }
            } else if (selection >= 72 && selection <= 78) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", mahogany";
                } else {
                    hairColor = "mahogany";
                }
            } else if (selection >= 79 && selection <= 84) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dirty blond";
                } else {
                    hairColor = "dirty blond";
                }
            } else if (selection >= 85 && selection <= 89) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden blond";
                } else {
                    hairColor = "golden blond";
                }
            } else if (selection >= 90 && selection <= 93) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light blond";
                } else {
                    hairColor = "light blond";
                }
            } else if (selection >= 94 && selection < 96) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden brown";
                } else {
                    hairColor = "golden brown";
                }
            } else if (selection == 97) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", caramel";
                } else {
                    hairColor = "caramel";
                }
            } else if (selection == 98) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", strawberry blond";
                } else {
                    hairColor = "strawberry blond";
                }
            } else if (selection == 99) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", copper";
                } else {
                    hairColor = "copper";
                }
            } else if (selection == 100) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", red";
                } else {
                    hairColor = "red";
                }
            } else if (selection == 101) {
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
            if (selection > 28) {
                hairColor = "graying";
            }
            if (selection >= 0 && selection <= 15) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", black";
                } else {
                    hairColor = "black";
                }
            } else if (selection >= 16 && selection <= 22) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dark brown";
                } else {
                    hairColor = "dark brown";
                }
            } else if (selection >= 23 && selection <= 29) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", medium brown";
                } else {
                    hairColor = "medium brown";
                }
            } else if (selection >= 30 && selection <= 35) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light brown";
                } else {
                    hairColor = "light brown";
                }
            } else if (selection >= 36 && selection <= 40) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", blond";
                } else {
                    hairColor = "blond";
                }
            } else if (selection >= 41 && selection <= 43) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", chestnut";
                } else {
                    hairColor = "chestnut";
                }
            } else if (selection >= 44 && selection <= 46) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", mahogany";
                } else {
                    hairColor = "mahogany";
                }
            } else if (selection >= 47 && selection <= 50) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", dirty blond";
                } else {
                    hairColor = "dirty blond";
                }
            } else if (selection >= 51 && selection <= 53) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden blond";
                } else {
                    hairColor = "golden blond";
                }
            } else if (selection >= 54 && selection <= 56) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", light blond";
                } else {
                    hairColor = "light blond";
                }
            } else if (selection >= 57 && selection < 59) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", golden brown";
                } else {
                    hairColor = "golden brown";
                }
            } else if (selection == 60) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", caramel";
                } else {
                    hairColor = "caramel";
                }
            } else if (selection == 61) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", strawberry blond";
                } else {
                    hairColor = "strawberry blond";
                }
            } else if (selection == 62) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", copper";
                } else {
                    hairColor = "copper";
                }
            } else if (selection == 63) {
                if(!hairColor.isBlank()) {
                    hairColor = hairColor + ", red";
                } else {
                    hairColor = "red";
                }
            } else if (selection == 64) {
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
            return "heterochromatic";
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
