package dnt.parkrun.stats;

import dnt.parkrun.datastructures.AthleteCourseSummary;

import java.util.List;
import java.util.Map;

public abstract class PIndex
{
    public static int pIndex(List<AthleteCourseSummary> listOfRuns)
    {
        int testPIndex;
        int resultPIndex = 0;
        for (testPIndex = 1; testPIndex < 100; testPIndex++)
        {
            int countOfRunsGreatherThanPIndex = 0;
            for (AthleteCourseSummary athleteCourseSummary : listOfRuns)
            {
                if(athleteCourseSummary.countOfRuns >= testPIndex)
                {
                    countOfRunsGreatherThanPIndex++;
                }
            }
            if(countOfRunsGreatherThanPIndex >= testPIndex)
            {
                resultPIndex = testPIndex;
            }
            else
            {
                break;
            }
        }
        return resultPIndex;
    }

    public static int pIndex(Map<String, Integer> listOfRuns)
    {
        int testPIndex;
        int resultPIndex = 0;
        for (testPIndex = 1; testPIndex < 100; testPIndex++)
        {
            int countOfRunsGreatherThanPIndex = 0;
            for (Map.Entry<String, Integer> entry : listOfRuns.entrySet())
            {
                int count = entry.getValue();
                if (count >= testPIndex)
                {
                    countOfRunsGreatherThanPIndex++;
                }
            }
//            for (AthleteCourseSummary athleteCourseSummary : listOfRuns)
//            {
//                if(athleteCourseSummary.countOfRuns >= testPIndex)
//                {
//                    countOfRunsGreatherThanPIndex++;
//                }
//            }
            if(countOfRunsGreatherThanPIndex >= testPIndex)
            {
                resultPIndex = testPIndex;
            }
            else
            {
                break;
            }
        }
        return resultPIndex;
    }
}
