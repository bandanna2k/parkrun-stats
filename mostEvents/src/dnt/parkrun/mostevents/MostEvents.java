package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.CountryEnum;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.mostevents.dao.AthleteDao;
import dnt.parkrun.mostevents.dao.CourseEventSummaryDao;
import dnt.parkrun.mostevents.dao.ResultDao;
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
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;

    private MostEvents(CourseRepository courseRepository, DataSource dataSource) throws SQLException
    {
        this.courseRepository = courseRepository;
        this.urlGenerator = new UrlGenerator();

        this.athleteDao = new AthleteDao(dataSource);
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
        this.resultDao = new ResultDao(dataSource);
    }

    public static MostEvents newInstance() throws SQLException, IOException
    {
        CourseRepository courseRepository = new CourseRepository();

        InputStream inputStream = Course.class.getResourceAsStream("/events.json");
        dnt.parkrun.courses.reader.EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCountry(courseRepository::addCountry)
                .forEachCourse(courseRepository::addCourse)
                .statusSupplier(() -> Course.Status.RUNNING)
                .build();
        reader.read();

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");

        return new MostEvents(courseRepository, dataSource);
    }

    public void collectMostEventRecords() throws IOException
    {
        System.out.println("* Filter courses *");
        Arrays.stream(CountryEnum.values())
                .filter(e -> e != CountryEnum.NZ)
                .forEach(e -> courseRepository.filterByCountryCode(e.getCountryCode()));

        boolean getFromWeb = true;
        List<CourseEventSummary> courseEventSummariesToGet = new ArrayList<>();
        if(getFromWeb)
        {
            System.out.println("* Get course summaries from DAO *");
            List<CourseEventSummary> courseEventSummariesFromDao = courseEventSummaryDao.getCourseEventSummaries();
            System.out.println("Count: " + courseEventSummariesFromDao.size());

            System.out.println("* Get course summaries from Web *");
            List<CourseEventSummary> courseEventSummariesFromWeb = getCourseEventSummariesFromWeb();
            // TODO Pollution System.out.println(courseEventSummariesFromWeb);

            System.out.println("* Filtering existing course event summaries *");
            courseEventSummariesFromWeb.removeAll(courseEventSummariesFromDao);

            courseEventSummariesToGet.addAll(courseEventSummariesFromWeb);
        }
        else
        {
            System.out.println("* Get course summaries from DAO 2 *");
            courseEventSummariesToGet = courseEventSummaryDao.getCourseEventSummariesWithNoResults();
        }
        System.out.println("Count: " + courseEventSummariesToGet.size());

        System.out.println("* Get all course event summaries *");
        for (CourseEventSummary ces : courseEventSummariesToGet)
        {
            System.out.printf("* Processing %s *\n", ces);

            Country courseCountry = courseRepository.getCountry(ces.course.countryCode);
            dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder()
                    .courseName(ces.course.name)
                    .url(urlGenerator.generateCourseEventUrl(courseCountry.url, ces.course.name, ces.eventNumber))
                    .forEachAthlete(athleteDao::insert)
                    .forEachResult(resultDao::insert)
                    .build();
            parser.parse();

            courseEventSummaryDao.insert(ces);
        }
    }

    private List<CourseEventSummary> getCourseEventSummariesFromWeb() throws IOException
    {
        List<CourseEventSummary> results = new ArrayList<>();
        for (Course course : courseRepository.getCourses())
        {
            if(!course.name.equals("lake2laketrail"))
//            if(!course.name.equals("gordonsprattreserve")) // DONE
//            if(!course.name.equals("northernpathway"))        // DONE
//            if(!course.name.equals("cornwall"))           // DONE
//            if(!course.name.equals("lowerhutt"))        // DONE
// 5
//            if(!course.name.equals("hamiltonlake"))
//            if(!course.name.equals("hamiltonpark"))
//            if(!course.name.equals("dunedin"))
//            if(!course.name.equals("millwater"))
//            if(!course.name.equals("waitangi"))     // DONE
// 10
//            if(!course.name.equals("westernsprings"))
//            if(!course.name.equals("barrycurtis"))
//            if(!course.name.equals("purenga"))
//            if(!course.name.equals("taupo"))
//            if(!course.name.equals("otakiriver"))       // DONE
// 15
//            if(!course.name.equals("queenstown"))
//            if(!course.name.equals("wanaka"))
//            if(!course.name.equals("invercargill"))
//            if(!course.name.equals("balclutha"))
//            if(!course.name.equals("blenheim"))
// 20
//            if(!course.name.equals("gisbourne"))
//            if(!course.name.equals("andersonpark"))
//            if(!course.name.equals("flaxmere"))
//            if(!course.name.equals("fosterpark"))
//            if(!course.name.equals("pegasus"))
// 25
//            if(!course.name.equals("tauranga"))
//            if(!course.name.equals("whangerei"))
//            if(!course.name.equals("owairaka"))
//            if(!course.name.equals("greytown"))
//            if(!course.name.equals("araharakehe"))
// 30
//            if(!course.name.equals("kapiticoast"))
//            if(!course.name.equals("russellpark"))
//            if(!course.name.equals("eastend"))
//            if(!course.name.equals("cambridge"))
//            if(!course.name.equals("universityofwaikato"))
// 35
//            if(!course.name.equals("hagley"))
//            if(!course.name.equals("trenthammemorial"))
//            if(!course.name.equals("whakatanegardens"))
//            if(!course.name.equals("broadpark"))        // DONE
//            if(!course.name.equals("palmerstonnorth"))
// 40
//            if(!course.name.equals("whanganui"))
//            if(!course.name.equals("sherwoodreserve"))
//            if(!course.name.equals("hobsonvillepoint"))     // DONE
// 43

            {
                continue;
            }

            System.out.printf("* Processing %s *\n", course);

            Country courseCountry = courseRepository.getCountry(course.countryCode);
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
