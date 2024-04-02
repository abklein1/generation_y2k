package utility;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class BirthdayGenerator {

    public static LocalDate generateDateFromClass(String gradClass) {
        LocalDate start = LocalDate.of(1900,1,1);
        LocalDate end = LocalDate.of(1900,1,2);
        long startEpochDay;
        long endEpochDay;
        long randomDate;

        switch (gradClass) {
            case "Freshman":
                start = LocalDate.of(1989,9,1);
                end = LocalDate.of(1990,8,31);
                break;
            case "Sophomore":
                start = LocalDate.of(1988,9,1);
                end = LocalDate.of(1989,8,31);
                break;
            case "Junior":
                start = LocalDate.of(1987,9,1);
                end = LocalDate.of(1988,8,31);
                break;
            case "Senior":
                start = LocalDate.of(1986,9,1);
                end = LocalDate.of(1987,8,31);
                break;
        }
        startEpochDay = start.toEpochDay();
        endEpochDay = end.toEpochDay();
        randomDate = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDate);
    }

    public static LocalDate generateRandomBirthdayStaff() {
        LocalDate start = LocalDate.of(1940, 1,1);
        LocalDate end = LocalDate.of(1981,1,1);
        long startEpochDay = start.toEpochDay();
        long endEpochDay = end.toEpochDay();
        long randomDate = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDate);
    }
}
