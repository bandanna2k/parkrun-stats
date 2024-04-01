package dnt.parkrun.datastructures;

public class Time
{
    public static final int NO_TIME_SECONDS = 0;
    public static final Time NO_TIME = new Time(0, 0, 0);

    public final int hours;
    public final int mins;
    public final int seconds;

    Time(int hours, int mins, int seconds)
    {
        if(hours < 0 || hours >= 100) throw new IllegalArgumentException();
        if(mins < 0 || mins >= 60) throw new IllegalArgumentException();
        if(seconds < 0 || seconds >= 60) throw new IllegalArgumentException();
        this.hours = hours;
        this.mins = mins;
        this.seconds = seconds;
    }

    @Override
    public String toString()
    {
        if(hours > 0)
        {
            return String.format("%d:%02d:%02d", hours, mins, seconds);
        }
        else
        {
            return String.format("%02d:%02d", mins, seconds);
        }
    }

    public static Time from(final int totalSeconds)
    {
        int runningTotalSeconds = totalSeconds;
        int hours = runningTotalSeconds / 3600;
        runningTotalSeconds -= (hours * 3600);
        int mins = runningTotalSeconds / 60;
        runningTotalSeconds -= (mins * 60);
        return new Time(hours, mins, runningTotalSeconds);
    }

    public static Time from(String time)
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

    public int getTotalSeconds()
    {
        return (hours * 3600) + (mins * 60) + seconds;
    }
}
