package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import dnt.parkrun.webpageprovider.WebpageProvider;
import dnt.parkrun.webpageprovider.WebpageProviderFactory;
import dnt.parkrun.weekendresults.WeekendResults;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class WeekendResultsTest
{
    private DataSource dataSource;
    private WeekendResults weekendResults;

    @Before
    public void setUp() throws Exception
    {
        dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");

        weekendResults = WeekendResults.newInstance(
                dataSource,
                List.of(
                        new Object[]{"test/test.events.json", Course.Status.RUNNING},
                        new Object[]{"test/test.events.missing.json", Course.Status.STOPPED}
                ), new TestWebpageProviderFactory());

        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void testMostEvents() throws IOException
    {
        weekendResults.collectMostEventRecords();
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