package dnt.parkrun.datastructures;

public class Result
{
    public final String courseName;
    public final int position;
    public final Athlete athlete;
    public final Time time;

    public Result(String courseName, int position, Athlete athlete, Time time)
    {
        this.courseName = courseName;
        this.position = position;
        this.athlete = athlete;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "courseName='" + courseName + '\'' +
                ", position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                '}';
    }
}
