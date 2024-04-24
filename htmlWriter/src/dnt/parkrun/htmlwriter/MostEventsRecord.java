package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;

public class MostEventsRecord
{
    public final Athlete athlete;
    public final int differentRegionCourseCount;
    public final int totalRegionRuns;
    public final int differentGlobalCourseCount;
    public final int totalGlobalRuns;
    public final String firstRuns;
    public final int regionnaireCount;

    public final int positionDelta;
    public final boolean isNewEntry;
    public final String runsNeeded;

    public MostEventsRecord(Athlete athlete,
                            int differentRegionCourseCount, int totalRegionRuns,
                            int differentGlobalCourseCount, int totalGlobalRuns,
                            int positionDelta, boolean isNewEntry,
                            String firstRuns, int regionnaireCount, String runsNeeded)
    {
        this.athlete = athlete;
        this.differentRegionCourseCount = differentRegionCourseCount;
        this.totalRegionRuns = totalRegionRuns;
        this.differentGlobalCourseCount = differentGlobalCourseCount;
        this.totalGlobalRuns = totalGlobalRuns;
        this.positionDelta = positionDelta;
        this.isNewEntry = isNewEntry;
        this.firstRuns = firstRuns;
        this.regionnaireCount = regionnaireCount;
        this.runsNeeded = runsNeeded;
    }

    public MostEventsRecord(Athlete athlete,
                            int differentRegionCourseCount,
                            int totalRegionRuns,
                            int differentGlobalCourseCount,
                            int totalGlobalRuns,
                            int positionDelta,
                            boolean isNewEntry)
    {
        this(athlete, differentRegionCourseCount, totalRegionRuns,
                differentGlobalCourseCount, totalGlobalRuns, positionDelta, isNewEntry,
                null, -1 , null);
    }

    @Override
    public String toString()
    {
        return "MostEventsRecord{" +
                "athlete=" + athlete +
                ", differentRegionCourseCount=" + differentRegionCourseCount +
                ", totalRegionRuns=" + totalRegionRuns +
                ", differentGlobalCourseCount=" + differentGlobalCourseCount +
                ", totalGlobalRuns=" + totalGlobalRuns +
                ", firstRuns='" + firstRuns + '\'' +
                ", regionnaireCount=" + regionnaireCount +
                ", positionDelta=" + positionDelta +
                ", isNewEntry=" + isNewEntry +
                ", runsNeeded='" + runsNeeded + '\'' +
                '}';
    }
}
