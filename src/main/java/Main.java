/*
    File: Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program

 */


import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        //Create hash maps for storage
        HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
        HashMap<Integer, Staff> staffHashMap = new HashMap<Integer, Staff>();
        HashMap<Integer, String> fNameReference = new HashMap<Integer, String>();
        HashMap<Integer, String> lNameReference = new HashMap<Integer, String>();
        int student_cap;
        int staff_cap;
        int dungeon;
        //Generate a new standard school with rooms
        System.out.println("Starting by generating the school");
        StandardSchool standardSchool = new StandardSchool();
        Director director = new Director(standardSchool);
        //Pull capacities
        student_cap = standardSchool.getTotalStudentCapacity();
        staff_cap = standardSchool.getMinimumStaffRequirements();

        System.out.println("Student capacity is " + student_cap);
        System.out.println("Staff capacity is " + staff_cap);

        System.out.println("Populating school...");
        //Store student objects in hashmap
        for (Integer i = 0; i < student_cap; i++) {
            studentHashMap.put(i, new Student());
        }
        //Store staff objects in another hashmap
        for (Integer j = 0; j < staff_cap; j++) {
            staffHashMap.put(j, new Staff());
        }
        //Set for student randomization
        System.out.println("Randomizing " + student_cap + " students...");
        fNameReference.putAll(NameLoader.readCSVFirst(true));
        lNameReference.putAll(NameLoader.readCSVLast(true));
        for (Integer k = 0; k < student_cap; k++) {
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() - 1));
            System.out.println("   Generating student " + f_name + " " + l_name);
            Student student = studentHashMap.get(k);
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(l_name);
            student.studentStatistics.setHairColor(hairSelection(setRandom(0, 102)));
            student.studentStatistics.setEyeColor(eyeSelection(setRandom(0, 109)));
            student.studentStatistics.setHeight(setRandom(48, 78));
            student.studentStatistics.setIntelligence(setRandom(0, 15));
            student.studentStatistics.setCharisma(setRandom(0, 15));
            student.studentStatistics.setAgility(setRandom(0, 15));
            student.studentStatistics.setDetermination(setRandom(0, 15));
            student.studentStatistics.setStrength(setRandom(0, 15));
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
        }
        //Clear map for new values
        fNameReference.clear();
        lNameReference.clear();
        //Set for staff randomization
        System.out.println("Randomizing " + staff_cap + " staff");
        fNameReference.putAll(NameLoader.readCSVFirst(false));
        lNameReference.putAll(NameLoader.readCSVLast(false));
        for (Integer l = 0; l < staff_cap; l++) {
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() - 1));
            System.out.println("   Generating staff " + f_name + " " + l_name);
            Staff staff = staffHashMap.get(l);
            staff.teacherName.setFirstName(f_name);
            staff.teacherName.setLastName(l_name);
            staff.teacherStatistics.setHairColor(hairSelection(setRandom(0, 102)));
            staff.teacherStatistics.setEyeColor(eyeSelection(setRandom(0, 109)));
            staff.teacherStatistics.setHeight(setRandom(50, 84));
            staff.teacherStatistics.setIntelligence(setRandom(2, 22));
            staff.teacherStatistics.setCharisma(setRandom(1, 22));
            staff.teacherStatistics.setAgility(setRandom(1, 19));
            staff.teacherStatistics.setDetermination(setRandom(1, 20));
            staff.teacherStatistics.setStrength(setRandom(1, 20));
        }
        System.out.println("Done creating school and students");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        //Introduce me to random student
        System.out.println("Introduce me to a random student, please.");
        studentInspection(studentHashMap.get(setRandom(0, studentHashMap.size() - 1)));
        //Introduce me to a random teacher
        System.out.println("Ok now introduce me to a random teacher, please.");
        staffInspection(staffHashMap.get(setRandom(0, staffHashMap.size() - 1)));
        //This is the first Monday of school
        System.out.println("Alright time to get on with the day then...");
        Day day = Day.getInstance();
        System.out.println("Today is " + day.getDayName());
        System.out.println("Each day might present a new challenge that every student must face");
        Homework first_homework = new Homework();
        //Each student must face the first boss
        for (Integer m = 0; m < student_cap; m++) {
            dungeonFight(studentHashMap.get(m), first_homework);
        }
        System.out.println("Alright let's see how a random student did...");
        Student day1 = studentHashMap.get(setRandom(0, studentHashMap.size() - 1));
        System.out.println("Hey " + day1.studentName.getFirstName());
        System.out.println("Ok so " + day1.studentName.getFirstName() + " got a " + day1.studentStatistics.getGradeAverage());
        System.out.println("Well tomorrow's a new day. Let's simulate another 10 of them!");
        day.setDayCounter();
        for (int n = 0; n < 9; n++) {
            System.out.println("Today is " + day.getDayName());
            dungeon = bossDecision(day);
            if (dungeon == 1) {
                Homework homework = new Homework();
                for (Integer o = 0; o < student_cap; o++) {
                    dungeonFight(studentHashMap.get(o), homework);
                }
                day.setDayName();
                day.setDayCounter();
            } else if (dungeon == 2) {
                Quiz quiz = new Quiz();
                for (Integer p = 0; p < student_cap; p++) {
                    dungeonFight(studentHashMap.get(p), quiz);
                }
                day.setDayName();
                day.setDayCounter();
            } else {
                Exam exam = new Exam();
                for (Integer q = 0; q < student_cap; q++) {
                    dungeonFight(studentHashMap.get(q), exam);
                }
                day.setDayName();
                day.setDayCounter();
            }
        }
        System.out.println("Now let's check on a random student...");
        Student lastStudent = studentHashMap.get(setRandom(0, studentHashMap.size() - 1));
        studentInspection(lastStudent);
        System.out.println("So " + lastStudent.studentName.getFirstName() + " you have the following grades:");
        lastStudent.studentStatistics.getAllGrades();
        System.out.println(lastStudent.studentName.getFirstName() + "'s average was " + lastStudent.studentStatistics.getGradeAverage());

    }

    //Not ideal design but need to add a few helpers here for simulation
    private static Integer setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

    private static String hairSelection(int selection) {
        if (selection >= 0 && selection <= 21) {
            return "Black";
        } else if (selection >= 22 && selection <= 37) {
            return "Dark Brown";
        } else if (selection >= 38 && selection <= 48) {
            return "Medium Brown";
        } else if (selection >= 49 && selection <= 56) {
            return "Light Brown";
        } else if (selection >= 57 && selection <= 64) {
            return "Blond";
        } else if (selection >= 65 && selection <= 71) {
            return "Chestnut";
        } else if (selection >= 72 && selection <= 78) {
            return "Mahogany";
        } else if (selection >= 79 && selection <= 84) {
            return "Dirty Blond";
        } else if (selection >= 85 && selection <= 89) {
            return "Golden Blond";
        } else if (selection >= 90 && selection <= 93) {
            return "Light Blond";
        } else if (selection >= 94 && selection < 96) {
            return "Golden Brown";
        } else if (selection == 97) {
            return "Caramel";
        } else if (selection == 98) {
            return "Strawberry Blond";
        } else if (selection == 99) {
            return "Copper";
        } else if (selection == 100) {
            return "Red";
        } else if (selection == 101) {
            return "Platinum Blond";
        } else {
            int random = setRandom(0, 6);
            if (random == 0) {
                return "Auburn";
            } else if (random == 1) {
                return "Amber";
            } else if (random == 2) {
                return "Titian";
            } else if (random == 3) {
                return "White";
            } else if (random == 4) {
                return "No";
            } else if (random == 5) {
                return "Gray";
            } else {
                return "Champagne";
            }
        }
    }

    private static String eyeSelection(int selection) {
        if (selection >= 0 && selection <= 52) {
            return "Dark Brown";
        } else if (selection >= 53 && selection <= 75) {
            return "Light Brown";
        } else if (selection >= 76 && selection <= 83) {
            return "Blue";
        } else if (selection >= 84 && selection <= 90) {
            return "Light Blue";
        } else if (selection >= 91 && selection <= 96) {
            return "Hazel";
        } else if (selection >= 97 && selection <= 102) {
            return "Amber";
        } else if (selection >= 103 && selection <= 105) {
            return "Green";
        } else if (selection == 106) {
            return "Gray";
        } else if (selection == 107) {
            return "Violet";
        } else if (selection == 108) {
            return "Black";
        } else {
            return "Heterochromia";
        }
    }

    private static void studentInspection(Student student) {
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


    private static void staffInspection(Staff staff) {
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

    private static int bossDecision(Day day) {
        if (day.getDayCounter() == 1 || day.getDayCounter() == 2 || day.getDayCounter() == 4 || day.getDayCounter() == 6 || day.getDayCounter() == 7 || day.getDayCounter() == 9) {
            System.out.println("Today all students will have to face homework!");
            return 1;
        } else if (day.getDayCounter() == 3 || day.getDayCounter() == 8 || day.getDayCounter() == 13) {
            System.out.println("Today all students will have to be ready for a quiz!");
            return 2;
        } else {
            System.out.println("Today the students better be ready for the dreaded exam!");
            return 3;
        }
    }

    private static void dungeonFight(Student student, Boss boss) {
        int finalGrade = 0;
        int bossStat = 0;
        int bossHP = 0;
        double studentAtk = 0;
        double result = 0;
        int expGain = 0;
        //Start to calculate Boss HP
        bossStat = boss.getStatsNumberOfQuestions() / boss.getStatsTime();
        bossHP = bossStat * boss.getStatsDifficulty();
        //Calculate student attack power
        studentAtk = student.studentStatistics.getIntelligence() * (student.studentStatistics.getDetermination() - student.studentStatistics.getBoredom() * .10);
        //Run the fight
        result = bossHP / studentAtk;

        //if student is asleep random chance to wake back up before test based on determination
        if (student.studentStatistics.getSleepState()) {
            int chance = setRandom(0, 10) * student.studentStatistics.getDetermination();
            if (chance >= 50) {
                student.studentStatistics.setSleepState(false);
                student.studentStatistics.setBoredom(0);
            }
        }
        if (result <= 0.5) {
            //Student got an A
            finalGrade = setRandom(90, 100);
            student.studentStatistics.setExperience(15);
            //Chance for stat boost
            if (finalGrade == 100) {
                student.studentStatistics.setBoredom(0);
                student.studentStatistics.setDetermination(student.studentStatistics.getDetermination() + 1);
                student.studentStatistics.setExperience(20);
            }
        } else if (result >= .5 && result <= 1) {
            //Student got a B
            finalGrade = setRandom(80, 89);
            student.studentStatistics.setExperience(12);
        } else if (result >= 4 && result <= 6) {
            //Student got a C
            finalGrade = setRandom(70, 79);
            student.studentStatistics.setExperience(9);

        } else if (result >= 7 && result <= 8) {
            //Student got a D
            finalGrade = setRandom(60, 69);
            student.studentStatistics.setExperience(5);
        } else if (result >= 9) {
            //Student got an F
            finalGrade = setRandom(0, 59);
            //Chance for boredom to set in
            if (finalGrade < 3) {
                student.studentStatistics.setBoredom(student.studentStatistics.getBoredom() + 1);
            }
            if (finalGrade < 2) {
                student.studentStatistics.setSleepState(true);
            }
            student.studentStatistics.setExperience(2);
        }
        //Record student grade
        student.studentStatistics.setNewGrade(finalGrade);
        student.studentStatistics.setGradeAverage();
    }

}