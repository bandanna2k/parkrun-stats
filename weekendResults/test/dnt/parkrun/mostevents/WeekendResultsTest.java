package dnt.parkrun.mostevents;

import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import dnt.parkrun.webpageprovider.WebpageProvider;
import dnt.parkrun.webpageprovider.WebpageProviderFactory;
import dnt.parkrun.weekendresults.WeekendResults;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static dnt.parkrun.datastructures.Country.NZ;

public class WeekendResultsTest extends BaseDaoTest
{
    private WeekendResults weekendResults;

    @Before
    public void setUp() throws Exception
    {
        CourseRepository courseRepository = new CourseRepository();
        CourseDao courseDao = new CourseDao(dataSource, courseRepository);

        Course bushy = courseDao.insert(new Course(Course.NO_COURSE_ID, "bushynewzealand", NZ, "Fake Bushy parkrun", Course.Status.RUNNING));

        CourseEventSummaryDao courseEventSummaryDao = new CourseEventSummaryDao(country, dataSource, courseRepository);
        courseEventSummaryDao.insert(new CourseEventSummary(bushy, 1, Date.from(Instant.EPOCH), 2545,
                Optional.of(johnDoe), Optional.of(janeDoe)));

        weekendResults = WeekendResults.newInstance(bushy.country, dataSource, new TestWebpageProviderFactory());

        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void testFetchWeekendResults() throws IOException
    {
        weekendResults.fetchWeekendResults();
    }

    private static class TestWebpageProviderFactory implements WebpageProviderFactory
    {
        @Override
        public WebpageProvider createCourseEventWebpageProvider(String courseName, int eventNumber)
        {
            URL resource = this.getClass().getResource("/test/" + courseName + "/course.event." + eventNumber + ".html");
            return new FileWebpageProvider(new File(resource.getFile()));
        }

        @Override
        public WebpageProvider createCourseEventSummaryWebpageProvider(String courseName)
        {
            URL resource = this.getClass().getResource("/test/" + courseName + "/course.event.summary.html");
            return new FileWebpageProvider(new File(resource.getFile()));
        }
    }
}