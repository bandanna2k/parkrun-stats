package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.List;

public class MostEventsDaoTest extends BaseDaoTest
{
    private MostEventsDao dao;
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        dao = new MostEventsDao(dataSource);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    public void shouldFetchMostEventsForRegion()
    {
        List<MostEventsDao.Record> mostEventsForRegion = dao.getMostEventsForRegion();
        mostEventsForRegion.forEach(record -> {
            System.out.println(record);
        });
        System.out.println(mostEventsForRegion.size());
    }
}
