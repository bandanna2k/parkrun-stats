package dnt.parkrun.datastructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseRepository
{
    private final Map<Integer, Course> courseIdToCourse = new HashMap<>();
    private final Map<String, Course> courseNameToCourse = new HashMap<>();
    private final Map<String, Course> courseLongNameToCourse = new HashMap<>();

    public void addCourse(Course course)
    {
        courseIdToCourse.put(course.courseId, course);
        courseNameToCourse.put(course.name, course);
        courseLongNameToCourse.put(course.longName, course);
    }

    public Course getCourseFromName(String courseName)
    {
        return courseNameToCourse.get(courseName);
    }

    public Course getCourseFromLongName(String courseLongName)
    {
        return courseLongNameToCourse.get(courseLongName);
    }

    public void filterByCountryCode(int countryCode)
    {
        courseNameToCourse.entrySet().removeIf(entry -> entry.getValue().country.countryCode == countryCode);
    }

    public Collection<Course> getCourses(Country country)
    {
        return courseNameToCourse.values().stream().filter(c -> c.country == country).collect(Collectors.toList());
    }

    public Course getCourse(int courseId)
    {
        return courseIdToCourse.get(courseId);
    }

    @Override
    public String toString()
    {
        return "CourseRepository{" +
                "courseIdToCourse=" + courseIdToCourse +
                ", courseNameToCourse=" + courseNameToCourse +
                ", courseLongNameToCourse=" + courseLongNameToCourse +
                '}';
    }
}
