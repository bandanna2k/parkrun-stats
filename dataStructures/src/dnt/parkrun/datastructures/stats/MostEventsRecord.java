package dnt.parkrun.datastructures.stats;

public class MostEventsRecord
{
    public final int athleteId;
    public final String differentRegionCourseCount;
    public final String totalRegionRuns;
    public final String differentCourseCount;
    public final String totalRuns;
    public final String name;
    public final int positionDelta;

    public MostEventsRecord(String name,
                            int athleteId,
                            String differentRegionCourseCount, String totalRegionRuns,
                            String differentCourseCount, String totalRuns,
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
    public MostEventsRecord(String name,
                            int athleteId,
                            int differentRegionCourseCount, int totalRegionRuns,
                            int differentCourseCount, int totalRuns,
                            int positionDelta)
    {
        this(name,
                athleteId,
                String.valueOf(differentRegionCourseCount),
                String.valueOf(totalRegionRuns),
                String.valueOf(differentCourseCount),
                String.valueOf(totalRuns),
                positionDelta
        );
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
