package dnt.parkrun.athletecourseevents;

import dnt.parkrun.datastructures.Time;

import java.util.Date;

public class AthleteCourseEvent
{
    private final int athleteId;
    private final String courseName;
    private final Date date;
    private final int eventNumber;
    private final int position;
    private final Time time;

    public AthleteCourseEvent(int athleteId,
                              String courseName,
                              Date date,
                              int eventNumber,
                              int position,
                              Time time)
    {
        this.athleteId = athleteId;
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
                "athleteId=" + athleteId +
                ", courseName='" + courseName + '\'' +
                ", date=" + date +
                ", eventNumber=" + eventNumber +
                ", position=" + position +
                ", time=" + time +
                '}';
    }
}
