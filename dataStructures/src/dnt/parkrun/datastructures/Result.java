package dnt.parkrun.datastructures;

public class Result
{
    public final String courseName;
    public final int eventNumber;
    public final int position;
    public final Athlete athlete;
    public final Time time;

    public Result(String courseName, int eventNumber, int position, Athlete athlete, Time time)
    {
        this.courseName = courseName;
        this.eventNumber = eventNumber;
        this.position = position;
        this.athlete = athlete;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "courseName='" + courseName + '\'' +
                ", eventNumber=" + eventNumber +
                ", position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                '}';
    }
}
