package dnt.parkrun.datastructures.stats;

import java.util.Date;

public class EventDateCount
{
    public final int eventNumber;
    public final Date date;
    public final int count;

    public EventDateCount(Date date, int count)
    {
        this.date = date;
        this.count = count;
        this.eventNumber = -1;
    }

    @Override
    public String toString()
    {
        return "EventDateCount{" +
                "eventNumber=" + eventNumber +
                ", date=" + date +
                ", count=" + count +
                '}';
    }
}
