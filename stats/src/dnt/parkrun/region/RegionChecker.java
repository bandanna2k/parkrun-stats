package dnt.parkrun.region;

import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;

import java.util.List;

public abstract class RegionChecker
{
    public int getRegionRunCount(Course homeParkrun, List<AthleteCourseSummary> summariesForAthlete)
    {
        int count = 0;
        for (AthleteCourseSummary acs : summariesForAthlete)
        {
            if (isSameRegion(homeParkrun, acs.course))
            {
                count += acs.countOfRuns;
            }
        }
        return count;
    }

    public abstract boolean isSameRegion(Course homeParkrun, Course course);
}
