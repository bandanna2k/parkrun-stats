package dnt.parkrun.addathleteevents;

import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddAthleteEvents
{
    private static final String PARKRUN_CO_NZ = "parkrun.co.nz";

    private final UrlGenerator urlGenerator = new UrlGenerator(1, 2);


    public static void main(String[] args) throws IOException
    {
        new AddAthleteEvents().go(Arrays.stream(args).map(Integer::parseInt).collect(Collectors.toList()));
    }

    private void go(List<Integer> athletes) throws IOException
    {
        CourseRepository courseRepository = new CourseRepository();

        {
            System.out.println("* Adding courses (Running) *");
            InputStream inputStream = Course.class.getResourceAsStream("/events.json");
            EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                    .forEachCountry(courseRepository::addCountry)
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

        System.out.println(athletes);

        List<AthleteCourseSummary> courseSummaries = new ArrayList<>();
        for (Integer athleteId : athletes)
        {
            Parser parser = new Parser.Builder()
                    .url(urlGenerator.generateAthleteEventSummaryUrl(PARKRUN_CO_NZ, athleteId))
                    .forEachAthleteCourseSummary(courseSummaries::add)
                    .build();
            parser.parse();
        }

        courseSummaries.forEach(acs -> {
            Course course = courseRepository.getCourseFromLongName(acs.courseLongName);
            System.out.println(urlGenerator.generateAthleteEventUrl(course.country.url, course.name, acs.athlete.athleteId));
        });

//        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder()
//                .url(urlGenerator.generateCourseEventUrl(PARKRUN_CO_NZ, acs.course, 99999))
//                .forEachResult(result -> {
//                    System.out.println(result);
//                })
//                .build();
    }
}
