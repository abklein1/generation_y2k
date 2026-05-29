package utility.music;

import entity.Radio.MusicGenre;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * A clique's (or student's) music taste: a signed weight per
 * {@link MusicGenre} plus an {@code openness} scalar.
 *
 * <p>Weights run roughly {@code -1.0} (strong dislike) to {@code +1.0}
 * (strong like); a genre with no entry is neutral ({@code 0.0}).
 * {@code openness} (0.0-1.0) is how tolerant the listener is of unfamiliar or
 * disliked genres - higher means dislikes weigh less. Scoring/commute logic
 * (a later slice) consumes these values; this type is just the parsed data.</p>
 */
public final class MusicPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<MusicGenre, Double> genreWeights;
    private final double openness;

    public MusicPreference(Map<MusicGenre, Double> genreWeights,
                           double openness) {
        this.genreWeights = new EnumMap<>(MusicGenre.class);
        if (genreWeights != null) {
            this.genreWeights.putAll(genreWeights);
        }
        this.openness = openness;
    }

    /**
     * @param genre a canonical genre
     * @return this listener's weight for the genre, or {@code 0.0} if neutral
     */
    public double weightFor(MusicGenre genre) {
        if (genre == null) {
            return 0.0;
        }
        return genreWeights.getOrDefault(genre, 0.0);
    }

    /** @return openness scalar in {@code [0.0, 1.0]}. */
    public double getOpenness() {
        return openness;
    }

    /** @return immutable view of all non-neutral genre weights. */
    public Map<MusicGenre, Double> getGenreWeights() {
        return Collections.unmodifiableMap(genreWeights);
    }

    @Override
    public String toString() {
        return "MusicPreference{openness=" + openness
                + ", genres=" + genreWeights + "}";
    }
}
