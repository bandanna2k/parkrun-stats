package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.AgeGrade;
import dnt.parkrun.datastructures.AgeGroup;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Time;

import java.util.Date;

public class StatsRecord
{
    private Athlete athlete;
    private Time time;
    private AgeGrade ageGrade;
    private Date date;
    private AgeGroup ageGroup;

    StatsRecord athlete(Athlete athlete)
    {
        this.athlete = athlete;
        return this;
    }
    Athlete athlete() { return athlete; }

    public StatsRecord time(Time time)
    {
        this.time = time;
        return this;
    }
    Time time() { return time; }

    public StatsRecord ageGrade(AgeGrade ageGrade)
    {
        this.ageGrade = ageGrade;
        return this;
    }
    AgeGrade ageGrade() { return ageGrade; }

    public StatsRecord date(Date date)
    {
        this.date = date;
        return this;
    }
    public Date date() { return date; }

    public StatsRecord ageGroup(AgeGroup ageGroup)
    {
        this.ageGroup = ageGroup;
        return this;
    }
    public AgeGroup ageGroup() { return ageGroup; }
}
