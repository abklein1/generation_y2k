package utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading flavor text from JSON resources.
 * Used during player character creation to display story background.
 */
public class FlavorTextLoader {
    
    private static final String FLAVOR_TEXT_PATH = "src/main/java/Resources.People/player_character_background.json";
    
    private static JSONObject flavorData = null;
    
    /**
     * Loads the flavor text JSON file if not already loaded.
     */
    private static void ensureLoaded() {
        if (flavorData != null) {
            return;
        }
        
        try {
            JSONParser parser = new JSONParser();
            flavorData = (JSONObject) parser.parse(new FileReader(FLAVOR_TEXT_PATH, StandardCharsets.UTF_8));
        } catch (IOException | ParseException e) {
            GameLogger.logDebug("Error loading flavor text: " + e.getMessage());
            flavorData = new JSONObject();
        }
    }
    
    /**
     * Gets the text lines for a specific section.
     *
     * @param section the section name (intro, background, situation, closing)
     * @return list of text lines, or empty list if section not found
     */
    public static List<String> getSection(String section) {
        ensureLoaded();
        
        List<String> lines = new ArrayList<>();
        
        JSONObject sectionObj = (JSONObject) flavorData.get(section);
        if (sectionObj == null) {
            return lines;
        }
        
        JSONArray textArray = (JSONArray) sectionObj.get("text");
        if (textArray == null) {
            return lines;
        }
        
        for (Object line : textArray) {
            if (line != null) {
                lines.add(line.toString());
            }
        }
        
        return lines;
    }
    
    /**
     * Gets all flavor text sections combined in order.
     *
     * @return list of all text lines from all sections
     */
    public static List<String> getAllText() {
        List<String> allLines = new ArrayList<>();
        
        allLines.addAll(getSection("intro"));
        allLines.addAll(getSection("background"));
        allLines.addAll(getSection("situation"));
        allLines.addAll(getSection("closing"));
        
        return allLines;
    }
    
    /**
     * Gets all flavor text as a single formatted string.
     *
     * @return all flavor text with newlines between lines
     */
    public static String getAllTextFormatted() {
        StringBuilder sb = new StringBuilder();
        
        for (String line : getAllText()) {
            sb.append(line).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Gets a specific section as a formatted string.
     *
     * @param section the section name
     * @return the section text with newlines between lines
     */
    public static String getSectionFormatted(String section) {
        StringBuilder sb = new StringBuilder();
        
        for (String line : getSection(section)) {
            sb.append(line).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Appends all flavor text to a text area.
     *
     * @param textArea the JTextArea to append to
     */
    public static void appendToTextArea(javax.swing.JTextArea textArea) {
        textArea.append("\n");
        textArea.append("═".repeat(40) + "\n");
        textArea.append(getAllTextFormatted());
        textArea.append("═".repeat(40) + "\n");
    }
    
    /**
     * Appends a specific section to a text area.
     *
     * @param textArea the JTextArea to append to
     * @param section the section name
     */
    public static void appendSectionToTextArea(javax.swing.JTextArea textArea, String section) {
        textArea.append(getSectionFormatted(section));
    }
    
    /**
     * Reloads the flavor text from disk.
     * Useful if the file has been edited while the application is running.
     */
    public static void reload() {
        flavorData = null;
        ensureLoaded();
    }
}
