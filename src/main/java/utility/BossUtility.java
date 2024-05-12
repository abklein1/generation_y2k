package utility;

import entity.Boss;
import entity.Student;
import entity.Time;

import static utility.Randomizer.setRandom;

public class BossUtility {
    //What type of boss will we get today
    public static int bossDecision(Time time) {
        if (time.getDayName().equals("Monday") || time.getDayName().equals("Tuesday") || time.getDayName().equals("Thursday")) {
            System.out.println("Today all students will have to face homework!");
            return 1;
        } else if (time.getDayName().equals("Wednesday")) {
            System.out.println("Today all students will have to be ready for a quiz!");
            return 2;
        } else if (time.getDayName().equals("Friday")) {
            System.out.println("Today the students better be ready for the dreaded exam!");
            return 3;
        } else {
            System.out.println("Today is the weekend. No school!");
            return 4;
        }
    }

    //TODO: Revise stat vs grade system to create better dist of grades/abilities
    //The showdown between a student and exam/quiz/homework
    public static void dungeonFight(Student student, Boss boss) {
        int finalGrade = 0;
        int bossStat = 0;
        int bossHP = 0;
        double studentAtk = 0;
        double result = 0;
        int expGain = 0;
        //Start to calculate entity.Boss HP
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
            //entity.Student got an A
            finalGrade = setRandom(90, 100);
            student.studentStatistics.setExperience(15);
            //Chance for stat boost
            if (finalGrade == 100) {
                student.studentStatistics.setBoredom(0);
                student.studentStatistics.setDetermination(student.studentStatistics.getDetermination() + 1);
                student.studentStatistics.setExperience(20);
            }
        } else if (result >= .5 && result <= 1) {
            //entity.Student got a B
            finalGrade = setRandom(80, 89);
            student.studentStatistics.setExperience(12);
        } else if (result >= 4 && result <= 6) {
            //entity.Student got a C
            finalGrade = setRandom(70, 79);
            student.studentStatistics.setExperience(9);

        } else if (result >= 7 && result <= 8) {
            //entity.Student got a D
            finalGrade = setRandom(60, 69);
            student.studentStatistics.setExperience(5);
        } else if (result >= 9) {
            //entity.Student got an F
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
