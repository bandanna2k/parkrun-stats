package dnt.parkrun.datastructures.stats;

public class MostEventsRecord
{
    public final int athleteId;
    public final String differentRegionCourseCount;
    public final String totalRegionRuns;
    public final String differentCourseCount;
    public final String totalRuns;
    public final String name;

    public MostEventsRecord(String name,
                            int athleteId, String differentRegionCourseCount,
                            String totalRegionRuns, String differentCourseCount, String totalRuns)
    {
        this.athleteId = athleteId;
        this.differentRegionCourseCount = differentRegionCourseCount;
        this.totalRegionRuns = totalRegionRuns;
        this.differentCourseCount = differentCourseCount;
        this.totalRuns = totalRuns;
        this.name = name;
    }
    public MostEventsRecord(String name,
                            int athleteId, int differentRegionCourseCount,
                            int totalRegionRuns, int differentCourseCount, int totalRuns)
    {
        this(name,
                athleteId,
                String.valueOf(differentRegionCourseCount),
                String.valueOf(totalRegionRuns),
                String.valueOf(differentCourseCount),
                String.valueOf(totalRuns));
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
                '}';
    }
}
