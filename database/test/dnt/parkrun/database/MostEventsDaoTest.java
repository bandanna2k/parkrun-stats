package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.stats.MostEventsDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class MostEventsDaoTest extends BaseDaoTest
{
    private AttendanceRecordsDao dao;
    private MostEventsDao mostEventsDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        mostEventsDao = MostEventsDao.getOrCreate(dataSource, DateConverter.parseWebsiteDate("02/03/2024"));
    }

    @Test
    public void shouldCreateAndIgnoreNextCreation()
    {
        mostEventsDao.populateMostEventsTable();
    }
}
