package dnt.parkrun.datastructures;

public class CourseEventSummary
{
    public final int eventNumber;
    public final Athlete firstMale;
    public final Athlete firstFemale;

    @Override
    public String toString()
    {
        return "CourseEvent{" +
                "eventNumber=" + eventNumber +
                ", firstMale=" + firstMale +
                ", firstFemale=" + firstFemale +
                '}';
    }

    public CourseEventSummary(int eventNumber, Athlete firstMale, Athlete firstFemale)
    {
        this.eventNumber = eventNumber;
        this.firstMale = firstMale;
        this.firstFemale = firstFemale;
    }
}
