package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class TraitLoader {
    public static String[] getOptionsFromJson(String filePath) {
        try {
            // Convert resource path to file path (remove leading slash and prepend src/main/java)
            String actualPath;
            if (filePath.startsWith("/")) {
                actualPath = "src/main/java" + filePath;
            } else {
                actualPath = "src/main/java/" + filePath;
            }
            
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(actualPath));
            return extractKeys(jsonObject);
        } catch (IOException | ParseException e) {
            System.err.println("Error loading trait options from: " + filePath);
            e.printStackTrace();
            return new String[]{};
        }
    }

    @SuppressWarnings("unchecked")
    private static String[] extractKeys(JSONObject jsonObject) {
        Iterator<String> keys = ((Set<String>) jsonObject.keySet()).iterator();
        return keys.hasNext() ? ((Set<String>) ((JSONObject) jsonObject.get(keys.next())).keySet()).toArray(new String[0]) : new String[]{};
    }
}
