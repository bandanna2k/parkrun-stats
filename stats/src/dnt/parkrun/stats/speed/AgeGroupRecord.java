package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.AgeGrade;
import dnt.parkrun.datastructures.Result;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE;

public class AgeGroupRecord
{
    private static final Result NO_RESULT = new Result(0, null, 0, NO_ATHLETE, null, null, AgeGrade.newInstance(0));
    Result resultGold = NO_RESULT;
    Result resultSilver = NO_RESULT;
    Result resultBronze = NO_RESULT;

    public void maybeAdd(Result result)
    {
        if (result.athlete.athleteId == resultGold.athlete.athleteId)
        {
            if (result.ageGrade.ageGrade > resultGold.ageGrade.ageGrade)
            {
                resultGold = result;
            }
            return;
        }
        if (result.athlete.athleteId == resultSilver.athlete.athleteId)
        {
            if (result.ageGrade.ageGrade > resultSilver.ageGrade.ageGrade)
            {
                resultSilver = result;
            }
            return;
        }
        if (result.athlete.athleteId == resultBronze.athlete.athleteId)
        {
            if (result.ageGrade.ageGrade > resultBronze.ageGrade.ageGrade)
            {
                resultBronze = result;
            }
            return;
        }
        if (result.ageGrade.ageGrade > resultGold.ageGrade.ageGrade)
        {
            resultBronze = resultSilver;
            resultSilver = resultGold;
            resultGold = result;
        }
        else if (result.ageGrade.ageGrade > resultSilver.ageGrade.ageGrade)
        {
            resultBronze = resultSilver;
            resultSilver = result;
        }
        else if (result.ageGrade.ageGrade > resultBronze.ageGrade.ageGrade)
        {
            resultBronze = result;
        }
    }

    @Override
    public String toString()
    {
        return "AgeGroupRecord{" +
                "resultGold=" + resultGold +
                ", resultSilver=" + resultSilver +
                ", resultBronze=" + resultBronze +
                '}';
    }
}
