package entity;

/**
 * A student's sexual orientation, used as the demographic foundation for
 * future romantic relationship mechanics. Orientation is assigned at
 * generation time (see {@code utility.OrientationAssigner}) using
 * 2004-era simulation parameters and never influences platonic
 * friendship generation.
 */
public enum SexualOrientation {
    STRAIGHT,
    GAY,
    BISEXUAL,
    ASEXUAL;

    /**
     * @return true when this orientation is anything other than straight
     */
    public boolean isNonHeterosexual() {
        return this != STRAIGHT;
    }
}
