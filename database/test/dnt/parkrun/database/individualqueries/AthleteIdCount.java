package dnt.parkrun.database.individualqueries;

import java.util.Comparator;

public class AthleteIdCount
{
    public static Comparator<AthleteIdCount> COMPARATOR = (r1, r2) -> {
        if(r1.count < r2.count) return 1;
        if(r1.count > r2.count) return -1;
        if(r1.athleteId > r2.athleteId) return 1;
        if(r1.athleteId < r2.athleteId) return -1;
        return 0;
    };

    public final int athleteId;
    public final int count;

    public AthleteIdCount(int athleteId, int count)
    {
        this.athleteId = athleteId;
        this.count = count;
    }

    @Override
    public String toString()
    {
        return "AthleteIdCount{" +
                "athleteId=" + athleteId +
                ", count=" + count +
                '}';
    }
}
