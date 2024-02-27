package dnt.parkrun.datastructures;

public class CourseEventSummary
{
    private final int eventNumber;
    private final Athlete firstMale;
    private final Athlete firstFemale;

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
