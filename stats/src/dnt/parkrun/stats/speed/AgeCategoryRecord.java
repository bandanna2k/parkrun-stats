package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.AgeGrade;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE;

public class AgeCategoryRecord
{
    private static final Result NO_RESULT = new Result(
            0, null, 0, NO_ATHLETE, Time.MAX_TIME, null, AgeGrade.newInstance(0));

    StatsRecord recordGold = new StatsRecord().result(NO_RESULT);
    StatsRecord recordSilver = new StatsRecord().result(NO_RESULT);
    StatsRecord recordBronze = new StatsRecord().result(NO_RESULT);

    public void maybeAddByTime(StatsRecord statsRecord)
    {
        if(statsRecord.result().time.getTotalSeconds() == Time.NO_TIME.getTotalSeconds()) return;

        Result result = statsRecord.result();
        Result resultGold = recordGold.result();
        Result resultSilver = recordSilver.result();
        Result resultBronze = recordBronze.result();

        if (result.athlete.athleteId == resultBronze.athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < resultBronze.time.getTotalSeconds())
            {
                recordBronze = new StatsRecord().result(NO_RESULT);
                resultBronze = NO_RESULT;
            }
            else
            {
                return;
            }
        }
        else if (result.athlete.athleteId == resultSilver.athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < resultSilver.time.getTotalSeconds())
            {
                recordSilver = recordBronze;
                recordBronze = new StatsRecord().result(NO_RESULT);
                resultSilver = resultBronze;
                resultBronze = NO_RESULT;
            }
            else
            {
                return;
            }
        }
        else if (result.athlete.athleteId == resultGold.athlete.athleteId)
        {
            if (result.time.getTotalSeconds() < resultGold.time.getTotalSeconds())
            {
                recordGold = recordSilver;
                recordSilver = recordBronze;
                recordBronze = new StatsRecord().result(NO_RESULT);
                resultGold = resultSilver;
                resultSilver = resultBronze;
                resultBronze = NO_RESULT;
            }
            else
            {
                return;
            }
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

        int check = checkByTime();
        if(check > 0)
        {
            String error = STR."""
\{this.getClass().getSimpleName()} Invariant failure. \{check}
\{recordGold.result()}
\{recordSilver.result()}
\{recordBronze.result()}
            """;
            checkByTime();
            throw new AssertionError(error);
        }
    }

    public void maybeAddByAgeGrade(StatsRecord statsRecord)
    {
        if(statsRecord.result().time.getTotalSeconds() == Time.NO_TIME.getTotalSeconds()) return;

        Result result = statsRecord.result();
        Result resultGold = recordGold.result();
        Result resultSilver = recordSilver.result();
        Result resultBronze = recordBronze.result();

        if (statsRecord.result().athlete.athleteId == resultBronze.athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > resultBronze.ageGrade.ageGrade)
            {
                recordBronze = new StatsRecord().result(NO_RESULT);
                resultBronze = NO_RESULT;
            }
            else
            {
                return;
            }
        }
        else if (statsRecord.result().athlete.athleteId == resultSilver.athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > resultSilver.ageGrade.ageGrade)
            {
                recordSilver = recordBronze;
                recordBronze = new StatsRecord().result(NO_RESULT);
                resultSilver = resultBronze;
                resultBronze = NO_RESULT;
            }
            else
            {
                return;
            }
        }
        else if (statsRecord.result().athlete.athleteId == resultGold.athlete.athleteId)
        {
            if (statsRecord.result().ageGrade.ageGrade > resultGold.ageGrade.ageGrade)
            {
                recordGold = recordSilver;
                recordSilver = recordBronze;
                recordBronze = new StatsRecord().result(NO_RESULT);
                resultGold = resultSilver;
                resultSilver = resultBronze;
                resultBronze = NO_RESULT;
            }
            else
            {
                return;
            }
        }
        if (statsRecord.result().ageGrade.ageGrade > resultGold.ageGrade.ageGrade)
        {
            recordBronze = recordSilver;
            recordSilver = recordGold;
            recordGold = statsRecord;
        }
        else if (statsRecord.result().ageGrade.ageGrade > resultSilver.ageGrade.ageGrade)
        {
            recordBronze = recordSilver;
            recordSilver = statsRecord;
        }
        else if (statsRecord.result().ageGrade.ageGrade > resultBronze.ageGrade.ageGrade)
        {
            recordBronze = statsRecord;
        }

        int check = checkByAgeGrade();
        if(check > 0)
        {
            String error = STR."""
\{this.getClass().getSimpleName()} Age grade invariant failure. \{check}
\{recordGold.result()}
\{recordSilver.result()}
\{recordBronze.result()}
            """;
            checkByTime();
            throw new AssertionError(error);
        }

    }

    private int checkByTime()
    {
        if(recordGold.result().date == null || recordSilver.result().date == null || recordBronze.result().date == null) return 0;

//        if(recordSilver.result().ageGrade.ageGrade > recordGold.result().ageGrade.ageGrade) return 1;
//        if(recordBronze.result().ageGrade.ageGrade > recordSilver.result().ageGrade.ageGrade) return 2;
        if(recordSilver.result().time.getTotalSeconds() < recordGold.result().time.getTotalSeconds()) return 3;
        if(recordBronze.result().time.getTotalSeconds() < recordSilver.result().time.getTotalSeconds()) return 4;

        if(recordGold.result().athlete.athleteId == recordSilver.result().athlete.athleteId) return 5;
        if(recordGold.result().athlete.athleteId == recordBronze.result().athlete.athleteId) return 6;
        if(recordSilver.result().athlete.athleteId == recordBronze.result().athlete.athleteId) return 7;
        return 0;
    }

    private int checkByAgeGrade()
    {
        if(recordGold.result().date == null || recordSilver.result().date == null || recordBronze.result().date == null) return 0;

        if(recordSilver.result().ageGrade.ageGrade > recordGold.result().ageGrade.ageGrade) return 1;
        if(recordBronze.result().ageGrade.ageGrade > recordSilver.result().ageGrade.ageGrade) return 2;
//        if(recordSilver.result().time.getTotalSeconds() > recordGold.result().time.getTotalSeconds()) return 3;
//        if(recordBronze.result().time.getTotalSeconds() > recordSilver.result().time.getTotalSeconds()) return 4;

        if(recordGold.result().athlete.athleteId == recordSilver.result().athlete.athleteId) return 5;
        if(recordGold.result().athlete.athleteId == recordBronze.result().athlete.athleteId) return 6;
        if(recordSilver.result().athlete.athleteId == recordBronze.result().athlete.athleteId) return 7;
        return 0;
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
