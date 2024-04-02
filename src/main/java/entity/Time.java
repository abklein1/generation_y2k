package entity;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {
    private final Calendar calendar;
    private final Format f;
    private int dayCounter;
    private final SimpleDateFormat simpleDateFormat;
    public Time() {
        this.dayCounter = 1;
        this.calendar = Calendar.getInstance();
        this.calendar.set(Calendar.YEAR, 2004);
        this.calendar.set(Calendar.MONTH, 7);
        this.calendar.set(Calendar.DATE, 23);
        this.simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm aaa");
        this.f = new SimpleDateFormat("EEEE");
    }

    public String getDayName() {
        return f.format(calendar.getTime());
    }

    public String getFormattedDate() {
        return simpleDateFormat.format(calendar.getTime());
    }

    public void incrementDayCounter() {
        dayCounter++;
        calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    public int getDayCounter() {
        return dayCounter;
    }
}
