package dnt.parkrun.pindex;

import java.util.List;

import static java.util.Comparator.comparingInt;

public class PIndex
{
    public static Result pIndexAndNeeded(List<Integer> listOfRuns)
    {
        listOfRuns.sort((t1, t2) -> -t1.compareTo(t2));

        // pIndex calculation
        int pIndex = pIndex(listOfRuns);

        // Needed calculation
        int needed = 0;
        int nextPIndex = pIndex + 1;
        for (int i = 0; i < pIndex; i++)
        {
            needed += Math.max(0, nextPIndex - listOfRuns.get(i));
        }
        if(listOfRuns.size() > nextPIndex)
        {
            needed += Math.max(0, nextPIndex - listOfRuns.get(pIndex));
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
