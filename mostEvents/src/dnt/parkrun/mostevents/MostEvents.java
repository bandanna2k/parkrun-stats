package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.mostevents.dao.CourseEventSummaryDao;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class MostEvents
{
    private final UrlGenerator urlGenerator;
    private final Map<Long, Record> athleteIdToMostEventRecord = new HashMap<>();
    private final CourseRepository courseRepository;
    private final CourseEventSummaryDao courseEventSummaryDao;

    private MostEvents(CourseRepository courseRepository, DataSource dataSource) throws SQLException
    {
        this.courseRepository = courseRepository;
        this.urlGenerator = new UrlGenerator();
        this.courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
    }

    public static MostEvents newInstance() throws SQLException, IOException
    {
        CourseRepository courseRepository = new CourseRepository();

        InputStream inputStream = Course.class.getResourceAsStream("/events.json");
        dnt.parkrun.courses.reader.EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCountry(courseRepository::addCountry)
                .forEachCourse(courseRepository::addCourse)
                .build();
        reader.read();

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");

        return new MostEvents(courseRepository, dataSource);
    }

    public void collectMostEventRecords() throws IOException
    {
        System.out.println("* Filter courses *");
        Set<String> coursesToScan = new HashSet<>() {{ add("cornwall"); add("lowerhutt"); }};
        courseRepository.removeIf(c -> !coursesToScan.contains(c));

        System.out.println("* Get course summaries from DAO *");
        List<CourseEventSummary> courseEventSummariesFromDao = courseEventSummaryDao.getCourseEventSummaries();
        System.out.println(courseEventSummariesFromDao);

        System.out.println("* Get course summaries from Web *");
        List<CourseEventSummary> courseEventSummariesFromWeb = getCourseEventSummariesFromWeb();
        // TODO Polution System.out.println(courseEventSummariesFromWeb);

        System.out.println("* Filtering by event number temporarily"); // TODO Remove
        courseEventSummariesFromWeb.removeIf(ces -> ces.eventNumber > 2);

        System.out.println("* Filtering existing course event summaries *");
        courseEventSummariesFromWeb.removeAll(courseEventSummariesFromDao);
        System.out.println(courseEventSummariesFromWeb);

        System.out.println("* Get all course event summaries *");
        for (CourseEventSummary ces : courseEventSummariesFromWeb)
        {
            System.out.printf("* Processing %s *\n", ces);
            courseEventSummaryDao.insert(ces);

            Country courseCountry = courseRepository.getCountry(ces.course.countryCode);
            dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder()
                    .courseName(ces.course.name)
                    .url(urlGenerator.generateCourseEventUrl(courseCountry.url, ces.course.name, ces.eventNumber))
                    .forEachResult(r -> processResult(r))
                    .build();
            parser.parse();
        }

        athleteIdToMostEventRecord.forEach((athleteId, record) -> {
            System.out.println(record);
        });
    }

    private List<CourseEventSummary> getCourseEventSummariesFromWeb() throws IOException
    {
        List<CourseEventSummary> results = new ArrayList<>();
        for (Course course : courseRepository.getCourses())
        {
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

    private void processResult(Result result)
    {
        athleteIdToMostEventRecord.compute(result.athlete.athleteId, (athleteId, record) ->
        {
            if(record == null)
            {
                record = new Record(result.athlete);
            }
            record.incrementTotalEventsCount();
            record.incrementDifferentEventsCount(result.courseName);
            return record;
        });
    }

    private class Record
    {
        private final Athlete athlete;
        private int totalEventsCount = 0;
        private Map<String, Integer> courseNameToCount = new HashMap<>();

        private Record(Athlete athlete)
        {
            this.athlete = athlete;
            this.totalEventsCount = totalEventsCount;
        }

        public int getTotalEventsCount()
        {
            return totalEventsCount;
        }

        public Athlete getAthlete()
        {
            return athlete;
        }

        public Record incrementTotalEventsCount()
        {
            totalEventsCount++;
            return this;
        }

        public Record incrementDifferentEventsCount(String courseName)
        {
            courseNameToCount.compute(courseName, (courseName1, count) -> count == null ? 1 : count + 1);
            return this;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", totalEventsCount=" + totalEventsCount +
                    ", courseNameToCount=" + courseNameToCount +
                    '}';
        }
    }
}
