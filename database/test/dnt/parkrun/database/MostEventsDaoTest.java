package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.stats.MostEventsDao;
import dnt.parkrun.database.weekly.AttendanceRecordsDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class MostEventsDaoTest extends BaseDaoTest
{
    private AttendanceRecordsDao dao;
    private MostEventsDao mostEventsDao;

    private final Date epoch = Date.from(Instant.EPOCH);

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        mostEventsDao = MostEventsDao.getOrCreate(dataSource, epoch);
    }

    @Test
    public void shouldCreateAndIgnoreNextCreation()
    {
        mostEventsDao.populateMostEventsTable();
        assertThat(mostEventsDao.getMostEvents()).isNotEmpty();
    }
}
