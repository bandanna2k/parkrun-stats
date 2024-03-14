package dnt.parkrun.datastructures.stats;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;

public class RunsAtEvent
{
    public final Athlete athlete;
    public final Course course;
    public final int runCount;

    public RunsAtEvent(Athlete athlete, Course course, int runCount)
    {
        this.course = course;
        this.athlete = athlete;
        this.runCount = runCount;
    }

    @Override
    public String toString()
    {
        return "RunsAtEvent{" +
                "athlete=" + athlete +
                ", course=" + course +
                ", runCount=" + runCount +
                '}';
    }
}
