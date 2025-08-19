package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class TraitLoader {
    public static String[] getOptionsFromJson(String filePath) {
        try (InputStream is = TraitLoader.class.getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new InputStreamReader(is));
            return extractKeys(jsonObject);
        } catch (IOException | ParseException e) {
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
