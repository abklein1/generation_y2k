package simulation.action;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of executing an action.
 */
public class ActionResult {
    
    private final boolean success;
    private final String message;
    private final Map<String, Object> effects;
    private final boolean wasCaught;
    private final String catchMessage;
    
    /**
     * Creates a successful action result.
     *
     * @param message description of what happened
     * @return the result
     */
    public static ActionResult success(String message) {
        return new ActionResult(true, message, false, null);
    }
    
    /**
     * Creates a successful action result with effects.
     *
     * @param message description of what happened
     * @param effects map of stat changes and other effects
     * @return the result
     */
    public static ActionResult success(String message, Map<String, Object> effects) {
        ActionResult result = new ActionResult(true, message, false, null);
        result.effects.putAll(effects);
        return result;
    }
    
    /**
     * Creates a failure action result.
     *
     * @param message description of what happened
     * @return the result
     */
    public static ActionResult failure(String message) {
        return new ActionResult(false, message, false, null);
    }
    
    /**
     * Creates a result where the student was caught doing something.
     *
     * @param message description of what happened
     * @param catchMessage what the teacher said/did
     * @return the result
     */
    public static ActionResult caught(String message, String catchMessage) {
        return new ActionResult(false, message, true, catchMessage);
    }
    
    /**
     * Creates an action result.
     *
     * @param success whether the action succeeded
     * @param message description of what happened
     * @param wasCaught whether the entity was caught
     * @param catchMessage what happened when caught
     */
    private ActionResult(boolean success, String message, boolean wasCaught, String catchMessage) {
        this.success = success;
        this.message = message;
        this.wasCaught = wasCaught;
        this.catchMessage = catchMessage;
        this.effects = new HashMap<>();
    }
    
    /**
     * Checks if the action succeeded.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Gets the result message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the effects map.
     *
     * @return map of effects
     */
    public Map<String, Object> getEffects() {
        return effects;
    }
    
    /**
     * Adds an effect to the result.
     *
     * @param key the effect name
     * @param value the effect value
     * @return this result for chaining
     */
    public ActionResult withEffect(String key, Object value) {
        effects.put(key, value);
        return this;
    }
    
    /**
     * Gets an effect value.
     *
     * @param key the effect name
     * @param <T> the expected type
     * @return the effect value, or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getEffect(String key) {
        return (T) effects.get(key);
    }
    
    /**
     * Gets an integer effect with a default value.
     *
     * @param key the effect name
     * @param defaultValue the default
     * @return the effect value
     */
    public int getIntEffect(String key, int defaultValue) {
        Object value = effects.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }
    
    /**
     * Checks if the entity was caught doing something wrong.
     *
     * @return true if caught
     */
    public boolean wasCaught() {
        return wasCaught;
    }
    
    /**
     * Gets the message about being caught.
     *
     * @return the catch message
     */
    public String getCatchMessage() {
        return catchMessage;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ActionResult{");
        sb.append("success=").append(success);
        sb.append(", message='").append(message).append("'");
        if (wasCaught) {
            sb.append(", CAUGHT: ").append(catchMessage);
        }
        if (!effects.isEmpty()) {
            sb.append(", effects=").append(effects);
        }
        sb.append("}");
        return sb.toString();
    }
}
