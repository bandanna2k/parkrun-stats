package dnt.parkrun.weekendresults;

import dnt.parkrun.common.ParkrunDay;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProviderFactory;
import dnt.parkrun.webpageprovider.WebpageProviderFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class WeekendResults
{
    public static void main(String[] args) throws SQLException, IOException
    {
        Country country = Country.valueOf(args[0]);
        Database database = new LiveDatabase(country, getDataSourceUrl(), "dao", "0b851094", "sudo");
        WeekendResults weekendResults = WeekendResults.newInstance(
                database,
                new WebpageProviderFactoryImpl(new UrlGenerator(country.baseUrl)));

        weekendResults.fetchWeekendResults();
    }

    private final Country country;
    private final CourseRepository courseRepository;

    private final AthleteDao athleteDao;
    private final CourseDao courseDao;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;
    private final VolunteerDao volunteerDao;
    private final WebpageProviderFactory webpageProviderFactory;

    private WeekendResults(Database database,
                           WebpageProviderFactory webpageProviderFactory) throws SQLException
    {
        this.country = database.country;
        this.courseRepository = new CourseRepository();
        this.courseDao = new CourseDao(database, courseRepository);
        this.athleteDao = new AthleteDao(database);
        this.courseEventSummaryDao = new CourseEventSummaryDao(database, courseRepository);
        this.resultDao = new ResultDao(database);
        this.volunteerDao = new VolunteerDao(database);
        this.webpageProviderFactory = webpageProviderFactory;
    }

    public static WeekendResults newInstance(Database database,
                                             WebpageProviderFactory webpageProviderFactory) throws SQLException
    {
        return new WeekendResults(database, webpageProviderFactory);
    }

    public void fetchWeekendResults() throws IOException
    {
        Date parkrunDay = ParkrunDay.getParkrunDay(new Date());
        courseRepository.getCourses(country).forEach(course ->
        {
            if(course.status != Course.Status.RUNNING) return;

//            if(course.name.startsWith("a")) return;
//            if(course.name.startsWith("b")) return;
//            if(course.name.startsWith("c")) return;
//            if(course.name.startsWith("d")) return;
//            if(course.name.startsWith("e")) return;
//            if(course.name.startsWith("f")) return;
//            if(course.name.startsWith("g")) return;
//            if(course.name.startsWith("h")) return;
//            if(course.name.startsWith("i")) return;
//            if(course.name.startsWith("j")) return;
//            if(course.name.startsWith("k")) return;
//            if(course.name.startsWith("l")) return;
//            if(course.name.startsWith("m")) return;
//            if(course.name.startsWith("n")) return;
//            if(course.name.startsWith("o")) return;
//            if(course.name.startsWith("p")) return;
//            if(course.name.startsWith("q")) return;
//            if(course.name.startsWith("r")) return;
////            if(course.name.startsWith("s")) return;
//            if(course.name.startsWith("t")) return;
//            if(course.name.startsWith("u")) return;
//            if(course.name.startsWith("v")) return;
//            if(course.name.startsWith("w")) return;
//            if(course.name.startsWith("x")) return;
//            if(course.name.startsWith("y")) return;
//            if(course.name.startsWith("z")) return;
//            if(course.name.startsWith("hagley")) return;
//            if(!course.name.startsWith("balclutha")) return;

            System.out.printf("* [%s] Get course summaries from database... ", course.longName);
            List<CourseEventSummary> courseEventSummariesFromDao = courseEventSummaryDao.getCourseEventSummaries(course);
            System.out.printf("Count: %d *%n", courseEventSummariesFromDao.size());

            if(courseEventSummariesFromDao.stream()
                    .anyMatch(ces ->
                            ces.course.courseId == course.courseId && 0 == ces.date.compareTo(parkrunDay)))
            {
                return;
            }

            System.out.printf("* [%s] Get course summaries from web... ", course.longName);
            List<CourseEventSummary> courseEventSummariesFromWeb = getCourseEventSummariesFromWeb(course);
            System.out.printf("Count: %d *%n", courseEventSummariesFromDao.size());

            System.out.printf("* [%s] Filtering existing course event summaries *%n", course.longName);
            courseEventSummariesFromWeb.removeAll(courseEventSummariesFromDao);

            List<CourseEventSummary> courseEventSummariesToGet = new ArrayList<>(courseEventSummariesFromWeb);
            System.out.printf("* [%s] Courses to download: %d *%n", course.longName, courseEventSummariesToGet.size());

            System.out.printf("* [%s] Getting course events *%n", course.longName);
            for (CourseEventSummary ces : courseEventSummariesToGet)
            {
                System.out.printf("* Processing %s *%n", ces);

                tryTwiceIfFails(() -> {
                    List<Athlete> runners = new ArrayList<>();
                    List<Result> results = new ArrayList<>();
                    List<Volunteer> volunteers = new ArrayList<>();

                    dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(courseRepository)
                            .webpageProvider(webpageProviderFactory.createCourseEventWebpageProvider(ces.course.name, ces.eventNumber))
                            .forEachAthlete(runners::add)
                            .forEachResult(results::add)
                            .forEachVolunteer(volunteers::add)
                            .build();
                    parser.parse();

                    courseEventSummaryDao.insert(ces);
                    athleteDao.insert(runners);
                    volunteerDao.insert(volunteers);
                    resultDao.insert(results);
                });
            }
            System.out.println();
        });
    }

    private void tryTwiceIfFails(Runnable runnable)
    {
        while(true)
        {
            try
            {
                runnable.run();
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

    private List<CourseEventSummary> getCourseEventSummariesFromWeb(Course course)
    {
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();

        Parser courseEventSummaryParser = new Parser.Builder()
                .course(course)
                .webpageProvider(webpageProviderFactory.createCourseEventSummaryWebpageProvider(course.name))
                .forEachCourseEvent(courseEventSummaries::add)
                .build();
        courseEventSummaryParser.parse();

        return courseEventSummaries;
    }
}
