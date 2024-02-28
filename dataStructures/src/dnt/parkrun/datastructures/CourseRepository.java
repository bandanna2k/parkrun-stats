package dnt.parkrun.datastructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CourseRepository
{
    private Map<Integer, Country> countryCodeToCountry = new HashMap<>();
    private Map<String, Course> courseNameToCourse = new HashMap<>();

    public void addCountry(Country country)
    {
        countryCodeToCountry.put(country.countryCode, country);
    }

    public Country getCountry(int countryCode)
    {
        return countryCodeToCountry.get(countryCode);
    }

    public void addCourse(Course course)
    {
        courseNameToCourse.put(course.name, course);
    }

    public Course getCourse(String courseName)
    {
        return courseNameToCourse.get(courseName);
    }

    public void filterByCountryCode(int countryCode)
    {
        courseNameToCourse.entrySet().removeIf(entry -> entry.getValue().countryCode == countryCode);
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
