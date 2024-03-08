package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class InvariantsTest
{
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "dao", "daoFractaldao");

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    @Ignore
    public void shouldHaveNoZeroResult()
    {
        // TODO
    }
}
