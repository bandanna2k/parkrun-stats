package dnt.parkrun.tests;

import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class WalkerTest
{
    @Test
    public void walkTest() throws IOException
    {
        Random random = new Random(1);

        // Get all countries and course
        List<Course> courses = new ArrayList<>();
        Supplier<InputStream> supplier = () -> EventsJsonFileReader.class.getResourceAsStream("/events.json");
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(supplier)
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
        URL courseEventSummaryUrl = new URL("https://" + course.country.baseUrl + "/" + course.name + "/results/eventhistory/");
        Parser courseEventSummaryParser = new Parser.Builder()
                .course(course)
                .webpageProvider(new WebpageProviderImpl(courseEventSummaryUrl))
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
        URL courseEventUrl = new URL("https://" + course.country.baseUrl + "/" + course.name + "/results/" + event.eventNumber + "/");
        dnt.parkrun.courseevent.Parser courseEventParser = new dnt.parkrun.courseevent.Parser.Builder(null)
                .webpageProvider(new WebpageProviderImpl(courseEventUrl))
                .forEachResult(results::add)
                .build();
        courseEventParser.parse();

        // Choose result at random
        nextInt = random.nextInt(results.size());
        Result result = results.get(nextInt);

        System.out.println("Result:" + result);

        // https://www.parkrun.co.nz/parkrunner/902393/
        // List course summary for random athlete
        CourseRepository courseRepository = new CourseRepository();
        URL athleteCourseSummaryUrl = new URL("https://" + course.country.baseUrl + "/parkrunner/" + result.athlete.athleteId + "/");
        dnt.parkrun.athletecoursesummary.Parser athleteCourseSummaryParser = new dnt.parkrun.athletecoursesummary.Parser.Builder()
                .webpageProvider(new WebpageProviderImpl(athleteCourseSummaryUrl))
                .forEachAthleteCourseSummary(System.out::println)
                .build(courseRepository);
        athleteCourseSummaryParser.parse();
    }
}
