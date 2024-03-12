package dnt.parkrun.datastructures.stats;

public class RunsAtEvent
{
    public final String courseLongName;
    public final String courseName;
    public final int athleteId;
    public final String name;
    public final int maxRunCount;

    public RunsAtEvent(String courseLongName, String courseName, int athleteId, String name, int maxRunCount)
    {

        this.courseLongName = courseLongName;
        this.courseName = courseName;
        this.athleteId = athleteId;
        this.name = name;
        this.maxRunCount = maxRunCount;
    }

    @Override
    public String toString()
    {
        return "RunsAtEvent{" +
                "courseLongName='" + courseLongName + '\'' +
                ", courseName='" + courseName + '\'' +
                ", athleteId=" + athleteId +
                ", name=" + name +
                ", maxRunCount=" + maxRunCount +
                '}';
    }
}
