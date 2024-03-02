package dnt.parkrun.addathleteevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecourseevents.AthleteCourseEvent;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.*;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.empty;

public class AddAthleteEvents
{
    private static final String PARKRUN_CO_NZ = "parkrun.co.nz";

    private final UrlGenerator urlGenerator;
    private final CourseRepository courseRepository;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;


    public static void main(String[] args) throws IOException, SQLException
    {
        AddAthleteEvents addAthleteEvents = AddAthleteEvents.newInstance();
        addAthleteEvents.go(Arrays.stream(args).map(Integer::parseInt).collect(Collectors.toList()));
    }

    private AddAthleteEvents(DataSource dataSource) throws SQLException
    {
        this.urlGenerator = new UrlGenerator();

        this.courseRepository = new CourseRepository();
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
        this.resultDao = new ResultDao(dataSource);
    }

    public static AddAthleteEvents newInstance() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        return new AddAthleteEvents(dataSource);
    }

    private void go(List<Integer> athletes) throws IOException
    {
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

        System.out.println("* Getting athlete course summaries *");
        List<AthleteCourseSummary> courseSummaries = new ArrayList<>();
        for (Integer athleteId : athletes)
        {
            Parser parser = new Parser.Builder()
                    .url(urlGenerator.generateAthleteEventSummaryUrl(PARKRUN_CO_NZ, athleteId))
                    .forEachAthleteCourseSummary(courseSummaries::add)
                    .build();
            parser.parse();
        }

        System.out.println("* Processing results *");
        for (AthleteCourseSummary acs : courseSummaries)
        {
            Course course = courseRepository.getCourseFromLongName(acs.courseLongName);
            if(course.status == Course.Status.STOPPED)
            {
                // Still process
            }
            else if(course.country.countryEnum == CountryEnum.NZ)
            {
                continue;
            }

            for (Integer athlete : athletes)
            {
                dnt.parkrun.athletecourseevents.Parser parser = new dnt.parkrun.athletecourseevents.Parser.Builder()
                        .url(urlGenerator.generateAthleteEventUrl(course.country.url, course.name, athlete))
                        .forEachAthleteCourseEvent(this::processAthleteCourseEvent)
                        .build();
                parser.parse();
            }
        }
    }

    private void processAthleteCourseEvent(AthleteCourseEvent ace)
    {
        System.out.println();
        System.out.println(ace);

        Course course = courseRepository.getCourseFromName(ace.courseName);
        System.out.println(course);

        CourseEventSummary courseEventSummary = new CourseEventSummary(
                course, ace.eventNumber, ace.date, 0, empty(), empty());
        System.out.println(courseEventSummary);

        Result result = new Result(ace.courseName, ace.eventNumber, ace.position, ace.athlete, ace.time);
        System.out.println(result);
    }
}