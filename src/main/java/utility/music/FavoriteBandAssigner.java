package utility.music;

import constants.SimConstants;
import entity.Radio.MusicGenre;
import entity.Student;
import utility.GameRandom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Assigns each student a small set of favorite bands drawn from the genres
 * their (blended primary + secondary clique) taste likes most.
 *
 * <p>Bands come overwhelmingly from the top-ranked liked genre, with a chance
 * to reach into the 2nd or 3rd most-liked genre. Because {@link MusicTaste}
 * already folds the secondary clique into the genre ranking, a student whose
 * secondary clique aligns with another genre (e.g. an Emo with a Punk
 * secondary) naturally has that genre's bands surface more often.</p>
 *
 * <p><b>Tastemakers</b> - students with high perception or who are in the Band
 * clique - occasionally like acts a few months before they break, pulling a
 * pick from the sim's near future (see {@link BandsByGenreProvider#pickFutureBand}).</p>
 */
public final class FavoriteBandAssigner {

    private static final int MIN_BANDS = 7;
    private static final int MAX_BANDS = 7;

    /**
     * Exponent applied to a clique's genre weight when choosing which genre a
     * pick comes from. Squaring sharply favors the most-liked genre (e.g. a
     * Metal kid pulls mostly METAL over their weaker ROCK/PUNK affinities).
     */
    private static final double GENRE_SELECT_EXPONENT = 2.0;

    /** Perception at/above which a student can spot bands before they break. */
    private static final int TASTEMAKER_PERCEPTION = 70;

    /** Clique whose members are musically plugged-in tastemakers. */
    private static final String BAND_CLIQUE = "Band";

    /** Per-pick chance a tastemaker reaches into the near future for a band. */
    private static final double FUTURE_PICK_CHANCE = 0.35;

    /** Used when no liked genre yields a real band (keeps merch sensible). */
    public static final String FALLBACK_BAND = "local band";

    private FavoriteBandAssigner() {
    }

    /**
     * Picks favorite bands for a single student, using the sim's start date as
     * the "now" reference for tastemaker future picks.
     */
    public static void assign(Student student) {
        assign(student, defaultReferenceDate());
    }

    /**
     * Picks favorite bands for a single student and stores them on their
     * statistics. Safe to call after clique assignment; a no-op for a null
     * student.
     *
     * @param student       the student to assign bands to
     * @param referenceDate the current sim date (the "now" tastemakers look
     *                      ahead from); falls back to the sim start if null
     */
    public static void assign(Student student, LocalDate referenceDate) {
        if (student == null || student.studentStatistics == null) {
            return;
        }
        LocalDate now = referenceDate == null
                ? defaultReferenceDate() : referenceDate;

        int target = GameRandom.nextInt(MIN_BANDS, MAX_BANDS);
        LinkedHashSet<String> chosen = new LinkedHashSet<>();

        // Outcasts have deliberately eclectic taste: their bands are drawn
        // completely at random from any genre, ignoring clique taste and the
        // recency/popularity weighting other students follow.
        if (isOutcast(student)) {
            fillRandomly(chosen, target);
        } else {
            MusicPreference preference = MusicTaste.forStudent(student);
            List<Map.Entry<MusicGenre, Double>> liked =
                    likedGenres(preference);
            if (liked.isEmpty()) {
                fillRandomly(chosen, target);
            } else {
                fillFromGenres(chosen, liked, target,
                        isTastemaker(student), now);
            }
        }

        if (chosen.isEmpty()) {
            chosen.add(FALLBACK_BAND);
        }
        student.studentStatistics.setFavoriteBands(new ArrayList<>(chosen));
    }

    private static boolean isOutcast(Student student) {
        return "Outcast".equals(student.studentStatistics.getMainClique());
    }

    /**
     * A tastemaker has keen perception or is in the Band clique (primary or
     * secondary), so they can pick up on acts before they hit the mainstream.
     */
    private static boolean isTastemaker(Student student) {
        if (student.studentStatistics.getPerception() >= TASTEMAKER_PERCEPTION) {
            return true;
        }
        return BAND_CLIQUE.equals(student.studentStatistics.getMainClique())
                || BAND_CLIQUE.equals(student.studentStatistics.getSecondaryClique());
    }

    /** The sim's configured start date, used as the default "now". */
    private static LocalDate defaultReferenceDate() {
        // Calendar months are 0-based in SimConstants; LocalDate months are 1-based.
        return LocalDate.of(SimConstants.STARTING_YEAR,
                SimConstants.STARTING_MONTH + 1, SimConstants.STARTING_DATE);
    }

    /**
     * Fills {@code chosen} with weighted picks from the ranked liked genres.
     * Tastemakers occasionally substitute a future-charting act for the genre.
     */
    private static void fillFromGenres(LinkedHashSet<String> chosen,
                                       List<Map.Entry<MusicGenre, Double>> liked,
                                       int target,
                                       boolean tastemaker, LocalDate now) {
        int attempts = 0;
        int maxAttempts = target * 8;
        while (chosen.size() < target && attempts < maxAttempts) {
            attempts++;
            MusicGenre genre = pickGenre(liked);
            String band = null;
            if (tastemaker && GameRandom.nextDouble() < FUTURE_PICK_CHANCE) {
                band = BandsByGenreProvider.pickFutureBand(genre, now);
            }
            if (band == null) {
                band = BandsByGenreProvider.pickBand(genre);
            }
            if (band != null) {
                chosen.add(band);
            }
        }
    }

    /** Fills {@code chosen} with completely random bands from any genre. */
    private static void fillRandomly(LinkedHashSet<String> chosen, int target) {
        int attempts = 0;
        int maxAttempts = target * 8;
        while (chosen.size() < target && attempts < maxAttempts) {
            attempts++;
            String band = BandsByGenreProvider.pickAnyBand();
            if (band != null) {
                chosen.add(band);
            }
        }
    }

    /** Genres with a positive weight, ordered most-liked first. */
    private static List<Map.Entry<MusicGenre, Double>> likedGenres(
            MusicPreference preference) {
        List<Map.Entry<MusicGenre, Double>> liked = new ArrayList<>();
        for (Map.Entry<MusicGenre, Double> entry
                : preference.getGenreWeights().entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0.0) {
                liked.add(entry);
            }
        }
        liked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return liked;
    }

    /**
     * Chooses a genre weighted by the clique's affinity for it (raised to
     * {@link #GENRE_SELECT_EXPONENT}), so the strongest-liked genre dominates a
     * student's picks while weaker affinities still surface occasionally.
     */
    private static MusicGenre pickGenre(List<Map.Entry<MusicGenre, Double>> liked) {
        if (liked.size() == 1) {
            return liked.get(0).getKey();
        }
        double total = 0.0;
        for (Map.Entry<MusicGenre, Double> entry : liked) {
            total += Math.pow(entry.getValue(), GENRE_SELECT_EXPONENT);
        }
        double roll = GameRandom.nextDouble() * total;
        for (Map.Entry<MusicGenre, Double> entry : liked) {
            roll -= Math.pow(entry.getValue(), GENRE_SELECT_EXPONENT);
            if (roll <= 0.0) {
                return entry.getKey();
            }
        }
        return liked.get(0).getKey();
    }
}
