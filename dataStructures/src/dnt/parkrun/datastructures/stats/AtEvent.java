package dnt.parkrun.datastructures.stats;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;

public class AtEvent
{
    public final Athlete athlete;
    public final Course course;
    public final int count;

    public AtEvent(Athlete athlete, Course course, int count)
    {
        this.course = course;
        this.athlete = athlete;
        this.count = count;
    }

    @Override
    public String toString()
    {
        return "AtEvent{" +
                "athlete=" + athlete +
                ", course=" + course +
                ", count=" + count +
                '}';
    }
}
