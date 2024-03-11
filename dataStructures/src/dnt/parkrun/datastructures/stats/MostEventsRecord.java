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
    public final int pIndex;

    public MostEventsRecord(String name,
                            int athleteId,
                            int differentRegionCourseCount, int totalRegionRuns,
                            int differentCourseCount, int totalRuns,
                            int positionDelta,
                            int pIndex)
    {
        this.athleteId = athleteId;
        this.differentRegionCourseCount = differentRegionCourseCount;
        this.totalRegionRuns = totalRegionRuns;
        this.differentCourseCount = differentCourseCount;
        this.totalRuns = totalRuns;
        this.name = name;
        this.positionDelta = positionDelta;
        this.pIndex = pIndex;
    }

    @Override
    public String toString()
    {
        return "MostEventsRecord{" +
                "athleteId=" + athleteId +
                ", differentRegionCourseCount=" + differentRegionCourseCount +
                ", totalRegionRuns=" + totalRegionRuns +
                ", differentCourseCount=" + differentCourseCount +
                ", totalRuns=" + totalRuns +
                ", name='" + name + '\'' +
                ", positionDelta=" + positionDelta +
                ", pIndex=" + pIndex +
                '}';
    }
}
