package dnt.parkrun.common;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.SATURDAY;

public abstract class ParkrunDay
{
    /*
    From today, get the most recent parkrun date (in the past)
     */
    public static Date getParkrunDay(Date date)
    {
        Calendar calResult = Calendar.getInstance();
        calResult.setTime(date);

        for (int i = 0; i < 7; i++)
        {
            if (calResult.get(Calendar.DAY_OF_WEEK) == SATURDAY)
            {
                calResult.set(Calendar.HOUR_OF_DAY, 0);
                calResult.set(Calendar.MINUTE, 0);
                calResult.set(Calendar.SECOND, 0);
                calResult.set(Calendar.MILLISECOND, 0);
                return calResult.getTime();
            }
            calResult.add(Calendar.DAY_OF_MONTH, -1);
        }
        throw new UnsupportedOperationException();
    }
}
