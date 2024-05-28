package utility;

import entity.Rooms.Classroom;
import entity.Rooms.Room;
import entity.Staff;
import entity.Student;

import javax.swing.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Inspector {
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
        sb.append("Their family has the following income: " ).append(income).append("\n");

        inspectionArea.setText(sb.toString());
    }

    public static void staffInspection(Staff staff, JTextArea inspectionArea) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();
        String gender = staff.teacherStatistics.getGender().toLowerCase();
        String hairColor = staff.teacherStatistics.getHairColor().toLowerCase();
        String hairLength = staff.teacherStatistics.getHairLength().toLowerCase();
        String hairType = staff.teacherStatistics.getHairType().toLowerCase();
        String eyeColor = staff.teacherStatistics.getEyeColor().toLowerCase();
        double height = staff.teacherStatistics.getHeight();
        LocalDate birth = staff.teacherStatistics.getBirthday();
        String assignment = staff.teacherStatistics.getStaffType().toString().toLowerCase();

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");
        sb.append(firstName).append(" is a ").append(gender).append(" with ");
        sb.append(hairLength).append(", ").append(hairType).append(", ").append(hairColor);
        sb.append(" hair and ").append(eyeColor).append(" eyes. They stand ");
        sb.append(df.format(height)).append(" inches tall.\n");
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

    public static void findHighestStudent(HashMap<Integer, Student> studentHashMap, JTextArea inspectionArea) {
        Student temp;
        Student high = null;
        int total;
        int top = 0;

        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            temp = entry.getValue();
            total = temp.studentStatistics.getIntelligence() + temp.studentStatistics.getCharisma()
                    + temp.studentStatistics.getAgility() + temp.studentStatistics.getDetermination()
                    + temp.studentStatistics.getPerception() + temp.studentStatistics.getStrength();
            if (total > top) {
                top = total;
                high = temp;
            }
        }

        assert high != null;
        studentInspection(high, inspectionArea);
    }

    public static void findLowestStudent(HashMap<Integer, Student> studentHashMap, JTextArea inspectionArea) {
        Student temp;
        Student low = null;
        int total;
        int bottom = 1000;

        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            temp = entry.getValue();
            total = temp.studentStatistics.getIntelligence() + temp.studentStatistics.getCharisma()
                    + temp.studentStatistics.getAgility() + temp.studentStatistics.getDetermination()
                    + temp.studentStatistics.getPerception() + temp.studentStatistics.getStrength();
            if (total < bottom) {
                bottom = total;
                low = temp;
            }
        }

        assert low != null;
        studentInspection(low, inspectionArea);
    }

    public static void inspectRoom(Room room) {
        String roomName = room.getRoomName();
        List<Staff> staff = room.getAssignedStaff();
        int studentCap = room.getStudentCapacity();

        StringBuilder roomDetails = new StringBuilder();
        roomDetails.append("Welcome to ").append(roomName).append("\n");
        roomDetails.append("The room contains the following staff:\n");
        for (Staff value : staff) {
            roomDetails.append(value.teacherName.getFirstName()).append(" ").append(value.teacherName.getLastName()).append("\n");
        }
        roomDetails.append("It has a student capacity of ").append(studentCap).append("\n");
        if (room instanceof Classroom) {
            String abbrev = ((Classroom) room).getClassRoomType();
            roomDetails.append("It is a classroom of type: ").append(abbrev).append("\n");
        }

        JOptionPane.showMessageDialog(null, roomDetails.toString(), "Room Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
