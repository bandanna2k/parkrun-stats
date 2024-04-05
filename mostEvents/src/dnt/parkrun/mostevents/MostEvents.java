package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dnt.parkrun.datastructures.Country.NZ;

public class MostEvents
{
    private final List<Object[]> listOfCourseAndStatus;
    private final UrlGenerator urlGenerator;

    public static void main(String[] args) throws SQLException, IOException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        MostEvents mostEvents = MostEvents.newInstance(
                dataSource,
                List.of(
                        new Object[] { "events.json", Course.Status.RUNNING },
                        new Object[] { "events.missing.json", Course.Status.STOPPED }
                        ));

        mostEvents.collectMostEventRecords();
    }

    private final CourseRepository courseRepository;

    private final AthleteDao athleteDao;
    private final CourseDao courseDao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;
    private final VolunteerDao volunteerDao;
    private final WebpageProviderFactory webpageProviderFactory;

    private MostEvents(DataSource dataSource,
                       List<Object[]> listOfCourseAndStatus) throws SQLException
    {
        this.listOfCourseAndStatus = listOfCourseAndStatus;
        this.courseRepository = new CourseRepository();
        this.courseDao = new CourseDao(dataSource, courseRepository);
        this.athleteDao = new AthleteDao(dataSource);
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
        this.resultDao = new ResultDao(dataSource);
        this.volunteerDao = new VolunteerDao(dataSource);
        this.urlGenerator = new UrlGenerator(NZ.baseUrl);
        this.webpageProviderFactory = new WebpageProviderFactory(urlGenerator);
    }

    public static MostEvents newInstance(DataSource dataSource,
                                         List<Object[]> listOfCourseAndStatus) throws SQLException
    {
        return new MostEvents(dataSource, listOfCourseAndStatus);
    }

    public void collectMostEventRecords() throws IOException
    {
        System.out.println("* Adding courses *");
        for (Object[] eventFiles : listOfCourseAndStatus)
        {
            String file = (String) eventFiles[0];
            Course.Status status = (Course.Status) eventFiles[1];
            addCourses(file, status);
        }

        System.out.println("* Filter courses *");
        Arrays.stream(Country.values())
                .filter(e -> e != NZ)
                .forEach(e -> courseRepository.filterByCountryCode(e.getCountryCode()));

        List<CourseEventSummary> courseEventSummariesToGet = new ArrayList<>();

        System.out.println("* Get course summaries from DAO *");
        List<CourseEventSummary> courseEventSummariesFromDao = courseEventSummaryDao.getCourseEventSummaries();
        System.out.println("Count: " + courseEventSummariesFromDao.size());

        System.out.println("* Get course summaries from Web *");
        List<CourseEventSummary> courseEventSummariesFromWeb = getCourseEventSummariesFromWeb();

        System.out.println("* Filtering existing course event summaries *");
        courseEventSummariesFromWeb.removeAll(courseEventSummariesFromDao);

        courseEventSummariesToGet.addAll(courseEventSummariesFromWeb);
        System.out.println("Count: " + courseEventSummariesToGet.size());

        System.out.println("* Get all course event summaries *");
        for (CourseEventSummary ces : courseEventSummariesToGet)
        {
            if(ces.course.status != Course.Status.RUNNING)
            {
                continue;
            }

            System.out.printf("* Processing %s *\n", ces);

            while(true)
            {
                try
                {
                    dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(ces.course)
                            .webpageProvider(webpageProviderFactory.createCourseEventWebpageProvider(ces.course.name, ces.eventNumber))
                            .forEachAthlete(athleteDao::insert)
                            .forEachResult(resultDao::insert)
                            .forEachVolunteer(volunteerDao::insert)
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

    private void addCourses(String filename, Course.Status status) throws IOException
    {
        InputStream inputStream = Course.class.getResourceAsStream("/" + filename);
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCourse(course ->
                {
                    Course doesItExistCourse = courseRepository.getCourseFromName(course.name); // Bit weird
                    if(doesItExistCourse == null)
                    {
                        System.out.println("INFO Adding new course " + course);
                        courseDao.insert(course);
                    }
                })
                .statusSupplier(() -> status)
                .build();
        reader.read();
    }

    private List<CourseEventSummary> getCourseEventSummariesFromWeb() throws IOException
    {
        List<CourseEventSummary> results = new ArrayList<>();
        for (Course course : courseRepository.getCourses(NZ))
        {
            System.out.printf("* Processing %s *\n", course);
            System.out.println(System.getProperty("user.dir"));
            Parser courseEventSummaryParser = new Parser.Builder()
                    .course(course)
                    .webpageProvider(webpageProviderFactory.createCourseEventSummaryWebpageProvider(course.name))
                    .forEachCourseEvent(results::add)
                    .build();
            courseEventSummaryParser.parse();
        }
        return results;
    }
}
