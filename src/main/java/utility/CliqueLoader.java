package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads and caches clique relationship data from cliques.json and
 * clique_popularity.json. Provides query methods for subgroups,
 * inter-clique relationships, and group categories.
 */
public final class CliqueLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLIQUES_PATH =
            "/Resources/Cliques/cliques.json";
    private static final String POPULARITY_PATH =
            "/Resources/Cliques/clique_popularity.json";
    private static final String YEAR = "2004";

    private static boolean loaded = false;

    private static final Map<String, List<String>> subgroupsByClique =
            new HashMap<>();
    private static final Map<String, Map<String, String>> relationships =
            new HashMap<>();

    private static final List<String> allCliques = new ArrayList<>();
    private static final Set<String> inGroups = new HashSet<>();
    private static final Set<String> neutralGroups = new HashSet<>();
    private static final Set<String> outGroups = new HashSet<>();
    private static final Set<String> decliningLabels = new HashSet<>();
    private static final Set<String> risingLabels = new HashSet<>();

    private CliqueLoader() {
    }

    public static List<String> getAllCliques() {
        ensureLoaded();
        return Collections.unmodifiableList(allCliques);
    }

    public static List<String> getSubgroups(String clique) {
        ensureLoaded();
        return subgroupsByClique.getOrDefault(clique, List.of());
    }

    /**
     * Returns how {@code cliqueA} feels about {@code cliqueB}.
     * One of "Aligns", "Positive", "Neutral", "Negative", or "Hate".
     */
    public static String getRelationship(String cliqueA, String cliqueB) {
        ensureLoaded();
        if (cliqueA == null || cliqueB == null) {
            return "Neutral";
        }
        Map<String, String> relations = relationships.get(cliqueA);
        if (relations == null) {
            return "Neutral";
        }
        return relations.getOrDefault(cliqueB, "Neutral");
    }

    public static Set<String> getInGroups() {
        ensureLoaded();
        return Collections.unmodifiableSet(inGroups);
    }

    public static Set<String> getOutGroups() {
        ensureLoaded();
        return Collections.unmodifiableSet(outGroups);
    }

    public static Set<String> getNeutralGroups() {
        ensureLoaded();
        return Collections.unmodifiableSet(neutralGroups);
    }

    /**
     * Returns "in-group", "out-group", or "neutral" for the given clique.
     */
    public static String getGroupCategory(String clique) {
        ensureLoaded();
        if (inGroups.contains(clique)) {
            return "in-group";
        }
        if (outGroups.contains(clique)) {
            return "out-group";
        }
        return "neutral";
    }

    public static boolean isDecliningSubgroup(String clique, String subgroup) {
        ensureLoaded();
        return decliningLabels.contains(subgroup + " " + clique);
    }

    public static boolean isRisingSubgroup(String clique, String subgroup) {
        ensureLoaded();
        return risingLabels.contains(subgroup + " " + clique);
    }

    /**
     * Returns the "Aligns" list for the given clique, used when
     * assigning secondary cliques.
     */
    public static List<String> getAligns(String clique) {
        ensureLoaded();
        Map<String, String> relations = relationships.get(clique);
        if (relations == null) {
            return List.of();
        }
        List<String> aligns = new ArrayList<>();
        for (Map.Entry<String, String> entry : relations.entrySet()) {
            if ("Aligns".equals(entry.getValue())) {
                aligns.add(entry.getKey());
            }
        }
        return aligns;
    }

    // ---- Loading ----

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadPopularity();
            loadCliques();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load clique data", e);
        }
    }

    private static void loadPopularity() throws IOException, ParseException {
        JSONObject root;
        try (var reader = ResourceAccess.reader(POPULARITY_PATH)) {
            root = (JSONObject) new JSONParser().parse(reader);
        }
        JSONObject yearData = (JSONObject) root.get(YEAR);

        allCliques.addAll(readStringList(yearData.get("cliques")));
        inGroups.addAll(readStringList(yearData.get("In-groups")));
        neutralGroups.addAll(readStringList(yearData.get("Neutral Groups")));
        outGroups.addAll(readStringList(yearData.get("Out-groups")));
        decliningLabels.addAll(
                readStringList(yearData.get("Declining Subgroups")));
        risingLabels.addAll(
                readStringList(yearData.get("Rising Subgroups")));
    }

    private static void loadCliques() throws IOException, ParseException {
        JSONObject root;
        try (var reader = ResourceAccess.reader(CLIQUES_PATH)) {
            root = (JSONObject) new JSONParser().parse(reader);
        }
        JSONObject yearData = (JSONObject) root.get(YEAR);

        String[] categories =
                {"Aligns", "Positive", "Neutral", "Negative", "Hate"};

        for (Object key : yearData.keySet()) {
            String cliqueName = (String) key;
            JSONObject cliqueData = (JSONObject) yearData.get(cliqueName);

            subgroupsByClique.put(cliqueName,
                    readStringList(cliqueData.get("subgroups")));

            Map<String, String> cliqueRelations = new HashMap<>();
            for (String category : categories) {
                for (String other : readStringList(
                        cliqueData.get(category))) {
                    cliqueRelations.put(other, category);
                }
            }
            relationships.put(cliqueName, cliqueRelations);
        }
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
