package utility;

import entity.Staff;
import entity.Student;
import entity.Rooms.Room;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared helpers for rendering clickable entity links in the inspection UI.
 *
 * <p>Inspection text is rendered into a {@link JEditorPane} (HTML) wrapped in a
 * {@code <pre>} block so the existing monospaced column alignment is preserved
 * while still allowing anchors. Entity names are turned into anchors whose
 * {@code href} encodes a registry token; clicking one resolves the token back
 * to the live object and dispatches to the registered {@link InspectionNavigator}.</p>
 */
public final class LinkSupport {

    private static final EntityRegistry REGISTRY = new EntityRegistry();
    private static InspectionNavigator navigator;

    private LinkSupport() {
    }

    /**
     * Registers the navigator that link clicks are dispatched to.
     *
     * @param nav the navigator (typically the {@code SchoolController})
     */
    public static void setNavigator(InspectionNavigator nav) {
        navigator = nav;
    }

    /**
     * @return the shared entity/token registry
     */
    public static EntityRegistry getRegistry() {
        return REGISTRY;
    }

    /**
     * HTML-escapes a string for safe inclusion in markup.
     *
     * @param text the raw text (may be {@code null})
     * @return the escaped text, never {@code null}
     */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns an anchor for the given entity, or the plain (escaped) display
     * text when the entity is null or not a linkable type.
     *
     * @param entity      the entity to link to
     * @param displayText the visible text
     * @return HTML markup
     */
    public static String link(Object entity, String displayText) {
        String safe = escape(displayText);
        if (!isLinkable(entity)) {
            return safe;
        }
        return "<a href=\"ent:" + REGISTRY.tokenFor(entity) + "\">" + safe + "</a>";
    }

    /**
     * Wraps a pre-escaped body in the standard inspection HTML document. Uses a
     * {@code <pre>} block, which preserves column alignment but does not wrap
     * long lines (best for tabular content).
     *
     * @param preEscapedBody body content that is already HTML-safe
     * @return a complete HTML document string
     */
    public static String wrapBody(String preEscapedBody) {
        return "<html><body><pre>" + preEscapedBody + "</pre></body></html>";
    }

    /**
     * Wraps already-escaped/linkified body content for prose display: newlines
     * become {@code <br>} and leading spaces are preserved, while long lines
     * wrap to the component width (best for paragraphs like descriptions).
     *
     * @param linkifiedEscaped HTML-safe content that may contain newlines
     * @return a complete HTML document string
     */
    public static String wrapBodyWrapping(String linkifiedEscaped) {
        String[] lines = linkifiedEscaped.split("\n", -1);
        StringBuilder sb = new StringBuilder("<html><body><div>");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lead = 0;
            while (lead < line.length() && line.charAt(lead) == ' ') {
                lead++;
            }
            for (int s = 0; s < lead; s++) {
                sb.append("&nbsp;");
            }
            sb.append(line.substring(lead));
            if (i < lines.length - 1) {
                sb.append("<br>");
            }
        }
        sb.append("</div></body></html>");
        return sb.toString();
    }

    /**
     * Creates a read-only HTML editor pane configured to look like the previous
     * monospaced {@code JTextArea}s and to dispatch link clicks.
     *
     * @return a configured {@link JEditorPane}
     */
    public static JEditorPane htmlPane() {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        pane.addHyperlinkListener(ev -> {
            if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleActivate(ev.getDescription());
            }
        });
        return pane;
    }

    /**
     * Renders plain text into the pane, turning any occurrence of a known
     * entity name into a clickable link. Alignment is preserved because the
     * content is escaped and wrapped in a {@code <pre>} block.
     *
     * @param pane            the target pane
     * @param plainText       the plain (unescaped) text to render
     * @param entitiesByName  map of display name to entity (may be null/empty)
     */
    public static void setLinkedText(JEditorPane pane, String plainText,
            Map<String, Object> entitiesByName) {
        pane.setText(wrapBody(linkifyEscaped(plainText, entitiesByName)));
        pane.setCaretPosition(0);
    }

    /**
     * Like {@link #setLinkedText} but renders the content as wrapping prose
     * (long lines wrap to the component width) instead of a fixed-width
     * {@code <pre>} block.
     *
     * @param pane           the target pane
     * @param plainText      the plain (unescaped) text to render
     * @param entitiesByName map of display name to entity (may be null/empty)
     */
    public static void setLinkedTextWrapped(JEditorPane pane, String plainText,
            Map<String, Object> entitiesByName) {
        pane.setText(wrapBodyWrapping(linkifyEscaped(plainText, entitiesByName)));
        pane.setCaretPosition(0);
    }

    /**
     * Escapes plain text and replaces occurrences of known entity names with
     * anchors. Longer names are matched first so a shorter name that is a
     * substring of a longer one does not pre-empt it.
     *
     * @param plainText      the plain text
     * @param entitiesByName map of display name to entity
     * @return escaped HTML body content
     */
    public static String linkifyEscaped(String plainText, Map<String, Object> entitiesByName) {
        String escaped = escape(plainText);
        if (entitiesByName == null || entitiesByName.isEmpty()) {
            return escaped;
        }
        Map<String, Object> escapedNameToEntity = new LinkedHashMap<>();
        List<String> names = new ArrayList<>(entitiesByName.keySet());
        names.sort((a, b) -> Integer.compare(
                b == null ? 0 : b.length(), a == null ? 0 : a.length()));
        StringBuilder pattern = new StringBuilder();
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String escName = escape(name);
            if (!escapedNameToEntity.containsKey(escName)) {
                escapedNameToEntity.put(escName, entitiesByName.get(name));
            }
            if (pattern.length() > 0) {
                pattern.append("|");
            }
            pattern.append(Pattern.quote(escName));
        }
        if (pattern.length() == 0) {
            return escaped;
        }
        Matcher matcher = Pattern.compile(pattern.toString()).matcher(escaped);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String group = matcher.group();
            Object entity = escapedNameToEntity.get(group);
            String replacement = isLinkable(entity)
                    ? "<a href=\"ent:" + REGISTRY.tokenFor(entity) + "\">" + group + "</a>"
                    : group;
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /**
     * Dispatches a click on a live entity (e.g. from a table cell or graph
     * node) to the registered navigator.
     *
     * @param entity the clicked entity
     */
    public static void navigate(Object entity) {
        if (navigator == null) {
            return;
        }
        if (entity instanceof Student student) {
            navigator.navigateToStudent(student);
        } else if (entity instanceof Staff staff) {
            navigator.navigateToStaff(staff);
        } else if (entity instanceof Room room) {
            navigator.navigateToRoom(room);
        }
    }

    private static boolean isLinkable(Object entity) {
        return entity instanceof Student || entity instanceof Staff || entity instanceof Room;
    }

    private static void handleActivate(String description) {
        if (description == null) {
            return;
        }
        String token = description.startsWith("ent:") ? description.substring(4) : description;
        navigate(REGISTRY.resolve(token));
    }
}
