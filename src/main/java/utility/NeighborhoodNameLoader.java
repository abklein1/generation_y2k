package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads neighborhood name parts and generates unique names by wealth tier.
 */
public final class NeighborhoodNameLoader implements Serializable {

    private static final String RESOURCE_PATH = "src/main/java/Resources/Town/neighborhoods.json";
    private static final double UNIQUE_NAME_PROBABILITY = 0.15;
    private static final int MAX_NAME_ATTEMPTS = 500;
    private static boolean loaded = false;
    private static final Map<String, List<String>> firstNamesByTier = new HashMap<>();
    private static final Map<String, List<String>> secondNamesByTier = new HashMap<>();
    private static final Map<String, List<String>> uniqueNamesByTier = new HashMap<>();

    private NeighborhoodNameLoader() {
    }

    public static String generateUniqueNeighborhoodName(String wealthLevel, Set<String> usedNames) {
        ensureLoaded();
        String tierKey = getResourceTierKey(wealthLevel);
        Set<String> reservedNames = usedNames == null ? new HashSet<>() : usedNames;

        for (int attempt = 0; attempt < MAX_NAME_ATTEMPTS; attempt++) {
            String candidate = generateNeighborhoodName(tierKey);
            if (!reservedNames.contains(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to generate a unique neighborhood name for tier: " + wealthLevel);
    }

    public static boolean isValidNameForWealthLevel(String wealthLevel, String name) {
        ensureLoaded();
        String tierKey = getResourceTierKey(wealthLevel);
        if (uniqueNamesByTier.getOrDefault(tierKey, List.of()).contains(name)) {
            return true;
        }

        for (String firstName : firstNamesByTier.getOrDefault(tierKey, List.of())) {
            for (String secondName : secondNamesByTier.getOrDefault(tierKey, List.of())) {
                if ((firstName + " " + secondName).equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getResourceTierKey(String wealthLevel) {
        return switch (wealthLevel) {
            case "high" -> "affluent";
            case "middle" -> "middle_class";
            case "low" -> "working_class";
            default -> throw new IllegalArgumentException("Unsupported neighborhood wealth level: " + wealthLevel);
        };
    }

    private static String generateNeighborhoodName(String tierKey) {
        List<String> uniqueNames = uniqueNamesByTier.getOrDefault(tierKey, List.of());
        boolean useUniqueName = !uniqueNames.isEmpty() && GameRandom.nextDouble() < UNIQUE_NAME_PROBABILITY;
        if (useUniqueName) {
            return uniqueNames.get(GameRandom.nextInt(uniqueNames.size()));
        }

        List<String> firstNames = firstNamesByTier.getOrDefault(tierKey, List.of());
        List<String> secondNames = secondNamesByTier.getOrDefault(tierKey, List.of());
        if (firstNames.isEmpty() || secondNames.isEmpty()) {
            throw new IllegalStateException("Neighborhood naming data is incomplete for tier: " + tierKey);
        }
        return firstNames.get(GameRandom.nextInt(firstNames.size())) + " "
                + secondNames.get(GameRandom.nextInt(secondNames.size()));
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }

        try {
            Object parsed = new JSONParser().parse(new FileReader(RESOURCE_PATH, StandardCharsets.UTF_8));
            JSONObject root = (JSONObject) parsed;
            loadTier(root, "affluent");
            loadTier(root, "middle_class");
            loadTier(root, "working_class");
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load neighborhood name data", e);
        }
    }

    private static void loadTier(JSONObject root, String tierKey) {
        firstNamesByTier.put(tierKey, readStringList(root.get("firstNames_" + tierKey)));
        secondNamesByTier.put(tierKey, readStringList(root.get("secondNames_" + tierKey)));
        uniqueNamesByTier.put(tierKey, readStringList(root.get("uniqueNames_" + tierKey)));
    }

    private static List<String> readStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof Iterable<?> values) {
            for (Object entry : values) {
                if (entry != null) {
                    result.add(entry.toString());
                }
            }
        }
        return result;
    }
}
