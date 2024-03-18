package dnt.parkrun.datastructures;

import java.util.Date;

public class Volunteer
{
    public final int courseId;
    public final Date date;
    public final Athlete athlete;

    public Volunteer(int courseId, Date date, Athlete athlete)
    {
        this.courseId = courseId;
        this.date = date;
        this.athlete = athlete;
    }

    @Override
    public String toString()
    {
        return "Volunteer{" +
                "courseId=" + courseId +
                ", date=" + date +
                ", athlete=" + athlete +
                '}';
    }
}
