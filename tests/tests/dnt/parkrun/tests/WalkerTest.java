package dnt.parkrun.tests;

import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.Result;
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
                .forEachCourse(courses::add)
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
        URL courseEventSummaryUrl = new URL("https://" + country.url + "/" + course.name + "/results/eventhistory/");
        Parser courseEventSummaryParser = new Parser.Builder()
                .course(course)
                .url(courseEventSummaryUrl)
                .forEachCourseEvent(events::add)
                .build();
        courseEventSummaryParser.parse();

        // Choose event at random
        nextInt = random.nextInt(events.size());
        CourseEventSummary event = events.get(nextInt);

        System.out.println("Event:" + event);

        // https://www.parkrun.co.nz/hamiltonlake/results/275/
        // List athletes at event
        List<Result> results = new ArrayList<>();
        URL courseEventUrl = new URL("https://" + country.url + "/" + course.name + "/results/" + event.eventNumber + "/");
        dnt.parkrun.courseevent.Parser courseEventParser = new dnt.parkrun.courseevent.Parser.Builder()
                .url(courseEventUrl)
                .forEachResult(results::add)
                .build();
        courseEventParser.parse();

        // Choose result at random
        nextInt = random.nextInt(results.size());
        Result result = results.get(nextInt);

        System.out.println("Result:" + result);

        // https://www.parkrun.co.nz/parkrunner/902393/
        // List course summary for random athlete
        URL athleteCourseSummaryUrl = new URL("https://" + country.url + "/parkrunner/" + result.athlete.athleteId + "/");
        dnt.parkrun.athletecoursesummary.Parser athleteCourseSummaryParser = new dnt.parkrun.athletecoursesummary.Parser.Builder()
                .url(athleteCourseSummaryUrl)
                .forEachAthleteCourseSummary(System.out::println)
                .build();
        athleteCourseSummaryParser.parse();
    }
}