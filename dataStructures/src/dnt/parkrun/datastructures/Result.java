package dnt.parkrun.datastructures;

public class Result
{
    public final int courseId;
    public final int eventNumber;
    public final int position;
    public final Athlete athlete;
    public final Time time;

    public Result(int courseId, int eventNumber, int position, Athlete athlete, Time time)
    {
        this.courseId = courseId;
        this.eventNumber = eventNumber;
        this.position = position;
        this.athlete = athlete;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "courseId=" + courseId +
                ", eventNumber=" + eventNumber +
                ", position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                '}';
    }
}
