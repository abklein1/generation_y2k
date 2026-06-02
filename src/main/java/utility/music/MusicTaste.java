package utility.music;

import entity.Radio.MusicGenre;
import entity.Student;

import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves a student's <em>effective</em> {@link MusicPreference} by blending
 * the genre weights of their primary clique with a lighter contribution from
 * their secondary clique. A student with no secondary clique simply takes their
 * primary clique's preference unchanged.
 *
 * <p>This is the single entry point shared by the commute-radio reaction logic
 * and the favorite-band picker so both reason about taste the same way.</p>
 */
public final class MusicTaste {

    /**
     * Fraction of the secondary clique's weight folded into the blend. Kept
     * well below 1.0 so the primary clique still dominates a student's taste.
     */
    private static final double SECONDARY_WEIGHT = 0.4;

    private MusicTaste() {
    }

    /** Convenience overload that reads cliques from the student's statistics. */
    public static MusicPreference forStudent(Student student) {
        if (student == null || student.studentStatistics == null) {
            return CliqueMusicPreferenceLoader.getDefaultPreference();
        }
        return forCliques(student.studentStatistics.getMainClique(),
                student.studentStatistics.getSecondaryClique());
    }

    /**
     * Blend a primary and (optional) secondary clique into one preference.
     *
     * @param mainClique      primary clique name (may be {@code null})
     * @param secondaryClique secondary clique name (may be {@code null})
     * @return the blended preference; never {@code null}
     */
    public static MusicPreference forCliques(String mainClique,
                                             String secondaryClique) {
        MusicPreference primary =
                CliqueMusicPreferenceLoader.getPreference(mainClique);
        if (secondaryClique == null || secondaryClique.isEmpty()) {
            return primary;
        }
        MusicPreference secondary =
                CliqueMusicPreferenceLoader.getPreference(secondaryClique);

        Map<MusicGenre, Double> blended = new EnumMap<>(MusicGenre.class);
        for (MusicGenre genre : MusicGenre.values()) {
            double weight = primary.weightFor(genre)
                    + SECONDARY_WEIGHT * secondary.weightFor(genre);
            if (weight != 0.0) {
                blended.put(genre, clamp(weight));
            }
        }
        // Identity is anchored on the primary clique, so keep its openness.
        return new MusicPreference(blended, primary.getOpenness());
    }

    private static double clamp(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }
}
