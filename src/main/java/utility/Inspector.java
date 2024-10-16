package utility;

import entity.Rooms.Classroom;
import entity.Rooms.Room;
import entity.*;

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
    public static void studentInspection(Student student, JTextArea inspectionArea, SocialLinkConnector socialLinkConnector) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        StringBuilder sb = new StringBuilder();
        String firstName = student.studentName.getFirstName();
        String lastName = student.studentName.getLastName();
        String suffix = student.studentName.getSuffix();
        String gender = student.studentStatistics.getGender();
        //String race = student.studentStatistics.getRace();
        String hairColor = student.studentStatistics.getHairColor();
        String eyeColor = student.studentStatistics.getEyeColor();
        String skinColor = student.studentStatistics.getSkinColor();
        String hairLength = student.studentStatistics.getHairLength();
        String hairType = student.studentStatistics.getHairType();
        double height = student.studentStatistics.getHeight();
        String grade = student.studentStatistics.getGradeLevel();
        String income = student.studentStatistics.getIncomeLevel();
        LocalDate birth = student.studentStatistics.getBirthday();
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        List<String> siblingsNotInSchool = student.studentStatistics.getSiblingsNotInSchool();
        List<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();

        if (suffix != null) {
            sb.append(firstName).append(" ").append(lastName).append(" ").append(suffix).append("\n=====================================\n");
        } else {
            sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");
        }
        sb.append(firstName).append(" is a ").append(gender.toLowerCase()).append(" with ");
        sb.append(skinColor).append(" colored skin and ");
        sb.append(hairLength.toLowerCase()).append(", ").append(hairType.toLowerCase()).append(", ").append(hairColor.toLowerCase());
        sb.append(" hair and ").append(eyeColor.toLowerCase()).append(" eyes. ");
        sb.append("They stand ").append(df.format(height)).append(" inches tall.\n");
        sb.append(firstName).append(" is a ").append(grade).append(".\n");
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");
        sb.append("They have the following base stats:\n   INTELLIGENCE: ").append(student.studentStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(student.studentStatistics.getCharisma()).append("\n   AGILITY: ");
        sb.append(student.studentStatistics.getAgility()).append("\n   DETERMINATION: ").append(student.studentStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(student.studentStatistics.getPerception()).append("\n   STRENGTH: ");
        sb.append(student.studentStatistics.getStrength()).append("\n   LUCK: ").append(student.studentStatistics.getLuck()).append("\n");
        sb.append("   EXP: ").append(student.studentStatistics.getExperience()).append("\n");
        sb.append("They have the following secondary stats:\n   Creativity: ").append(student.studentStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(student.studentStatistics.getEmpathy()).append("\n   Adaptability: ");
        sb.append(student.studentStatistics.getAdaptability()).append("\n   Initiative: ").append(student.studentStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(student.studentStatistics.getResilience()).append("\n   Curiosity: ");
        sb.append(student.studentStatistics.getCuriosity()).append("\n   Responsibility: ").append(student.studentStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(student.studentStatistics.getOpenMindedness()).append("\n");
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
        sb.append("Their family has the following income: ").append(income).append("\n");
        if (!siblingsInSchool.isEmpty()) {
            sb.append("They have the following siblings in school: ").append("\n");
            for (Student sibling : siblingsInSchool) {
                sb.append(sibling.studentName.getFirstName()).append(" ").append(sibling.studentName.getLastName()).append("\n");
            }
        }
        if (!siblingsNotInSchool.isEmpty()) {
            sb.append("They have the following siblings not in school: ").append("\n");
            for (String sibling : siblingsNotInSchool) {
                sb.append(sibling).append("\n");
            }
        }
        for (StudentBlock block : schedule) {
            int blockNum = block.getBlockNumber();
            switch (blockNum) {
                case 1, 2 -> {
                    blockNum = 1;
                }
                case 3, 4 -> {
                    blockNum = 2;
                }
                case 5, 6 -> {
                    blockNum = 3;
                }
                case 7, 8 -> {
                    blockNum = 4;
                }
            }
            sb.append(block.getSemester()).append(" ").append(blockNum).append(" ").append(block.getTeacher()).append(" ").append(block.getClassName()).append("\n");
        }

        inspectionArea.setText(sb.toString());

        socialLinkConnector.studentVisualizer(student);
    }

    public static void studentInspection(Student student, JTextArea inspectionArea) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        StringBuilder sb = new StringBuilder();
        String firstName = student.studentName.getFirstName();
        String lastName = student.studentName.getLastName();
        String suffix = student.studentName.getSuffix();
        String gender = student.studentStatistics.getGender();
        //String race = student.studentStatistics.getRace();
        String hairColor = student.studentStatistics.getHairColor();
        String eyeColor = student.studentStatistics.getEyeColor();
        String skinColor = student.studentStatistics.getSkinColor();
        String hairLength = student.studentStatistics.getHairLength();
        String hairType = student.studentStatistics.getHairType();
        double height = student.studentStatistics.getHeight();
        String grade = student.studentStatistics.getGradeLevel();
        String income = student.studentStatistics.getIncomeLevel();
        LocalDate birth = student.studentStatistics.getBirthday();
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        List<String> siblingsNotInSchool = student.studentStatistics.getSiblingsNotInSchool();
        List<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();

        if (suffix != null) {
            sb.append(firstName).append(" ").append(lastName).append(" ").append(suffix).append("\n=====================================\n");
        } else {
            sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");
        }
        sb.append(firstName).append(" is a ").append(gender.toLowerCase()).append(" with ");
        sb.append(skinColor).append(" colored skin and ");
        sb.append(hairLength.toLowerCase()).append(", ").append(hairType.toLowerCase()).append(", ").append(hairColor.toLowerCase());
        sb.append(" hair and ").append(eyeColor.toLowerCase()).append(" eyes. ");
        sb.append("They stand ").append(df.format(height)).append(" inches tall.\n");
        sb.append(firstName).append(" is a ").append(grade).append(".\n");
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");
        sb.append("They have the following base stats:\n   INTELLIGENCE: ").append(student.studentStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(student.studentStatistics.getCharisma()).append("\n   AGILITY: ");
        sb.append(student.studentStatistics.getAgility()).append("\n   DETERMINATION: ").append(student.studentStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(student.studentStatistics.getPerception()).append("\n   STRENGTH: ");
        sb.append(student.studentStatistics.getStrength()).append("\n   LUCK: ").append(student.studentStatistics.getLuck()).append("\n");
        sb.append("   EXP: ").append(student.studentStatistics.getExperience()).append("\n");
        sb.append("They have the following secondary stats:\n   Creativity: ").append(student.studentStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(student.studentStatistics.getEmpathy()).append("\n   Adaptability: ");
        sb.append(student.studentStatistics.getAdaptability()).append("\n   Initiative: ").append(student.studentStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(student.studentStatistics.getResilience()).append("\n   Curiosity: ");
        sb.append(student.studentStatistics.getCuriosity()).append("\n   Responsibility: ").append(student.studentStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(student.studentStatistics.getOpenMindedness()).append("\n");
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
        sb.append("Their family has the following income: ").append(income).append("\n");
        if (!siblingsInSchool.isEmpty()) {
            sb.append("They have the following siblings in school: ").append("\n");
            for (Student sibling : siblingsInSchool) {
                sb.append(sibling.studentName.getFirstName()).append(" ").append(sibling.studentName.getLastName()).append("\n");
            }
        }
        if (!siblingsNotInSchool.isEmpty()) {
            sb.append("They have the following siblings not in school: ").append("\n");
            for (String sibling : siblingsNotInSchool) {
                sb.append(sibling).append("\n");
            }
        }
        for (StudentBlock block : schedule) {
            int blockNum = block.getBlockNumber();
            switch (blockNum) {
                case 1, 2 -> {
                    blockNum = 1;
                }
                case 3, 4 -> {
                    blockNum = 2;
                }
                case 5, 6 -> {
                    blockNum = 3;
                }
                case 7, 8 -> {
                    blockNum = 4;
                }
            }
            sb.append(block.getSemester()).append(" ").append(blockNum).append(" ").append(block.getTeacher()).append(" ").append(block.getClassName()).append("\n");
        }

        inspectionArea.setText(sb.toString());
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

        sb.append("They stand ").append(df.format(height)).append(" inches tall.\n");
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");
        sb.append("They have the following stats:\n   INTELLIGENCE: ").append(staff.teacherStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(staff.teacherStatistics.getCharisma()).append("\n   AGILITY: ");
        sb.append(staff.teacherStatistics.getAgility()).append("\n   DETERMINATION: ").append(staff.teacherStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(staff.teacherStatistics.getPerception()).append("\n   STRENGTH: ");
        sb.append(staff.teacherStatistics.getStrength()).append("\n");
        sb.append("   LUCK: ").append(staff.teacherStatistics.getLuck()).append("\n");
        sb.append("They have the following secondary stats:\n   Creativity: ").append(staff.teacherStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(staff.teacherStatistics.getEmpathy()).append("\n   Adaptability: ");
        sb.append(staff.teacherStatistics.getAdaptability()).append("\n   Initiative: ").append(staff.teacherStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(staff.teacherStatistics.getResilience()).append("\n   Curiosity: ");
        sb.append(staff.teacherStatistics.getCuriosity()).append("\n   Responsibility: ").append(staff.teacherStatistics.getResponsibility());
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
        sb.append("They are assigned as: ").append(assignment).append("\n");
        sb.append("Teacher schedule is: ").append(teacherSchedule);

        inspectionArea.setText(sb.toString());
    }

    public static String gradeClassInspection(HashMap<Integer, Student> studentGradeClass) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Student> entry : studentGradeClass.entrySet()) {
            Student student = entry.getValue();
            sb.append(student.studentName.getFirstName()).append(" ").append(student.studentName.getLastName()).append("\n");
        }
        return sb.toString();
    }

    public static String staffListInspection(HashMap<Integer, Staff> staffHashMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            Staff staff = entry.getValue();
            sb.append(staff.teacherName.getFirstName()).append(" ").append(staff.teacherName.getLastName()).append("\n");
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
            roomDetails.append(value.teacherName.getFirstName()).append(" ").append(value.teacherName.getLastName()).append("\n");
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
                    Student student = seatingArrangements.get(1)[row][col];
                    JTextArea studentInfoArea = new JTextArea();
                    studentInspection(student, studentInfoArea);
                    JOptionPane.showMessageDialog(null, new JScrollPane(studentInfoArea), "Student Details", JOptionPane.INFORMATION_MESSAGE);
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
                            tableModel.setValueAt(seats[row][col].studentName.getFirstName() + " " + seats[row][col].studentName.getLastName(), row, col);
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
                        studentListArea.append(student.studentName.getFirstName());
                        studentListArea.append(" ");
                        studentListArea.append(student.studentName.getLastName());
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
