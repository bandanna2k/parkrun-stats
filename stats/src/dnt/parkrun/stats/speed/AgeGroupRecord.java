package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.AgeGrade;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE;

public class AgeGroupRecord
{
    private static final Result NO_RESULT = new Result(
            0, null, 0, NO_ATHLETE, Time.MAX_TIME, null, AgeGrade.newInstance(0));

    StatsRecord recordGold = new StatsRecord().result(NO_RESULT);
    StatsRecord recordSilver = new StatsRecord().result(NO_RESULT);
    StatsRecord recordBronze = new StatsRecord().result(NO_RESULT);

    public void maybeAddByTime(StatsRecord statsRecord)
    {
        Result result = statsRecord.result();
        Result resultGold = recordGold.result();
        Result resultSilver = recordSilver.result();
        Result resultBronze = recordBronze.result();

        if (result.athlete.athleteId == resultGold.athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < resultGold.time.getTotalSeconds())
            {
                recordGold = statsRecord;
            }
            return;
        }
        if (result.athlete.athleteId == resultSilver.athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < resultSilver.time.getTotalSeconds())
            {
                recordSilver = statsRecord;
            }
            return;
        }
        if (result.athlete.athleteId == resultBronze.athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < resultBronze.time.getTotalSeconds())
            {
                recordBronze = statsRecord;
            }
            return;
        }
        if (result.time.getTotalSeconds() < resultGold.time.getTotalSeconds())
        {
            recordBronze = recordSilver;
            recordSilver = recordGold;
            recordGold = statsRecord;
        }
        else if (result.time.getTotalSeconds() < resultSilver.time.getTotalSeconds())
        {
            recordBronze = recordSilver;
            recordSilver = statsRecord;
        }
        else if (result.time.getTotalSeconds() < resultBronze.time.getTotalSeconds())
        {
            recordBronze = statsRecord;
        }
    }

    public void maybeAddByAgeGrade(StatsRecord statsRecord)
    {
        Result result = statsRecord.result();
        Result resultGold = recordGold.result();
        Result resultSilver = recordSilver.result();
        Result resultBronze = recordBronze.result();

        if (statsRecord.result().athlete.athleteId == recordGold.result().athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > recordGold.result().ageGrade.ageGrade)
            {
                recordGold = statsRecord;
            }
            return;
        }
        if (statsRecord.result().athlete.athleteId == recordSilver.result().athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > recordSilver.result().ageGrade.ageGrade)
            {
                recordSilver = statsRecord;
            }
            return;
        }
        if (statsRecord.result().athlete.athleteId == recordBronze.result().athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > recordBronze.result().ageGrade.ageGrade)
            {
                recordBronze = statsRecord;
            }
            return;
        }
        if (statsRecord.result().ageGrade.ageGrade > recordGold.result().ageGrade.ageGrade)
        {
            recordBronze = recordSilver;
            recordSilver = recordGold;
            recordGold = statsRecord;
        }
        else if (statsRecord.result().ageGrade.ageGrade > recordSilver.result().ageGrade.ageGrade)
        {
            recordBronze = recordSilver;
            recordSilver = statsRecord;
        }
        else if (statsRecord.result().ageGrade.ageGrade > recordBronze.result().ageGrade.ageGrade)
        {
            recordBronze = statsRecord;
        }
    }

    @Override
    public String toString()
    {
        return "AgeGroupRecord{" +
                "recordGold=" + recordGold +
                ", recordSilver=" + recordSilver +
                ", recordBronze=" + recordBronze +
                '}';
    }
}
