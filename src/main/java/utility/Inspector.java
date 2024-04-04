package utility;

import entity.Student;
import entity.Staff;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Inspector {
    public static void studentInspection(Student student) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        String fir = student.studentName.getFirstName();
        String las = student.studentName.getLastName();
        String h = student.studentStatistics.getHairColor();
        String e = student.studentStatistics.getEyeColor();
        double hei = student.studentStatistics.getHeight();
        int in = student.studentStatistics.getIntelligence();
        int chr = student.studentStatistics.getCharisma();
        int agl = student.studentStatistics.getAgility();
        int det = student.studentStatistics.getDetermination();
        int str = student.studentStatistics.getStrength();
        int bor = student.studentStatistics.getBoredom();
        boolean slp = student.studentStatistics.getSleepState();
        int exp = student.studentStatistics.getExperience();
        String grade = student.studentStatistics.getGradeLevel();
        LocalDate birth = student.studentStatistics.getBirthday();
        String gen = student.studentStatistics.getGender();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " is a " + gen + " and has " + h + " hair and " + e + " eyes. They are " + df.format(hei) + " inches tall.");
        System.out.println(fir + " is a " + grade + ".");
        System.out.println(fir + " was born on " + birth);
        System.out.println("They have the following stats: ");
        System.out.println("   INT: " + in);
        System.out.println("   CHR: " + chr);
        System.out.println("   AGL: " + agl);
        System.out.println("   DET: " + det);
        System.out.println("   STR: " + str);
        System.out.println("   EXP: " + exp);
        System.out.println(fir + " has the following status effects: ");
        if (bor == 0) {
            System.out.println(fir + " is not bored");
        } else {
            System.out.println(fir + " is slightly bored");
        }
        if (slp) {
            System.out.println(fir + " is asleep!");
        } else {
            System.out.println(fir + " is not asleep");
        }
        System.out.println("Nice to meet you " + fir + "!");
    }

    public static void staffInspection(Staff staff) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        String fir = staff.teacherName.getFirstName();
        String las = staff.teacherName.getLastName();
        String h = staff.teacherStatistics.getHairColor();
        String e = staff.teacherStatistics.getEyeColor();
        double hei = staff.teacherStatistics.getHeight();
        int in = staff.teacherStatistics.getIntelligence();
        int chr = staff.teacherStatistics.getCharisma();
        int agl = staff.teacherStatistics.getAgility();
        int det = staff.teacherStatistics.getDetermination();
        int str = staff.teacherStatistics.getStrength();
        int bor = staff.teacherStatistics.getBoredom();
        boolean slp = staff.teacherStatistics.getSleepState();
        LocalDate birth = staff.teacherStatistics.getBirthday();
        String gen = staff.teacherStatistics.getGender();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " is a " + gen + " and has " + h + " hair and " + e + " eyes. They are " + df.format(hei) + " inches tall.");
        System.out.println(fir + " was born on " + birth);
        System.out.println("They have the following stats: ");
        System.out.println("   INT: " + in);
        System.out.println("   CHR: " + chr);
        System.out.println("   AGL: " + agl);
        System.out.println("   DET: " + det);
        System.out.println("   STR: " + str);
        System.out.println(fir + " has the following status effects: ");
        if (bor == 0) {
            System.out.println(fir + " is not bored");
        } else {
            System.out.println(fir + " is slightly bored");
        }
        if (slp) {
            System.out.println(fir + " is asleep!");
        } else {
            System.out.println(fir + " is not asleep");
        }
        System.out.println("Nice to meet you " + fir + "!");
    }

    public static void gradeClassInspection(HashMap<Integer, Student> studentGradeClass) {
        for(Map.Entry<Integer, Student> entry : studentGradeClass.entrySet()) {
            System.out.println(entry.getValue().studentName.getFirstName() + " " + entry.getValue().studentName.getLastName());
        }
    }
}
