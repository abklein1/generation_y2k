package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NameLoader {
    private static HashMap<Integer, String> firstNames = new HashMap<Integer, String>();
    private static HashMap<Integer, Long> frequency = new HashMap<>();
    private static HashMap<Integer, Character> gender = new HashMap<>();

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

    public static HashMap readCSVLast(boolean student) {
        String csvLast;
        HashMap<Integer, String> lastNames = new HashMap<Integer, String>();
        BufferedReader lr = null;
        if(student){
            csvLast = "src/main/java/Resources/surname_1.txt";
        }else{
            csvLast = "src/main/java/Resources/surname_2.txt";
        }
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

}
