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
     * Builds the inspection text for a student containing their personal info,
     * stats, status effects, family info, and schedule.
     *
     * @param student the student to build inspection text for
     * @return the formatted inspection text as a String
     */
    private static String buildStudentInspectionText(Student student) {
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
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        List<Student> siblingsNotInSchool = student.studentStatistics.getSiblingsNotInSchool();
        List<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();

        // Header with name (using getFullName for consistency)
        sb.append(student.studentName.getFullName()).append("\n=====================================\n");

        // Physical description
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
        // Add glasses to physical description (contacts are not visible)
        if (student.studentStatistics.getHasGlasses() && !student.studentStatistics.getHasContacts()) {
            sb.append(" They wear glasses.");
        }
        sb.append("\n");

        // Grade and birthday
        sb.append(firstName).append(" is a ").append(grade).append(".\n");
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");

        // Base stats
        sb.append("They have the following base stats:\n   INTELLIGENCE: ")
                .append(student.studentStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(student.studentStatistics.getEffectiveCharisma());
        if (hasBraces) {
            sb.append(" (reduced by braces)");
        } else if (hadBracesRemoved) {
            sb.append(" (boosted by past braces)");
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
        sb.append("They have the following secondary stats:\n   Creativity: ")
                .append(student.studentStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(student.studentStatistics.getEmpathy()).append("\n   Adaptability: ");
        sb.append(student.studentStatistics.getAdaptability()).append("\n   Initiative: ")
                .append(student.studentStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(student.studentStatistics.getResilience()).append("\n   Curiosity: ");
        sb.append(student.studentStatistics.getCuriosity()).append("\n   Responsibility: ")
                .append(student.studentStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(student.studentStatistics.getOpenMindedness()).append("\n");

        // Status effects
        sb.append(firstName).append(" has the following status effects:\n");
        if (student.studentStatistics.getBoredom() == 0) {
            sb.append(firstName).append(" is not bored.\n");
        } else {
            sb.append(firstName).append(" is slightly bored.\n");
        }
        if (student.studentStatistics.getSleepState()) {
            sb.append(firstName).append(" is asleep!\n");
        } else {
            sb.append(firstName).append(" is not asleep.\n");
        }
        // Vision issues and corrective lenses
        if (student.studentStatistics.hasVisionIssue()) {
            String visionDescription = student.studentStatistics.getVisionIssueDescription();
            sb.append(firstName).append(" has ").append(visionDescription);
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
            sb.append(firstName).append(" has normal vision.\n");
        }

        // Family info
        sb.append("Their family has the following income: ").append(income).append("\n");
        if (!siblingsInSchool.isEmpty()) {
            sb.append("They have the following siblings in school: ").append("\n");
            for (Student sibling : siblingsInSchool) {
                sb.append(sibling.studentName.getFullName()).append("\n");
            }
        }
        if (!siblingsNotInSchool.isEmpty()) {
            sb.append("They have the following siblings not in school: ").append("\n");
            for (Student sibling : siblingsNotInSchool) {
                sb.append(sibling.studentName.getFullName()).append("\n");
            }
        }

        // Braces history
        if (hasBraces) {
            if (bracesStartDate != null && bracesEndDate != null) {
                sb.append("(Got braces: ").append(bracesStartDate).append(", Expected removal: ").append(bracesEndDate)
                        .append(")").append("\n");
            }
        } else if (hadBracesRemoved) {
            sb.append(" They previously had braces");
            if (bracesStartDate != null && bracesEndDate != null) {
                sb.append(" (").append(bracesStartDate).append(" to ").append(bracesEndDate).append(")");
            }
            sb.append(".");
            sb.append("\n");
        }

        // Schedule is now displayed in its own tab via buildSchedulePanel()
        // Include a brief summary here for the text view
        if (!schedule.isEmpty()) {
            sb.append("\n(See Schedule tab for full class schedule)\n");
        } else {
            sb.append("\nNo classes scheduled.\n");
        }

        return sb.toString();
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
     * In a 4x4 block schedule, block numbers 1-4 correspond directly to periods 1-4.
     * Each period exists in both Fall and Spring semesters.
     */
    private static int mapBlockToPeriod(int blockNumber) {
        return blockNumber;
    }

    /**
     * Public accessor for the schedule panel, used by SchoolController's inspection window.
     *
     * @param student the student whose schedule to display
     * @return a JPanel containing the schedule table
     */
    public static JPanel buildStudentSchedulePanel(Student student) {
        return buildSchedulePanel(student);
    }

    /**
     * Builds a schedule panel as a JTable organized by semester.
     * Columns: Period | Fall Class | Fall Teacher | Fall Room | Spring Class | Spring Teacher | Spring Room
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
        String[] columns = {"Period", "Fall Class", "Fall Teacher", "Fall Room",
                            "Spring Class", "Spring Teacher", "Spring Room"};
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
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Period
        table.getColumnModel().getColumn(1).setPreferredWidth(160);  // Fall Class
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Fall Teacher
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Fall Room
        table.getColumnModel().getColumn(4).setPreferredWidth(160);  // Spring Class
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Spring Teacher
        table.getColumnModel().getColumn(6).setPreferredWidth(80);   // Spring Room

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
     * Opens a full student inspection dialog with tabbed panes.
     * Tab 1 (Info): Personal details, stats, status effects, and family info.
     * Tab 2 (Schedule): Class schedule organized by Fall/Spring semesters with periods 1-4.
     *
     * @param student the student to inspect
     */
    public static void inspectStudent(Student student) {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Info tab
        JTextArea infoArea = new JTextArea(buildStudentInspectionText(student));
        infoArea.setEditable(false);
        infoArea.setCaretPosition(0);
        JScrollPane infoScroll = new JScrollPane(infoArea);
        tabbedPane.addTab("Info", infoScroll);

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

    public static void staffInspection(Staff staff, JTextArea inspectionArea) {
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
        List<String> teacherSchedule = staff.teacherStatistics.getTeacherSchedule().toStringArray();
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
        // Add glasses to physical description (contacts are not visible)
        if (staff.teacherStatistics.getHasGlasses() && !staff.teacherStatistics.getHasContacts()) {
            sb.append(" They wear glasses.");
        }
        sb.append("\n");
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");
        sb.append("They have the following stats:\n   INTELLIGENCE: ")
                .append(staff.teacherStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(staff.teacherStatistics.getCharisma()).append("\n   AGILITY: ");
        sb.append(staff.teacherStatistics.getAgility()).append("\n   DETERMINATION: ")
                .append(staff.teacherStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(staff.teacherStatistics.getPerception()).append("\n   STRENGTH: ");
        sb.append(staff.teacherStatistics.getStrength()).append("\n");
        sb.append("   LUCK: ").append(staff.teacherStatistics.getLuck()).append("\n");
        sb.append("They have the following secondary stats:\n   Creativity: ")
                .append(staff.teacherStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(staff.teacherStatistics.getEmpathy()).append("\n   Adaptability: ");
        sb.append(staff.teacherStatistics.getAdaptability()).append("\n   Initiative: ")
                .append(staff.teacherStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(staff.teacherStatistics.getResilience()).append("\n   Curiosity: ");
        sb.append(staff.teacherStatistics.getCuriosity()).append("\n   Responsibility: ")
                .append(staff.teacherStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(staff.teacherStatistics.getOpenMindedness()).append("\n");
        sb.append("They have ").append(yearsOfExperience).append(" year(s) of teaching experience.").append("\n");
        sb.append(firstName).append(" has the following status effects:\n");
        if (staff.teacherStatistics.getBoredom() == 0) {
            sb.append(firstName).append(" is not bored.\n");
        } else {
            sb.append(firstName).append(" is slightly bored.\n");
        }
        if (staff.teacherStatistics.getSleepState()) {
            sb.append(firstName).append(" is asleep!\n");
        } else {
            sb.append(firstName).append(" is not asleep.\n");
        }
        // Vision issues and corrective lenses
        if (staff.teacherStatistics.hasVisionIssue()) {
            String visionDescription = staff.teacherStatistics.getVisionIssueDescription();
            sb.append(firstName).append(" has ").append(visionDescription);
            if (staff.teacherStatistics.hasVisionCorrection()) {
                String correctionDesc = staff.teacherStatistics.getVisionCorrectionDescription();
                sb.append(", corrected with ").append(correctionDesc).append(".\n");
            } else {
                sb.append(" (uncorrected).\n");
            }
        } else {
            sb.append(firstName).append(" has normal vision.\n");
        }
        sb.append("They are assigned as: ").append(assignment).append("\n");
        sb.append("Teacher schedule is: ").append(teacherSchedule);

        inspectionArea.setText(sb.toString());
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
