package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
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

public class MostEvents
{
    public static void main(String[] args) throws SQLException, IOException
    {
        MostEvents mostEvents = MostEvents.newInstance();
        mostEvents.collectMostEventRecords();
    }

    private final UrlGenerator urlGenerator;
    private final CourseRepository courseRepository;

    private final AthleteDao athleteDao;
    private final CourseDao courseDao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;

    private MostEvents(DataSource dataSource) throws SQLException
    {
        this.urlGenerator = new UrlGenerator();

        this.courseRepository = new CourseRepository();
        this.athleteDao = new AthleteDao(dataSource);
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
        this.resultDao = new ResultDao(dataSource);
        this.courseDao = new CourseDao(dataSource);
    }

    public static MostEvents newInstance() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        return new MostEvents(dataSource);
    }

    public void collectMostEventRecords() throws IOException
    {
        System.out.println("* Adding courses *");
        InputStream inputStream = Course.class.getResourceAsStream("/events.json");
        dnt.parkrun.courses.reader.EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCountry(courseRepository::addCountry)
                .forEachCourse(course ->
                {
                    if(course.country.countryEnum == CountryEnum.NZ)
                    {
                        courseDao.insert(course);
                        courseRepository.addCourse(course);
                    }
                })
                .statusSupplier(() -> Course.Status.RUNNING)
                .build();
        reader.read();

        System.out.println("* Filter courses *");
        Arrays.stream(CountryEnum.values())
                .filter(e -> e != CountryEnum.NZ)
                .forEach(e -> courseRepository.filterByCountryCode(e.getCountryCode()));

        List<CourseEventSummary> courseEventSummariesToGet = new ArrayList<>();

        System.out.println("* Get course summaries from DAO *");
        List<CourseEventSummary> courseEventSummariesFromDao = courseEventSummaryDao.getCourseEventSummaries();
        System.out.println("Count: " + courseEventSummariesFromDao.size());

        System.out.println("* Get course summaries from Web *");
        List<CourseEventSummary> courseEventSummariesFromWeb = getCourseEventSummariesFromWeb();
        // System.out.println(courseEventSummariesFromWeb); // Pollutes the logs

        System.out.println("* Filtering existing course event summaries *");
        courseEventSummariesFromWeb.removeAll(courseEventSummariesFromDao);

        courseEventSummariesToGet.addAll(courseEventSummariesFromWeb);
        System.out.println("Count: " + courseEventSummariesToGet.size());

        System.out.println("* Get all course event summaries *");
        for (CourseEventSummary ces : courseEventSummariesToGet)
        {
            System.out.printf("* Processing %s *\n", ces);

            Country courseCountry = courseRepository.getCountry(ces.course.country.countryEnum.getCountryCode());
            while(true)
            {
                try
                {
                    dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder()
                            .courseName(ces.course.name)
                            .url(urlGenerator.generateCourseEventUrl(courseCountry.url, ces.course.name, ces.eventNumber))
                            .forEachAthlete(athleteDao::insert)
                            .forEachResult(resultDao::insert)
                            .build();
                    parser.parse();

                    courseEventSummaryDao.insert(ces);
                    break;
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private List<CourseEventSummary> getCourseEventSummariesFromWeb() throws IOException
    {
        List<CourseEventSummary> results = new ArrayList<>();
        for (Course course : courseRepository.getCourses())
        {
            System.out.printf("* Processing %s *\n", course);

            Country courseCountry = courseRepository.getCountry(course.country.getCountryCode());
            Parser courseEventSummaryParser = new Parser.Builder()
                    .course(course)
                    .url(urlGenerator.generateCourseEventSummaryUrl(courseCountry.url, course.name))
                    .forEachCourseEvent(results::add)
                    .build();
            courseEventSummaryParser.parse();
        }
        return results;
    }
}
