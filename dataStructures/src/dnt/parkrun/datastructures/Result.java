package dnt.parkrun.datastructures;

import java.util.Date;

public class Result
{
    public final int courseId;
    public final Date date;
    public final int position;
    public final Athlete athlete;
    public final Time time;

    public Result(int courseId, Date date, int position, Athlete athlete, Time time)
    {
        this.courseId = courseId;
        this.date = date;
        this.position = position;
        this.athlete = athlete;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "courseId=" + courseId +
                ", date=" + date +
                ", position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                '}';
    }
}
