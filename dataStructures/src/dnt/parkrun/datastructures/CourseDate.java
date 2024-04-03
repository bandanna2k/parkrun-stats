package dnt.parkrun.datastructures;

import java.util.Comparator;
import java.util.Date;

public class CourseDate
{
    public static final Comparator<CourseDate> COMPARATOR = (r1, r2) ->
    {
        if (r1.date.after(r2.date))
        {
            return 1;
        }
        if (r2.date.after(r1.date))
        {
            return -1;
        }
        return 0;
    };

    public final Course course;
    public final Date date;

    public CourseDate(Course course, Date date)
    {
        this.course = course;
        this.date = date;
    }

    @Override
    public String toString()
    {
        return "CourseDate{" +
                "course=" + course +
                ", date=" + date +
                '}';
    }
}
