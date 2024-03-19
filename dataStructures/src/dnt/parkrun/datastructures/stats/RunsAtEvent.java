package dnt.parkrun.datastructures.stats;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;

public class RunsAtEvent extends AtEvent
{
    public RunsAtEvent(Athlete athlete, Course course, int runCount)
    {
        super(athlete, course, runCount);
    }

    public int getRunCount()
    {
        return count;
    }
}
