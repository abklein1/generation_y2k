
public class Day {
    private int dayCounter;
    private String dayName;
    private static Day day;

    private Day(){
        this.dayCounter = 1;
        this.dayName = null;
    }

    public int getDayCounter() {
        return this.dayCounter;
    }

    public String getDayName() {
        return this.dayName;
    }

    public void setDayCounter() {
        this.dayCounter++;
    }

    public void setDayName() {
        if(dayCounter == 1 || dayCounter == 6 || dayCounter == 11){
            this.dayName = "Monday";
        } else if (dayCounter == 2 || dayCounter == 7 || dayCounter == 12){
            this.dayName = "Tuesday";
        } else if (dayCounter == 3 || dayCounter == 8 || dayCounter == 13){
            this.dayName = "Wednesday";
        } else if (dayCounter == 4 || dayCounter == 9 || dayCounter == 14){
            this.dayName = "Thursday";
        } else {
            this.dayName = "Friday";
        }
    }

    public static Day getInstance(){
        Day day = new Day();
        day.setDayName();

        return day;
    }
}
