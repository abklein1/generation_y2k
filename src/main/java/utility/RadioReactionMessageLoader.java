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

/**
 * Loads commute radio song reaction message templates from JSON.
 */
public final class RadioReactionMessageLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String MESSAGES_PATH =
            "/Resources/Radio/reaction_messages.json";

    private static boolean loaded = false;
    private static final List<String> likeMessages = new ArrayList<>();
    private static final List<String> dislikeMessages = new ArrayList<>();

    private RadioReactionMessageLoader() {
    }

    /**
     * @return immutable like reaction templates in source order.
     */
    public static List<String> getLikeMessages() {
        ensureLoaded();
        return Collections.unmodifiableList(likeMessages);
    }

    /**
     * @return immutable dislike reaction templates in source order.
     */
    public static List<String> getDislikeMessages() {
        ensureLoaded();
        return Collections.unmodifiableList(dislikeMessages);
    }

    /**
     * Pick a random like reaction template.
     *
     * @param random RNG to draw from
     * @return selected message template
     */
    public static String pickLikeMessage(Random random) {
        ensureLoaded();
        return pickRandom(likeMessages, random);
    }

    /**
     * Pick a random dislike reaction template.
     *
     * @param random RNG to draw from
     * @return selected message template
     */
    public static String pickDislikeMessage(Random random) {
        ensureLoaded();
        return pickRandom(dislikeMessages, random);
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadMessages();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load radio reaction messages", e);
        }
    }

    private static void loadMessages() throws IOException, ParseException {
        try (var reader = ResourceAccess.reader(MESSAGES_PATH)) {
            JSONObject root = (JSONObject) new JSONParser().parse(reader);
            loadMessageArray(root, "likes", likeMessages);
            loadMessageArray(root, "dislikes", dislikeMessages);
        }
    }

    private static void loadMessageArray(JSONObject root, String key,
                                         List<String> target)
            throws ParseException {
        Object value = root.get(key);
        if (!(value instanceof JSONArray array)) {
            throw new ParseException(0);
        }

        target.clear();
        for (Object entry : array) {
            if (entry == null) {
                continue;
            }
            String message = entry.toString().trim();
            if (!message.isEmpty()) {
                target.add(message);
            }
        }
        if (target.isEmpty()) {
            throw new ParseException(0);
        }
    }

    private static String pickRandom(List<String> messages, Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random is required");
        }
        return messages.get(random.nextInt(messages.size()));
    }

    /** Reset the cache. Visible for tests. */
    static synchronized void resetForTests() {
        loaded = false;
        likeMessages.clear();
        dislikeMessages.clear();
    }
}
