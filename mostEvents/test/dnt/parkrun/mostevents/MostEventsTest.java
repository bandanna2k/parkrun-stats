package dnt.parkrun.mostevents;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MostEventsTest
{
    @Test
    public void name() throws SQLException, IOException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        MostEvents mostEvents = MostEvents.newInstance(
                dataSource,
                List.of(
                        new Object[] { "test.events.json", Course.Status.RUNNING },
                        new Object[] { "test.events.missing.json", Course.Status.STOPPED }
                ));
        mostEvents.collectMostEventRecords();
    }
}