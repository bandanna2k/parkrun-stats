package dnt.parkrun.datastructures;

import java.util.*;
import java.util.function.Consumer;

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

    public List<Course> getCourses(Country country)
    {
        List<Course> courses = new ArrayList<>(courseNameToCourse.values().stream().filter(c -> c.country == country).toList());
        courses.sort(Comparator.comparing(course -> course.name));
        return courses;
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

    public void forEachCourse(Consumer<Course> consumer)
    {
        courseIdToCourse.values().forEach(consumer);
    }
}
