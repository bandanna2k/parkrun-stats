package dnt.parkrun.datastructures.stats;

import dnt.parkrun.datastructures.Athlete;

public class RunsAtEvent
{
    public final Athlete athlete;
    public final String courseLongName;
    public final String courseName;
    public final int runCount;

    public RunsAtEvent(Athlete athlete, String courseLongName, String courseName, int runCount)
    {
        this.courseLongName = courseLongName;
        this.courseName = courseName;
        this.athlete = athlete;
        this.runCount = runCount;
    }

    @Override
    public String toString()
    {
        return "RunsAtEvent{" +
                "athlete=" + athlete +
                ", courseLongName='" + courseLongName + '\'' +
                ", courseName='" + courseName + '\'' +
                ", runCount=" + runCount +
                '}';
    }
}
