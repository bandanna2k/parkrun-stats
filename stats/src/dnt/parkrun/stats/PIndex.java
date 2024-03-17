package dnt.parkrun.stats;

import dnt.parkrun.datastructures.AthleteCourseSummary;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingInt;

public abstract class PIndex
{
    public static Point pIndexNextMax(List<AthleteCourseSummary> listOfRuns)
    {
        listOfRuns.sort(comparingInt(acs -> -acs.countOfRuns));

        int nextMax = 0;
        List<AthleteCourseSummary> listOfPIndexACS = new ArrayList<>();
        for (AthleteCourseSummary acs : listOfRuns)
        {
            if (listOfPIndexACS.size() >= acs.countOfRuns)
            {
                nextMax = acs.countOfRuns;
                break;
            }
            listOfPIndexACS.add(acs);
        }
        return new Point(listOfPIndexACS.size(), nextMax);
    }

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

    public static int pIndex(Map<Integer, Integer> listOfRuns)
    {
        int testPIndex;
        int resultPIndex = 0;
        for (testPIndex = 1; testPIndex < 100; testPIndex++)
        {
            int countOfRunsGreatherThanPIndex = 0;
            for (Map.Entry<Integer, Integer> entry : listOfRuns.entrySet())
            {
                int count = entry.getValue();
                if (count >= testPIndex)
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
}
