import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class NameLoader {
    public static HashMap readCSVFirst(boolean student) {
        String csvFirst;
        HashMap<Integer, String> firstNames = new HashMap<Integer, String>();
        BufferedReader fr = null;
        if(student){
            csvFirst = "src/main/java/Resources/babies-first-names-1990.txt";
        } else {
            csvFirst = "src/main/java/Resources/babies-first-names-1974.txt";
        }
        try {
            File firstFile = new File(csvFirst);
            fr = new BufferedReader(new FileReader(firstFile));
            String f_line = null;
            Integer iterator = 0;

            while ((f_line = fr.readLine()) != null) {
                String name = f_line.trim();
                firstNames.put(iterator, name);
                iterator++;
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

        return firstNames;
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
