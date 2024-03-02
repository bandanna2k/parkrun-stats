package dnt.parkrun.datastructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CourseRepository
{
    private final Map<Integer, Country> countryCodeToCountry = new HashMap<>();
    private final Map<String, Course> courseNameToCourse = new HashMap<>();
    private final Map<String, Course> courseLongNameToCourse = new HashMap<>();

    public void addCountry(Country country)
    {
        countryCodeToCountry.put(country.countryEnum.getCountryCode(), country);
    }

    public Country getCountry(int countryCode)
    {
        return countryCodeToCountry.get(countryCode);
    }

    public void addCourse(Course course)
    {
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
        courseNameToCourse.entrySet().removeIf(entry -> entry.getValue().country.countryEnum.getCountryCode() == countryCode);
    }

    public Collection<Course> getCourses()
    {
        return courseNameToCourse.values();
    }

    @Override
    public String toString()
    {
        return "CourseRepository{" +
                "countryCodeToCountry=" + countryCodeToCountry +
                ", courseNameToCourse=" + courseNameToCourse +
                '}';
    }
}
