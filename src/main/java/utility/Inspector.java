package utility;

import entity.Rooms.Classroom;
import entity.*;
import entity.Rooms.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inspector {

    /**
     * Builds the physical description text for a student.
     * Includes appearance, grade, birthday, family info, and braces/piercing history.
     *
     * @param student the student to describe
     * @return the formatted description text
     */
    private static String buildStudentDescriptionText(Student student) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        StringBuilder sb = new StringBuilder();
        String firstName = student.studentName.getFirstName();
        String gender = student.studentStatistics.getGender();
        String hairColor = student.studentStatistics.getHairColor();
        String eyeColor = student.studentStatistics.getEyeColor();
        String skinColor = student.studentStatistics.getSkinColor();
        String hairLength = student.studentStatistics.getHairLength();
        String hairType = student.studentStatistics.getHairType();
        double height = student.studentStatistics.getHeight();
        boolean hasBraces = student.studentStatistics.getHasBraces();
        String bracesBandColor = student.studentStatistics.getBracesBandColor();
        String bracesSecondBandColor = student.studentStatistics.getBracesSecondBandColor();
        boolean hasAlternatingBands = student.studentStatistics.hasAlternatingBandColors();
        String bracesBracketType = student.studentStatistics.getBracesBracketType();
        boolean bracesHasElastics = student.studentStatistics.getBracesHasElastics();
        String bracesElasticColor = student.studentStatistics.getBracesElasticColor();
        String bracesElasticType = student.studentStatistics.getBracesElasticType();
        LocalDate bracesStartDate = student.studentStatistics.getBracesStartDate();
        LocalDate bracesEndDate = student.studentStatistics.getBracesEndDate();
        boolean hadBracesRemoved = student.studentStatistics.getHadBracesRemoved();
        String grade = student.studentStatistics.getGradeLevel();
        String income = student.studentStatistics.getIncomeLevel();
        LocalDate birth = student.studentStatistics.getBirthday();
        List<Student> siblingsNotInSchool = student.studentStatistics.getSiblingsNotInSchool();
        List<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();

        sb.append(student.studentName.getFullName()).append("\n=====================================\n");

        sb.append(firstName).append(" is a ").append(gender.toLowerCase()).append(" with ");
        sb.append(skinColor).append(" colored skin and ");
        sb.append(hairLength.toLowerCase()).append(", ").append(hairType.toLowerCase()).append(", ")
                .append(hairColor.toLowerCase());
        sb.append(" hair and ").append(eyeColor.toLowerCase()).append(" eyes. ");
        sb.append("They stand ").append(df.format(height)).append(" inches tall.");
        if (hasBraces) {
            sb.append(" They have braces with ");
            if (hasAlternatingBands) {
                sb.append("alternating ").append(bracesBandColor).append(" and ")
                        .append(bracesSecondBandColor).append(" bands, ");
            } else {
                sb.append(bracesBandColor).append(" bands, ");
            }
            sb.append(bracesBracketType).append(" brackets");
            if (bracesHasElastics) {
                sb.append(", and a pair of ").append(bracesElasticColor).append(" ").append(bracesElasticType);
            }
            sb.append(".");
        }
        if (student.studentStatistics.getHasGlasses() && !student.studentStatistics.getHasContacts()) {
            sb.append(" They wear glasses.");
        }
        if (student.studentStatistics.getHasEarPiercing()) {
            sb.append(" ").append(student.studentStatistics.getEarPiercingDescription());
        }
        sb.append("\n");

        sb.append(firstName).append(" is a ").append(grade).append(".\n");
        String cliqueLabel = student.studentStatistics.getCliqueLabel();
        if (cliqueLabel != null) {
            sb.append(firstName).append(" is a ").append(cliqueLabel).append(".");
            String secondary = student.studentStatistics.getSecondaryClique();
            if (secondary != null) {
                sb.append(" Secondary: ").append(secondary).append(".");
            }
            sb.append("\n");
        }
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");

        // Family info
        sb.append("\nTheir family has the following income: ").append(income).append("\n");
        if (!siblingsInSchool.isEmpty()) {
            sb.append("They have the following siblings in school:\n");
            for (Student sibling : siblingsInSchool) {
                sb.append("   ").append(sibling.studentName.getFullName()).append("\n");
            }
        }
        if (!siblingsNotInSchool.isEmpty()) {
            sb.append("They have the following siblings not in school:\n");
            for (Student sibling : siblingsNotInSchool) {
                sb.append("   ").append(sibling.studentName.getFullName()).append("\n");
            }
        }

        // Braces history
        if (hasBraces) {
            if (bracesStartDate != null && bracesEndDate != null) {
                sb.append("\n(Got braces: ").append(bracesStartDate)
                        .append(", Expected removal: ").append(bracesEndDate).append(")\n");
            }
        } else if (hadBracesRemoved) {
            sb.append("\nThey previously had braces");
            if (bracesStartDate != null && bracesEndDate != null) {
                sb.append(" (").append(bracesStartDate).append(" to ").append(bracesEndDate).append(")");
            }
            sb.append(".\n");
        }

        return sb.toString();
    }

    /**
     * Builds the stats and status effects text for a student.
     * Includes base stats, secondary stats, and active status effects.
     *
     * @param student the student to build stats for
     * @return the formatted stats text
     */
    private static String buildStudentStatsText(Student student) {
        StringBuilder sb = new StringBuilder();
        String firstName = student.studentName.getFirstName();
        boolean hasBraces = student.studentStatistics.getHasBraces();
        boolean hadBracesRemoved = student.studentStatistics.getHadBracesRemoved();

        sb.append(student.studentName.getFullName()).append("\n=====================================\n");

        // Base stats
        sb.append("Base Stats:\n   INTELLIGENCE: ")
                .append(student.studentStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(student.studentStatistics.getEffectiveCharisma());
        if (hasBraces) {
            sb.append(" (reduced by braces)");
        } else if (hadBracesRemoved) {
            sb.append(" (boosted by past braces)");
        }
        if (student.studentStatistics.getHasEarPiercing()) {
            sb.append(" (boosted by earrings)");
        }
        sb.append("\n   AGILITY: ");
        sb.append(student.studentStatistics.getEffectiveAgility());
        if (student.studentStatistics.hasUncorrectedVision()) {
            sb.append(" (reduced by uncorrected vision)");
        }
        sb.append("\n   DETERMINATION: ")
                .append(student.studentStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(student.studentStatistics.getEffectivePerception());
        if (student.studentStatistics.hasUncorrectedVision()) {
            sb.append(" (reduced by uncorrected vision)");
        }
        sb.append("\n   STRENGTH: ");
        sb.append(student.studentStatistics.getStrength()).append("\n   LUCK: ")
                .append(student.studentStatistics.getLuck()).append("\n");
        sb.append("   EXP: ").append(student.studentStatistics.getExperience()).append("\n");

        // Secondary stats
        sb.append("\nSecondary Stats:\n   Creativity: ")
                .append(student.studentStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(student.studentStatistics.getEmpathy());
        sb.append("\n   Adaptability: ").append(student.studentStatistics.getAdaptability());
        sb.append("\n   Initiative: ").append(student.studentStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(student.studentStatistics.getResilience());
        sb.append("\n   Curiosity: ").append(student.studentStatistics.getCuriosity());
        sb.append("\n   Responsibility: ").append(student.studentStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(student.studentStatistics.getOpenMindedness()).append("\n");

        // Status effects
        sb.append("\nStatus Effects:\n");
        if (student.studentStatistics.getBoredom() == 0) {
            sb.append("   ").append(firstName).append(" is not bored.\n");
        } else {
            sb.append("   ").append(firstName).append(" is slightly bored.\n");
        }
        if (student.studentStatistics.getSleepState()) {
            sb.append("   ").append(firstName).append(" is asleep!\n");
        } else {
            sb.append("   ").append(firstName).append(" is not asleep.\n");
        }
        if (student.studentStatistics.hasVisionIssue()) {
            String visionDescription = student.studentStatistics.getVisionIssueDescription();
            sb.append("   ").append(firstName).append(" has ").append(visionDescription);
            if (student.studentStatistics.hasVisionCorrection()) {
                String correctionDesc = student.studentStatistics.getVisionCorrectionDescription();
                sb.append(", corrected with ").append(correctionDesc).append(".\n");
            } else {
                sb.append(" (uncorrected - Perception -")
                        .append(student.studentStatistics.getVisionPerceptionPenalty())
                        .append(", Agility -")
                        .append(student.studentStatistics.getVisionAgilityPenalty())
                        .append(").\n");
            }
        } else {
            sb.append("   ").append(firstName).append(" has normal vision.\n");
        }

        return sb.toString();
    }

    /**
     * Builds the combined inspection text for legacy single-text-area views.
     * Combines description and stats into one block.
     *
     * @param student the student to build inspection text for
     * @return the formatted inspection text as a String
     */
    private static String buildStudentInspectionText(Student student) {
        return buildStudentDescriptionText(student) + "\n" + buildStudentStatsText(student);
    }

    /**
     * Builds the schedule text organized by semester with periods in order.
     * Fall semester periods 1-4 are listed first, then Spring semester periods 1-4.
     *
     * @param student the student whose schedule to format
     * @return the formatted schedule string
     */
    private static String buildScheduleText(Student student) {
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        StringBuilder sb = new StringBuilder();

        if (schedule.isEmpty()) {
            sb.append("No classes scheduled.\n");
            return sb.toString();
        }

        // Separate blocks by semester and sort by period
        List<StudentBlock> fallBlocks = new java.util.ArrayList<>();
        List<StudentBlock> springBlocks = new java.util.ArrayList<>();

        for (StudentBlock block : schedule) {
            if ("Fall".equalsIgnoreCase(block.getSemester())) {
                fallBlocks.add(block);
            } else if ("Spring".equalsIgnoreCase(block.getSemester())) {
                springBlocks.add(block);
            }
        }

        // Sort each semester by block number
        fallBlocks.sort(java.util.Comparator.comparingInt(StudentBlock::getBlockNumber));
        springBlocks.sort(java.util.Comparator.comparingInt(StudentBlock::getBlockNumber));

        // Fall semester
        sb.append("===============================\n");
        sb.append("        FALL SEMESTER\n");
        sb.append("===============================\n");
        if (fallBlocks.isEmpty()) {
            sb.append("  (No fall classes)\n");
        } else {
            for (StudentBlock block : fallBlocks) {
                int displayPeriod = mapBlockToPeriod(block.getBlockNumber());
                sb.append("  Period ").append(displayPeriod).append(": ");
                sb.append(block.getClassName());
                if (block.getTeacher() != null) {
                    sb.append("\n           ").append(block.getTeacher().teacherName.getFirstName())
                            .append(" ").append(block.getTeacher().teacherName.getLastName());
                }
                if (block.getRoom() != null) {
                    sb.append("  [").append(block.getRoom().getRoomName()).append("]");
                }
                sb.append("\n");
            }
        }

        sb.append("\n");

        // Spring semester
        sb.append("===============================\n");
        sb.append("       SPRING SEMESTER\n");
        sb.append("===============================\n");
        if (springBlocks.isEmpty()) {
            sb.append("  (No spring classes)\n");
        } else {
            for (StudentBlock block : springBlocks) {
                int displayPeriod = mapBlockToPeriod(block.getBlockNumber());
                sb.append("  Period ").append(displayPeriod).append(": ");
                sb.append(block.getClassName());
                if (block.getTeacher() != null) {
                    sb.append("\n           ").append(block.getTeacher().teacherName.getFirstName())
                            .append(" ").append(block.getTeacher().teacherName.getLastName());
                }
                if (block.getRoom() != null) {
                    sb.append("  [").append(block.getRoom().getRoomName()).append("]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Maps block numbers to display periods.
     * In a 4x4 block schedule, block numbers 1-4 correspond directly to periods
     * 1-4.
     * Each period exists in both Fall and Spring semesters.
     */
    private static int mapBlockToPeriod(int blockNumber) {
        return blockNumber;
    }

    /**
     * Public accessor for the schedule panel, used by SchoolController's inspection
     * window.
     *
     * @param student the student whose schedule to display
     * @return a JPanel containing the schedule table
     */
    public static JPanel buildStudentSchedulePanel(Student student) {
        return buildSchedulePanel(student);
    }

    /**
     * Builds a schedule panel as a JTable organized by semester.
     * Columns: Period | Fall Class | Fall Teacher | Fall Room | Spring Class |
     * Spring Teacher | Spring Room
     *
     * @param student the student whose schedule to display
     * @return a JPanel containing the schedule table
     */
    private static JPanel buildSchedulePanel(Student student) {
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        // Index blocks by semester and period for table layout
        // Key: "Fall-1" or "Spring-3", Value: StudentBlock
        Map<String, StudentBlock> blockIndex = new HashMap<>();
        for (StudentBlock block : schedule) {
            int period = mapBlockToPeriod(block.getBlockNumber());
            String key = block.getSemester() + "-" + period;
            blockIndex.put(key, block);
        }

        // Build table data: 4 periods x 7 columns
        String[] columns = { "Period", "Fall Class", "Fall Teacher", "Fall Room",
                "Spring Class", "Spring Teacher", "Spring Room" };
        Object[][] data = new Object[4][7];

        for (int period = 1; period <= 4; period++) {
            data[period - 1][0] = "Period " + period;

            // Fall semester
            StudentBlock fallBlock = blockIndex.get("Fall-" + period);
            if (fallBlock != null) {
                data[period - 1][1] = fallBlock.getClassName();
                data[period - 1][2] = fallBlock.getTeacher() != null
                        ? fallBlock.getTeacher().teacherName.getFirstName() + " "
                                + fallBlock.getTeacher().teacherName.getLastName()
                        : "";
                data[period - 1][3] = fallBlock.getRoom() != null
                        ? fallBlock.getRoom().getRoomName()
                        : "";
            } else {
                data[period - 1][1] = "--";
                data[period - 1][2] = "";
                data[period - 1][3] = "";
            }

            // Spring semester
            StudentBlock springBlock = blockIndex.get("Spring-" + period);
            if (springBlock != null) {
                data[period - 1][4] = springBlock.getClassName();
                data[period - 1][5] = springBlock.getTeacher() != null
                        ? springBlock.getTeacher().teacherName.getFirstName() + " "
                                + springBlock.getTeacher().teacherName.getLastName()
                        : "";
                data[period - 1][6] = springBlock.getRoom() != null
                        ? springBlock.getRoom().getRoomName()
                        : "";
            } else {
                data[period - 1][4] = "--";
                data[period - 1][5] = "";
                data[period - 1][6] = "";
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60); // Period
        table.getColumnModel().getColumn(1).setPreferredWidth(160); // Fall Class
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Fall Teacher
        table.getColumnModel().getColumn(3).setPreferredWidth(80); // Fall Room
        table.getColumnModel().getColumn(4).setPreferredWidth(160); // Spring Class
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Spring Teacher
        table.getColumnModel().getColumn(6).setPreferredWidth(80); // Spring Room

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Also include the formatted text view below the table
        JTextArea scheduleText = new JTextArea(buildScheduleText(student));
        scheduleText.setEditable(false);
        scheduleText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane textScroll = new JScrollPane(scheduleText);
        textScroll.setPreferredSize(new Dimension(600, 180));
        panel.add(textScroll, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Displays student inspection information in a text area (legacy method).
     * For the full tabbed view with schedule, use {@link #inspectStudent(Student)}.
     *
     * @param student        the student to inspect
     * @param inspectionArea the text area to display the information in
     */
    public static void studentInspection(Student student, JTextArea inspectionArea) {
        inspectionArea.setText(buildStudentInspectionText(student));
    }

    /**
     * Updates a JTextArea with the student's physical description.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStudentDescriptionArea(Student student, JTextArea area) {
        area.setText(buildStudentDescriptionText(student));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the student's stats and status effects.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStudentStatsArea(Student student, JTextArea area) {
        area.setText(buildStudentStatsText(student));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the staff member's physical description.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStaffDescriptionArea(Staff staff, JTextArea area) {
        area.setText(buildStaffDescriptionText(staff));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the staff member's stats and status effects.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStaffStatsArea(Staff staff, JTextArea area) {
        area.setText(buildStaffStatsText(staff));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the staff member's teaching schedule.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStaffScheduleArea(Staff staff, JTextArea area) {
        area.setText(buildStaffScheduleText(staff));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with cell phone information.
     * Shows phone details if the person owns one, or a message if they don't.
     * Used by SchoolController's tabbed inspection window.
     *
     * @param phone     the CellPhone object, or null if the person has no phone
     * @param ownerName the display name of the owner
     * @param area      the JTextArea to update
     */
    public static void updateCellPhoneArea(CellPhone phone, String ownerName, JTextArea area) {
        if (phone == null) {
            area.setText(ownerName + " does not own a cell phone.");
        } else {
            StringBuilder sb = new StringBuilder();
            String make = phone.getMake();
            String model = phone.getModel();
            if (make != null && !make.isEmpty() && model != null && !model.isEmpty()) {
                sb.append(make).append(" ").append(model).append("\n");
            } else {
                sb.append("Cell Phone\n");
            }
            sb.append("=====================================\n\n");
            sb.append("Owner:        ").append(phone.getOwnerName()).append("\n");
            sb.append("Number:       ").append(phone.getPhoneNumber()).append("\n");
            if (make != null && !make.isEmpty()) {
                sb.append("Make:         ").append(make).append("\n");
            }
            if (model != null && !model.isEmpty()) {
                sb.append("Model:        ").append(model).append("\n");
            }
            sb.append("Color:        ").append(phone.getColor()).append("\n");

            sb.append("\nData Plan\n-------------------------------------\n");
            sb.append("Minutes:      ").append(phone.getMinutePlan()).append("/month\n");
            sb.append("Text Limit:   ").append(phone.getTextLimit()).append("/month\n");
            area.setText(sb.toString());
        }
        area.setCaretPosition(0);
    }

    /**
     * Opens a full student inspection dialog with tabbed panes.
     * Tab 1 (Description): Physical appearance, grade, birthday, family, history.
     * Tab 2 (Stats): Base stats, secondary stats, and status effects.
     * Tab 3 (Schedule): Class schedule organized by Fall/Spring semesters.
     *
     * @param student the student to inspect
     */
    public static void inspectStudent(Student student) {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Description tab
        JTextArea descArea = new JTextArea(buildStudentDescriptionText(student));
        descArea.setEditable(false);
        descArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        descArea.setCaretPosition(0);
        JScrollPane descScroll = new JScrollPane(descArea);
        tabbedPane.addTab("Description", descScroll);

        // Stats tab
        JTextArea statsArea = new JTextArea(buildStudentStatsText(student));
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsArea.setCaretPosition(0);
        JScrollPane statsScroll = new JScrollPane(statsArea);
        tabbedPane.addTab("Stats", statsScroll);

        // Schedule tab
        JPanel schedulePanel = buildSchedulePanel(student);
        tabbedPane.addTab("Schedule", schedulePanel);

        // Create the dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Student: " + student.studentName.getFullName());
        dialog.setContentPane(tabbedPane);
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Builds the physical description text for a staff member.
     * Includes appearance, age, birthday, assignment, and experience.
     *
     * @param staff the staff member to describe
     * @return the formatted description text
     */
    private static String buildStaffDescriptionText(Staff staff) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();
        String gender = staff.teacherStatistics.getGender().toLowerCase();
        String age = Integer.toString(staff.teacherStatistics.getAge());
        String hairColor = staff.teacherStatistics.getHairColor().toLowerCase();
        String hairLength = staff.teacherStatistics.getHairLength().toLowerCase();
        String hairType = staff.teacherStatistics.getHairType().toLowerCase();
        String eyeColor = staff.teacherStatistics.getEyeColor().toLowerCase();
        double height = staff.teacherStatistics.getHeight();
        LocalDate birth = staff.teacherStatistics.getBirthday();
        String assignment = staff.teacherStatistics.getStaffType().toString().toLowerCase();
        String yearsOfExperience = Integer.toString(staff.teacherStatistics.getYearsOfExperience());

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");
        sb.append(firstName).append(" is a ").append(age).append(" year-old ").append(gender).append(". ");

        if (hairLength.equalsIgnoreCase("bald")) {
            sb.append("They are bald and have ").append(eyeColor).append(" eyes. ");
        } else {
            sb.append("They have ").append(hairLength).append(", ").append(hairType).append(", ").append(hairColor)
                    .append(" hair and ").append(eyeColor).append(" eyes. ");
        }

        sb.append("They stand ").append(df.format(height)).append(" inches tall.");
        if (staff.teacherStatistics.getHasGlasses() && !staff.teacherStatistics.getHasContacts()) {
            sb.append(" They wear glasses.");
        }
        sb.append("\n");

        sb.append(firstName).append(" was born on ").append(birth).append(".\n");
        sb.append("\nThey are assigned as: ").append(assignment).append("\n");
        sb.append("They have ").append(yearsOfExperience).append(" year(s) of teaching experience.\n");

        return sb.toString();
    }

    /**
     * Builds the stats and status effects text for a staff member.
     * Includes base stats, secondary stats, and active status effects.
     *
     * @param staff the staff member to build stats for
     * @return the formatted stats text
     */
    private static String buildStaffStatsText(Staff staff) {
        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");

        // Base stats
        sb.append("Base Stats:\n   INTELLIGENCE: ")
                .append(staff.teacherStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(staff.teacherStatistics.getCharisma());
        sb.append("\n   AGILITY: ").append(staff.teacherStatistics.getAgility());
        sb.append("\n   DETERMINATION: ").append(staff.teacherStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(staff.teacherStatistics.getPerception());
        sb.append("\n   STRENGTH: ").append(staff.teacherStatistics.getStrength());
        sb.append("\n   LUCK: ").append(staff.teacherStatistics.getLuck()).append("\n");

        // Secondary stats
        sb.append("\nSecondary Stats:\n   Creativity: ")
                .append(staff.teacherStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(staff.teacherStatistics.getEmpathy());
        sb.append("\n   Adaptability: ").append(staff.teacherStatistics.getAdaptability());
        sb.append("\n   Initiative: ").append(staff.teacherStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(staff.teacherStatistics.getResilience());
        sb.append("\n   Curiosity: ").append(staff.teacherStatistics.getCuriosity());
        sb.append("\n   Responsibility: ").append(staff.teacherStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(staff.teacherStatistics.getOpenMindedness()).append("\n");

        // Status effects
        sb.append("\nStatus Effects:\n");
        if (staff.teacherStatistics.getBoredom() == 0) {
            sb.append("   ").append(firstName).append(" is not bored.\n");
        } else {
            sb.append("   ").append(firstName).append(" is slightly bored.\n");
        }
        if (staff.teacherStatistics.getSleepState()) {
            sb.append("   ").append(firstName).append(" is asleep!\n");
        } else {
            sb.append("   ").append(firstName).append(" is not asleep.\n");
        }
        if (staff.teacherStatistics.hasVisionIssue()) {
            String visionDescription = staff.teacherStatistics.getVisionIssueDescription();
            sb.append("   ").append(firstName).append(" has ").append(visionDescription);
            if (staff.teacherStatistics.hasVisionCorrection()) {
                String correctionDesc = staff.teacherStatistics.getVisionCorrectionDescription();
                sb.append(", corrected with ").append(correctionDesc).append(".\n");
            } else {
                sb.append(" (uncorrected).\n");
            }
        } else {
            sb.append("   ").append(firstName).append(" has normal vision.\n");
        }

        return sb.toString();
    }

    /**
     * Builds the schedule text for a staff member.
     * Lists each teaching block with semester, period, and class name.
     *
     * @param staff the staff member whose schedule to format
     * @return the formatted schedule text
     */
    private static String buildStaffScheduleText(Staff staff) {
        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();
        List<TeacherBlock> blocks = staff.teacherStatistics.getTeacherSchedule().getTeacherSchedule();

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");

        if (blocks.isEmpty()) {
            sb.append("No classes assigned.\n");
            return sb.toString();
        }

        List<TeacherBlock> fallBlocks = new java.util.ArrayList<>();
        List<TeacherBlock> springBlocks = new java.util.ArrayList<>();

        for (TeacherBlock block : blocks) {
            if ("Fall".equalsIgnoreCase(block.getSemester())) {
                fallBlocks.add(block);
            } else if ("Spring".equalsIgnoreCase(block.getSemester())) {
                springBlocks.add(block);
            }
        }

        fallBlocks.sort(java.util.Comparator.comparingInt(TeacherBlock::getBlockNumber));
        springBlocks.sort(java.util.Comparator.comparingInt(TeacherBlock::getBlockNumber));

        sb.append("===============================\n");
        sb.append("        FALL SEMESTER\n");
        sb.append("===============================\n");
        if (fallBlocks.isEmpty()) {
            sb.append("  (No fall classes)\n");
        } else {
            for (TeacherBlock block : fallBlocks) {
                sb.append("  Period ").append(block.getBlockNumber()).append(": ");
                sb.append(block.getClassName());
                List<Student> students = block.getClassPopulation();
                if (students != null) {
                    sb.append("  [").append(students.size()).append(" students]");
                }
                sb.append("\n");
            }
        }

        sb.append("\n");

        sb.append("===============================\n");
        sb.append("       SPRING SEMESTER\n");
        sb.append("===============================\n");
        if (springBlocks.isEmpty()) {
            sb.append("  (No spring classes)\n");
        } else {
            for (TeacherBlock block : springBlocks) {
                sb.append("  Period ").append(block.getBlockNumber()).append(": ");
                sb.append(block.getClassName());
                List<Student> students = block.getClassPopulation();
                if (students != null) {
                    sb.append("  [").append(students.size()).append(" students]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Displays staff inspection information in a text area (legacy method).
     * Combines description and stats into one view.
     * For the full tabbed view, use {@link #inspectStaff(Staff)}.
     *
     * @param staff          the staff member to inspect
     * @param inspectionArea the text area to display the information in
     */
    public static void staffInspection(Staff staff, JTextArea inspectionArea) {
        inspectionArea.setText(buildStaffDescriptionText(staff) + "\n" + buildStaffStatsText(staff));
    }

    /**
     * Opens a full staff inspection dialog with tabbed panes.
     * Tab 1 (Description): Physical appearance, birthday, assignment, experience.
     * Tab 2 (Stats): Base stats, secondary stats, and status effects.
     * Tab 3 (Schedule): Teaching schedule organized by Fall/Spring semesters.
     *
     * @param staff the staff member to inspect
     */
    public static void inspectStaff(Staff staff) {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Description tab
        JTextArea descArea = new JTextArea(buildStaffDescriptionText(staff));
        descArea.setEditable(false);
        descArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        descArea.setCaretPosition(0);
        JScrollPane descScroll = new JScrollPane(descArea);
        tabbedPane.addTab("Description", descScroll);

        // Stats tab
        JTextArea statsArea = new JTextArea(buildStaffStatsText(staff));
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsArea.setCaretPosition(0);
        JScrollPane statsScroll = new JScrollPane(statsArea);
        tabbedPane.addTab("Stats", statsScroll);

        // Schedule tab
        JTextArea schedArea = new JTextArea(buildStaffScheduleText(staff));
        schedArea.setEditable(false);
        schedArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        schedArea.setCaretPosition(0);
        JScrollPane schedScroll = new JScrollPane(schedArea);
        tabbedPane.addTab("Schedule", schedScroll);

        // Create the dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Staff: " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName());
        dialog.setContentPane(tabbedPane);
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static String gradeClassInspection(HashMap<Integer, Student> studentGradeClass) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Student> entry : studentGradeClass.entrySet()) {
            Student student = entry.getValue();
            sb.append(student.studentName.getFullName()).append("\n");
        }
        return sb.toString();
    }

    public static String staffListInspection(HashMap<Integer, Staff> staffHashMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            Staff staff = entry.getValue();
            sb.append(staff.teacherName.getFirstName()).append(" ").append(staff.teacherName.getLastName())
                    .append("\n");
        }
        return sb.toString();
    }

    public static void inspectRoom(Room room) {
        String roomName = room.getRoomName();
        StringBuilder roomDetails = new StringBuilder();
        List<Staff> staff = room.getAssignedStaff();
        int studentCap = room.getStudentCapacity();
        List<TeacherBlock> teacherBlocks = null;
        HashMap<Integer, Student[][]> seatingArrangements = room.getPeriodSeatingArrangement();
        JPanel panel = new JPanel();

        if (staff.isEmpty()) {
            roomDetails.append("There are no staff assigned to this room.\n");
        } else {
            TeacherSchedule teacherSchedule = staff.get(0).teacherStatistics.getTeacherSchedule();
            teacherBlocks = teacherSchedule.getTeacherSchedule();
        }

        roomDetails.append("Welcome to ").append(roomName).append("\n");
        roomDetails.append("The room contains the following staff:\n");
        for (Staff value : staff) {
            roomDetails.append(value.teacherName.getFirstName()).append(" ").append(value.teacherName.getLastName())
                    .append("\n");
        }
        roomDetails.append("It has a student capacity of ").append(studentCap).append("\n");
        if (room instanceof Classroom) {
            String abbrev = ((Classroom) room).getClassRoomType();
            roomDetails.append("It is a classroom of type: ").append(abbrev).append("\n");
        } else {
            roomDetails.append("It is a ").append(room.getRoomName()).append("\n");
        }

        JTextArea roomInfoArea = new JTextArea(roomDetails.toString());
        roomInfoArea.setEditable(false);

        // Create a panel for block buttons
        JPanel blockButtonPanel = new JPanel();
        blockButtonPanel.setLayout(new GridLayout(1, 8));
        JButton[] blockButtons = new JButton[8];

        Student[][] firstArrangement = seatingArrangements.values().iterator().next();
        String[] columnNames = new String[firstArrangement[0].length];
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = "Col " + (i + 1);
        }
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, firstArrangement.length);
        JTable studentTable = new JTable(tableModel);
        studentTable.setFillsViewportHeight(true);

        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                int col = studentTable.columnAtPoint(e.getPoint());
                if (!"Empty".equals(tableModel.getValueAt(row, col))) {
                    // Determine which block is currently displayed to find the right student
                    Student[][] currentSeats = seatingArrangements.get(1);
                    if (currentSeats != null && row < currentSeats.length
                            && col < currentSeats[0].length && currentSeats[row][col] != null) {
                        inspectStudent(currentSeats[row][col]);
                    }
                }
            }
        });

        // ActionListener for the buttons to update the seating arrangement
        ActionListener blockButtonListener = e -> {
            int blockNumber = Integer.parseInt(e.getActionCommand());
            Student[][] seats = seatingArrangements.get(blockNumber);
            if (seats != null) {
                for (int row = 0; row < seats.length; row++) {
                    for (int col = 0; col < seats[0].length; col++) {
                        if (seats[row][col] != null) {
                            tableModel.setValueAt(seats[row][col].studentName.getFullName(), row, col);
                        } else {
                            tableModel.setValueAt("Empty", row, col);
                        }
                    }
                }
            } else {
                // If no seating arrangement for this block, set all cells to "Empty"
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        tableModel.setValueAt("Empty", row, col);
                    }
                }
            }
            tableModel.fireTableDataChanged();
        };

        // Create and add buttons for each block
        for (int i = 0; i < 8; i++) {
            blockButtons[i] = new JButton("Block " + (i + 1));
            blockButtons[i].setActionCommand(String.valueOf(i + 1));
            blockButtons[i].addActionListener(blockButtonListener);
            blockButtonPanel.add(blockButtons[i]);
        }

        // Initialize with the first block
        blockButtons[0].doClick();

        JScrollPane studentScrollPane = new JScrollPane(studentTable);
        studentScrollPane.setPreferredSize(new Dimension(400, 200));

        JTextArea studentListArea = new JTextArea();
        studentListArea.setEditable(false);
        if (teacherBlocks != null && !teacherBlocks.isEmpty()) {
            for (TeacherBlock block : teacherBlocks) {
                studentListArea.append("Block: ");
                studentListArea.append(String.valueOf(block.getBlockNumber()));
                studentListArea.append("\n");
                studentListArea.append(block.getClassName());
                studentListArea.append("\n");
                studentListArea.append(block.getSemester());
                studentListArea.append("\n");
                List<Student> students = block.getClassPopulation();
                if (students != null) {
                    for (Student student : students) {
                        studentListArea.append(student.studentName.getFullName());
                        studentListArea.append("\n");
                    }
                } else {
                    studentListArea.append("Students are null!\n");
                }
            }
        } else {
            studentListArea.append("No teacher blocks or students assigned to this room.\n");
        }
        JScrollPane studentListScrollPane = new JScrollPane(studentListArea);
        studentListScrollPane.setPreferredSize(new Dimension(200, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, studentListScrollPane, studentScrollPane);
        splitPane.setResizeWeight(0.3);

        panel.setLayout(new BorderLayout());
        panel.add(roomInfoArea, BorderLayout.NORTH);
        panel.add(blockButtonPanel, BorderLayout.SOUTH); // Buttons at the bottom
        panel.add(splitPane, BorderLayout.CENTER);

        // Create a resizable JDialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Room Details");
        dialog.setContentPane(panel);
        dialog.setModal(true);
        dialog.pack();
        dialog.setSize(800, 600); // Initial size
        dialog.setLocationRelativeTo(null); // Center on screen
        dialog.setVisible(true);
    }
}
