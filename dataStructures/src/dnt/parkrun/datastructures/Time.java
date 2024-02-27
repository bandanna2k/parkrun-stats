package dnt.parkrun.datastructures;

public class Time
{
    public static final int NO_TIME_SECONDS = 0;
    public static final Time NO_TIME = new Time(0, 0, 0);

    public final int hours;
    public final int mins;
    public final int seconds;

    private Time(int hours, int mins, int seconds)
    {
        this.hours = hours;
        this.mins = mins;
        this.seconds = seconds;
    }

    @Override
    public String toString()
    {
        return "Time{" +
                "hours=" + hours +
                ", mins=" + mins +
                ", seconds=" + seconds +
                '}';
    }

    public static Time fromString(String time)
    {
        if(null == time)
        {
            return NO_TIME;
        }

        String trimmedTime = time.trim();
        int indexOf = trimmedTime.indexOf(":");
        int lastIndexOf = trimmedTime.lastIndexOf(":");
        if(indexOf == -1 && lastIndexOf == -1)
        {
            return NO_TIME;
        }
        try
        {
            if(indexOf == lastIndexOf)
            {
                int mins = Integer.parseInt(trimmedTime.substring(0, 2));
                int seconds = Integer.parseInt(trimmedTime.substring(3, 5));
                return new Time(0, mins, seconds);
            }
            else
            {
                int hours = Integer.parseInt(trimmedTime.substring(0, indexOf));
                int mins = Integer.parseInt(trimmedTime.substring(indexOf + 1, lastIndexOf));
                int seconds = Integer.parseInt(trimmedTime.substring(lastIndexOf + 1, lastIndexOf + 3));
                return new Time(hours, mins, seconds);
            }
        }
        catch(NumberFormatException ex)
        {
            return NO_TIME;
        }
    }

    public long getTotalSeconds()
    {
        return (hours * 3600) + (mins * 60) + seconds;
    }
}
