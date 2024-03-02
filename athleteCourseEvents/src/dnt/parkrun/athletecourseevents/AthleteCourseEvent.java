package dnt.parkrun.athletecourseevents;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Time;

import java.util.Date;

public class AthleteCourseEvent
{
    public final Athlete athlete;
    public final String courseName;
    public final Date date;
    public final int eventNumber;
    public final int position;
    public final Time time;

    public AthleteCourseEvent(Athlete athlete,
                              String courseName,
                              Date date,
                              int eventNumber,
                              int position,
                              Time time)
    {
        this.athlete = athlete;
        this.courseName = courseName;
        this.date = date;
        this.eventNumber = eventNumber;
        this.position = position;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "AthleteCourseEvent{" +
                "athlete=" + athlete +
                ", courseName='" + courseName + '\'' +
                ", date=" + date +
                ", eventNumber=" + eventNumber +
                ", position=" + position +
                ", time=" + time +
                '}';
    }
}
