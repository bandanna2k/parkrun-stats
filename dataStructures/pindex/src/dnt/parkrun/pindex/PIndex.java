package dnt.parkrun.pindex;

import dnt.parkrun.datastructures.AthleteCourseSummary;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

public class PIndex
{
    public static Result pIndexAndNeeded(List<AthleteCourseSummary> listOfRuns)
    {
        listOfRuns.sort(comparingInt(acs -> -acs.countOfRuns));

        // pIndex calculation
        int pIndex = pIndex(listOfRuns.stream().map(acs -> acs.countOfRuns).collect(Collectors.toList()));

        // Needed calculation
        int needed = 0;
        int nextPIndex = pIndex + 1;
        for (int i = 0; i < pIndex; i++)
        {
            needed += Math.max(0, nextPIndex - listOfRuns.get(i).countOfRuns);
        }
        if(listOfRuns.size() > nextPIndex)
        {
            needed += Math.max(0, nextPIndex - listOfRuns.get(pIndex).countOfRuns);
        }
        else
        {
            needed += nextPIndex;
        }
        return new Result(pIndex, needed);
    }

    public static int pIndex(List<Integer> listOfAllRuns)
    {
        listOfAllRuns.sort(comparingInt(count -> -count));

        int pIndex = 0;
        for (int countOfRuns : listOfAllRuns)
        {
            if (pIndex >= countOfRuns)
            {
                break;
            }
            pIndex++;
        }
        return pIndex;
    }

    public static class Result
    {
        public final int pIndex;
        public final int neededForNextPIndex;

        public Result(int pIndex, int neededForNextPIndex)
        {
            this.pIndex = pIndex;
            this.neededForNextPIndex = neededForNextPIndex;
        }

        @Override
        public String toString()
        {
            return "Result{" +
                    "pIndex=" + pIndex +
                    ", neededForNextPIndex=" + neededForNextPIndex +
                    '}';
        }
    }
}
