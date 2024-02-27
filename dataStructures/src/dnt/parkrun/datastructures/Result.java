package dnt.parkrun.datastructures;

public class Result
{
    public final int position;
    public final Athlete athlete;
    public final Time time;

    public Result(int position, Athlete athlete, Time time)
    {
        this.position = position;
        this.athlete = athlete;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                '}';
    }
}
