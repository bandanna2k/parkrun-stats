package dnt.parkrun.datastructures.stats;

public class MostEventsRecord
{
    public final int athleteId;
    public final int differentRegionCourseCount;
    public final int totalRegionRuns;
    public final int differentCourseCount;
    public final int totalRuns;
    public final String name;
    public final int positionDelta;

    public MostEventsRecord(String name,
                            int athleteId,
                            int differentRegionCourseCount, int totalRegionRuns,
                            int differentCourseCount, int totalRuns,
                            int positionDelta)
    {
        this.athleteId = athleteId;
        this.differentRegionCourseCount = differentRegionCourseCount;
        this.totalRegionRuns = totalRegionRuns;
        this.differentCourseCount = differentCourseCount;
        this.totalRuns = totalRuns;
        this.name = name;
        this.positionDelta = positionDelta;
    }

    @Override
    public String toString()
    {
        return "MostEventsRecord{" +
                "athleteId=" + athleteId +
                ", differentRegionCourseCount='" + differentRegionCourseCount + '\'' +
                ", totalRegionRuns='" + totalRegionRuns + '\'' +
                ", differentCourseCount='" + differentCourseCount + '\'' +
                ", totalRuns='" + totalRuns + '\'' +
                ", name='" + name + '\'' +
                ", positionDelta='" + positionDelta + '\'' +
                '}';
    }
}
