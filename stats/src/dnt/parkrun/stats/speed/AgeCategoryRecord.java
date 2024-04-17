package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.AgeGrade;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE;

public class AgeCategoryRecord
{
    private static final Result NO_RESULT = new Result(
            0, null, 0, NO_ATHLETE, Time.MAX_TIME, null, AgeGrade.newInstance(0));

    int GOLD = 0;
    int SILVER = 1;
    int BRONZE = 2;
//    StatsRecord records[GOLD = new StatsRecord().result(NO_RESULT);   // 0
//    StatsRecord records[SILVER = new StatsRecord().result(NO_RESULT); // 1
//    StatsRecord records[BRONZE = new StatsRecord().result(NO_RESULT); // 2
    StatsRecord[] records = getNullRecords(3);

    private StatsRecord[] getNullRecords(int count)
    {
        StatsRecord[] records = new StatsRecord[count];
        for (int i = 0; i < count; i++)
        {
            records[i] = new StatsRecord().result(NO_RESULT);
        }
        return records;
    }

    public void maybeAddByTime(StatsRecord statsRecord)
    {
        if(statsRecord.result().time.getTotalSeconds() == Time.NO_TIME.getTotalSeconds()) return;

        Result result = statsRecord.result();
        if (result.athlete.athleteId == records[BRONZE].result().athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < records[BRONZE].result().time.getTotalSeconds())
            {
                records[BRONZE] = new StatsRecord().result(NO_RESULT);
            }
            else
            {
                return;
            }
        }
        else if (result.athlete.athleteId == records[SILVER].result().athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < records[SILVER].result().time.getTotalSeconds())
            {
                records[SILVER] = records[BRONZE];
                records[BRONZE] = new StatsRecord().result(NO_RESULT);
            }
            else
            {
                return;
            }
        }
        else if (result.athlete.athleteId == records[GOLD].result().athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < records[GOLD].result().time.getTotalSeconds())
            {
                records[GOLD] = records[SILVER];
                records[SILVER] = records[BRONZE];
                records[BRONZE] = new StatsRecord().result(NO_RESULT);
            }
            else
            {
                return;
            }
        }
        if (result.time.getTotalSeconds() < records[GOLD].result().time.getTotalSeconds())
        {
            records[BRONZE] = records[SILVER];
            records[SILVER] = records[GOLD];
            records[GOLD] = statsRecord;
        }
        else if (result.time.getTotalSeconds() < records[SILVER].result().time.getTotalSeconds())
        {
            records[BRONZE] = records[SILVER];
            records[SILVER] = statsRecord;
        }
        else if (result.time.getTotalSeconds() < records[BRONZE].result().time.getTotalSeconds())
        {
            records[BRONZE] = statsRecord;
        }

        int check = checkByTime();
        if(check > 0)
        {
            String error = STR."""
\{this.getClass().getSimpleName()} Invariant failure. \{check}
            """;
            checkByTime();
            throw new AssertionError(error);
        }
    }

    public void maybeAddByAgeGrade(StatsRecord statsRecord)
    {
        if(statsRecord.result().time.getTotalSeconds() == Time.NO_TIME.getTotalSeconds()) return;

        Result result = statsRecord.result();

        if (statsRecord.result().athlete.athleteId == records[BRONZE].result().athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > records[BRONZE].result().ageGrade.ageGrade)
            {
                records[BRONZE] = new StatsRecord().result(NO_RESULT);
            }
            else
            {
                return;
            }
        }
        else if (statsRecord.result().athlete.athleteId == records[SILVER].result().athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > records[SILVER].result().ageGrade.ageGrade)
            {
                records[SILVER] = records[BRONZE];
                records[BRONZE] = new StatsRecord().result(NO_RESULT);
            }
            else
            {
                return;
            }
        }
        else if (statsRecord.result().athlete.athleteId == records[GOLD].result().athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > records[GOLD].result().ageGrade.ageGrade)
            {
                records[GOLD] = records[SILVER];
                records[SILVER] = records[BRONZE];
                records[BRONZE] = new StatsRecord().result(NO_RESULT);
            }
            else
            {
                return;
            }
        }
        if (statsRecord.result().ageGrade.ageGrade > records[GOLD].result().ageGrade.ageGrade)
        {
            records[BRONZE] = records[SILVER];
            records[SILVER] = records[GOLD];
            records[GOLD] = statsRecord;
        }
        else if (statsRecord.result().ageGrade.ageGrade > records[SILVER].result().ageGrade.ageGrade)
        {
            records[BRONZE] = records[SILVER];
            records[SILVER] = statsRecord;
        }
        else if (statsRecord.result().ageGrade.ageGrade > records[BRONZE].result().ageGrade.ageGrade)
        {
            records[BRONZE] = statsRecord;
        }

        int check = checkByAgeGrade();
        if(check > 0)
        {
            String error = STR."""
\{this.getClass().getSimpleName()} Age grade invariant failure. \{check}
            """;
            checkByTime();
            throw new AssertionError(error);
        }

    }

    private int checkByTime()
    {
        if(records[GOLD].result().date == null || records[SILVER].result().date == null || records[BRONZE].result().date == null) return 0;

//        if(records[SILVER.result().ageGrade.ageGrade > records[GOLD.result().ageGrade.ageGrade) return 1;
//        if(records[BRONZE.result().ageGrade.ageGrade > records[SILVER.result().ageGrade.ageGrade) return 2;
        if(records[SILVER].result().time.getTotalSeconds() < records[GOLD].result().time.getTotalSeconds()) return 3;
        if(records[BRONZE].result().time.getTotalSeconds() < records[SILVER].result().time.getTotalSeconds()) return 4;

        if(records[GOLD].result().athlete.athleteId == records[SILVER].result().athlete.athleteId) return 5;
        if(records[GOLD].result().athlete.athleteId == records[BRONZE].result().athlete.athleteId) return 6;
        if(records[SILVER].result().athlete.athleteId == records[BRONZE].result().athlete.athleteId) return 7;
        return 0;
    }

    private int checkByAgeGrade()
    {
        if(records[GOLD].result().date == null || records[SILVER].result().date == null || records[BRONZE].result().date == null) return 0;

        if(records[SILVER].result().ageGrade.ageGrade > records[GOLD].result().ageGrade.ageGrade) return 1;
        if(records[BRONZE].result().ageGrade.ageGrade > records[SILVER].result().ageGrade.ageGrade) return 2;
//        if(records[SILVER.result().time.getTotalSeconds() > records[GOLD.result().time.getTotalSeconds()) return 3;
//        if(records[BRONZE.result().time.getTotalSeconds() > records[SILVER.result().time.getTotalSeconds()) return 4;

        if(records[GOLD].result().athlete.athleteId == records[SILVER].result().athlete.athleteId) return 5;
        if(records[GOLD].result().athlete.athleteId == records[BRONZE].result().athlete.athleteId) return 6;
        if(records[SILVER].result().athlete.athleteId == records[BRONZE].result().athlete.athleteId) return 7;
        return 0;
    }

    @Override
    public String toString()
    {
        return "AgeGroupRecord{" +
                "records[GOLD=" + records[GOLD] +
                ", records[SILVER=" + records[SILVER] +
                ", records[BRONZE=" + records[BRONZE] +
                '}';
    }
}
