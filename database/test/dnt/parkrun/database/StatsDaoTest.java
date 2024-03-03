package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class StatsDaoTest
{
    private StatsDao dao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");
        dao = new StatsDao(dataSource, DateConverter.parseWebsiteDate("02/03/2024"));
    }

    @Test
    public void shouldCreateAndIgnoreNextCreation()
    {
        dao.generateDifferentCourseCountTable();
    }
}
