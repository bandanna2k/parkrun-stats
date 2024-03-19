package dnt.parkrun.datastructures.stats;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;

public class VolunteersAtEvent extends AtEvent
{
    public VolunteersAtEvent(Athlete athlete, Course course, int volunteersAtEvent)
    {
        super(athlete, course, volunteersAtEvent);
    }

    public int getVolunteersAtEvent()
    {
        return count;
    }
}
