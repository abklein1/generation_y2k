package utility;

import utility.io.ResourceAccess;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NameLoader {
    /**
     * SSA/census yob files include hospital and form placeholders that are not
     * real given names. Matched case-insensitively when loading first-name data.
     */
    private static final Set<String> PLACEHOLDER_FIRST_NAMES = Set.of(
            "unknown", "unkown", "unk",
            "infant", "infantof", "infantboy",
            "baby", "babygirl", "babyboy", "babby",
            "unnamed",
            "male", "female", "boy", "girl",
            "newborn",
            "na",
            "doe");

    private static final HashMap<Integer, String> firstNames = new HashMap<Integer, String>();
    // cache students since we are generating more
    private static final HashMap<Integer, String> firstNames1986 = new HashMap<Integer, String>();
    private static final HashMap<Integer, String> firstNames1987 = new HashMap<Integer, String>();
    private static final HashMap<Integer, String> firstNames1988 = new HashMap<Integer, String>();
    private static final HashMap<Integer, String> firstNames1989 = new HashMap<Integer, String>();
    private static final HashMap<Integer, String> firstNames1990 = new HashMap<Integer, String>();
    private static final HashMap<String, NameData> lastNamesStudent = new HashMap<>();
    private static final HashMap<Integer, Long> frequency = new HashMap<>();
    // cache frequency for students
    private static final HashMap<Integer, Long> frequency1986 = new HashMap<>();
    private static final HashMap<Integer, Long> frequency1987 = new HashMap<>();
    private static final HashMap<Integer, Long> frequency1988 = new HashMap<>();
    private static final HashMap<Integer, Long> frequency1989 = new HashMap<>();
    private static final HashMap<Integer, Long> frequency1990 = new HashMap<>();
    private static final HashMap<Integer, Character> gender = new HashMap<>();
    // cache gender maps for students
    private static final HashMap<Integer, Character> gender1986 = new HashMap<>();
    private static final HashMap<Integer, Character> gender1987 = new HashMap<>();
    private static final HashMap<Integer, Character> gender1988 = new HashMap<>();
    private static final HashMap<Integer, Character> gender1989 = new HashMap<>();
    private static final HashMap<Integer, Character> gender1990 = new HashMap<>();

    // TODO: this is some nasty code to refactor, but for performance
    public static void readCSVFirst(String birth) {
        String basePath = "/Resources.People/";
        String csvFirst = basePath + "yob" + birth + ".txt";
        BufferedReader fr = null;
        int iterator = 0;

        try {
            fr = new BufferedReader(ResourceAccess.reader(csvFirst));
            String f_line;

            while ((f_line = fr.readLine()) != null) {
                String[] parts = f_line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    if (isPlaceholderFirstName(name)) {
                        continue;
                    }
                    char gen = parts[1].trim().charAt(0);
                    long freq = Long.parseLong(parts[2].trim());
                    switch (birth) {
                        case "1986":
                            firstNames1986.put(iterator, name);
                            frequency1986.put(iterator, freq);
                            gender1986.put(iterator, gen);
                            break;
                        case "1987":
                            firstNames1987.put(iterator, name);
                            frequency1987.put(iterator, freq);
                            gender1987.put(iterator, gen);
                            break;
                        case "1988":
                            firstNames1988.put(iterator, name);
                            frequency1988.put(iterator, freq);
                            gender1988.put(iterator, gen);
                            break;
                        case "1989":
                            firstNames1989.put(iterator, name);
                            frequency1989.put(iterator, freq);
                            gender1989.put(iterator, gen);
                            break;
                        case "1990":
                            firstNames1990.put(iterator, name);
                            frequency1990.put(iterator, freq);
                            gender1990.put(iterator, gen);
                            break;
                        default:
                            firstNames.put(iterator, name);
                            frequency.put(iterator, freq);
                            gender.put(iterator, gen);
                            break;
                    }
                    iterator++;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception c) {
                    c.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns true when {@code name} is a known census/SSA placeholder rather
     * than a usable given name (e.g. {@code Unknown}, {@code Infant}, {@code Babyboy}).
     */
    public static boolean isPlaceholderFirstName(String name) {
        return name != null && PLACEHOLDER_FIRST_NAMES.contains(name.toLowerCase(Locale.ROOT));
    }

    // TODO: bad code to refactor later for readability
    public static String nameGenerator(String year, String genderR) {
        if (Integer.parseInt(year) < 1986) {
            readCSVFirst(year);
        }

        char genR = genderR.equalsIgnoreCase("Male") ? 'M' : 'F';
        List<String> filteredNames = new ArrayList<>();
        List<Long> weights = new ArrayList<>();
        String f_name;

        boolean targetGenderFound = false;

        switch (year) {
            case "1986":
                for (int i = 0; i < gender1986.size(); i++) {
                    char currentGender = gender1986.get(i);

                    if (currentGender == genR) {
                        targetGenderFound = true;
                        filteredNames.add(firstNames1986.get(i));
                        weights.add(frequency1986.get(i));
                    } else if (targetGenderFound) {
                        break;
                    }
                }
                break;
            case "1987":
                for (int i = 0; i < gender1987.size(); i++) {
                    char currentGender = gender1987.get(i);

                    if (currentGender == genR) {
                        targetGenderFound = true;
                        filteredNames.add(firstNames1987.get(i));
                        weights.add(frequency1987.get(i));
                    } else if (targetGenderFound) {
                        break;
                    }
                }
                break;
            case "1988":
                for (int i = 0; i < gender1988.size(); i++) {
                    char currentGender = gender1988.get(i);

                    if (currentGender == genR) {
                        targetGenderFound = true;
                        filteredNames.add(firstNames1988.get(i));
                        weights.add(frequency1988.get(i));
                    } else if (targetGenderFound) {
                        break;
                    }
                }
                break;
            case "1989":
                for (int i = 0; i < gender1989.size(); i++) {
                    char currentGender = gender1989.get(i);

                    if (currentGender == genR) {
                        targetGenderFound = true;
                        filteredNames.add(firstNames1989.get(i));
                        weights.add(frequency1989.get(i));
                    } else if (targetGenderFound) {
                        break;
                    }
                }
                break;
            case "1990":
                for (int i = 0; i < gender1990.size(); i++) {
                    char currentGender = gender1990.get(i);

                    if (currentGender == genR) {
                        targetGenderFound = true;
                        filteredNames.add(firstNames1990.get(i));
                        weights.add(frequency1990.get(i));
                    } else if (targetGenderFound) {
                        break;
                    }
                }
                break;
            default:
                for (int i = 0; i < gender.size(); i++) {
                    char currentGender = gender.get(i);

                    if (currentGender == genR) {
                        targetGenderFound = true;
                        filteredNames.add(firstNames.get(i));
                        weights.add(frequency.get(i));
                    } else if (targetGenderFound) {
                        break;
                    }
                }
                break;
        }

        f_name = chooseByWeight(filteredNames, weights);

        return f_name;
    }

    // TODO: handle fail states better
    private static String chooseByWeight(List<String> names, List<Long> weights) {
        if (names.isEmpty() || weights.isEmpty() || names.size() != weights.size()) {
            return null;
        }

        long totalWeight = 0;

        for (Long weight : weights) {
            totalWeight += weight;
        }

        long value = (long) (GameRandom.nextDouble() * totalWeight);
        long cumulativeWeight = 0;

        for (int i = 0; i < names.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (value < cumulativeWeight) {
                return names.get(i);
            }
        }

        return null;
    }

    public static HashMap<Integer, String> readCSVLast() {
        String csvLast = "/Resources.People/surname_2.txt";
        HashMap<Integer, String> lastNames = new HashMap<Integer, String>();
        BufferedReader lr = null;

        try {
            lr = new BufferedReader(ResourceAccess.reader(csvLast));
            String l_line = null;
            Integer iterator = 0;

            while ((l_line = lr.readLine()) != null) {
                String name_2 = l_line.trim();
                lastNames.put(iterator, name_2);
                iterator++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lr != null) {
                try {
                    lr.close();
                } catch (Exception c) {
                    c.printStackTrace();
                }
            }
        }

        return lastNames;
    }

    public static void readCSVLastStudent() {
        String csvLast = "/Resources.People/app_c.csv";

        BufferedReader lr = null;

        try {
            lr = new BufferedReader(ResourceAccess.reader(csvLast));
            String l_line;
            lr.readLine();

            while ((l_line = lr.readLine()) != null) {
                String[] data = l_line.trim().split("\\s*,\\s*");
                String name = data[0].trim();
                double weight = Double.parseDouble(data[2].trim());
                Map<String, Double> raceDistribution = new HashMap<>();

                raceDistribution.put("white", parseDoubleOrS(data[5].trim()));
                raceDistribution.put("black", parseDoubleOrS(data[6].trim()));
                raceDistribution.put("api", parseDoubleOrS(data[7].trim()));
                raceDistribution.put("aian", parseDoubleOrS(data[8].trim()));
                raceDistribution.put("2prace", parseDoubleOrS(data[9].trim()));
                raceDistribution.put("hispanic", parseDoubleOrS(data[10].trim()));

                lastNamesStudent.put(name, new NameData(weight, raceDistribution));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (lr != null)
                    lr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static double parseDoubleOrS(String str) {
        if ("(S)".equals(str)) {
            return 0.1;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.1;
            }
        }
    }

    // Select a surname based on the weight and then a race based on the
    // distribution
    public static String[] selectWeightedRandom() {

        double totalWeight = lastNamesStudent.values().stream().mapToDouble(nd -> nd.weight).sum();
        double value = GameRandom.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (Map.Entry<String, NameData> entry : lastNamesStudent.entrySet()) {
            cumulativeWeight += entry.getValue().weight;
            if (cumulativeWeight >= value) {
                // We've selected a name, now select the race
                String selectedRace = selectRace(entry.getValue().raceDistribution);
                return new String[] { entry.getKey(), selectedRace };
            }
        }
        return new String[] { "", "" }; // Empty result as a fallback
    }

    // Weighted random selection for race
    private static String selectRace(Map<String, Double> raceDist) {
        double total = raceDist.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = GameRandom.nextDouble() * total;
        double cumulative = 0.0;

        for (Map.Entry<String, Double> entry : raceDist.entrySet()) {
            cumulative += entry.getValue();
            if (cumulative >= roll) {
                return entry.getKey();
            }
        }
        return "unknown"; // Fallback in case of error
    }

    public static String suffixNameGenerator(String gender) {
        if (gender.equals("female")) {
            return "";
        }

        int roll = GameRandom.nextInt(1, 100);

        if (roll <= 50) {
            return "Jr.";
        } else if (roll <= 80) {
            return "II";
        } else if (roll <= 95) {
            return "III";
        } else if (roll <= 98) {
            return "IV";
        } else {
            return "V";
        }
    }

    public static char generateMiddleInitial() {
        char middleInitial = (char) (GameRandom.nextInt(26) + 'A');
        return middleInitial;
    }

    static class NameData {
        double weight;
        Map<String, Double> raceDistribution;

        NameData(double weight, Map<String, Double> raceDistribution) {
            this.weight = weight;
            this.raceDistribution = raceDistribution;
        }
    }

}
