package utility.music;

import entity.Radio.MusicGenre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline, one-shot tool that turns the locally-downloaded MusicOSet metadata
 * into the trimmed genre assets the runtime matcher consumes, and prints a
 * coverage report against the repo's Billboard chart CSV.
 *
 * <p>This is <strong>not</strong> part of the game runtime. It reads dataset
 * dumps from {@code tools/musicgenre/data/} (git-ignored) and writes committed
 * assets into {@code src/main/java/Resources/Music/}. Run it with:</p>
 *
 * <pre>./gradlew generateGenreAssets</pre>
 *
 * <p>Genre collapse ({@link GenreCollapser}) and performer normalization
 * ({@link PerformerNormalizer}) happen here so the committed assets already
 * hold canonical {@link MusicGenre} names; the runtime never re-collapses.</p>
 */
public final class GenreAssetGenerator {

    private static final String MUSICOSET_DIR =
            "tools/musicgenre/data/musicoset_metadata/";
    private static final String ARTISTS_TSV = MUSICOSET_DIR + "artists.csv";
    private static final String SONGS_TSV = MUSICOSET_DIR + "songs.csv";
    private static final String BILLBOARD_CSV =
            "src/main/java/Resources/Music/song_popularity.csv";

    private static final String OUT_DIR = "src/main/java/Resources/Music/";
    private static final String ARTIST_GENRES_OUT = OUT_DIR + "artist_genres.tsv";
    private static final String SONG_GENRES_OUT = OUT_DIR + "song_genres.tsv";
    private static final String REPORT_OUT =
            "tools/musicgenre/genre_coverage_report.txt";

    /** Spotify IDs are 22-character base62 tokens. */
    private static final Pattern SPOTIFY_ID = Pattern.compile("[0-9A-Za-z]{22}");

    private GenreAssetGenerator() {
    }

    /** Canonical genres for one external artist, plus a display name. */
    private static final class ArtistRecord {
        final String displayName;
        final String normalizedKey;
        final Set<MusicGenre> genres;

        ArtistRecord(String displayName, List<String> rawGenres) {
            this.displayName = displayName;
            this.normalizedKey = PerformerNormalizer.normalize(displayName);
            this.genres = GenreCollapser.collapseAll(rawGenres);
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, ArtistRecord> byId = new HashMap<>();
        Map<String, ArtistRecord> byNameKey = new HashMap<>();
        Map<String, Integer> rawGenreFreq = new HashMap<>();
        loadArtists(byId, byNameKey, rawGenreFreq);
        System.out.println("Loaded " + byId.size() + " MusicOSet artists.");

        Map<String, Set<MusicGenre>> songGenresByKey = new HashMap<>();
        loadSongs(byId, songGenresByKey);
        System.out.println("Indexed " + songGenresByKey.size()
                + " MusicOSet song keys.");

        BillboardScan scan = scanBillboard();
        System.out.println("Scanned Billboard: " + scan.uniquePerformers.size()
                + " unique performers, " + scan.songKeys.size()
                + " unique song keys.");

        Coverage cov = computeCoverage(scan, byNameKey, songGenresByKey);
        Set<String> neededArtistKeys = cov.referencedArtistKeys;

        writeArtistGenres(byNameKey, neededArtistKeys);
        writeSongGenres(scan.songKeys, songGenresByKey);
        writeReport(scan, cov, rawGenreFreq);

        System.out.println("Done. Assets written to " + OUT_DIR);
        System.out.println("Coverage report: " + REPORT_OUT);
    }

    // ---- Loading MusicOSet ----

    private static void loadArtists(Map<String, ArtistRecord> byId,
                                    Map<String, ArtistRecord> byNameKey,
                                    Map<String, Integer> rawGenreFreq)
            throws IOException {
        try (BufferedReader br = open(ARTISTS_TSV)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] f = line.split("\t", -1);
                if (f.length < 7) {
                    continue;
                }
                String id = f[0].trim();
                String name = f[1];
                List<String> raw = parsePyList(f[6]);
                for (String g : raw) {
                    rawGenreFreq.merge(g.toLowerCase(), 1, Integer::sum);
                }
                ArtistRecord rec = new ArtistRecord(name, raw);
                if (!id.isEmpty()) {
                    byId.put(id, rec);
                }
                if (!rec.normalizedKey.isEmpty()) {
                    // Keep the more "popular" record on key collisions by
                    // preferring the first seen (MusicOSet is popularity-sorted).
                    byNameKey.putIfAbsent(rec.normalizedKey, rec);
                }
            }
        }
    }

    private static void loadSongs(Map<String, ArtistRecord> byId,
                                  Map<String, Set<MusicGenre>> songGenresByKey)
            throws IOException {
        try (BufferedReader br = open(SONGS_TSV)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] f = line.split("\t", -1);
                if (f.length < 4) {
                    continue;
                }
                String songName = f[1];
                String performer = extractTuplePerformer(f[2]);
                if (performer == null) {
                    continue;
                }
                Set<MusicGenre> genres = new LinkedHashSet<>();
                Matcher m = SPOTIFY_ID.matcher(f[3]);
                while (m.find()) {
                    ArtistRecord rec = byId.get(m.group());
                    if (rec != null) {
                        for (MusicGenre g : rec.genres) {
                            if (g != MusicGenre.OTHER) {
                                genres.add(g);
                            }
                        }
                    }
                }
                if (genres.isEmpty()) {
                    continue;
                }
                String key = PerformerNormalizer.songKey(songName, performer);
                songGenresByKey.putIfAbsent(key, genres);
            }
        }
    }

    // ---- Billboard scan ----

    private static final class BillboardScan {
        final Map<String, Integer> uniquePerformers = new HashMap<>();
        final Set<String> songKeys = new HashSet<>();
    }

    private static BillboardScan scanBillboard() throws IOException {
        BillboardScan scan = new BillboardScan();
        try (BufferedReader br = open(BILLBOARD_CSV)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] f = parseCsvLine(line);
                if (f.length < 4) {
                    continue;
                }
                String title = f[2];
                String performer = f[3];
                scan.uniquePerformers.merge(performer, 1, Integer::sum);
                scan.songKeys.add(PerformerNormalizer.songKey(title, performer));
            }
        }
        return scan;
    }

    // ---- Coverage ----

    private static final class Coverage {
        int performersWhole;
        int performersPartial;
        int performersUnmatched;
        int rowsWithGenre;
        int totalRows;
        final Set<String> referencedArtistKeys = new HashSet<>();
        final List<Map.Entry<String, Integer>> topUnmatched = new ArrayList<>();
    }

    private static Coverage computeCoverage(
            BillboardScan scan,
            Map<String, ArtistRecord> byNameKey,
            Map<String, Set<MusicGenre>> songGenresByKey) {
        Coverage cov = new Coverage();
        Map<String, Integer> unmatched = new HashMap<>();

        for (Map.Entry<String, Integer> e : scan.uniquePerformers.entrySet()) {
            String performer = e.getKey();
            int freq = e.getValue();
            cov.totalRows += freq;

            String wholeKey = PerformerNormalizer.normalize(performer);
            boolean matched = false;
            if (byNameKey.containsKey(wholeKey)) {
                cov.performersWhole++;
                cov.referencedArtistKeys.add(wholeKey);
                matched = true;
            } else {
                boolean anyPart = false;
                for (String part : PerformerNormalizer.splitCollaborators(performer)) {
                    String partKey = PerformerNormalizer.normalize(part);
                    if (byNameKey.containsKey(partKey)) {
                        cov.referencedArtistKeys.add(partKey);
                        anyPart = true;
                    }
                }
                if (anyPart) {
                    cov.performersPartial++;
                    matched = true;
                } else {
                    cov.performersUnmatched++;
                    unmatched.merge(performer, freq, Integer::sum);
                }
            }
            if (matched) {
                cov.rowsWithGenre += freq;
            }
        }

        // Song-level matches can rescue rows whose performer did not match.
        // (Reported separately below; rowsWithGenre already counts performer
        // matches, which dominate.)
        unmatched.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(
                        Map.Entry::getValue).reversed())
                .limit(40)
                .forEach(cov.topUnmatched::add);
        return cov;
    }

    // ---- Output ----

    private static void writeArtistGenres(Map<String, ArtistRecord> byNameKey,
                                          Set<String> neededKeys)
            throws IOException {
        List<String> keys = new ArrayList<>(neededKeys);
        keys.sort(null);
        try (BufferedWriter w = create(ARTIST_GENRES_OUT)) {
            w.write("# normalized_key\tdisplay_name\tgenres");
            w.newLine();
            for (String key : keys) {
                ArtistRecord rec = byNameKey.get(key);
                if (rec == null) {
                    continue;
                }
                w.write(key);
                w.write('\t');
                w.write(rec.displayName);
                w.write('\t');
                w.write(joinGenres(rec.genres));
                w.newLine();
            }
        }
    }

    private static void writeSongGenres(Set<String> billboardSongKeys,
                                        Map<String, Set<MusicGenre>> songGenresByKey)
            throws IOException {
        Set<String> keys = new HashSet<>(songGenresByKey.keySet());
        keys.retainAll(billboardSongKeys);
        List<String> sorted = new ArrayList<>(keys);
        sorted.sort(null);
        try (BufferedWriter w = create(SONG_GENRES_OUT)) {
            w.write("# song_key (normalized title|performer)\tgenres");
            w.newLine();
            for (String key : sorted) {
                w.write(key);
                w.write('\t');
                w.write(joinGenres(songGenresByKey.get(key)));
                w.newLine();
            }
        }
    }

    private static void writeReport(BillboardScan scan, Coverage cov,
                                    Map<String, Integer> rawGenreFreq)
            throws IOException {
        int totalPerf = scan.uniquePerformers.size();
        try (BufferedWriter w = create(REPORT_OUT)) {
            w.write("MusicOSet -> Billboard genre coverage report");
            w.newLine();
            w.write("=============================================");
            w.newLine();
            w.newLine();
            w.write(String.format("Unique Billboard performers: %d%n", totalPerf));
            w.write(String.format("  whole-performer matches:   %d (%.1f%%)%n",
                    cov.performersWhole, pct(cov.performersWhole, totalPerf)));
            w.write(String.format("  collaboration-part matches:%d (%.1f%%)%n",
                    cov.performersPartial, pct(cov.performersPartial, totalPerf)));
            w.write(String.format("  unmatched:                 %d (%.1f%%)%n",
                    cov.performersUnmatched, pct(cov.performersUnmatched, totalPerf)));
            w.newLine();
            w.write(String.format("Chart rows with >=1 genre (performer match): "
                            + "%d / %d (%.1f%%)%n",
                    cov.rowsWithGenre, cov.totalRows,
                    pct(cov.rowsWithGenre, cov.totalRows)));
            w.newLine();
            w.write("Top unmatched performers by chart frequency:");
            w.newLine();
            for (Map.Entry<String, Integer> e : cov.topUnmatched) {
                w.write(String.format("  %6d  %s%n", e.getValue(), e.getKey()));
            }
            w.newLine();
            w.write("Raw genre tags that collapse to OTHER (need a rule?), "
                    + "by frequency:");
            w.newLine();
            Map<Integer, List<String>> otherByFreq = new TreeMap<>(
                    Comparator.reverseOrder());
            for (Map.Entry<String, Integer> e : rawGenreFreq.entrySet()) {
                if (GenreCollapser.collapse(e.getKey()) == MusicGenre.OTHER) {
                    otherByFreq.computeIfAbsent(e.getValue(),
                            k -> new ArrayList<>()).add(e.getKey());
                }
            }
            int shown = 0;
            outer:
            for (Map.Entry<Integer, List<String>> e : otherByFreq.entrySet()) {
                for (String tag : e.getValue()) {
                    w.write(String.format("  %6d  %s%n", e.getKey(), tag));
                    if (++shown >= 60) {
                        break outer;
                    }
                }
            }
        }
    }

    // ---- Small helpers ----

    private static String joinGenres(Set<MusicGenre> genres) {
        StringBuilder sb = new StringBuilder();
        for (MusicGenre g : genres) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(g.name());
        }
        return sb.toString();
    }

    private static double pct(int part, int total) {
        return total == 0 ? 0.0 : 100.0 * part / total;
    }

    /** Parse a Python-style list literal: {@code ['a', 'b']} -> [a, b]. */
    private static List<String> parsePyList(String field) {
        List<String> out = new ArrayList<>();
        if (field == null) {
            return out;
        }
        String s = field.trim();
        if (s.startsWith("[")) {
            s = s.substring(1);
        }
        if (s.endsWith("]")) {
            s = s.substring(0, s.length() - 1);
        }
        for (String tok : s.split(",")) {
            String t = tok.trim();
            if (t.length() >= 2
                    && (t.startsWith("'") || t.startsWith("\""))) {
                t = t.substring(1, t.length() - 1);
            }
            t = t.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * Extract the performer (second element) from a MusicOSet billboard tuple
     * literal like {@code ('Thank U, Next', 'Ariana Grande')}. Splits on the
     * literal {@code ', '} between the two quoted elements.
     */
    private static String extractTuplePerformer(String field) {
        if (field == null) {
            return null;
        }
        String s = field.trim();
        if (s.startsWith("(")) {
            s = s.substring(1);
        }
        if (s.endsWith(")")) {
            s = s.substring(0, s.length() - 1);
        }
        int sep = s.lastIndexOf("', '");
        if (sep < 0) {
            return null;
        }
        String performer = s.substring(sep + 4).trim();
        if (performer.endsWith("'") || performer.endsWith("\"")) {
            performer = performer.substring(0, performer.length() - 1);
        }
        return performer.trim();
    }

    private static BufferedReader open(String path) throws IOException {
        return new BufferedReader(new FileReader(path, StandardCharsets.UTF_8));
    }

    private static BufferedWriter create(String path) throws IOException {
        return new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8));
    }

    /**
     * Minimal RFC-style CSV splitter (comma delimiter, double-quoted fields),
     * matching the rules used by {@code BillboardSongLoader}.
     */
    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>(8);
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == ',') {
                out.add(cur.toString());
                cur.setLength(0);
            } else if (c == '"' && cur.length() == 0) {
                inQuotes = true;
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
