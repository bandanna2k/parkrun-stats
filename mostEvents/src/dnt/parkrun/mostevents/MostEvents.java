package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.mostevents.dao.AthleteDao;
import dnt.parkrun.mostevents.dao.CourseDao;
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

            Country courseCountry = courseRepository.getCountry(ces.course.country.countryEnum.getCountryCode());
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
//            if(!course.name.equals("lake2laketrail"))//DONE
//            if(!course.name.equals("gordonsprattreserve")) // DONE
//            if(!course.name.equals("northernpathway"))        // DONE
//            if(!course.name.equals("cornwall"))           // DONE
//            if(!course.name.equals("lowerhutt"))        // DONE
// 5
//            if(!course.name.equals("hamiltonlake"))//DONE
//            if(!course.name.equals("hamiltonpark"))//DONE
//            if(!course.name.equals("otakiriver"))       // DONE
//            if(!course.name.equals("anderson"))//DONE
//            if(!course.name.equals("waitangi"))     // DONE
// 10
//            if(!course.name.equals("flaxmere"))//DONE
//            if(!course.name.equals("foster"))//DONE
//            if(!course.name.equals("hagley"))//DONE
//            if(!course.name.equals("broadpark"))        // DONE
//            if(!course.name.equals("palmerstonnorth"))//DONE
// 15
//            if(!course.name.equals("russellpark"))//DONE
//            if(!course.name.equals("eastend"))//DONE
//            if(!course.name.equals("owairaka"))//DONE
//            if(!course.name.equals("greytownwoodsidetrail"))
//            if(!course.name.equals("araharakeke"))//DONE
// 20
//            if(!course.name.equals("gisborne"))//DONE
            if(!course.name.equals("millwater"))
//            if(!course.name.equals("trenthammemorial"))
//            if(!course.name.equals("whakatanegardens"))
//            if(!course.name.equals("pegasus"))//DONE
// 25
//            if(!course.name.equals("queenstown"))
//            if(!course.name.equals("wanaka"))
//            if(!course.name.equals("invercargill"))
//            if(!course.name.equals("balclutha"))
//            if(!course.name.equals("blenheim"))
// 30
//            if(!course.name.equals("tauranga"))
//            if(!course.name.equals("whangerei"))
//            if(!course.name.equals("kapiticoast"))
//            if(!course.name.equals("cambridgenz"))
//            if(!course.name.equals("universityofwaikato"))
// 35
//            if(!course.name.equals("westernsprings"))
//            if(!course.name.equals("barrycurtis"))
//            if(!course.name.equals("dunedin"))
//            if(!course.name.equals("purenga"))
//            if(!course.name.equals("taupo"))
// 40
//            if(!course.name.equals("whanganuiriverbank"))//DONE
//            if(!course.name.equals("sherwoodreserve"))//DONE
//            if(!course.name.equals("hobsonvillepoint"))     // DONE
// 43

            {
                continue;
            }

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
