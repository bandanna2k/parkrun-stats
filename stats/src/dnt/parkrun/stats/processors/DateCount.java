package dnt.parkrun.stats.processors;

import java.util.Date;

public class DateCount
{
    final Date date;
    final int count;

    public DateCount(Date date, int count)
    {
        this.date = date;
        this.count = count;
    }

    @Override
    public String toString()
    {
        return "DateCount{" +
                "date=" + date +
                ", count=" + count +
                '}';
    }
}
