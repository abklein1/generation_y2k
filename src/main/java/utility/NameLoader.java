package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NameLoader {
    private static final HashMap<Integer, String> firstNames = new HashMap<Integer, String>();
    private static final HashMap<String, NameData> lastNamesStudent = new HashMap<>();
    private static final HashMap<Integer, Long> frequency = new HashMap<>();
    private static final HashMap<Integer, Character> gender = new HashMap<>();

    private static void readCSVFirst(String birth) {
        String basePath = "src/main/java/Resources/";
        String csvFirst = basePath + "yob" + birth + ".txt";
        BufferedReader fr = null;
        int iterator = 0;

        try {
            File firstFile = new File(csvFirst);
            fr = new BufferedReader(new FileReader(firstFile));
            String f_line;

            while ((f_line = fr.readLine()) != null) {
                String [] parts = f_line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    char gen = parts[1].trim().charAt(0);
                    long freq = Long.parseLong(parts[2].trim());

                    firstNames.put(iterator, name);
                    frequency.put(iterator, freq);
                    gender.put(iterator, gen);

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

    public static String nameGenerator(String year, String genderR) {

        readCSVFirst(year);

        char genR = genderR.equalsIgnoreCase("Male") ? 'M' : 'F';
        List<String> filteredNames = new ArrayList<>();
        List<Long> weights = new ArrayList<>();
        String f_name;

        boolean targetGenderFound = false;

        for (int i = 0; i < gender.size(); i++) {
            char currentGender= gender.get(i);

            if (currentGender == genR) {
                targetGenderFound = true;
                filteredNames.add(firstNames.get(i));
                weights.add(frequency.get(i));
            } else if (targetGenderFound) {
                break;
            }
        }

        f_name = chooseByWeight(filteredNames, weights);

        firstNames.clear();
        frequency.clear();
        gender.clear();

        return f_name;
    }

    //TODO: handle fail states better
    private static String chooseByWeight(List<String> names, List<Long> weights) {
        if (names.isEmpty() || weights.isEmpty() || names.size() != weights.size()) {
            return null;
        }

        long totalWeight = 0;

        for (Long weight : weights) {
            totalWeight += weight;
        }

        long value = (long) (Math.random() * totalWeight);
        long cumulativeWeight = 0;

        for (int i = 0; i < names.size(); i++) {
            cumulativeWeight += weights.get(i);
            if(value < cumulativeWeight) {
                return names.get(i);
            }
        }

        return null;
    }

    public static HashMap readCSVLast() {
        String csvLast = "src/main/java/Resources/surname_2.txt";
        HashMap<Integer, String> lastNames = new HashMap<Integer, String>();
        BufferedReader lr = null;

        try {
            File lastFile = new File(csvLast);
            lr = new BufferedReader(new FileReader(lastFile));
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
        String csvLast = "src/main/java/Resources/app_c.csv";

        BufferedReader lr = null;

        try {
            lr = new BufferedReader(new FileReader(new File(csvLast)));
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
                if (lr != null) lr.close();
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

    // Select a surname based on the weight and then a race based on the distribution
    public static String[] selectWeightedRandom() {

        double totalWeight = lastNamesStudent.values().stream().mapToDouble(nd -> nd.weight).sum();
        double value = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;



        for (Map.Entry<String, NameData> entry : lastNamesStudent.entrySet()) {
            cumulativeWeight += entry.getValue().weight;
            if (cumulativeWeight >= value) {
                // We've selected a name, now select the race
                String selectedRace = selectRace(entry.getValue().raceDistribution);
                return new String[]{entry.getKey(), selectedRace};
            }
        }
        return new String[]{"", ""}; // Empty result as a fallback
    }

    // Weighted random selection for race
    private static String selectRace(Map<String, Double> raceDist) {
        double total = raceDist.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = Math.random() * total;
        double cumulative = 0.0;

        for (Map.Entry<String, Double> entry : raceDist.entrySet()) {
            cumulative += entry.getValue();
            if (cumulative >= roll) {
                return entry.getKey();
            }
        }
        return "unknown"; // Fallback in case of error
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
