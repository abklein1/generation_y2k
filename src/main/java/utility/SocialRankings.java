package utility;

import entity.RomanticStatus;
import entity.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A leaderboard window ranking students by social standing. Popularity is
 * the sum of every incoming directed score (how the school collectively
 * feels about a student), so one-sided friendships and secret enemies all
 * count. The romance tab ranks who is crushed on the most (including hidden
 * crushes -- this is an omniscient debug view, like the inspector) and who
 * is most romantically entangled by total tie strength. Names are clickable
 * and navigate to the student's inspection window.
 */
public final class SocialRankings {

    private static final int TOP_N = 15;
    private static final int NAME_COL_WIDTH = 26;

    private SocialRankings() {
    }

    /**
     * Opens the rankings window. Contents are computed on open and on demand
     * via the Refresh button.
     *
     * @param students  the student population
     * @param connector the social link connector holding graph and romance data
     */
    public static void show(HashMap<Integer, Student> students, SocialLinkConnector connector) {
        if (students == null || connector == null) {
            return;
        }
        JEditorPane popularPane = LinkSupport.htmlPane();
        JEditorPane unpopularPane = LinkSupport.htmlPane();
        JEditorPane romancePane = LinkSupport.htmlPane();

        Runnable refresh = () -> {
            HashMap<Student, Double> totals = connector.computeIncomingScoreTotals();
            // The graph can hold vertices for students no longer (or never)
            // enrolled -- e.g. removed by graduation verification after the
            // graph was built. Rank only the current roster.
            HashSet<Student> roster = new HashSet<>(students.values());
            List<Student> ranked = new ArrayList<>(totals.keySet());
            ranked.removeIf(student -> !roster.contains(student));
            ranked.sort(Comparator.comparingDouble(s -> -totals.get(s)));

            renderPopularity(popularPane, ranked, totals, true);
            renderPopularity(unpopularPane, ranked, totals, false);
            renderRomance(romancePane, students, connector);
        };
        refresh.run();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Most Popular", new JScrollPane(popularPane));
        tabs.addTab("Least Popular", new JScrollPane(unpopularPane));
        tabs.addTab("Romance", new JScrollPane(romancePane));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh.run());
        toolbar.add(refreshButton);
        JLabel hint = new JLabel("Popularity = sum of everyone's feelings toward the student \u00b7 click a name to inspect");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        toolbar.add(hint);

        JFrame frame = new JFrame("Social Rankings");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(toolbar, BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.setSize(780, 640);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Renders the top (or bottom) popularity leaderboard into the pane. */
    private static void renderPopularity(JEditorPane pane, List<Student> ranked,
            Map<Student, Double> totals, boolean top) {
        StringBuilder sb = new StringBuilder();
        sb.append(top ? "MOST POPULAR STUDENTS\n" : "LEAST POPULAR STUDENTS\n");
        sb.append(pad("", 5)).append(pad("Name", NAME_COL_WIDTH))
                .append(pad("Grade", 11)).append(pad("Clique", 12))
                .append("Reputation\n");
        sb.append("-".repeat(5 + NAME_COL_WIDTH + 11 + 12 + 10)).append("\n");

        int count = Math.min(TOP_N, ranked.size());
        for (int i = 0; i < count; i++) {
            Student student = top ? ranked.get(i) : ranked.get(ranked.size() - 1 - i);
            int rank = top ? i + 1 : ranked.size() - i;
            sb.append(pad(rank + ".", 5))
                    .append(paddedLink(student, NAME_COL_WIDTH))
                    .append(pad(safe(student.studentStatistics.getGradeLevel()), 11))
                    .append(pad(safe(student.studentStatistics.getMainClique()), 12))
                    .append(String.format("%+.0f", totals.get(student)))
                    .append("\n");
        }
        pane.setText(LinkSupport.wrapBody(sb.toString()));
        pane.setCaretPosition(0);
    }

    /** Renders the crushed-on and most-involved leaderboards into the pane. */
    private static void renderRomance(JEditorPane pane, HashMap<Integer, Student> students,
            SocialLinkConnector connector) {
        HashMap<Student, Integer> crushCounts = new HashMap<>();
        HashMap<Student, Integer> tieCounts = new HashMap<>();
        HashMap<Student, Double> tieStrength = new HashMap<>();

        for (Student student : students.values()) {
            for (Student other : connector.getRomanticInterests(student)) {
                if (connector.getRomanticStatus(student, other) == RomanticStatus.CRUSH) {
                    crushCounts.merge(other, 1, Integer::sum);
                }
                // Count the tie and its combined strength for both ends; pairs
                // with records in both directions are visited twice, once per
                // holder, so each participant is tallied exactly once per tie
                double pairStrength = connector.getSocialScore(student, other)
                        + connector.getSocialScore(other, student);
                tieCounts.merge(student, 1, Integer::sum);
                tieStrength.merge(student, pairStrength, Double::sum);
                if (connector.getRomanticStatus(other, student) == RomanticStatus.NONE) {
                    // One-sided record: still entangles the (unaware) target
                    tieCounts.merge(other, 1, Integer::sum);
                    tieStrength.merge(other, pairStrength, Double::sum);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("MOST CRUSHED-ON STUDENTS  (incoming crushes, hidden ones included)\n");
        sb.append(pad("", 5)).append(pad("Name", NAME_COL_WIDTH))
                .append(pad("Grade", 11)).append("Crushes\n");
        sb.append("-".repeat(5 + NAME_COL_WIDTH + 11 + 7)).append("\n");
        List<Student> crushRanked = new ArrayList<>(crushCounts.keySet());
        crushRanked.sort(Comparator.comparingInt((Student s) -> -crushCounts.get(s)));
        int shown = Math.min(TOP_N, crushRanked.size());
        for (int i = 0; i < shown; i++) {
            Student student = crushRanked.get(i);
            sb.append(pad((i + 1) + ".", 5))
                    .append(paddedLink(student, NAME_COL_WIDTH))
                    .append(pad(safe(student.studentStatistics.getGradeLevel()), 11))
                    .append(crushCounts.get(student))
                    .append("\n");
        }
        if (shown == 0) {
            sb.append("   (nobody is being crushed on right now)\n");
        }

        sb.append("\n\nMOST ROMANTICALLY INVOLVED  (total strength of all romantic ties)\n");
        sb.append(pad("", 5)).append(pad("Name", NAME_COL_WIDTH))
                .append(pad("Grade", 11)).append(pad("Ties", 6)).append("Strength\n");
        sb.append("-".repeat(5 + NAME_COL_WIDTH + 11 + 6 + 8)).append("\n");
        List<Student> involvedRanked = new ArrayList<>(tieStrength.keySet());
        involvedRanked.sort(Comparator.comparingDouble((Student s) -> -tieStrength.get(s)));
        shown = Math.min(TOP_N, involvedRanked.size());
        for (int i = 0; i < shown; i++) {
            Student student = involvedRanked.get(i);
            sb.append(pad((i + 1) + ".", 5))
                    .append(paddedLink(student, NAME_COL_WIDTH))
                    .append(pad(safe(student.studentStatistics.getGradeLevel()), 11))
                    .append(pad(String.valueOf(tieCounts.get(student)), 6))
                    .append(String.format("%.0f", tieStrength.get(student)))
                    .append("\n");
        }
        if (shown == 0) {
            sb.append("   (no romantic entanglements right now)\n");
        }

        pane.setText(LinkSupport.wrapBody(sb.toString()));
        pane.setCaretPosition(0);
    }

    /**
     * A clickable name anchor padded (by visible text length) to the column
     * width, so the surrounding {@code <pre>} table stays aligned.
     */
    private static String paddedLink(Student student, int width) {
        String name = student.studentName.getFullName();
        String display = name.length() > width - 2 ? name.substring(0, width - 2) : name;
        return LinkSupport.link(student, display)
                + " ".repeat(Math.max(1, width - display.length()));
    }

    private static String pad(String text, int width) {
        String value = text == null ? "" : text;
        return value.length() >= width ? value : value + " ".repeat(width - value.length());
    }

    private static String safe(String value) {
        return value == null ? "?" : value;
    }
}
