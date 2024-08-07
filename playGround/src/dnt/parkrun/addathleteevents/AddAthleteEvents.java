package dnt.parkrun.addathleteevents;

import dnt.parkrun.athletecourseevents.AthleteCourseEvent;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.empty;

@Deprecated(since = "Wrong solution")
public class AddAthleteEvents
{
    private final UrlGenerator urlGenerator;

    private final CourseRepository courseRepository;


    public static void main(String[] args) throws IOException, SQLException
    {
        AddAthleteEvents addAthleteEvents = AddAthleteEvents.newInstance(Country.valueOf(args[0]));
        addAthleteEvents.go(Arrays.stream(args).map(Integer::parseInt).collect(Collectors.toList()));
    }

    private AddAthleteEvents(Country country) throws SQLException
    {
        this.courseRepository = new CourseRepository();
        this.urlGenerator = new UrlGenerator(country.baseUrl);
    }

    public static AddAthleteEvents newInstance(Country country) throws SQLException
    {
        return new AddAthleteEvents(country);
    }

    private void go(List<Integer> athletes) throws IOException
    {
        {
            System.out.println("* Adding courses (Running) *");
            InputStream inputStream = Course.class.getResourceAsStream("/events.json");
            EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                    .forEachCourse(courseRepository::addCourse)
                    .statusSupplier(() -> Course.Status.RUNNING)
                    .build();
            reader.read();
        }
        {
            System.out.println("* Adding courses (Old) *");
            InputStream inputStream = Course.class.getResourceAsStream("/events.missing.json");
            EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                    .forEachCourse(courseRepository::addCourse)
                    .statusSupplier(() -> Course.Status.STOPPED)
                    .build();
            reader.read();
        }

        System.out.println("* Getting athlete course summaries *");
        List<AthleteCourseSummary> courseSummaries = new ArrayList<>();
        for (Integer athleteId : athletes)
        {
            Parser parser = new Parser.Builder()
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateAthleteEventSummaryUrl(athleteId)))
                    .forEachAthleteCourseSummary(courseSummaries::add)
                    .build(courseRepository);
            parser.parse();
        }

        System.out.println("* NOT Processing results *");
        /*
        for (AthleteCourseSummary acs : courseSummaries)
        {
            Course course = courseRepository.getCourseFromLongName(acs.courseLongName);
            if(course.status == Course.Status.STOPPED)
            {
                // Still process
            }
            else if(course.country.countryEnum == country)
            {
                continue;
            }

            for (Integer athlete : athletes)
            {
                dnt.parkrun.athletecourseevents.Parser parser = new dnt.parkrun.athletecourseevents.Parser.Builder()
                        .url(generateAthleteEventUrl(course.country.url, course.name, athlete))
                        .forEachAthleteCourseEvent(this::processAthleteCourseEvent)
                        .build();
                parser.parse();
            }
        }

         */
    }

    private void processAthleteCourseEvent(AthleteCourseEvent ace)
    {
        System.out.println();
        System.out.println(ace);

        Course course = courseRepository.getCourseFromName(ace.courseName);
        System.out.println(course);

        CourseEventSummary courseEventSummary = new CourseEventSummary(
                course, ace.eventNumber, ace.date, 0, 0, empty(), empty());
        System.out.println(courseEventSummary);

        Result result = new Result(course.courseId, ace.date, ace.eventNumber,
                ace.position, ace.athlete, ace.time, AgeCategory.UNKNOWN, AgeGrade.newInstanceNoAgeGrade());
        System.out.println(result);
    }
}
