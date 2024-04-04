package utility;

import entity.Staff;
import entity.Student;

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
        int per = student.studentStatistics.getPerception();
        int str = student.studentStatistics.getStrength();
        int cre = student.studentStatistics.getCreativity();
        int emp = student.studentStatistics.getEmpathy();
        int adp = student.studentStatistics.getAdaptability();
        int ini = student.studentStatistics.getInitiative();
        int res = student.studentStatistics.getResilience();
        int cur = student.studentStatistics.getCuriosity();
        int rsp = student.studentStatistics.getResponsibility();
        int opm = student.studentStatistics.getOpenMindedness();
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
        System.out.println("They have the following base stats: ");
        System.out.println("   INTELLIGENCE: " + in);
        System.out.println("   CHARISMA: " + chr);
        System.out.println("   AGILITY: " + agl);
        System.out.println("   DETERMINATION: " + det);
        System.out.println("   PERCEPTION: " + per);
        System.out.println("   STRENGTH: " + str);
        System.out.println("   EXP: " + exp);
        System.out.println("They have the following secondary stats: ");
        System.out.println("   Creativity: " + cre);
        System.out.println("   Empathy: " + emp);
        System.out.println("   Adaptability: " + adp);
        System.out.println("   Initiative: " + ini);
        System.out.println("   Resilience: " + res);
        System.out.println("   Curiosity: " + cur);
        System.out.println("   Responsibility: " + rsp);
        System.out.println("   Open-Mindedness: " + opm);
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
        int per = staff.teacherStatistics.getPerception();
        int str = staff.teacherStatistics.getStrength();
        int cre = staff.teacherStatistics.getCreativity();
        int emp = staff.teacherStatistics.getEmpathy();
        int adp = staff.teacherStatistics.getAdaptability();
        int ini = staff.teacherStatistics.getInitiative();
        int res = staff.teacherStatistics.getResilience();
        int cur = staff.teacherStatistics.getCuriosity();
        int rsp = staff.teacherStatistics.getResponsibility();
        int opm = staff.teacherStatistics.getOpenMindedness();
        int bor = staff.teacherStatistics.getBoredom();
        boolean slp = staff.teacherStatistics.getSleepState();
        LocalDate birth = staff.teacherStatistics.getBirthday();
        String gen = staff.teacherStatistics.getGender();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " is a " + gen + " and has " + h + " hair and " + e + " eyes. They are " + df.format(hei) + " inches tall.");
        System.out.println(fir + " was born on " + birth);
        System.out.println("They have the following stats: ");
        System.out.println("   INTELLIGENCE: " + in);
        System.out.println("   CHARISMA: " + chr);
        System.out.println("   AGILITY: " + agl);
        System.out.println("   DETERMINATION: " + det);
        System.out.println("   PERCEPTION: " + per);
        System.out.println("   STRENGTH: " + str);
        System.out.println("They have the following secondary stats: ");
        System.out.println("   Creativity: " + cre);
        System.out.println("   Empathy: " + emp);
        System.out.println("   Adaptability: " + adp);
        System.out.println("   Initiative: " + ini);
        System.out.println("   Resilience: " + res);
        System.out.println("   Curiosity: " + cur);
        System.out.println("   Responsibility: " + rsp);
        System.out.println("   Open-Mindedness: " + opm);
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
        for (Map.Entry<Integer, Student> entry : studentGradeClass.entrySet()) {
            System.out.println(entry.getValue().studentName.getFirstName() + " " + entry.getValue().studentName.getLastName());
        }
    }

    public static void findHighestStudent(HashMap<Integer, Student> studentHashMap) {
        Student temp;
        Student high = null;
        int total;
        int top = 0;

        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            temp = entry.getValue();
            total = temp.studentStatistics.getIntelligence() + temp.studentStatistics.getCharisma()
                    + temp.studentStatistics.getAgility() + temp.studentStatistics.getDetermination()
                    + temp.studentStatistics.getPerception() + temp.studentStatistics.getStrength();
            if(total > top) {
                top = total;
                high = temp;
            }
        }

        studentInspection(high);
    }

    public static void findLowestStudent(HashMap<Integer, Student> studentHashMap) {
        Student temp;
        Student low = null;
        int total;
        int bottom = 1000;

        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            temp = entry.getValue();
            total = temp.studentStatistics.getIntelligence() + temp.studentStatistics.getCharisma()
                    + temp.studentStatistics.getAgility() + temp.studentStatistics.getDetermination()
                    + temp.studentStatistics.getPerception() + temp.studentStatistics.getStrength();
            if(total < bottom) {
                bottom = total;
                low = temp;
            }
        }

        studentInspection(low);
    }
}
