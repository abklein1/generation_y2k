package utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Loads and caches FM station nicknames from
 * {@code Resources/Radio/station_nicknames.json}. Mirrors the lazy
 * static-cache pattern used by {@link OutfitTypeLoader}.
 */
public final class RadioNicknameLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String NICKNAMES_PATH =
            "/Resources/Radio/station_nicknames.json";

    private static boolean loaded = false;
    private static final List<String> nicknames = new ArrayList<>();

    private RadioNicknameLoader() {
    }

    /**
     * @return immutable list of all loaded nicknames in source order.
     */
    public static List<String> getNicknames() {
        ensureLoaded();
        return Collections.unmodifiableList(nicknames);
    }

    /**
     * Pick a random nickname not already used elsewhere in the game.
     *
     * @param random      RNG to draw from
     * @param alreadyUsed set of nicknames already assigned to stations
     * @return a fresh nickname, or {@code null} if every entry has been used
     */
    public static String pickRandomNickname(Random random, Set<String> alreadyUsed) {
        ensureLoaded();
        List<String> available = new ArrayList<>();
        for (String name : nicknames) {
            if (!alreadyUsed.contains(name)) {
                available.add(name);
            }
        }
        if (available.isEmpty()) {
            return null;
        }
        return available.get(random.nextInt(available.size()));
    }

    /**
     * Visible for tests so the cache can be repopulated after a clear.
     */
    static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadNicknames();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load station nicknames", e);
        }
    }

    private static void loadNicknames() throws IOException, ParseException {
        try (var reader = ResourceAccess.reader(NICKNAMES_PATH)) {
            JSONObject root = (JSONObject) new JSONParser().parse(reader);
            JSONArray array = (JSONArray) root.get("nicknames");
            if (array == null) {
                throw new ParseException(0);
            }
            nicknames.clear();
            for (Object value : array) {
                if (value != null) {
                    nicknames.add(value.toString());
                }
            }
        }
    }
}
