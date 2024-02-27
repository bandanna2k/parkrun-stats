package dnt.parkrun.tests;

import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.Country;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.CourseEventSummary;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

public class WalkerTest
{
    @Test
    public void walkTest() throws IOException
    {
        Random random = new Random(1);

        // Get all countries and course
        List<Course> courses = new ArrayList<>();
        Map<Integer, Country> countries = new HashMap<>();
        Supplier<InputStream> supplier = () -> EventsJsonFileReader.class.getResourceAsStream("/events.json");
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(supplier)
                .forEachCountry(c -> countries.put(c.countryCode, c))
                .forEachEvent(courses::add)
                .build();
        reader.read();

        // Choose course at random
        int nextInt;
        nextInt = random.nextInt(courses.size());
        Course course = courses.get(nextInt);

        System.out.println("Course:" + course);

        // https://www.parkrun.org.uk/bushy/results/eventhistory/
        // Get course events
        List<CourseEventSummary> events = new ArrayList<>();
        Country country = countries.get(course.countryCode);
        URL courseEventHistoryUrl = new URL("https://" + country.url + "/" + course.name + "/results/eventhistory/");
        Parser courseEventParser = new Parser.Builder()
                .url(courseEventHistoryUrl)
                .forEachCourseEvent(events::add)
                .build();
        courseEventParser.parse();

        // Choose event at random
        nextInt = random.nextInt(events.size());
        CourseEventSummary event = events.get(nextInt);

        System.out.println("Event:" + event);

        // List athletes at event





        List<AthleteCourseSummary> athletesAtCourse = new ArrayList<>();
        dnt.parkrun.athletecoursesummary.Parser athleteAtEventParser = new dnt.parkrun.athletecoursesummary.Parser.Builder()
                .url(courseEventHistoryUrl)
                .forEachAthleteCourseSummary(athletesAtCourse::add)
                .build();
        athleteAtEventParser.parse();

        // Pick random athlete
        nextInt = random.nextInt(events.size());
        AthleteCourseSummary athlete = athletesAtCourse.get(nextInt);

        System.out.println("Athlete:" + athlete);
    }
}
