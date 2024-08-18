package dnt.parkrun.datastructures.stats;

public class MostEventsRecord
{
    public final int athleteId;
    public final String name;

    public final int differentRegionCourseCount;
    public final int totalRegionRuns;

    public int differentGlobalCourseCount;
    public int totalGlobalRuns;

    public int pIndex;

    public String firstRuns;
    public int regionnaireCount;

    public int positionDelta;
    public boolean isNewEntry;

    public int runsNeeded;
    public int maxRunsNeeded;

    public int inauguralRuns;

    public MostEventsRecord(String name,
                            int athleteId,
                            int differentRegionCourseCount, int totalRegionRuns,
                            int differentGlobalCourseCount, int totalGlobalRuns,
                            int runsNeeded,
                            int inauguralRuns,
                            int regionnaireCount)
    {
        this.athleteId = athleteId;
        this.differentRegionCourseCount = differentRegionCourseCount;
        this.totalRegionRuns = totalRegionRuns;
        this.differentGlobalCourseCount = differentGlobalCourseCount;
        this.totalGlobalRuns = totalGlobalRuns;
        this.name = name;
        this.runsNeeded = runsNeeded;
        this.inauguralRuns = inauguralRuns;
        this.regionnaireCount = regionnaireCount;
    }

    @Override
    public String toString()
    {
        return "MostEventsRecord{" +
                "athleteId=" + athleteId +
                ", name='" + name + '\'' +
                ", differentRegionCourseCount=" + differentRegionCourseCount +
                ", totalRegionRuns=" + totalRegionRuns +
                ", differentGlobalCourseCount=" + differentGlobalCourseCount +
                ", totalGlobalRuns=" + totalGlobalRuns +
                ", pIndex=" + pIndex +
                ", firstRuns='" + firstRuns + '\'' +
                ", regionnaireCount=" + regionnaireCount +
                ", positionDelta=" + positionDelta +
                ", isNewEntry=" + isNewEntry +
                ", runsNeeded=" + runsNeeded +
                ", maxRunsNeeded=" + maxRunsNeeded +
                ", inauguralRuns=" + inauguralRuns +
                '}';
    }
}
