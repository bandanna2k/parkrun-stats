package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;

public class AthleteTestBuilder
{
    private String name = "Test ATHLETE";
    private int athleteId = 141414;

    public Athlete build()
    {
        return Athlete.from(name, athleteId);
    }
}
