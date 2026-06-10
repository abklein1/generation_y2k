package utility;

import entity.Staff;
import entity.Student;
import entity.Rooms.Room;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Session-scoped registry that maps live domain objects (students, staff,
 * rooms) to stable string tokens and back. Tokens are used as hyperlink
 * targets in the inspection UI so a clicked link can be resolved back to the
 * concrete object regardless of where it appeared.
 *
 * <p>Resolution is by object identity, so two distinct objects that happen to
 * share a display name still receive distinct tokens.</p>
 */
public class EntityRegistry {

    private final Map<Object, String> tokensByObject = new IdentityHashMap<>();
    private final Map<String, Object> objectsByToken = new HashMap<>();
    private int counter = 0;

    /**
     * Returns the stable token for the given entity, creating one on first use.
     *
     * @param entity the live object to identify
     * @return a stable token, or {@code null} if the entity is {@code null}
     */
    public synchronized String tokenFor(Object entity) {
        if (entity == null) {
            return null;
        }
        String existing = tokensByObject.get(entity);
        if (existing != null) {
            return existing;
        }
        String prefix;
        if (entity instanceof Student) {
            prefix = "student";
        } else if (entity instanceof Staff) {
            prefix = "staff";
        } else if (entity instanceof Room) {
            prefix = "room";
        } else {
            prefix = "obj";
        }
        String token = prefix + ":" + (counter++);
        tokensByObject.put(entity, token);
        objectsByToken.put(token, entity);
        return token;
    }

    /**
     * Resolves a previously issued token back to its live object.
     *
     * @param token the token to resolve
     * @return the live object, or {@code null} if unknown
     */
    public synchronized Object resolve(String token) {
        if (token == null) {
            return null;
        }
        return objectsByToken.get(token);
    }
}
