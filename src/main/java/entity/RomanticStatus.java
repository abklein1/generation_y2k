package entity;

/**
 * One student's directed perception of a romantic relationship with another
 * student. Perceptions are stored per direction on the social graph, so the
 * two parties of a pair can (and sometimes do) disagree about what their
 * relationship is: surveys of adolescent couples found notable asymmetry in
 * whether each party believed they were in a romantic relationship at all.
 */
public enum RomanticStatus {
    /** No romantic feelings toward the other student. */
    NONE,
    /**
     * A one-directional romantic hope. The other student is typically
     * unaware; crushes held by closeted students toward same-gender peers
     * are kept hidden entirely.
     */
    CRUSH,
    /** A casual, low-commitment involvement ("hooking up"). */
    FLING,
    /**
     * A serious relationship ("going out"/"dating"); when both parties
     * agree on it, it's "official".
     */
    STEADY;

    /** Human-readable lowercase label for inspection text. */
    public String label() {
        return switch (this) {
            case NONE -> "nothing";
            case CRUSH -> "a crush";
            case FLING -> "hooking up";
            case STEADY -> "going out";
        };
    }

    /**
     * Label used when both parties report the same status. Agreement
     * upgrades the slang: mutual hooking up reads as "FWB", and a mutually
     * acknowledged relationship is "official".
     */
    public String mutualLabel() {
        return switch (this) {
            case FLING -> "FWB";
            case STEADY -> "official";
            default -> label();
        };
    }
}
