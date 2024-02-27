package dnt.parkrun.datastructures;

public class CourseEventSummary
{
    public final Course course;
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

    public CourseEventSummary(Course course, int eventNumber, Athlete firstMale, Athlete firstFemale)
    {
        this.course = course;
        this.eventNumber = eventNumber;
        this.firstMale = firstMale;
        this.firstFemale = firstFemale;
    }
}
