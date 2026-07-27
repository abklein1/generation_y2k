package entity;

/**
 * How publicly a student lives their sexual orientation. In the 2004
 * setting most non-heterosexual students are closeted: they present as
 * heterosexual and will not act on romantic feelings. Straight students
 * are always {@link #OPEN}.
 */
public enum OrientationDisclosure {
    /** The student's orientation is public knowledge (or unremarkable). */
    OPEN,
    /** The student hides their orientation and presents as heterosexual. */
    CLOSETED;
}
