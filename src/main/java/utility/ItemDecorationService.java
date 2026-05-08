package utility;

import entity.CellPhone;
import entity.Items.Decoration;
import entity.Student;

import java.util.List;

import static constants.SimConstants.DECORATION_PHONE_ACCESSORIES_RATE;
import static constants.SimConstants.DECORATION_PHONE_BACK_RATE;
import static constants.SimConstants.DECORATION_PHONE_CASE_RATE;
import static constants.SimConstants.DECORATION_PHONE_FRONT_RATE;
import static constants.SimConstants.DECORATION_PHONE_SCREEN_RATE;

/**
 * Applies clique-driven decorations to items owned by students.
 * Decorations are how a clique's visual identity gets expressed on
 * possessions (a studded phone case, a beaded lanyard, a band-sticker
 * collage on a backpack), and live in {@code clique_decorations.json}
 * via {@link CliqueDecorationLoader}.
 *
 * <p>This service is intentionally separate from the trait/condition
 * descriptor pipeline: traits describe what an item <i>is</i> (its
 * stat-driven flavor, e.g. "the screen has a hairline crack"),
 * decorations describe how its owner has personalized it.</p>
 */
public final class ItemDecorationService {

    /** Item-type key used in {@code clique_decorations.json} for phones. */
    public static final String ITEM_TYPE_PHONE = "cellphone";

    private static final String SLOT_CASE = "case";
    private static final String SLOT_SCREEN = "screen";
    private static final String SLOT_BACK = "back";
    private static final String SLOT_FRONT = "front";
    private static final String SLOT_ACCESSORIES = "accessories";

    private ItemDecorationService() {
    }

    /**
     * Decorates a freshly-assigned phone based on the owner's clique
     * and gender.  Each phone slot is rolled independently against its
     * own rate constant; slots whose clique catalog is empty (either
     * because the clique doesn't decorate phones at all, or that
     * particular slot is empty) are skipped.
     *
     * <p>This method is a no-op when the phone already has decorations
     * so re-running phone assignment in a later sim year leaves the
     * existing personalization intact.  Owners without a clique or
     * with no clique decoration data fall through to producing an
     * un-decorated phone, which is fine.</p>
     *
     * @param phone the phone to decorate (no-op when null)
     * @param owner the phone's owner; the owner's main clique and
     *              gender drive the decoration choices
     */
    public static void decoratePhone(CellPhone phone, Student owner) {
        if (phone == null || owner == null) {
            return;
        }
        if (phone.hasDecorations()) {
            return;
        }
        String clique = owner.studentStatistics.getMainClique();
        String gender = owner.studentStatistics.getGender();
        if (clique == null || gender == null) {
            return;
        }
        if (!CliqueDecorationLoader.hasDecorationData(clique, gender, ITEM_TYPE_PHONE)) {
            return;
        }

        List<String> palette = CliqueDecorationLoader.getColors(clique, gender);

        rollSlot(phone, clique, gender, SLOT_CASE,        DECORATION_PHONE_CASE_RATE,        palette);
        rollSlot(phone, clique, gender, SLOT_ACCESSORIES, DECORATION_PHONE_ACCESSORIES_RATE, palette);
        rollSlot(phone, clique, gender, SLOT_BACK,        DECORATION_PHONE_BACK_RATE,        palette);
        rollSlot(phone, clique, gender, SLOT_FRONT,       DECORATION_PHONE_FRONT_RATE,       palette);
        rollSlot(phone, clique, gender, SLOT_SCREEN,      DECORATION_PHONE_SCREEN_RATE,      palette);
    }

    /**
     * Rolls a single decoration slot.  Picks a uniform random type
     * from the clique's catalog for the slot, attaches a color from
     * the palette when one is available, and skips silently when the
     * slot catalog is empty or the per-slot rate roll fails.
     */
    private static void rollSlot(CellPhone phone, String clique, String gender,
                                 String slot, double rate, List<String> palette) {
        List<String> options = CliqueDecorationLoader.getDecorationTypes(
                clique, gender, ITEM_TYPE_PHONE, slot);
        if (options.isEmpty()) {
            return;
        }
        if (GameRandom.nextDouble() >= rate) {
            return;
        }

        String type = options.get(GameRandom.nextInt(options.size()));
        String color = pickColor(palette);
        phone.addDecoration(new Decoration(type, ITEM_TYPE_PHONE, slot, color));
    }

    private static String pickColor(List<String> palette) {
        if (palette == null || palette.isEmpty()) {
            return null;
        }
        return palette.get(GameRandom.nextInt(palette.size()));
    }
}
