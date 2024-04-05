package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import dnt.parkrun.webpageprovider.WebpageProvider;
import dnt.parkrun.webpageprovider.WebpageProviderFactory;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class MostEventsTest
{
    @Test
    public void name() throws SQLException, IOException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        WebpageProviderFactory webpageProviderFactory = new WebpageProviderFactory()
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
        };
        MostEvents mostEvents = MostEvents.newInstance(
                dataSource,
                List.of(
                        new Object[] {"test/test.events.json", Course.Status.RUNNING },
                        new Object[] {"test/test.events.missing.json", Course.Status.STOPPED }
                ), webpageProviderFactory);
        mostEvents.collectMostEventRecords();
    }
}