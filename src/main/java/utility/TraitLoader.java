package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class TraitLoader {
    public static String[] getOptionsFromJson(String filePath) {
        try {
            JSONParser parser = new JSONParser();
            String resourcePath = filePath.startsWith("/") ? filePath : "/" + filePath;
            JSONObject jsonObject;
            try (var reader = ResourceAccess.reader(resourcePath)) {
                jsonObject = (JSONObject) parser.parse(reader);
            }
            return extractKeys(jsonObject);
        } catch (IOException | ParseException e) {
            GameLogger.logDebug("Error loading trait options from: " + filePath);
            e.printStackTrace();
            return new String[] {};
        }
    }

    @SuppressWarnings("unchecked")
    private static String[] extractKeys(JSONObject jsonObject) {
        Iterator<String> keys = ((Set<String>) jsonObject.keySet()).iterator();
        return keys.hasNext()
                ? ((Set<String>) ((JSONObject) jsonObject.get(keys.next())).keySet()).toArray(new String[0])
                : new String[] {};
    }
}
