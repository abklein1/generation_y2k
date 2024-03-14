package utility;

import entity.Student;
import entity.Staff;

public class Inspector {
    public static void studentInspection(Student student) {
        String fir = student.studentName.getFirstName();
        String las = student.studentName.getLastName();
        String h = student.studentStatistics.getHairColor();
        String e = student.studentStatistics.getEyeColor();
        int hei = student.studentStatistics.getHeight();
        int in = student.studentStatistics.getIntelligence();
        int chr = student.studentStatistics.getCharisma();
        int agl = student.studentStatistics.getAgility();
        int det = student.studentStatistics.getDetermination();
        int str = student.studentStatistics.getStrength();
        int bor = student.studentStatistics.getBoredom();
        boolean slp = student.studentStatistics.getSleepState();
        int exp = student.studentStatistics.getExperience();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " has " + h + " hair and " + e + " eyes. They are " + hei + " inches tall.");
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
        String fir = staff.teacherName.getFirstName();
        String las = staff.teacherName.getLastName();
        String h = staff.teacherStatistics.getHairColor();
        String e = staff.teacherStatistics.getEyeColor();
        int hei = staff.teacherStatistics.getHeight();
        int in = staff.teacherStatistics.getIntelligence();
        int chr = staff.teacherStatistics.getCharisma();
        int agl = staff.teacherStatistics.getAgility();
        int det = staff.teacherStatistics.getDetermination();
        int str = staff.teacherStatistics.getStrength();
        int bor = staff.teacherStatistics.getBoredom();
        boolean slp = staff.teacherStatistics.getSleepState();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " has " + h + " hair and " + e + " eyes. They are " + hei + " inches tall.");
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
}
