package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;

import static dnt.parkrun.datastructures.Course.NO_COURSE_ID;

public class CourseTestBuilder
{
    private int courseId = NO_COURSE_ID;
    private String name = "testcourse";
    private Country country = Country.NZ;
    private String longName = "Test Course";
    private Course.Status status = Course.Status.RUNNING;

    public CourseTestBuilder courseId(int courseId)
    {
        this.courseId = courseId;
        return this;
    }

    public Course build()
    {
        return new Course(courseId, name, country, longName, status);
    }
}
