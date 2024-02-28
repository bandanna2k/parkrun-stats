package dnt.parkrun.datastructures;

import java.util.Objects;

public class CourseEventSummary
{
    public final Course course;
    public final int eventNumber;
    public final Athlete firstMale;
    public final Athlete firstFemale;

    public CourseEventSummary(Course course, int eventNumber, Athlete firstMale, Athlete firstFemale)
    {
        this.course = course;
        this.eventNumber = eventNumber;
        this.firstMale = firstMale;
        this.firstFemale = firstFemale;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CourseEventSummary that = (CourseEventSummary) o;
        return eventNumber == that.eventNumber && Objects.equals(course, that.course);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(course, eventNumber);
    }

    @Override
    public String toString()
    {
        return "CourseEvent{" +
                "eventNumber=" + eventNumber +
                ", firstMale=" + firstMale +
                ", firstFemale=" + firstFemale +
                '}';
    }


}
