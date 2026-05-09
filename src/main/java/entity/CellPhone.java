package entity;

import entity.Items.Decoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a cell phone owned by a student or staff member.
 * Phone numbers use a local 7-digit format (XXX-XXXX) with no area code.
 * Hardware fields (keyboard, camera, pda, etc.) are stored for future use.
 *
 * <p>
 * Each phone also stores a personal contact list — the phone numbers (with
 * display names) the owner has saved.  Texting another person requires that
 * person's number be in this contact list, just like a real phone in 2004.
 * </p>
 */
public class CellPhone implements Serializable {

    private static final long serialVersionUID = 4L;

    private String phoneNumber;
    private String ownerName;
    private String make;
    private String model;
    private String color;
    private int minutePlan;
    private int textLimit;
    private int textsRemaining;

    private int price;
    private String size;
    private int battery;
    private int releaseYear;
    private boolean keyboard;
    private boolean camera;
    private boolean video;
    private boolean wifi;
    private boolean bluetooth;
    private boolean sms;
    private boolean im;
    private boolean pda;
    private boolean mp3;

    /**
     * The phone's overall physical condition bucket (e.g.
     * {@code "excellent"}, {@code "good"}, {@code "fair"}, {@code "damaged"}),
     * derived at assignment time from phone age, owner agility/luck, and
     * household income.  Null when the condition system hasn't been run on
     * this phone (e.g. legacy save data or a unit-test stub).
     */
    private String condition;

    /**
     * The 2-3 flavor-text descriptors chosen from
     * {@code cellphone_traits.json} based on this phone's condition.  Each
     * string is drawn from a distinct subcategory (screen / casing /
     * overall) so a single phone can't repeat e.g. two casing lines.
     */
    private final List<String> conditionTraits = new ArrayList<>();

    /**
     * Saved contacts keyed by phone number so duplicates can never be added
     * and lookups by number are O(1).  Insertion order is preserved (the most
     * recent contact added appears last) for natural iteration in the UI.
     */
    private final Map<String, Contact> contacts = new LinkedHashMap<>();

    /**
     * Decorations applied to this phone, grouped by slot (e.g.
     * {@code "case"}, {@code "screen"}, {@code "accessories"}) so a
     * single slot can hold more than one decoration when needed (e.g.
     * multiple charms on the wrist strap).  Driven by the clique
     * decoration system at assignment time and intentionally separate
     * from the trait/condition descriptor system.
     */
    private final Map<String, List<Decoration>> decorations = new LinkedHashMap<>();

    /**
     * Creates a cell phone with core fields specified.
     * Hardware fields default to false/zero and can be set via setters.
     */
    public CellPhone(String phoneNumber, String ownerName, String make,
                     String model, String color, int minutePlan, int textLimit) {
        this.phoneNumber = phoneNumber;
        this.ownerName = ownerName;
        this.make = make;
        this.model = model;
        this.color = color;
        this.minutePlan = minutePlan;
        this.textLimit = textLimit;
        this.textsRemaining = textLimit;
        this.size = "";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getMinutePlan() {
        return minutePlan;
    }

    public void setMinutePlan(int minutePlan) {
        this.minutePlan = minutePlan;
    }

    public int getTextLimit() {
        return textLimit;
    }

    public void setTextLimit(int textLimit) {
        this.textLimit = textLimit;
    }

    public int getTextsRemaining() {
        return textsRemaining;
    }

    public void setTextsRemaining(int textsRemaining) {
        this.textsRemaining = textsRemaining;
    }

    /**
     * Consumes one text from the monthly allowance.
     *
     * @return true if there was remaining capacity and the text was sent,
     *         false if the limit has been reached
     */
    public boolean useText() {
        if (textsRemaining <= 0) {
            return false;
        }
        textsRemaining--;
        return true;
    }

    /**
     * Resets the remaining text count back to the plan's monthly limit.
     * Called at the start of each new month.
     */
    public void resetTextLimit() {
        this.textsRemaining = this.textLimit;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    /**
     * @return the year this phone model was released (the outer key in
     *         {@code phones.json}); used together with the simulation year
     *         to compute how aged the phone is when rolling its condition.
     *         Returns 0 when the year is unknown.
     */
    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    /**
     * @return the phone's overall condition bucket, or null if the
     *         condition system has not been applied to this phone
     */
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * @return an unmodifiable snapshot of the flavor-text descriptors
     *         currently attached to this phone (possibly empty)
     */
    public List<String> getConditionTraits() {
        return Collections.unmodifiableList(new ArrayList<>(conditionTraits));
    }

    /**
     * Replaces this phone's condition descriptors with the given list.
     * A null argument clears the descriptors.  Called once at phone
     * assignment by the condition / trait pipeline.
     *
     * @param traits the new descriptor list (copied defensively)
     */
    public void setConditionTraits(List<String> traits) {
        conditionTraits.clear();
        if (traits != null) {
            conditionTraits.addAll(traits);
        }
    }

    public boolean hasKeyboard() {
        return keyboard;
    }

    public void setKeyboard(boolean keyboard) {
        this.keyboard = keyboard;
    }

    public boolean hasCamera() {
        return camera;
    }

    public void setCamera(boolean camera) {
        this.camera = camera;
    }

    public boolean hasVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public boolean hasWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean hasBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public boolean hasSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public boolean hasIm() {
        return im;
    }

    public void setIm(boolean im) {
        this.im = im;
    }

    public boolean hasPda() {
        return pda;
    }

    public void setPda(boolean pda) {
        this.pda = pda;
    }

    public boolean hasMp3() {
        return mp3;
    }

    public void setMp3(boolean mp3) {
        this.mp3 = mp3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CellPhone cellPhone = (CellPhone) o;
        return Objects.equals(phoneNumber, cellPhone.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }

    @Override
    public String toString() {
        return "CellPhone{" +
                "number='" + phoneNumber + '\'' +
                ", owner='" + ownerName + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    // ---- Contact list ----------------------------------------------------

    /**
     * Adds (or overwrites) a contact entry for the given phone number.
     * Self-entries (the owner's own number) and null/empty numbers are
     * silently ignored.
     *
     * @param contactName  the display name to show for this contact
     * @param contactNumber the contact's 7-digit phone number
     */
    public void addContact(String contactName, String contactNumber) {
        if (contactNumber == null || contactNumber.isEmpty()) {
            return;
        }
        if (contactNumber.equals(this.phoneNumber)) {
            return;
        }
        contacts.put(contactNumber, new Contact(contactName, contactNumber));
    }

    /**
     * Adds (or overwrites) the given contact.  No-op if the contact is null
     * or refers to this phone's own number.
     *
     * @param contact the contact entry to save
     */
    public void addContact(Contact contact) {
        if (contact == null || contact.getPhoneNumber() == null) {
            return;
        }
        if (contact.getPhoneNumber().equals(this.phoneNumber)) {
            return;
        }
        contacts.put(contact.getPhoneNumber(), contact);
    }

    /**
     * Removes the contact with the given phone number, if present.
     *
     * @param contactNumber the number to forget
     */
    public void removeContact(String contactNumber) {
        if (contactNumber != null) {
            contacts.remove(contactNumber);
        }
    }

    /**
     * Checks whether this phone has saved the given phone number as a
     * contact.  Used by the texting behavior to gate who the owner can
     * actually message.
     *
     * @param contactNumber the number to look up
     * @return true if the contact list contains this number
     */
    public boolean hasContactNumber(String contactNumber) {
        return contactNumber != null && contacts.containsKey(contactNumber);
    }

    /**
     * Returns an unmodifiable snapshot of all saved contacts in
     * insertion order.
     *
     * @return the saved contacts (never null, possibly empty)
     */
    public List<Contact> getContacts() {
        return Collections.unmodifiableList(new java.util.ArrayList<>(contacts.values()));
    }

    /**
     * @return the number of saved contacts on this phone
     */
    public int getContactCount() {
        return contacts.size();
    }

    /**
     * Removes every saved contact from this phone.  Useful for tests and
     * when a simulation is regenerated.
     */
    public void clearContacts() {
        contacts.clear();
    }

    // ---- Decorations ----------------------------------------------------

    /**
     * Attaches a decoration to this phone in its declared slot.  Null
     * decorations or decorations without a slot are silently ignored
     * so callers can pass through optional rolls without explicit null
     * checks.
     *
     * @param decoration the decoration to attach
     */
    public void addDecoration(Decoration decoration) {
        if (decoration == null || decoration.getSlot() == null) {
            return;
        }
        decorations.computeIfAbsent(decoration.getSlot(),
                k -> new ArrayList<>()).add(decoration);
    }

    /**
     * @return an unmodifiable, slot-grouped view of every decoration
     *         on this phone, with insertion order preserved
     */
    public Map<String, List<Decoration>> getDecorations() {
        Map<String, List<Decoration>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, List<Decoration>> entry : decorations.entrySet()) {
            snapshot.put(entry.getKey(),
                    Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Returns the decorations for a single slot.
     *
     * @param slot the slot key (e.g. {@code "case"}, {@code "accessories"})
     * @return an unmodifiable list of decorations in that slot
     *         (empty when no decorations are attached there)
     */
    public List<Decoration> getDecorationsBySlot(String slot) {
        List<Decoration> list = decorations.get(slot);
        if (list == null) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    /**
     * @return a flat unmodifiable list of every decoration on this
     *         phone, in slot iteration order
     */
    public List<Decoration> getAllDecorations() {
        List<Decoration> flat = new ArrayList<>();
        for (List<Decoration> list : decorations.values()) {
            flat.addAll(list);
        }
        return Collections.unmodifiableList(flat);
    }

    /**
     * @return true when at least one slot has at least one decoration
     */
    public boolean hasDecorations() {
        for (List<Decoration> list : decorations.values()) {
            if (!list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes every decoration from this phone.  Useful for tests and
     * for regenerating a phone when the simulation is reset.
     */
    public void clearDecorations() {
        decorations.clear();
    }

    /**
     * A single saved contact entry: a display name plus the phone number
     * stored on the owner's phone.  Equality is based on the phone number
     * alone — a contact is uniquely identified by their number, just like
     * on a real phone.
     */
    public static class Contact implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final String phoneNumber;

        /**
         * Creates a saved contact entry.
         *
         * @param name        the display name (may be null or empty)
         * @param phoneNumber the contact's phone number (must be non-null)
         */
        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Contact other)) {
                return false;
            }
            return Objects.equals(phoneNumber, other.phoneNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(phoneNumber);
        }

        @Override
        public String toString() {
            return (name == null || name.isEmpty() ? "?" : name)
                    + " <" + phoneNumber + ">";
        }
    }
}
